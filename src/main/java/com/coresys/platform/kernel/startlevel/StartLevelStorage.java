/**
 * CoreSys Platform Kernel
 * Copyright (c) 2026 Evgeniy Platonov
 * Licensed under the Apache License, Version 2.0
 * https://www.apache.org/licenses/LICENSE-2.0
 */

package com.coresys.platform.kernel.startlevel;

import java.util.List;

/**
 * Хранилище состояния ядра: StartLevelStorage.
 *
 * Предоставляет доступ к данным ядра и данным модулей на диске/в выбранном backend-е.
 *
 * @author Евгений Платонов
 */

public interface StartLevelStorage {

    Integer loadCurrentLevel();

    default Integer loadTargetLevel() {
        return null;
    }

    void saveCurrentLevel(int currentLevel);

    default void saveTargetLevel(int targetLevel) {
        // По умолчанию ничего не делает
    }

    default void saveLastFailureChain(List<String> chain) {
        // По умолчанию ничего не делает
    }
}
