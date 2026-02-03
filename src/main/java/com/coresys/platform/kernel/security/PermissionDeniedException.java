/**
 * CoreSys Platform Kernel
 * Copyright (c) 2026 Evgeniy Platonov
 * Licensed under the Apache License, Version 2.0
 * https://www.apache.org/licenses/LICENSE-2.0
 */

package com.coresys.platform.kernel.security;

/**
 * Исключение ядра: PermissionDeniedException.
 *
 * Используется для передачи причины сбоя и диагностической цепочки запуска.
 *
 * @author Евгений Платонов
 */

public final class PermissionDeniedException extends RuntimeException {

    public PermissionDeniedException(String message) {
        super(message);
    }
}
