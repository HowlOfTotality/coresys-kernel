/**
 * CoreSys Platform Kernel
 * Copyright (c) 2026 Evgeniy Platonov
 * Licensed under the Apache License, Version 2.0
 * https://www.apache.org/licenses/LICENSE-2.0
 */

package com.coresys.platform.kernel.di;

import com.coresys.platform.kernel.modules.ModuleId;

/**
 * Контракт: ServiceProvider.
 *
 * Определяет публичный API компонента.
 *
 * @author Евгений Платонов
 */
@FunctionalInterface
public interface ServiceProvider<T> {

    T get(ModuleId consumer);

    default void close() {
        // ничего не делаем
    }
}
