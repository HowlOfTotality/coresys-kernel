/**
 * CoreSys Platform Kernel
 * Copyright (c) 2026 Evgeniy Platonov
 * Licensed under the Apache License, Version 2.0
 * https://www.apache.org/licenses/LICENSE-2.0
 */

package com.coresys.platform.kernel.startlevel.impl;

import com.coresys.platform.kernel.startlevel.StartLevelStorage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Хранилище состояния ядра: FileStartLevelStorage.
 *
 * Предоставляет доступ к данным ядра и данным модулей на диске/в выбранном backend-е.
 *
 * @author Евгений Платонов
 */

public final class FileStartLevelStorage implements StartLevelStorage {

    private final Path file;

    public FileStartLevelStorage(Path file) {
        this.file = file;
    }

    @Override
    public Integer loadCurrentLevel() {
        try {
            if (!Files.exists(file)) return null;
            String s = Files.readString(file, StandardCharsets.UTF_8).trim();
            if (s.isEmpty()) return null;
            return Integer.parseInt(s);
        } catch (Exception ignored) {
            return null;
        }
    }

    @Override
    public void saveCurrentLevel(int currentLevel) {
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, Integer.toString(currentLevel), StandardCharsets.UTF_8);
        } catch (IOException ignored) {
        }
    }
}
