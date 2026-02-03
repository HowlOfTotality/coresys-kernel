/**
 * CoreSys Platform Kernel
 * Copyright (c) 2026 Evgeniy Platonov
 * Licensed under the Apache License, Version 2.0
 * https://www.apache.org/licenses/LICENSE-2.0
 */

package com.coresys.platform.kernel.props;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

/**
 * Свойства ядра: KernelProps.
 *
 * Источник параметров запуска и диагностических флагов.
 *
 * @author Евгений Платонов
 */

public interface KernelProps {

    Map<String, String> values();

    Map<String, PropSource> sources();

    Optional<String> find(String key);

    String getString(String key, String defaultValue);

    boolean getBoolean(String key, boolean defaultValue);

    int getInt(String key, int defaultValue);

    long getLong(String key, long defaultValue);

    Path getPath(String key, Path defaultValue);

    default boolean debug(String key, boolean defaultValue) {
        if (key == null || key.isBlank()) {
            return defaultValue;
        }
        String fullKey = key.startsWith("debug.") ? key : "debug." + key;
        return getBoolean(fullKey, defaultValue);
    }
}
