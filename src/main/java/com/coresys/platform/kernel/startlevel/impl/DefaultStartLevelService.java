/**
 * CoreSys Platform Kernel
 * Copyright (c) 2026 Evgeniy Platonov
 * Licensed under the Apache License, Version 2.0
 * https://www.apache.org/licenses/LICENSE-2.0
 */

package com.coresys.platform.kernel.startlevel.impl;

import com.coresys.platform.kernel.lifecycle.ModuleStartFailedException;
import com.coresys.platform.kernel.modules.Module;
import com.coresys.platform.kernel.modules.ModuleId;
import com.coresys.platform.kernel.modules.ModuleRegistry;
import com.coresys.platform.kernel.props.DebugFlags;
import com.coresys.platform.kernel.startlevel.LevelControllableLifecycle;
import com.coresys.platform.kernel.startlevel.StartLevelService;
import com.coresys.platform.kernel.startlevel.StartLevelStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Сервис ядра: DefaultStartLevelService.
 *
 * Предоставляет функциональность для модулей и инфраструктурных компонентов.
 *
 * @author Евгений Платонов
 */

public final class DefaultStartLevelService implements StartLevelService, Runnable {

    private final ModuleRegistry modules;
    private final LevelControllableLifecycle lifecycle;
    private final StartLevelStorage storage;
    private final DebugFlags debug;

    private final BlockingQueue<Runnable> jobs = new LinkedBlockingQueue<>();
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final Thread worker;

    private volatile int currentLevel;
    private volatile int targetLevel;

    public DefaultStartLevelService(ModuleRegistry modules, LevelControllableLifecycle lifecycle, StartLevelStorage storage, DebugFlags debug) {
        this.modules = Objects.requireNonNull(modules, "modules");
        this.lifecycle = Objects.requireNonNull(lifecycle, "lifecycle");
        this.storage = storage;
        this.debug = Objects.requireNonNull(debug, "debug");

        Integer persistedTarget = (storage == null) ? null : storage.loadTargetLevel();

        this.currentLevel = 0;

        this.targetLevel = persistedTarget == null ? 0 : Math.max(0, persistedTarget);


        //this.targetLevel = this.currentLevel;

        this.worker = new Thread(this, "coresys-startlevel");
        this.worker.setDaemon(false);
        this.worker.start();
    }

    @Override
    public int getCurrentLevel() {
        return currentLevel;
    }

    @Override
    public int getTargetLevel() {
        return targetLevel;
    }

    @Override
    public void setTargetLevel(int targetLevel) {
        if (targetLevel < 0) {
            throw new IllegalArgumentException("targetLevel must be >= 0, is " + targetLevel);
        }
        this.targetLevel = targetLevel;
        if (storage != null) storage.saveTargetLevel(targetLevel);
        enqueueReconcile();
    }

    private void enqueueReconcile() {
        if (!running.get()) return;
        jobs.offer(this::reconcile);
    }

    private void reconcile() {
        int desired = this.targetLevel;
        int active = this.currentLevel;

        if (desired == active) {
            return;
        }

        if (desired > active) {
            for (int lvl = active + 1; lvl <= desired; lvl++) {
                try {
                    lifecycle.startToLevel(lvl);
                    currentLevel = lvl;
                    if (storage != null) storage.saveCurrentLevel(currentLevel);
                } catch (ModuleStartFailedException e) {
                    if (storage != null) {
                        List<String> chain = new ArrayList<>();
                        chain.add("module=" + e.moduleId());
                        chain.addAll(e.chain());
                        storage.saveLastFailureChain(chain);
                    }
                    if (debug.lifecycle()) {
                        System.err.println("[kernel] StartLevel failed at level " + lvl + " (currentLevel=" + currentLevel + "): " + e.getMessage());
                        for (String s : e.chain()) {
                            System.err.println("[kernel]  - " + s);
                        }
                        if (e.getCause() != null) {
                            e.getCause().printStackTrace(System.err);
                        }
                    }
                    lifecycle.stopToLevel(currentLevel);
                    return;
                }
            }
        } else {
            lifecycle.stopToLevel(desired);
            currentLevel = desired;
            if (storage != null) storage.saveCurrentLevel(currentLevel);
        }
    }

    @Override
    public int getModuleLevel(ModuleId moduleId) {
        Objects.requireNonNull(moduleId, "moduleId");
        Module m = modules.find(moduleId);
        if (m == null) return -1;
        return m.descriptor().startLevel();
    }

    @Override
    public void shutdown() {
        if (!running.compareAndSet(true, false)) return;
        worker.interrupt();
        jobs.clear();
        if (storage != null) storage.saveCurrentLevel(currentLevel);
        lifecycle.stopToLevel(0);
    }

    @Override
    public void run() {
        while (running.get()) {
            try {
                Runnable job = jobs.take();
                job.run();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception ex) {
                if (debug.lifecycle()) {
                    System.err.println("[kernel] StartLevel worker error: " + ex);
                    ex.printStackTrace(System.err);
                }
            }
        }
    }
}
