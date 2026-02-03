/**
 * CoreSys Platform Kernel
 * Copyright (c) 2026 Evgeniy Platonov
 * Licensed under the Apache License, Version 2.0
 * https://www.apache.org/licenses/LICENSE-2.0
 */

package com.coresys.platform.kernel.report;

import com.coresys.platform.kernel.modules.ModuleRegistry;

/**
 * Формирователь отчётов: DiagnosticsReporter.
 *
 * Собирает диагностическую информацию и предоставляет удобное текстовое представление.
 *
 * @author Евгений Платонов
 */

public interface DiagnosticsReporter {

    DiagnosticsReport buildReport(ModuleRegistry registry);

    String format(DiagnosticsReport report);
}
