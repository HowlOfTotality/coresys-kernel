/**
 * CoreSys Platform Kernel
 * Copyright (c) 2026 Evgeniy Platonov
 * Licensed under the Apache License, Version 2.0
 * https://www.apache.org/licenses/LICENSE-2.0
 */

package com.coresys.platform.kernel.security;

/**
 * Контракт: Authorizer.
 *
 * Определяет публичный API компонента.
 *
 * @author Евгений Платонов
 */

public interface Authorizer {

    boolean has(Principal principal, KernelPermission permission);

    default void require(Principal principal, KernelPermission permission) {
        if (!has(principal, permission)) {
            String pid = principal == null ? "<anonymous>" : principal.id();
            throw new PermissionDeniedException("Permission denied: " + permission.id() + " for " + pid);
        }
    }
}
