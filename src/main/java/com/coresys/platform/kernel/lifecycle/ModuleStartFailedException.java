/**
 * CoreSys Platform Kernel
 * Copyright (c) 2026 Evgeniy Platonov
 * Licensed under the Apache License, Version 2.0
 * https://www.apache.org/licenses/LICENSE-2.0
 */

package com.coresys.platform.kernel.lifecycle;

import com.coresys.platform.kernel.modules.ModuleId;

import java.util.List;
import java.util.Objects;

/**
 * Исключение ядра: ModuleStartFailedException.
 *
 * Используется для передачи причины сбоя и диагностической цепочки запуска.
 *
 * @author Евгений Платонов
 */

public final class ModuleStartFailedException extends Exception {

    private final ModuleId moduleId;
    private final List<String> chain;

    public ModuleStartFailedException(ModuleId moduleId, String message, Throwable cause, List<String> chain) {
        super(message, cause);
        this.moduleId = Objects.requireNonNull(moduleId, "moduleId");
        this.chain = chain == null ? List.of() : List.copyOf(chain);
    }

    public ModuleId moduleId() {
        return moduleId;
    }

    public List<String> chain() {
        return chain;
    }
}
