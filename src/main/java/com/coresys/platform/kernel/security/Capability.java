/**
 * CoreSys Platform Kernel
 * Copyright (c) 2026 Evgeniy Platonov
 * Licensed under the Apache License, Version 2.0
 * https://www.apache.org/licenses/LICENSE-2.0
 */

package com.coresys.platform.kernel.security;

import java.util.Objects;

/**
 * Capability.
 *
 * Инфраструктурный компонент ядра CoreSys.
 *
 * @author Евгений Платонов
 */

public final class Capability {

    private final String id;

    public Capability(String id) {
        String v = Objects.requireNonNull(id, "id").trim();
        if (v.isEmpty()) {
            throw new IllegalArgumentException("capability id is empty");
        }
        this.id = v;
    }

    public String id() {
        return id;
    }

    @Override
    public String toString() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Capability)) return false;
        return id.equals(((Capability) o).id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
