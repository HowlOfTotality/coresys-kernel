/**
 * CoreSys Platform Kernel
 * Copyright (c) 2026 Evgeniy Platonov
 * Licensed under the Apache License, Version 2.0
 * https://www.apache.org/licenses/LICENSE-2.0
 */

package com.coresys.platform.kernel.security.impl;

import com.coresys.platform.kernel.security.Capability;
import com.coresys.platform.kernel.security.Principal;
import com.coresys.platform.kernel.security.Role;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * SimplePrincipal.
 *
 * Инфраструктурный компонент ядра CoreSys.
 *
 * @author Евгений Платонов
 */

public final class SimplePrincipal implements Principal {

    private final String id;
    private final Set<Role> roles;
    private final Set<Capability> capabilities;

    public SimplePrincipal(String id, Set<Role> roles, Set<Capability> capabilities) {
        String v = Objects.requireNonNull(id, "id").trim();
        if (v.isEmpty()) {
            throw new IllegalArgumentException("principal id is empty");
        }
        this.id = v;
        this.roles = Collections.unmodifiableSet(new LinkedHashSet<>(roles == null ? Set.of() : roles));
        this.capabilities = Collections.unmodifiableSet(new LinkedHashSet<>(capabilities == null ? Set.of() : capabilities));
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public Set<Role> roles() {
        return roles;
    }

    @Override
    public Set<Capability> capabilities() {
        return capabilities;
    }
}
