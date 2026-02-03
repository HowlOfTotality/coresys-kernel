/**
 * CoreSys Platform Kernel
 * Copyright (c) 2026 Evgeniy Platonov
 * Licensed under the Apache License, Version 2.0
 * https://www.apache.org/licenses/LICENSE-2.0
 */

package com.coresys.platform.kernel.startlevel.impl;

import com.coresys.platform.kernel.startlevel.StartLevelStorage;
import com.coresys.platform.kernel.storage.KernelState;
import com.coresys.platform.kernel.storage.KernelStorage;

import java.util.Objects;

/**
 * Хранилище состояния ядра: KernelStartLevelStorage.
 *
 * Предоставляет доступ к данным ядра и данным модулей на диске/в выбранном backend-е.
 *
 * @author Евгений Платонов
 */

public final class KernelStartLevelStorage implements StartLevelStorage {

    private final KernelStorage storage;

    public KernelStartLevelStorage(KernelStorage storage) {
        this.storage = Objects.requireNonNull(storage, "storage");
    }

    @Override
    public Integer loadCurrentLevel() {
        return storage.loadState().getCurrentLevel();
    }

    @Override
    public Integer loadTargetLevel() {
        return storage.loadState().getTargetLevel();
    }

    @Override
    public void saveCurrentLevel(int currentLevel) {
        KernelState st = storage.loadState();
        st.setCurrentLevel(currentLevel);
        storage.saveState(st);
    }

    @Override
    public void saveTargetLevel(int targetLevel) {
        KernelState st = storage.loadState();
        st.setTargetLevel(targetLevel);
        storage.saveState(st);
    }
    @Override
    public void saveLastFailureChain(java.util.List<String> chain) {
        KernelState st = storage.loadState();
        st.setLastFailureChain(chain);
        storage.saveState(st);
    }

}
