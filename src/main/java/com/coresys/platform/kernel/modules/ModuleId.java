/**
 * CoreSys Platform Kernel
 * Copyright (c) 2026 Evgeniy Platonov
 * Licensed under the Apache License, Version 2.0
 * https://www.apache.org/licenses/LICENSE-2.0
 */

package com.coresys.platform.kernel.modules;

import java.util.Objects;

/**
 * ModuleId.
 *
 * Инфраструктурный компонент ядра CoreSys.
 *
 * @author Евгений Платонов
 */

public final class ModuleId {

    private final String value;

    public ModuleId(String value) {
        String v = Objects.requireNonNull(value, "value").trim();
        if (v.isEmpty()) {
            throw new IllegalArgumentException("moduleId is empty");
        }
        this.value = v;
    }

    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ModuleId)) return false;
        return value.equals(((ModuleId) o).value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
