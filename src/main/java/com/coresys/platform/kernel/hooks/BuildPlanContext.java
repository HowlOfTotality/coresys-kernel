/**
 * CoreSys Platform Kernel
 * Copyright (c) 2026 Evgeniy Platonov
 * Licensed under the Apache License, Version 2.0
 * https://www.apache.org/licenses/LICENSE-2.0
 */

package com.coresys.platform.kernel.hooks;

import com.coresys.platform.kernel.config.ConfigService;
import com.coresys.platform.kernel.di.ServiceRegistry;
import com.coresys.platform.kernel.events.EventBus;
import com.coresys.platform.kernel.modules.ModuleRegistry;

import java.util.Objects;

/**
 * BuildPlanContext.
 *
 * Инфраструктурный компонент ядра CoreSys.
 *
 * @author Евгений Платонов
 */

public final class BuildPlanContext {

    private final ModuleRegistry modules;
    private final ServiceRegistry services;
    private final EventBus events;
    private final ConfigService config;
    private final int targetLevel;

    public BuildPlanContext(
            ModuleRegistry modules,
            ServiceRegistry services,
            EventBus events,
            ConfigService config,
            int targetLevel
    ) {
        this.modules = Objects.requireNonNull(modules, "modules");
        this.services = Objects.requireNonNull(services, "services");
        this.events = Objects.requireNonNull(events, "events");
        this.config = Objects.requireNonNull(config, "config");
        this.targetLevel = targetLevel;
    }

    public ModuleRegistry modules() {
        return modules;
    }

    public ServiceRegistry services() {
        return services;
    }

    public EventBus events() {
        return events;
    }

    public ConfigService config() {
        return config;
    }

    public int targetLevel() {
        return targetLevel;
    }
}
