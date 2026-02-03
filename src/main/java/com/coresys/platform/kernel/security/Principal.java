/**
 * CoreSys Platform Kernel
 * Copyright (c) 2026 Evgeniy Platonov
 * Licensed under the Apache License, Version 2.0
 * https://www.apache.org/licenses/LICENSE-2.0
 */

package com.coresys.platform.kernel.security;

import java.util.Set;

/**
 * Контракт: Principal.
 *
 * Определяет публичный API компонента.
 *
 * @author Евгений Платонов
 */

public interface Principal {

    String id();

    Set<Role> roles();

    Set<Capability> capabilities();
}
