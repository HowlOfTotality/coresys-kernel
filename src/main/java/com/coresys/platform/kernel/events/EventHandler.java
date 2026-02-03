/**
 * CoreSys Platform Kernel
 * Copyright (c) 2026 Evgeniy Platonov
 * Licensed under the Apache License, Version 2.0
 * https://www.apache.org/licenses/LICENSE-2.0
 */

package com.coresys.platform.kernel.events;

/**
 * Контракт: EventHandler.
 *
 * Определяет публичный API компонента.
 *
 * @author Евгений Платонов
 */
@FunctionalInterface
public interface EventHandler<E> {
    void onEvent(E event) throws Exception;
}
