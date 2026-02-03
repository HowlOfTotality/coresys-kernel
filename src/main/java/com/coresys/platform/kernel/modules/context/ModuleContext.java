/**
 * CoreSys Platform Kernel
 * Copyright (c) 2026 Evgeniy Platonov
 * Licensed under the Apache License, Version 2.0
 * https://www.apache.org/licenses/LICENSE-2.0
 */

package com.coresys.platform.kernel.modules.context;

import com.coresys.platform.kernel.config.ConfigService;
import com.coresys.platform.kernel.di.ServiceRegistry;
import com.coresys.platform.kernel.events.EventBus;
import com.coresys.platform.kernel.modules.ModuleId;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * ModuleContext.
 *
 * Инфраструктурный компонент ядра CoreSys.
 *
 * @author Евгений Платонов
 */

public final class ModuleContext {

    private final ModuleId moduleId;
    private final ServiceRegistry services;
    private final EventBus events;
    private final ConfigService config;
    private final Path dataDir;

    public ModuleContext(ModuleId moduleId, ServiceRegistry services, EventBus events, ConfigService config, Path dataDir) {
        this.moduleId = Objects.requireNonNull(moduleId, "moduleId");
        this.services = Objects.requireNonNull(services, "services");
        this.events = Objects.requireNonNull(events, "events");
        this.config = Objects.requireNonNull(config, "config");
        this.dataDir = Objects.requireNonNull(dataDir, "dataDir");
    }

    public ModuleId moduleId() {
        return moduleId;
    }

    public ServiceRegistry services() {
        return services;
    }

    public <T> T service(Class<T> type) {
        return services.getFor(moduleId, type);
    }

    public <T> List<T> services(Class<T> type) {
        return services.getAllFor(moduleId, type);
    }

    public EventBus events() {
        return events;
    }

    public ConfigService config() {
        return config;
    }

    public Path dataDir() {
        return dataDir;
    }
}
