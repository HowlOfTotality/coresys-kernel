/**
 * CoreSys Platform Kernel
 * Copyright (c) 2026 Evgeniy Platonov
 * Licensed under the Apache License, Version 2.0
 * https://www.apache.org/licenses/LICENSE-2.0
 */

package com.coresys.platform.kernel.security.impl;

import com.coresys.platform.kernel.security.SecurityContext;
import com.coresys.platform.kernel.security.SecurityService;

/**
 * Сервис ядра: DefaultSecurityService.
 *
 * Предоставляет функциональность для модулей и инфраструктурных компонентов.
 *
 * @author Евгений Платонов
 */

public final class DefaultSecurityService implements SecurityService {

    private final ThreadLocal<SecurityContext> ctx = new ThreadLocal<>();

    private final SecurityContext empty = new SecurityContext(java.util.Set.of());

    public void set(SecurityContext context) {
        ctx.set(context);
    }

    public void clear() {
        ctx.remove();
    }

    @Override
    public SecurityContext current() {
        SecurityContext c = ctx.get();
        return c != null ? c : empty;
    }
}
