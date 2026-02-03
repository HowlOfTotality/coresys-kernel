/**
 * CoreSys Platform Kernel
 * Copyright (c) 2026 Evgeniy Platonov
 * Licensed under the Apache License, Version 2.0
 * https://www.apache.org/licenses/LICENSE-2.0
 */

package com.coresys.platform.kernel.security;

import java.util.Objects;
import java.util.Set;

/**
 * SecurityContext.
 *
 * Инфраструктурный компонент ядра CoreSys.
 *
 * @author Евгений Платонов
 */

public final class SecurityContext {

    private final Set<Role> roles;

    public SecurityContext(Set<Role> roles) {
        this.roles = Set.copyOf(Objects.requireNonNull(roles, "roles"));
    }

    public boolean has(Capability capability) {
        for (Role r : roles) {
            if (r.capabilities().contains(capability)) {
                return true;
            }
        }
        return false;
    }

    public Set<Role> roles() {
        return roles;
    }
}
