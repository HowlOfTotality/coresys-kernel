/**
 * CoreSys Platform Kernel
 * Copyright (c) 2026 Evgeniy Platonov
 * Licensed under the Apache License, Version 2.0
 * https://www.apache.org/licenses/LICENSE-2.0
 */

package com.coresys.platform.kernel;

import com.coresys.platform.kernel.config.impl.SystemPropertiesConfigService;
import com.coresys.platform.kernel.di.impl.DefaultServiceRegistry;
import com.coresys.platform.kernel.events.impl.DefaultEventBus;
import com.coresys.platform.kernel.lifecycle.impl.TransactionalLifecycleManager;
import com.coresys.platform.kernel.props.DebugFlags;
import com.coresys.platform.kernel.props.DefaultKernelProps;
import com.coresys.platform.kernel.props.KernelProps;

import com.coresys.platform.kernel.security.AuthService;
import com.coresys.platform.kernel.security.Authorizer;
import com.coresys.platform.kernel.security.impl.DefaultAuthService;
import com.coresys.platform.kernel.security.impl.DefaultAuthorizer;
import com.coresys.platform.kernel.modules.ModuleId;
import com.coresys.platform.kernel.modules.ModuleRegistry;
import com.coresys.platform.kernel.report.impl.DefaultDiagnosticsReporter;
import com.coresys.platform.kernel.startlevel.StartLevelService;
import com.coresys.platform.kernel.startlevel.impl.DefaultStartLevelService;
import com.coresys.platform.kernel.startlevel.impl.KernelStartLevelStorage;
import com.coresys.platform.kernel.storage.KernelStorage;
import com.coresys.platform.kernel.storage.impl.FileKernelStorage;

import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Фабрика создания Kernel.
 *
 * Создаёт стандартную конфигурацию ядра: ServiceRegistry, EventBus, ConfigService,
 * DiagnosticsReporter, KernelStorage и LifecycleManager. Также регистрирует базовые
 * kernel-сервисы как exclusive, чтобы исключить конфликт реализаций.
 *
 * @author Евгений Платонов
 */

public final class KernelFactory {

    private KernelFactory() {
    }

    public static Kernel create(ModuleRegistry modules) {
        var services = new DefaultServiceRegistry();
        KernelProps props = DefaultKernelProps.loadDefaults();
        DebugFlags debug = new DebugFlags(props);

        int threads = props.getInt("coresys.kernel.events.threads", Math.max(2, Runtime.getRuntime().availableProcessors()));
        int queueSize = props.getInt("coresys.kernel.events.queue", 10_000);

        ThreadFactory tf = r -> {
            Thread t = new Thread(r);
            t.setName("coresys-events-" + t.getId());
            t.setDaemon(true);
            return t;
        };

        ExecutorService exec = new ThreadPoolExecutor(
                threads,
                threads,
                0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(queueSize),
                tf,
                new ThreadPoolExecutor.CallerRunsPolicy()
        );

        var eventBus = new DefaultEventBus(exec);
        var config = new SystemPropertiesConfigService();
        config.reload();

        var diagnostics = new DefaultDiagnosticsReporter();
        Path home = props.getPath(DefaultKernelProps.KERNEL_HOME,
        Path.of(System.getProperty("user.home"), ".coresys", "kernel"));
        boolean readOnly = props.getBoolean(DefaultKernelProps.KERNEL_READ_ONLY, false);
        KernelStorage storage = new FileKernelStorage(home, readOnly);

        var lifecycle = new TransactionalLifecycleManager(modules, services, eventBus, config, diagnostics, storage);

        StartLevelService startLevels = new DefaultStartLevelService(modules, lifecycle, new KernelStartLevelStorage(storage), debug);

        Authorizer authorizer = new DefaultAuthorizer();
        AuthService auth = new DefaultAuthService(authorizer);

        ModuleId kernelId = new ModuleId("kernel");
        services.register(kernelId, StartLevelService.class, startLevels, com.coresys.platform.kernel.di.ServiceRegistrationOptions.exclusive());
        services.register(kernelId, KernelStorage.class, storage, com.coresys.platform.kernel.di.ServiceRegistrationOptions.exclusive());
        services.register(kernelId, KernelProps.class, props, com.coresys.platform.kernel.di.ServiceRegistrationOptions.exclusive());
        services.register(kernelId, DebugFlags.class, debug, com.coresys.platform.kernel.di.ServiceRegistrationOptions.exclusive());
        services.register(kernelId, Authorizer.class, authorizer, com.coresys.platform.kernel.di.ServiceRegistrationOptions.exclusive());
        services.register(kernelId, AuthService.class, auth, com.coresys.platform.kernel.di.ServiceRegistrationOptions.exclusive());

        return new Kernel(modules, services, eventBus, config, lifecycle, diagnostics, startLevels, props, debug);
    }
}
