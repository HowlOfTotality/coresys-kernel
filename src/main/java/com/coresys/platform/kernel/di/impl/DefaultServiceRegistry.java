/**
 * CoreSys Platform Kernel
 * Copyright (c) 2026 Evgeniy Platonov
 * Licensed under the Apache License, Version 2.0
 * https://www.apache.org/licenses/LICENSE-2.0
 */

package com.coresys.platform.kernel.di.impl;

import com.coresys.platform.kernel.di.ServiceProvider;
import com.coresys.platform.kernel.di.ServiceRegistrationOptions;
import com.coresys.platform.kernel.di.ServiceRegistry;
import com.coresys.platform.kernel.di.ServiceProviders;
import com.coresys.platform.kernel.modules.ModuleId;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Реестр сервисов ядра (реализация по умолчанию).
 *
 * Поддерживает:
 * - регистрацию instance/providеr
 * - режим exclusive/shared
 * - резолвинг сервисов с учётом consumer ModuleId
 * - защиту от рекурсивного резолва (ThreadLocal-стек).
 *
 * @author Евгений Платонов
 */

public final class DefaultServiceRegistry implements ServiceRegistry {

    private static final class Entry {
        private final ModuleId owner;
        private final ServiceProvider<?> provider;
        private final boolean exclusive;

        private Entry(ModuleId owner, ServiceProvider<?> provider, boolean exclusive) {
            this.owner = owner;
            this.provider = provider;
            this.exclusive = exclusive;
        }
    }

    private static final class ResolveKey {
        private final Class<?> type;
        private final ModuleId consumer;

        private ResolveKey(Class<?> type, ModuleId consumer) {
            this.type = type;
            this.consumer = consumer;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ResolveKey)) return false;
            ResolveKey that = (ResolveKey) o;
            return type.equals(that.type) && Objects.equals(consumer, that.consumer);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, consumer);
        }
    }

    private static final ThreadLocal<Set<ResolveKey>> RESOLVING = ThreadLocal.withInitial(HashSet::new);

    private final Map<Class<?>, List<Entry>> services = new ConcurrentHashMap<>();

    @Override
    public <T> void register(ModuleId owner, Class<T> type, T instance, ServiceRegistrationOptions options) {
        Objects.requireNonNull(instance, "instance");
        registerProvider(owner, type, ServiceProviders.singleton(instance), options);
    }

    @Override
    public <T> void registerProvider(ModuleId owner, Class<T> type, ServiceProvider<? extends T> provider, ServiceRegistrationOptions options) {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(provider, "provider");
        Objects.requireNonNull(options, "options");

        services.compute(type, (k, list) -> {
            List<Entry> current = list == null ? new ArrayList<>() : new ArrayList<>(list);

            if (options.isExclusive() && !current.isEmpty()) {
                throw new IllegalStateException("Exclusive service already registered: " + type.getName());
            }
            for (Entry e : current) {
                if (e.exclusive) {
                    throw new IllegalStateException("Exclusive service already registered: " + type.getName());
                }
            }

            current.add(new Entry(owner, provider, options.isExclusive()));
            return Collections.unmodifiableList(current);
        });
    }

    @Override
    public void unregister(ModuleId owner, Class<?> type) {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(type, "type");

        services.computeIfPresent(type, (k, list) -> {
            List<Entry> kept = new ArrayList<>();
            for (Entry e : list) {
                if (owner.equals(e.owner)) {
                    try {
                        e.provider.close();
                    } catch (Exception ignored) {
                        // Игнор
                    }
                } else {
                    kept.add(e);
                }
            }
            return kept.isEmpty() ? null : Collections.unmodifiableList(kept);
        });
    }

    @Override
    public <T> T getFor(ModuleId consumer, Class<T> type) {
        Objects.requireNonNull(type, "type");
        List<Entry> list = services.get(type);
        if (list == null || list.isEmpty()) return null;

        Entry e = list.get(0);
        return resolve(type, consumer, e);
    }

    @Override
    public <T> List<T> getAllFor(ModuleId consumer, Class<T> type) {
        Objects.requireNonNull(type, "type");
        List<Entry> list = services.get(type);
        if (list == null || list.isEmpty()) return List.of();

        List<T> out = new ArrayList<>(list.size());
        for (Entry e : list) {
            out.add(resolve(type, consumer, e));
        }
        return Collections.unmodifiableList(out);
    }

    private <T> T resolve(Class<T> type, ModuleId consumer, Entry e) {
        ResolveKey key = new ResolveKey(type, consumer);
        Set<ResolveKey> set = RESOLVING.get();
        if (!set.add(key)) {
            throw new IllegalStateException("Recursive service resolution detected: " + type.getName()
                    + " for consumer=" + (consumer == null ? "<kernel>" : consumer.value()));
        }
        try {
            Object v = e.provider.get(consumer);
            if (v == null) return null;
            return type.cast(v);
        } finally {
            set.remove(key);
            if (set.isEmpty()) {
                RESOLVING.remove();
            }
        }
    }

    @Override
    public boolean has(Class<?> type) {
        Objects.requireNonNull(type, "type");
        List<Entry> list = services.get(type);
        return list != null && !list.isEmpty();
    }
}
