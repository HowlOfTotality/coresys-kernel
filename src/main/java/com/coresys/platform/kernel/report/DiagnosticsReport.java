/**
 * CoreSys Platform Kernel
 * Copyright (c) 2026 Evgeniy Platonov
 * Licensed under the Apache License, Version 2.0
 * https://www.apache.org/licenses/LICENSE-2.0
 */

package com.coresys.platform.kernel.report;

import com.coresys.platform.kernel.modules.ModuleId;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * DiagnosticsReport.
 *
 * Инфраструктурный компонент ядра CoreSys.
 *
 * @author Евгений Платонов
 */

public final class DiagnosticsReport {

    private final Map<ModuleId, List<ModuleId>> dependencyGraph;
    private final List<ModuleId> startOrder;
    private final List<String> missingRequirements;
    private final List<String> conflicts;

    public DiagnosticsReport(
            Map<ModuleId, List<ModuleId>> dependencyGraph,
            List<ModuleId> startOrder,
            List<String> missingRequirements,
            List<String> conflicts
    ) {
        this.dependencyGraph = Map.copyOf(Objects.requireNonNull(dependencyGraph, "dependencyGraph"));
        this.startOrder = List.copyOf(Objects.requireNonNull(startOrder, "startOrder"));
        this.missingRequirements = List.copyOf(Objects.requireNonNull(missingRequirements, "missingRequirements"));
        this.conflicts = List.copyOf(Objects.requireNonNull(conflicts, "conflicts"));
    }

    public Map<ModuleId, List<ModuleId>> dependencyGraph() {
        return dependencyGraph;
    }

    public List<ModuleId> startOrder() {
        return startOrder;
    }

    public List<String> missingRequirements() {
        return missingRequirements;
    }

    public List<String> conflicts() {
        return conflicts;
    }
}
