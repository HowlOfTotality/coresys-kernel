/**
 * CoreSys Platform Kernel
 * Copyright (c) 2026 Evgeniy Platonov
 * Licensed under the Apache License, Version 2.0
 * https://www.apache.org/licenses/LICENSE-2.0
 */

package com.coresys.platform.kernel.security;

/**
 * Сервис ядра: SecurityService.
 *
 * Предоставляет функциональность для модулей и инфраструктурных компонентов.
 *
 * @author Евгений Платонов
 */

public interface SecurityService {

    SecurityContext current();

    default void check(Capability capability) {
        if (!current().has(capability)) {
            throw new SecurityException("Missing capability: " + capability.id());
        }
    }
}
