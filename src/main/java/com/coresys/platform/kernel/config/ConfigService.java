/**
 * CoreSys Platform Kernel
 * Copyright (c) 2026 Evgeniy Platonov
 * Licensed under the Apache License, Version 2.0
 * https://www.apache.org/licenses/LICENSE-2.0
 */

package com.coresys.platform.kernel.config;

/**
 * Сервис ядра: ConfigService.
 *
 * Предоставляет функциональность для модулей и инфраструктурных компонентов.
 *
 * @author Евгений Платонов
 */

public interface ConfigService {

    ConfigSnapshot snapshot();

    ConfigSnapshot reload();
}
