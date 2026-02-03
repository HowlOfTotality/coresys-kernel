/**
 * CoreSys Platform Kernel
 * Copyright (c) 2026 Evgeniy Platonov
 * Licensed under the Apache License, Version 2.0
 * https://www.apache.org/licenses/LICENSE-2.0
 */

package com.coresys.platform.kernel.config;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * Снимок конфигурации: ConfigSnapshot.
 *
 * Иммутабельное представление значений конфигурации в конкретный момент времени.
 *
 * @author Евгений Платонов
 */

public final class ConfigSnapshot {

    private final long version;
    private final Instant loadedAt;
    private final Map<String, String> values;

    public ConfigSnapshot(long version, Instant loadedAt, Map<String, String> values) {
        this.version = version;
        this.loadedAt = Objects.requireNonNull(loadedAt, "loadedAt");
        this.values = Map.copyOf(Objects.requireNonNull(values, "values"));
    }

    public long version() {
        return version;
    }

    public Instant loadedAt() {
        return loadedAt;
    }

    public Map<String, String> values() {
        return values;
    }

    public String get(String key) {
        return values.get(key);
    }
}
