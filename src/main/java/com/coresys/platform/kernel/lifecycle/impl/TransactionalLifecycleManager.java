/**
 * CoreSys Platform Kernel
 * Copyright (c) 2026 Evgeniy Platonov
 * Licensed under the Apache License, Version 2.0
 * https://www.apache.org/licenses/LICENSE-2.0
 */

package com.coresys.platform.kernel.lifecycle.impl;

import com.coresys.platform.kernel.config.ConfigService;
import com.coresys.platform.kernel.di.ServiceRegistry;
import com.coresys.platform.kernel.events.EventBus;
import com.coresys.platform.kernel.lifecycle.LifecycleManager;
import com.coresys.platform.kernel.lifecycle.ModuleStartFailedException;
import com.coresys.platform.kernel.modules.*;
import com.coresys.platform.kernel.modules.context.ModuleContext;
import com.coresys.platform.kernel.report.DiagnosticsReport;
import com.coresys.platform.kernel.report.DiagnosticsReporter;
import com.coresys.platform.kernel.storage.KernelState;
import com.coresys.platform.kernel.storage.KernelStorage;
import com.coresys.platform.kernel.startlevel.LevelControllableLifecycle;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import com.coresys.platform.kernel.hooks.BuildPlanContext;
import com.coresys.platform.kernel.hooks.KernelHook;
import com.coresys.platform.kernel.hooks.Plan;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Менеджер жизненного цикла модулей.
 *
 * Строит план запуска на основе диагностики, проверяет зависимости по сервисам,
 * запускает модули в порядке start order, а при ошибке выполняет rollback (stop ранее
 * запущенных модулей) и сохраняет состояние в KernelStorage (если он подключён).
 *
 * @author Евгений Платонов
 */

public final class TransactionalLifecycleManager implements LifecycleManager, LevelControllableLifecycle {

    private static final Logger LOG = Logger.getLogger(TransactionalLifecycleManager.class.getName());

    private final ModuleRegistry registry;
    private final ServiceRegistry services;
    private final EventBus events;
    private final ConfigService config;
    private final DiagnosticsReporter diagnostics;
    private final KernelStorage storage;

    private final Map<ModuleId, ModuleState> states = new ConcurrentHashMap<>();

    public TransactionalLifecycleManager(
            ModuleRegistry registry,
            ServiceRegistry services,
            EventBus events,
            ConfigService config,
            DiagnosticsReporter diagnostics,
            KernelStorage storage
    ) {
        this.registry = Objects.requireNonNull(registry, "registry");
        this.services = Objects.requireNonNull(services, "services");
        this.events = Objects.requireNonNull(events, "events");
        this.config = Objects.requireNonNull(config, "config");
        this.diagnostics = Objects.requireNonNull(diagnostics, "diagnostics");
        this.storage = storage;
    }

    @Override
    public void startAll() throws ModuleStartFailedException {
        startToLevel(Integer.MAX_VALUE);
    }

    @Override
    public void stopAll() {
        stopToLevel(0);
    }

    @Override
    public ModuleState state(ModuleId moduleId) {
        return states.getOrDefault(moduleId, ModuleState.RESOLVED);
    }

    @Override
    public Map<ModuleId, ModuleState> statesSnapshot() {
        return Map.copyOf(states);
    }

    @Override
    public void startToLevel(int targetLevel) throws ModuleStartFailedException {
        if (targetLevel < 0) {
            throw new IllegalArgumentException("targetLevel must be >= 0, is " + targetLevel);
        }

        List<KernelHook> hooks = services.getAllFor(null, KernelHook.class);
        if (hooks == null) {
            hooks = List.of();
        }

        BuildPlanContext buildCtx = new BuildPlanContext(registry, services, events, config, targetLevel);
        for (KernelHook h : hooks) {
            try {
                h.beforePlan(buildCtx);
            } catch (Throwable t) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "KernelHook.beforePlan failed: " + h.getClass().getName(), t);
                }
            }
        }

        DiagnosticsReport report = diagnostics.buildReport(registry);
        List<ModuleId> order = report.startOrder();

        Plan plan = new Plan(report, order, targetLevel);
        for (KernelHook h : hooks) {
            try {
                h.afterPlan(plan);
            } catch (Throwable t) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "KernelHook.afterPlan failed: " + h.getClass().getName(), t);
                }
            }
        }

        if (!report.missingRequirements().isEmpty() || !report.conflicts().isEmpty()) {
            throw new ModuleStartFailedException(
                    new ModuleId("kernel"),
                    "Kernel cannot start due to missing requirements/conflicts:\n" + diagnostics.format(report),
                    null,
                    report.missingRequirements()
            );
        }

        List<com.coresys.platform.kernel.modules.Module> startedThisCall = new ArrayList<>();
        ModuleId currentId = null;
        try {
            for (ModuleId id : order) {
                com.coresys.platform.kernel.modules.Module m = registry.find(id);
                if (m == null) continue;

                if (m.descriptor().startLevel() > targetLevel) {
                    continue;
                }

                if (state(id) == ModuleState.ACTIVE) {
                    continue;
                }

                currentId = id;
                states.put(id, ModuleState.STARTING);

                for (KernelHook h : hooks) {
                    try {
                        h.beforeStart(id);
                    } catch (Throwable t) {
                        if (LOG.isLoggable(Level.FINE)) {
                            LOG.log(Level.FINE, "KernelHook.beforeStart failed: " + h.getClass().getName() + " for " + id, t);
                        }
                    }
                }

                checkServiceRequirements(m);

                ModuleContext ctx = new ModuleContext(id, services, events, config, dataDirFor(id));
                m.start(ctx);

                states.put(id, ModuleState.ACTIVE);
                startedThisCall.add(m);

                for (KernelHook h : hooks) {
                    try {
                        h.afterStart(id);
                    } catch (Throwable t) {
                        if (LOG.isLoggable(Level.FINE)) {
                            LOG.log(Level.FINE, "KernelHook.afterStart failed: " + h.getClass().getName() + " for " + id, t);
                        }
                    }
                }
            }
            persistState(order, null);
        } catch (Exception e) {
            ModuleId failedId = currentId != null ? currentId : new ModuleId("unknown");

            for (KernelHook h : hooks) {
                try {
                    h.onFailure(failedId, e);
                } catch (Throwable t) {
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.log(Level.FINE, "KernelHook.onFailure failed: " + h.getClass().getName() + " for " + failedId, t);
                    }
                }
            }

            rollback(startedThisCall);

            states.put(failedId, ModuleState.FAILED);
            java.util.List<String> chain = java.util.List.of(e.getClass().getName() + ": " + String.valueOf(e.getMessage()));
            persistState(order, chain);
            throw new ModuleStartFailedException(
                    failedId,
                    "Module start failed: " + failedId,
                    e,
                    chain
            );
        }
        persistState(new java.util.ArrayList<>(report.startOrder()), null);
    }

    @Override
    public void stopToLevel(int targetLevel) {
        if (targetLevel < 0) {
            targetLevel = 0;
        }

        DiagnosticsReport report = diagnostics.buildReport(registry);
        List<ModuleId> order = new ArrayList<>(report.startOrder());
        Collections.reverse(order);

        for (ModuleId id : order) {
            com.coresys.platform.kernel.modules.Module m = registry.find(id);
            if (m == null) continue;

            int level = m.descriptor().startLevel();
            if (level <= targetLevel) {
                continue;
            }

            ModuleState st = state(id);
            if (st != ModuleState.ACTIVE && st != ModuleState.STARTING) {
                continue;
            }

            try {
                states.put(id, ModuleState.STOPPING);
                ModuleContext ctx = new ModuleContext(id, services, events, config, dataDirFor(id));
                m.stop(ctx);
                states.put(id, ModuleState.RESOLVED);
            } catch (Exception ignored) {
                states.put(id, ModuleState.FAILED);
            }
        }
    }


private java.nio.file.Path dataDirFor(ModuleId id) {
    if (storage == null) {
        return java.nio.file.Path.of(System.getProperty("java.io.tmpdir"), "coresys-kernel", id.value());
    }
    return storage.moduleDataDir(id);
}

private void persistState(java.util.List<ModuleId> lastOrder, java.util.List<String> failureChain) {
    if (storage == null) return;
    KernelState st = storage.loadState();
    if (lastOrder != null) {
        java.util.List<String> ids = new java.util.ArrayList<>(lastOrder.size());
        for (ModuleId mid : lastOrder) ids.add(mid.value());
        st.setLastStartOrder(ids);
    }
    if (failureChain != null) {
        st.setLastFailureChain(failureChain);
    }
    java.util.Map<String, String> ms = new java.util.LinkedHashMap<>();
    for (var e : states.entrySet()) {
        ms.put(e.getKey().value(), e.getValue().name());
    }
    st.setModuleStates(ms);
    storage.saveState(st);
}

    private void checkServiceRequirements(com.coresys.platform.kernel.modules.Module module) {
        ModuleDescriptor d = module.descriptor();
        if (d.serviceMode() == ServiceMode.DYNAMIC) {
            return;
        }
        for (Requirement r : d.requirements()) {
            if (r instanceof Requirement.ServiceRequirement) {
                Requirement.ServiceRequirement sr = (Requirement.ServiceRequirement) r;
                if (!sr.optional() && !services.has(sr.serviceType())) {
                    throw new IllegalStateException("Missing required service: " + sr.serviceType().getName()
                            + " for module " + d.id());
                }
            }
        }
    }

    private void rollback(List<com.coresys.platform.kernel.modules.Module> started) {
        ListIterator<com.coresys.platform.kernel.modules.Module> it = started.listIterator(started.size());
        while (it.hasPrevious()) {
            com.coresys.platform.kernel.modules.Module m = it.previous();
            ModuleId id = m.descriptor().id();
            try {
                states.put(id, ModuleState.STOPPING);
                ModuleContext ctx = new ModuleContext(id, services, events, config, dataDirFor(id));
                m.stop(ctx);
                states.put(id, ModuleState.RESOLVED);
            } catch (Exception ignored) {
                states.put(id, ModuleState.FAILED);
            }
        }
    }
}
