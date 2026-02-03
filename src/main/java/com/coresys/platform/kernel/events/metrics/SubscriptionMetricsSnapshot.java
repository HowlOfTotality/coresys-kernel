/**
 * CoreSys Platform Kernel
 * Copyright (c) 2026 Evgeniy Platonov
 * Licensed under the Apache License, Version 2.0
 * https://www.apache.org/licenses/LICENSE-2.0
 */

package com.coresys.platform.kernel.events.metrics;

/**
 * Снимок конфигурации: SubscriptionMetricsSnapshot.
 *
 * Иммутабельное представление значений конфигурации в конкретный момент времени.
 *
 * @author Евгений Платонов
 */

public final class SubscriptionMetricsSnapshot {

    private final String id;
    private final String eventType;
    private final boolean active;

    private final int queueSize;
    private final int queueCapacity;

    private final long handled;
    private final long errors;
    private final long dropped;

    private final double avgHandlerMillis;
    private final double maxHandlerMillis;

    private final boolean blacklisted;
    private final long blacklistRemainingMillis;

    public SubscriptionMetricsSnapshot(
            String id,
            String eventType,
            boolean active,
            int queueSize,
            int queueCapacity,
            long handled,
            long errors,
            long dropped,
            double avgHandlerMillis,
            double maxHandlerMillis,
            boolean blacklisted,
            long blacklistRemainingMillis
    ) {
        this.id = id == null ? "" : id;
        this.eventType = eventType == null ? "" : eventType;
        this.active = active;
        this.queueSize = Math.max(0, queueSize);
        this.queueCapacity = Math.max(0, queueCapacity);
        this.handled = Math.max(0, handled);
        this.errors = Math.max(0, errors);
        this.dropped = Math.max(0, dropped);
        this.avgHandlerMillis = Math.max(0.0, avgHandlerMillis);
        this.maxHandlerMillis = Math.max(0.0, maxHandlerMillis);
        this.blacklisted = blacklisted;
        this.blacklistRemainingMillis = Math.max(0, blacklistRemainingMillis);
    }

    public String id() {
        return id;
    }

    public String eventType() {
        return eventType;
    }

    public boolean active() {
        return active;
    }

    public int queueSize() {
        return queueSize;
    }

    public int queueCapacity() {
        return queueCapacity;
    }

    public long handled() {
        return handled;
    }

    public long errors() {
        return errors;
    }

    public long dropped() {
        return dropped;
    }

    public double avgHandlerMillis() {
        return avgHandlerMillis;
    }

    public double maxHandlerMillis() {
        return maxHandlerMillis;
    }

    public boolean blacklisted() {
        return blacklisted;
    }

    public long blacklistRemainingMillis() {
        return blacklistRemainingMillis;
    }
}
