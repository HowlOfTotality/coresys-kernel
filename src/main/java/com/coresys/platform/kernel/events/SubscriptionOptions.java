/**
 * CoreSys Platform Kernel
 * Copyright (c) 2026 Evgeniy Platonov
 * Licensed under the Apache License, Version 2.0
 * https://www.apache.org/licenses/LICENSE-2.0
 */

package com.coresys.platform.kernel.events;

/**
 * Параметры: SubscriptionOptions.
 *
 * Набор настроек, управляющих поведением соответствующего компонента.
 *
 * @author Евгений Платонов
 */

public final class SubscriptionOptions {

    private final int queueCapacity;
    private final OverflowPolicy overflowPolicy;
    private final String name;

    private final long slowHandlerThresholdMillis;
    private final SlowHandlerPolicy slowHandlerPolicy;
    private final long blacklistDurationMillis;

    private SubscriptionOptions(Builder b) {
        this.queueCapacity = Math.max(1, b.queueCapacity);
        this.overflowPolicy = b.overflowPolicy == null ? OverflowPolicy.DROP : b.overflowPolicy;
        this.name = b.name == null ? "" : b.name;

        this.slowHandlerThresholdMillis = Math.max(0, b.slowHandlerThresholdMillis);
        this.slowHandlerPolicy = b.slowHandlerPolicy == null ? SlowHandlerPolicy.NONE : b.slowHandlerPolicy;
        this.blacklistDurationMillis = Math.max(0, b.blacklistDurationMillis);
    }

    public int queueCapacity() {
        return queueCapacity;
    }

    public OverflowPolicy overflowPolicy() {
        return overflowPolicy;
    }

    public String name() {
        return name;
    }

    public long slowHandlerThresholdMillis() {
        return slowHandlerThresholdMillis;
    }

    public SlowHandlerPolicy slowHandlerPolicy() {
        return slowHandlerPolicy;
    }

    public long blacklistDurationMillis() {
        return blacklistDurationMillis;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private int queueCapacity = 1024;
        private OverflowPolicy overflowPolicy = OverflowPolicy.DROP;
        private String name;

        private long slowHandlerThresholdMillis;
        private SlowHandlerPolicy slowHandlerPolicy = SlowHandlerPolicy.NONE;
        private long blacklistDurationMillis;

        public Builder queueCapacity(int queueCapacity) {
            this.queueCapacity = queueCapacity;
            return this;
        }

        public Builder overflowPolicy(OverflowPolicy overflowPolicy) {
            this.overflowPolicy = overflowPolicy;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder slowHandlerThresholdMillis(long thresholdMillis) {
            this.slowHandlerThresholdMillis = thresholdMillis;
            return this;
        }

        public Builder slowHandlerPolicy(SlowHandlerPolicy policy) {
            this.slowHandlerPolicy = policy;
            return this;
        }

        public Builder blacklistDurationMillis(long durationMillis) {
            this.blacklistDurationMillis = durationMillis;
            return this;
        }

        public SubscriptionOptions build() {
            return new SubscriptionOptions(this);
        }
    }
}
