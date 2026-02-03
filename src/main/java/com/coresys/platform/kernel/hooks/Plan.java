/**
 * CoreSys Platform Kernel
 * Copyright (c) 2026 Evgeniy Platonov
 * Licensed under the Apache License, Version 2.0
 * https://www.apache.org/licenses/LICENSE-2.0
 */

package com.coresys.platform.kernel.hooks;

import com.coresys.platform.kernel.modules.ModuleId;
import com.coresys.platform.kernel.report.DiagnosticsReport;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Plan.
 *
 * Инфраструктурный компонент ядра CoreSys.
 *
 * @author Евгений Платонов
 */

public final class Plan {

    private final DiagnosticsReport diagnosticsReport;
    private final List<ModuleId> startOrder;
    private final int targetLevel;

    public Plan(DiagnosticsReport diagnosticsReport, List<ModuleId> startOrder, int targetLevel) {
        this.diagnosticsReport = Objects.requireNonNull(diagnosticsReport, "diagnosticsReport");
        this.startOrder = List.copyOf(Objects.requireNonNull(startOrder, "startOrder"));
        this.targetLevel = targetLevel;
    }

    public DiagnosticsReport diagnosticsReport() {
        return diagnosticsReport;
    }

    public List<ModuleId> startOrder() {
        return Collections.unmodifiableList(startOrder);
    }

    public int targetLevel() {
        return targetLevel;
    }
}
