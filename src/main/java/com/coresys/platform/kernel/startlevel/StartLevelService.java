/**
 * CoreSys Platform Kernel
 * Copyright (c) 2026 Evgeniy Platonov
 * Licensed under the Apache License, Version 2.0
 * https://www.apache.org/licenses/LICENSE-2.0
 */

package com.coresys.platform.kernel.startlevel;

import com.coresys.platform.kernel.modules.ModuleId;

/**
 * Сервис ядра: StartLevelService.
 *
 * Предоставляет функциональность для модулей и инфраструктурных компонентов.
 *
 * @author Евгений Платонов
 */

public interface StartLevelService {

    int getCurrentLevel();

    int getTargetLevel();

    void setTargetLevel(int targetLevel);

    int getModuleLevel(ModuleId moduleId);

    void shutdown();
}
