/**
 * CoreSys Platform Kernel
 * Copyright (c) 2026 Evgeniy Platonov
 * Licensed under the Apache License, Version 2.0
 * https://www.apache.org/licenses/LICENSE-2.0
 */

package com.coresys.platform.kernel.security;

import java.util.Set;

/**
 * KernelRoles.
 *
 * Инфраструктурный компонент ядра CoreSys.
 *
 * @author Евгений Платонов
 */

public final class KernelRoles {

    private KernelRoles() {
    }

    public static final Role USER = new Role("USER", Set.of(
            new Capability(KernelPermissions.DIAGNOSTICS_READ.id())
    ));

    public static final Role OPERATOR = new Role("OPERATOR", Set.of(
            new Capability(KernelPermissions.DIAGNOSTICS_READ.id()),
            new Capability(KernelPermissions.CONTROL_COMMAND.id())
    ));

    public static final Role ADMIN = new Role("ADMIN", Set.of(
            new Capability(KernelPermissions.DIAGNOSTICS_READ.id()),
            new Capability(KernelPermissions.CONTROL_COMMAND.id()),
            new Capability(KernelPermissions.ADMIN_COMMAND.id())
    ));
}
