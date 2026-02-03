/**
 * CoreSys Platform Kernel
 * Copyright (c) 2026 Evgeniy Platonov
 * Licensed under the Apache License, Version 2.0
 * https://www.apache.org/licenses/LICENSE-2.0
 */

package com.coresys.platform.kernel.modules.impl;

import com.coresys.platform.kernel.modules.Module;
import com.coresys.platform.kernel.modules.ModuleId;
import com.coresys.platform.kernel.modules.ModuleRegistry;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Реестр InMemoryModule.
 *
 * Отвечает за регистрацию и получение объектов по ключу.
 *
 * @author Евгений Платонов
 */

public final class InMemoryModuleRegistry implements ModuleRegistry {

    private final Map<ModuleId, Module> modules = new LinkedHashMap<>();

    @Override
    public synchronized void register(Module module) {
        Module m = Objects.requireNonNull(module, "module");
        ModuleId id = Objects.requireNonNull(m.descriptor(), "descriptor").id();
        Module prev = modules.put(id, m);
        if (prev != null && prev != m) {
            throw new IllegalStateException("Duplicate module id: " + id);
        }
    }

    @Override
    public synchronized Module find(ModuleId id) {
        return modules.get(id);
    }

    @Override
    public synchronized Collection<Module> all() {
        return Collections.unmodifiableCollection(modules.values());
    }
}
