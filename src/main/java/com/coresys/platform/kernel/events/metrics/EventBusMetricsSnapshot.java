/**
 * CoreSys Platform Kernel
 * Copyright (c) 2026 Evgeniy Platonov
 * Licensed under the Apache License, Version 2.0
 * https://www.apache.org/licenses/LICENSE-2.0
 */

package com.coresys.platform.kernel.events.metrics;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Снимок конфигурации: EventBusMetricsSnapshot.
 *
 * Иммутабельное представление значений конфигурации в конкретный момент времени.
 *
 * @author Евгений Платонов
 */

public final class EventBusMetricsSnapshot {

    private final long publishedTotal;
    private final Map<String, SubscriptionMetricsSnapshot> subscriptions;

    public EventBusMetricsSnapshot(long publishedTotal, Map<String, SubscriptionMetricsSnapshot> subscriptions) {
        this.publishedTotal = Math.max(0, publishedTotal);
        this.subscriptions = Collections.unmodifiableMap(new LinkedHashMap<>(Objects.requireNonNull(subscriptions, "subscriptions")));
    }

    public long publishedTotal() {
        return publishedTotal;
    }

    public Map<String, SubscriptionMetricsSnapshot> subscriptions() {
        return subscriptions;
    }
}
