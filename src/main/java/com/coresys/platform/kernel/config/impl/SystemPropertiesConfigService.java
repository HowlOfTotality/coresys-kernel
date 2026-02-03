/**
 * CoreSys Platform Kernel
 * Copyright (c) 2026 Evgeniy Platonov
 * Licensed under the Apache License, Version 2.0
 * https://www.apache.org/licenses/LICENSE-2.0
 */

package com.coresys.platform.kernel.config.impl;

import com.coresys.platform.kernel.config.ConfigService;
import com.coresys.platform.kernel.config.ConfigSnapshot;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Сервис ядра: SystemPropertiesConfigService.
 *
 * Предоставляет функциональность для модулей и инфраструктурных компонентов.
 *
 * @author Евгений Платонов
 */

public final class SystemPropertiesConfigService implements ConfigService {

    private final AtomicLong version = new AtomicLong(0);
    private volatile ConfigSnapshot snapshot = new ConfigSnapshot(0, Instant.EPOCH, Map.of());

    @Override
    public ConfigSnapshot snapshot() {
        return snapshot;
    }

    @Override
    public synchronized ConfigSnapshot reload() {
        Map<String, String> values = new HashMap<>();

        for (Map.Entry<Object, Object> e : System.getProperties().entrySet()) {
            Object k = e.getKey();
            Object v = e.getValue();
            if (k != null && v != null) {
                values.put(String.valueOf(k), String.valueOf(v));
            }
        }

        for (Map.Entry<String, String> e : System.getenv().entrySet()) {
            String envKey = e.getKey();
            String envVal = e.getValue();
            if (envKey == null || envVal == null) continue;

            values.putIfAbsent(envKey, envVal);

            String dot = envKey.toLowerCase().replace('_', '.');
            values.putIfAbsent(dot, envVal);
        }

        long v = version.incrementAndGet();
        snapshot = new ConfigSnapshot(v, Instant.now(), values);
        return snapshot;
    }
}
