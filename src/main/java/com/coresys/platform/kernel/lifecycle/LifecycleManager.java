/**
 * CoreSys Platform Kernel
 * Copyright (c) 2026 Evgeniy Platonov
 * Licensed under the Apache License, Version 2.0
 * https://www.apache.org/licenses/LICENSE-2.0
 */

package com.coresys.platform.kernel.lifecycle;

import com.coresys.platform.kernel.modules.ModuleId;
import com.coresys.platform.kernel.modules.ModuleState;

import java.util.Map;

/**
 * Контракт: LifecycleManager.
 *
 * Определяет публичный API компонента.
 *
 * @author Евгений Платонов
 */

public interface LifecycleManager {

    void startAll() throws ModuleStartFailedException;

    void stopAll();

    ModuleState state(ModuleId moduleId);

    Map<ModuleId, ModuleState> statesSnapshot();
}
