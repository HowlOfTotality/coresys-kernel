/**
 * CoreSys Platform Kernel
 * Copyright (c) 2026 Evgeniy Platonov
 * Licensed under the Apache License, Version 2.0
 * https://www.apache.org/licenses/LICENSE-2.0
 */

package com.coresys.platform.kernel.di;

/**
 * Параметры: ServiceRegistrationOptions.
 *
 * Набор настроек, управляющих поведением соответствующего компонента.
 *
 * @author Евгений Платонов
 */

public final class ServiceRegistrationOptions {

    private final boolean exclusive;

    private ServiceRegistrationOptions(boolean exclusive) {
        this.exclusive = exclusive;
    }

    public boolean isExclusive() {
        return exclusive;
    }

    public static ServiceRegistrationOptions exclusive() {
        return new ServiceRegistrationOptions(true);
    }

    public static ServiceRegistrationOptions shared() {
        return new ServiceRegistrationOptions(false);
    }
}
