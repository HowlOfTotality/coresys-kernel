/**
 * CoreSys Platform Kernel
 * Copyright (c) 2026 Evgeniy Platonov
 * Licensed under the Apache License, Version 2.0
 * https://www.apache.org/licenses/LICENSE-2.0
 */

package com.coresys.platform.kernel.modules;

/**
 * Перечисление: ModuleState.
 *
 * Набор констант, определяющих режим работы компонента.
 *
 * @author Евгений Платонов
 */

public enum ModuleState {
    RESOLVED,
    STARTING,
    ACTIVE,
    DEGRADED,
    STOPPING,
    STOPPED,
    FAILED
}
