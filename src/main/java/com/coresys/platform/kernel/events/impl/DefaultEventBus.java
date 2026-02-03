/**
 * CoreSys Platform Kernel
 * Copyright (c) 2026 Evgeniy Platonov
 * Licensed under the Apache License, Version 2.0
 * https://www.apache.org/licenses/LICENSE-2.0
 */

package com.coresys.platform.kernel.events.impl;

import com.coresys.platform.kernel.events.EventBus;
import com.coresys.platform.kernel.events.EventHandler;
import com.coresys.platform.kernel.events.OverflowPolicy;
import com.coresys.platform.kernel.events.SlowHandlerPolicy;
import com.coresys.platform.kernel.events.SubscriptionOptions;
import com.coresys.platform.kernel.events.metrics.EventBusMetrics;
import com.coresys.platform.kernel.events.metrics.EventBusMetricsSnapshot;
import com.coresys.platform.kernel.events.metrics.SubscriptionMetricsSnapshot;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Шина событий ядра (реализация по умолчанию).
 *
 * Поддерживает подписки по типу события и доставку через общий ExecutorService.
 * Для каждой подписки создаётся внутренняя очередь, применяются политики переполнения,
 * а также контроль «медленных» обработчиков (лог/отключение/временный blacklist).
 *
 * @author Евгений Платонов
 */

public final class DefaultEventBus implements EventBus, EventBusMetrics {

    private static final Logger LOG = Logger.getLogger(DefaultEventBus.class.getName());

    private static final long DEFAULT_BLACKLIST_MILLIS = 5_000L;

    private static final class Sub<E> implements Subscription {
        final long id;
        final Class<E> type;
        final EventHandler<? super E> handler;
        final SubscriptionOptions options;
        final BlockingQueue<Object> queue;
        final Executor executor;

        final LongAdder handled = new LongAdder();
        final LongAdder errors = new LongAdder();
        final LongAdder dropped = new LongAdder();
        final LongAdder totalHandlerNanos = new LongAdder();
        final AtomicLong maxHandlerNanos = new AtomicLong(0);

        volatile boolean active = true;
        volatile long blacklistUntilNanos = 0L;
        volatile Runnable removeSelf = () -> { };

        // Сентинел для корректного завершения потока-помпы подписки.
        private static final Object POISON = new Object();

        Sub(long id, Class<E> type, EventHandler<? super E> handler, SubscriptionOptions options, Executor executor) {
            this.id = id;
            this.type = type;
            this.handler = handler;
            this.options = options;
            this.executor = executor;
            this.queue = new ArrayBlockingQueue<>(options.queueCapacity());
            startPump();
        }

        String metricsId() {
            String n = options.name();
            if (n != null && !n.isBlank()) return n;
            return type.getName() + "#" + id;
        }

        private void startPump() {
            executor.execute(() -> {
                while (active) {
                    try {
                        Object ev = queue.take();
                        if (ev == POISON) break;
                        if (!active) break;

                        long now = System.nanoTime();
                        long until = blacklistUntilNanos;
                        if (until > now) {
                            dropped.increment();
                            continue;
                        }

                        if (type.isInstance(ev)) {
                            long start = System.nanoTime();
                            try {
                                handler.onEvent(type.cast(ev));
                            } catch (Throwable t) {
                                errors.increment();
                                if (LOG.isLoggable(Level.FINE)) {
                                    LOG.log(Level.FINE, "Event handler failed for " + metricsId(), t);
                                }
                            } finally {
                                long dur = System.nanoTime() - start;
                                handled.increment();
                                totalHandlerNanos.add(dur);
                                maxHandlerNanos.accumulateAndGet(dur, Math::max);
                                applySlowPolicyIfNeeded(dur);
                            }
                        }
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return;
                    } catch (Throwable t) {
                        if (LOG.isLoggable(Level.FINE)) {
                            LOG.log(Level.FINE, "EventBus pump error for " + metricsId(), t);
                        }
                    }
                }
            });
        }

        void offer(Object event) {
            if (!active) return;

            if (options.overflowPolicy() == OverflowPolicy.BLOCK) {
                try {
                    queue.put(event);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
                return;
            }

            boolean ok = queue.offer(event);
            if (!ok) {
                dropped.increment();
                if (options.overflowPolicy() == OverflowPolicy.DEAD_LETTER) {
                    // сюда можно подключить «dead-letter» канал/логирование, если потребуется.
                }
            }
        }

        private void applySlowPolicyIfNeeded(long handlerNanos) {
            long thresholdMs = options.slowHandlerThresholdMillis();
            if (thresholdMs <= 0) return;

            long thresholdNanos = TimeUnit.MILLISECONDS.toNanos(thresholdMs);
            if (handlerNanos <= thresholdNanos) return;

            SlowHandlerPolicy p = options.slowHandlerPolicy();
            if (p == null || p == SlowHandlerPolicy.NONE) return;

            if (p == SlowHandlerPolicy.LOG) {
                LOG.warning(() -> "Slow EventBus handler: " + metricsId()
                        + ", timeMs=" + (handlerNanos / 1_000_000.0)
                        + ", queue=" + queue.size() + "/" + options.queueCapacity());
                return;
            }

            if (p == SlowHandlerPolicy.DISABLE) {
                LOG.warning(() -> "Disabling slow EventBus handler: " + metricsId()
                        + ", timeMs=" + (handlerNanos / 1_000_000.0));
                disable();
                return;
            }

            if (p == SlowHandlerPolicy.BLACKLIST) {
                long configuredMs = options.blacklistDurationMillis();
                final long durMs = configuredMs > 0 ? configuredMs : DEFAULT_BLACKLIST_MILLIS;

                blacklistUntilNanos = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(durMs);
                LOG.warning(() -> "Blacklisting slow EventBus handler: " + metricsId()
                        + ", timeMs=" + (handlerNanos / 1_000_000.0)
                        + ", blacklistMs=" + durMs);
            }
        }

        private void disable() {
            if (!active) {
                removeSelf.run();
                return;
            }
            active = false;
            queue.clear();
            queue.offer(POISON);
            removeSelf.run();
        }

        @Override
        public void unsubscribe() {
            disable();
        }

        SubscriptionMetricsSnapshot snapshot() {
            long handledCnt = handled.sum();
            long totalNanos = totalHandlerNanos.sum();
            double avgMs = handledCnt == 0 ? 0.0 : (totalNanos / 1_000_000.0) / handledCnt;
            double maxMs = maxHandlerNanos.get() / 1_000_000.0;

            long now = System.nanoTime();
            long until = blacklistUntilNanos;
            boolean bl = until > now;
            long remainingMs = bl ? TimeUnit.NANOSECONDS.toMillis(until - now) : 0L;

            return new SubscriptionMetricsSnapshot(
                    metricsId(),
                    type.getName(),
                    active,
                    queue.size(),
                    options.queueCapacity(),
                    handledCnt,
                    errors.sum(),
                    dropped.sum(),
                    avgMs,
                    maxMs,
                    bl,
                    remainingMs
            );
        }
    }

    private final ExecutorService exec;
    private final Map<Class<?>, CopyOnWriteArrayList<Sub<?>>> subs = new ConcurrentHashMap<>();
    private final AtomicLong idSeq = new AtomicLong(0);
    private final LongAdder publishedTotal = new LongAdder();

    public DefaultEventBus(ExecutorService exec) {
        this.exec = Objects.requireNonNull(exec, "exec");
    }

    @Override
    public <E> Subscription subscribe(Class<E> eventType, EventHandler<? super E> handler, SubscriptionOptions options) {
        Objects.requireNonNull(eventType, "eventType");
        Objects.requireNonNull(handler, "handler");
        SubscriptionOptions opt = options == null ? SubscriptionOptions.builder().build() : options;

        long id = idSeq.incrementAndGet();
        Sub<E> sub = new Sub<>(id, eventType, handler, opt, exec);

        CopyOnWriteArrayList<Sub<?>> list = subs.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>());
        list.add(sub);
        sub.removeSelf = () -> list.remove(sub);

        return () -> {
            list.remove(sub);
            sub.unsubscribe();
        };
    }

    @Override
    public void publish(Object event) {
        if (event == null) return;
        publishedTotal.increment();

        dispatchTo(event.getClass(), event);

        for (Class<?> key : subs.keySet()) {
            if (key != event.getClass() && key.isInstance(event)) {
                dispatchTo(key, event);
            }
        }
    }

    private void dispatchTo(Class<?> type, Object event) {
        List<Sub<?>> list = subs.get(type);
        if (list == null) return;
        for (Sub<?> s : list) {
            s.offer(event);
        }
    }

    @Override
    public Executor executor() {
        return exec;
    }

    @Override
    public EventBusMetricsSnapshot snapshot() {
        Map<String, SubscriptionMetricsSnapshot> out = new LinkedHashMap<>();
        for (CopyOnWriteArrayList<Sub<?>> list : subs.values()) {
            for (Sub<?> s : list) {
                out.put(s.metricsId(), s.snapshot());
            }
        }
        return new EventBusMetricsSnapshot(publishedTotal.sum(), out);
    }
}
