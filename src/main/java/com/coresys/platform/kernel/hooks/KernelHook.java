/**
 * CoreSys Platform Kernel
 * Copyright (c) 2026 Evgeniy Platonov
 * Licensed under the Apache License, Version 2.0
 * https://www.apache.org/licenses/LICENSE-2.0
 */

package com.coresys.platform.kernel.hooks;

import com.coresys.platform.kernel.modules.ModuleId;

/**
 * Контракт: KernelHook.
 *
 * Определяет публичный API компонента.
 *
 * @author Евгений Платонов
 */

public interface KernelHook {

    default void beforePlan(BuildPlanContext ctx) {
        // ничего не делаем
    }

    default void afterPlan(Plan plan) {
        // ничего не делаем
    }

    default void beforeStart(ModuleId moduleId) {
        // ничего не делаем
    }

    default void afterStart(ModuleId moduleId) {
        // ничего не делаем
    }

    default void onFailure(ModuleId moduleId, Throwable error) {
        // ничего не делаем
    }
}
