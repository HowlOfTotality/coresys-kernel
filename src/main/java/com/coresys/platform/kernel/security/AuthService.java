/**
 * CoreSys Platform Kernel
 * Copyright (c) 2026 Evgeniy Platonov
 * Licensed under the Apache License, Version 2.0
 * https://www.apache.org/licenses/LICENSE-2.0
 */

package com.coresys.platform.kernel.security;

import java.util.concurrent.Callable;

/**
 * Сервис ядра: AuthService.
 *
 * Предоставляет функциональность для модулей и инфраструктурных компонентов.
 *
 * @author Евгений Платонов
 */

public interface AuthService {

    Principal current();

    void runAs(Principal principal, Runnable action);

    <T> T callAs(Principal principal, Callable<T> action) throws Exception;

    void clear();

    boolean has(KernelPermission permission);

    void require(KernelPermission permission);
}
