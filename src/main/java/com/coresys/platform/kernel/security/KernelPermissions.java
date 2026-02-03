/**
 * CoreSys Platform Kernel
 * Copyright (c) 2026 Evgeniy Platonov
 * Licensed under the Apache License, Version 2.0
 * https://www.apache.org/licenses/LICENSE-2.0
 */

package com.coresys.platform.kernel.security;

/**
 * KernelPermissions.
 *
 * Инфраструктурный компонент ядра CoreSys.
 *
 * @author Евгений Платонов
 */

public final class KernelPermissions {

    private KernelPermissions() {
    }

    public static final KernelPermission CONTROL_COMMAND = new KernelPermission("kernel.control.command");

    public static final KernelPermission ADMIN_COMMAND = new KernelPermission("kernel.admin.command");

    public static final KernelPermission DIAGNOSTICS_READ = new KernelPermission("kernel.diagnostics.read");
}
