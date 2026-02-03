/**
 * CoreSys Platform Kernel
 * Copyright (c) 2026 Evgeniy Platonov
 * Licensed under the Apache License, Version 2.0
 * https://www.apache.org/licenses/LICENSE-2.0
 */

package com.coresys.platform.kernel.legacy;

/**
 * Контракт: LegacyPlugin.
 *
 * Определяет публичный API компонента.
 *
 * @author Евгений Платонов
 */

public interface LegacyPlugin {

    void start_plugin() throws Exception;

    void stop_plugin() throws Exception;
}
