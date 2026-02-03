/**
 * CoreSys Platform Kernel
 * Copyright (c) 2026 Evgeniy Platonov
 * Licensed under the Apache License, Version 2.0
 * https://www.apache.org/licenses/LICENSE-2.0
 */

package com.coresys.platform.kernel.storage;

import java.time.Instant;
import java.util.*;

/**
 * KernelState.
 *
 * Инфраструктурный компонент ядра CoreSys.
 *
 * @author Евгений Платонов
 */

public final class KernelState {

    private int currentLevel;
    private int targetLevel;

    private List<String> lastStartOrder = new ArrayList<>();
    private List<String> lastFailureChain = new ArrayList<>();

    private Map<String, String> moduleStates = new LinkedHashMap<>();

    private Instant updatedAt = Instant.now();

    public int getCurrentLevel() {
        return currentLevel;
    }

    public KernelState setCurrentLevel(int currentLevel) {
        this.currentLevel = Math.max(0, currentLevel);
        return this;
    }

    public int getTargetLevel() {
        return targetLevel;
    }

    public KernelState setTargetLevel(int targetLevel) {
        this.targetLevel = Math.max(0, targetLevel);
        return this;
    }

    public List<String> getLastStartOrder() {
        return lastStartOrder;
    }

    public KernelState setLastStartOrder(List<String> lastStartOrder) {
        this.lastStartOrder = (lastStartOrder == null) ? new ArrayList<>() : new ArrayList<>(lastStartOrder);
        return this;
    }

    public List<String> getLastFailureChain() {
        return lastFailureChain;
    }

    public KernelState setLastFailureChain(List<String> lastFailureChain) {
        this.lastFailureChain = (lastFailureChain == null) ? new ArrayList<>() : new ArrayList<>(lastFailureChain);
        return this;
    }

    public Map<String, String> getModuleStates() {
        return moduleStates;
    }

    public KernelState setModuleStates(Map<String, String> moduleStates) {
        this.moduleStates = (moduleStates == null) ? new LinkedHashMap<>() : new LinkedHashMap<>(moduleStates);
        return this;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public KernelState setUpdatedAt(Instant updatedAt) {
        this.updatedAt = (updatedAt == null) ? Instant.now() : updatedAt;
        return this;
    }
}
