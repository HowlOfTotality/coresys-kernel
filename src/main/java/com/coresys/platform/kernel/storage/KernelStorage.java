/**
 * CoreSys Platform Kernel
 * Copyright (c) 2026 Evgeniy Platonov
 * Licensed under the Apache License, Version 2.0
 * https://www.apache.org/licenses/LICENSE-2.0
 */

package com.coresys.platform.kernel.storage;

import com.coresys.platform.kernel.modules.ModuleId;

import java.nio.file.Path;

/**
 * Хранилище состояния ядра: KernelStorage.
 *
 * Предоставляет доступ к данным ядра и данным модулей на диске/в выбранном backend-е.
 *
 * @author Евгений Платонов
 */

public interface KernelStorage {

    KernelState loadState();

    void saveState(KernelState state);

    Path moduleDataDir(ModuleId moduleId);

    Path homeDir();

    boolean isReadOnly();
}
