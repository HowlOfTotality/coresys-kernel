/**
 * CoreSys Platform Kernel
 * Copyright (c) 2026 Evgeniy Platonov
 * Licensed under the Apache License, Version 2.0
 * https://www.apache.org/licenses/LICENSE-2.0
 */

package com.coresys.platform.kernel.security.impl;

import com.coresys.platform.kernel.security.Authorizer;
import com.coresys.platform.kernel.security.Capability;
import com.coresys.platform.kernel.security.KernelPermission;
import com.coresys.platform.kernel.security.Principal;
import com.coresys.platform.kernel.security.Role;

import java.util.Objects;
import java.util.Set;

/**
 * Базовая реализация: DefaultAuthorizer.
 *
 * Реализация по умолчанию для использования в большинстве окружений.
 *
 * @author Евгений Платонов
 */

public final class DefaultAuthorizer implements Authorizer {

    @Override
    public boolean has(Principal principal, KernelPermission permission) {
        Objects.requireNonNull(permission, "permission");
        if (principal == null) return false;

        String pid = permission.id();

        Set<Capability> caps = principal.capabilities();
        if (caps != null) {
            for (Capability c : caps) {
                if (c != null && pid.equals(c.id())) {
                    return true;
                }
            }
        }

        Set<Role> roles = principal.roles();
        if (roles != null) {
            for (Role r : roles) {
                if (r == null) continue;
                for (Capability c : r.capabilities()) {
                    if (c != null && pid.equals(c.id())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
