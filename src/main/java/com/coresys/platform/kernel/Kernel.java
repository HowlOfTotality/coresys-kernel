/**
 * CoreSys Platform Kernel
 * Copyright (c) 2026 Evgeniy Platonov
 * Licensed under the Apache License, Version 2.0
 * https://www.apache.org/licenses/LICENSE-2.0
 */

package com.coresys.platform.kernel;

import com.coresys.platform.kernel.config.ConfigService;
import com.coresys.platform.kernel.di.ServiceRegistry;
import com.coresys.platform.kernel.events.EventBus;
import com.coresys.platform.kernel.lifecycle.LifecycleManager;
import com.coresys.platform.kernel.startlevel.StartLevelService;
import com.coresys.platform.kernel.modules.ModuleRegistry;
import com.coresys.platform.kernel.report.DiagnosticsReporter;
import com.coresys.platform.kernel.props.DebugFlags;
import com.coresys.platform.kernel.props.KernelProps;

import java.util.Objects;

/**
 * Ядро CoreSys.
 *
 * Собирает и предоставляет доступ к ключевым инфраструктурным подсистемам:
 * - реестр модулей
 * - реестр сервисов (DI)
 * - шина событий
 * - конфигурация
 * - lifecycle модулей
 * - уровни запуска
 * - диагностика
 *
 * Kernel не содержит бизнес-логики; отвечает за корректный запуск и остановку окружения.
 *
 * @author Евгений Платонов
 */

public final class Kernel implements AutoCloseable {

    private final ModuleRegistry moduleRegistry;
    private final ServiceRegistry serviceRegistry;
    private final EventBus eventBus;
    private final ConfigService configService;
    private final LifecycleManager lifecycle;
    private final StartLevelService startLevels;
    private final DiagnosticsReporter diagnostics;
    private final KernelProps kernelProps;
    private final DebugFlags debugFlags;

    public Kernel(
            ModuleRegistry moduleRegistry,
            ServiceRegistry serviceRegistry,
            EventBus eventBus,
            ConfigService configService,
            LifecycleManager lifecycle,
            DiagnosticsReporter diagnostics,
            StartLevelService startLevels,
            KernelProps kernelProps,
            DebugFlags debugFlags
    ) {
        this.moduleRegistry = Objects.requireNonNull(moduleRegistry, "moduleRegistry");
        this.serviceRegistry = Objects.requireNonNull(serviceRegistry, "serviceRegistry");
        this.eventBus = Objects.requireNonNull(eventBus, "eventBus");
        this.configService = Objects.requireNonNull(configService, "configService");
        this.lifecycle = Objects.requireNonNull(lifecycle, "lifecycle");
        this.diagnostics = Objects.requireNonNull(diagnostics, "diagnostics");
        this.startLevels = Objects.requireNonNull(startLevels, "startLevels");
        this.kernelProps = Objects.requireNonNull(kernelProps, "kernelProps");
        this.debugFlags = Objects.requireNonNull(debugFlags, "debugFlags");
    }

    public ModuleRegistry modules() {
        return moduleRegistry;
    }

    public ServiceRegistry services() {
        return serviceRegistry;
    }

    public EventBus events() {
        return eventBus;
    }

    public ConfigService config() {
        return configService;
    }

    public LifecycleManager lifecycle() {
        return lifecycle;
    }

    public DiagnosticsReporter diagnostics() {
        return diagnostics;
    }

    public StartLevelService startLevels() {
        return startLevels;
    }

    public KernelProps kernelProps() {
        return kernelProps;
    }

    public DebugFlags debug() {
        return debugFlags;
    }


    @Override
    public void close() {
        try {
            // Сначала гасим уровни запуска, чтобы остановить дальнейшие переходы по start-level.
            startLevels.shutdown();
        } catch (Exception ignored) {
        }
        try {
            // Затем останавливаем все активные модули (в обратном порядке уровней).
            lifecycle.stopAll();
        } catch (Exception ignored) {
        }

        try {
            // В конце пытаемся корректно остановить executor шины событий (если это ExecutorService).
            java.util.concurrent.Executor ex = eventBus.executor();
            if (ex instanceof java.util.concurrent.ExecutorService) {
                java.util.concurrent.ExecutorService es = (java.util.concurrent.ExecutorService) ex;
                es.shutdown();
                // Небольшой таймаут на мягкую остановку; затем принудительный shutdownNow.
                if (!es.awaitTermination(2, java.util.concurrent.TimeUnit.SECONDS)) {
                    es.shutdownNow();
                }
            }
        } catch (Exception ignored) {
        }
    }
}
