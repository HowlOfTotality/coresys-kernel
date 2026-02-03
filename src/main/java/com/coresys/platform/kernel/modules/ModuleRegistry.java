/**
 * CoreSys Platform Kernel
 * Copyright (c) 2026 Evgeniy Platonov
 * Licensed under the Apache License, Version 2.0
 * https://www.apache.org/licenses/LICENSE-2.0
 */

package com.coresys.platform.kernel.modules;

import java.util.Collection;

/**
 * Реестр Module.
 *
 * Отвечает за регистрацию и получение объектов по ключу.
 *
 * @author Евгений Платонов
 */

public interface ModuleRegistry {

    void register(Module module);

    Module find(ModuleId id);

    Collection<Module> all();
}
