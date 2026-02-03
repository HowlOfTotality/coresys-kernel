/**
 * CoreSys Platform Kernel
 * Copyright (c) 2026 Evgeniy Platonov
 * Licensed under the Apache License, Version 2.0
 * https://www.apache.org/licenses/LICENSE-2.0
 */

package com.coresys.platform.kernel.startlevel;

import com.coresys.platform.kernel.lifecycle.ModuleStartFailedException;

/**
 * Контракт: LevelControllableLifecycle.
 *
 * Определяет публичный API компонента.
 *
 * @author Евгений Платонов
 */

public interface LevelControllableLifecycle {

    void startToLevel(int targetLevel) throws ModuleStartFailedException;

    void stopToLevel(int targetLevel);
}
