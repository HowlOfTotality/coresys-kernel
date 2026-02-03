/**
 * CoreSys Platform Kernel
 * Copyright (c) 2026 Evgeniy Platonov
 * Licensed under the Apache License, Version 2.0
 * https://www.apache.org/licenses/LICENSE-2.0
 */

package com.coresys.platform.kernel.legacy;

import com.coresys.platform.kernel.modules.Module;
import com.coresys.platform.kernel.modules.ModuleDescriptor;
import com.coresys.platform.kernel.modules.context.ModuleContext;

import java.util.Objects;

/**
 * LegacyPluginModule.
 *
 * Инфраструктурный компонент ядра CoreSys.
 *
 * @author Евгений Платонов
 */

public final class LegacyPluginModule implements Module {

    private final ModuleDescriptor descriptor;
    private final LegacyPlugin plugin;

    public LegacyPluginModule(ModuleDescriptor descriptor, LegacyPlugin plugin) {
        this.descriptor = Objects.requireNonNull(descriptor, "descriptor");
        this.plugin = Objects.requireNonNull(plugin, "plugin");
    }

    @Override
    public ModuleDescriptor descriptor() {
        return descriptor;
    }

    @Override
    public void start(ModuleContext ctx) throws Exception {
        plugin.start_plugin();
    }

    @Override
    public void stop(ModuleContext ctx) throws Exception {
        plugin.stop_plugin();
    }
}
