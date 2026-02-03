/**
 * CoreSys Platform Kernel
 * Copyright (c) 2026 Evgeniy Platonov
 * Licensed under the Apache License, Version 2.0
 * https://www.apache.org/licenses/LICENSE-2.0
 */

package com.coresys.platform.kernel.security.impl;

import com.coresys.platform.kernel.security.AuthService;
import com.coresys.platform.kernel.security.Authorizer;
import com.coresys.platform.kernel.security.KernelPermission;
import com.coresys.platform.kernel.security.Principal;

import java.util.Objects;
import java.util.concurrent.Callable;

/**
 * Сервис ядра: DefaultAuthService.
 *
 * Предоставляет функциональность для модулей и инфраструктурных компонентов.
 *
 * @author Евгений Платонов
 */

public final class DefaultAuthService implements AuthService {

    private final Authorizer authorizer;
    private final ThreadLocal<Principal> current = new ThreadLocal<>();

    public DefaultAuthService(Authorizer authorizer) {
        this.authorizer = Objects.requireNonNull(authorizer, "authorizer");
    }

    @Override
    public Principal current() {
        return current.get();
    }

    @Override
    public void runAs(Principal principal, Runnable action) {
        Objects.requireNonNull(action, "action");
        Principal prev = current.get();
        current.set(principal);
        try {
            action.run();
        } finally {
            if (prev == null) {
                current.remove();
            } else {
                current.set(prev);
            }
        }
    }

    @Override
    public <T> T callAs(Principal principal, Callable<T> action) throws Exception {
        Objects.requireNonNull(action, "action");
        Principal prev = current.get();
        current.set(principal);
        try {
            return action.call();
        } finally {
            if (prev == null) {
                current.remove();
            } else {
                current.set(prev);
            }
        }
    }

    @Override
    public void clear() {
        current.remove();
    }

    @Override
    public boolean has(KernelPermission permission) {
        return authorizer.has(current(), permission);
    }

    @Override
    public void require(KernelPermission permission) {
        authorizer.require(current(), permission);
    }
}
