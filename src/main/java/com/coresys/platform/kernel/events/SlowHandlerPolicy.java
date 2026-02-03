/**
 * CoreSys Platform Kernel
 * Copyright (c) 2026 Evgeniy Platonov
 * Licensed under the Apache License, Version 2.0
 * https://www.apache.org/licenses/LICENSE-2.0
 */

package com.coresys.platform.kernel.events;

/**
 * Перечисление: SlowHandlerPolicy.
 *
 * Набор констант, определяющих режим работы компонента.
 *
 * @author Евгений Платонов
 */

public enum SlowHandlerPolicy {

    NONE,
    LOG,
    DISABLE,
    BLACKLIST
}
