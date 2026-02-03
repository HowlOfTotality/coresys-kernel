/**
 * CoreSys Platform Kernel
 * Copyright (c) 2026 Evgeniy Platonov
 * Licensed under the Apache License, Version 2.0
 * https://www.apache.org/licenses/LICENSE-2.0
 */

package com.coresys.platform.kernel.modules;

import java.util.Objects;

/**
 * Provision.
 *
 * Инфраструктурный компонент ядра CoreSys.
 *
 * @author Евгений Платонов
 */

public final class Provision {

    private final Class<?> serviceType;
    private final boolean exclusive;

    public Provision(Class<?> serviceType, boolean exclusive) {
        this.serviceType = Objects.requireNonNull(serviceType, "serviceType");
        this.exclusive = exclusive;
    }

    public Class<?> serviceType() {
        return serviceType;
    }

    public boolean exclusive() {
        return exclusive;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Provision)) return false;
        Provision that = (Provision) o;
        return exclusive == that.exclusive && serviceType.equals(that.serviceType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceType, exclusive);
    }

    @Override
    public String toString() {
        return "Provision{serviceType=" + serviceType.getName() + ", exclusive=" + exclusive + "}";
    }
}
