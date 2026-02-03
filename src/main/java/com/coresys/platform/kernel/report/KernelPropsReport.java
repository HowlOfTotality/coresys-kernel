/**
 * CoreSys Platform Kernel
 * Copyright (c) 2026 Evgeniy Platonov
 * Licensed under the Apache License, Version 2.0
 * https://www.apache.org/licenses/LICENSE-2.0
 */

package com.coresys.platform.kernel.report;

import com.coresys.platform.kernel.props.PropSource;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * KernelPropsReport.
 *
 * Инфраструктурный компонент ядра CoreSys.
 *
 * @author Евгений Платонов
 */

public final class KernelPropsReport {

    private final Instant loadedAt;
    private final Map<String, String> values;
    private final Map<String, PropSource> sources;

    public KernelPropsReport(Instant loadedAt, Map<String, String> values, Map<String, PropSource> sources) {
        this.loadedAt = Objects.requireNonNull(loadedAt, "loadedAt");
        this.values = Map.copyOf(Objects.requireNonNull(values, "values"));
        this.sources = Map.copyOf(Objects.requireNonNull(sources, "sources"));
    }

    public Instant loadedAt() {
        return loadedAt;
    }

    public Map<String, String> values() {
        return values;
    }

    public Map<String, PropSource> sources() {
        return sources;
    }
}
