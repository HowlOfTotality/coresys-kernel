/**
 * CoreSys Platform Kernel
 * Copyright (c) 2026 Evgeniy Platonov
 * Licensed under the Apache License, Version 2.0
 * https://www.apache.org/licenses/LICENSE-2.0
 */

package com.coresys.platform.kernel.security;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Role.
 *
 * Инфраструктурный компонент ядра CoreSys.
 *
 * @author Евгений Платонов
 */

public final class Role {

    private final String name;
    private final Set<Capability> capabilities;

    public Role(String name, Set<Capability> capabilities) {
        String n = Objects.requireNonNull(name, "name").trim();
        if (n.isEmpty()) {
            throw new IllegalArgumentException("role name is empty");
        }
        this.name = n;
        this.capabilities = Collections.unmodifiableSet(new LinkedHashSet<>(Objects.requireNonNull(capabilities, "capabilities")));
    }

    public String name() {
        return name;
    }

    public Set<Capability> capabilities() {
        return capabilities;
    }

    @Override
    public String toString() {
        return "Role{" + name + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Role)) return false;
        Role role = (Role) o;
        return name.equals(role.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
