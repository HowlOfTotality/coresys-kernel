/**
 * CoreSys Platform Kernel
 * Copyright (c) 2026 Evgeniy Platonov
 * Licensed under the Apache License, Version 2.0
 * https://www.apache.org/licenses/LICENSE-2.0
 */

package com.coresys.platform.kernel.storage.impl;

import com.coresys.platform.kernel.modules.ModuleId;
import com.coresys.platform.kernel.storage.KernelState;
import com.coresys.platform.kernel.storage.KernelStorage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Instant;
import java.util.Objects;

/**
 * Хранилище состояния ядра: FileKernelStorage.
 *
 * Предоставляет доступ к данным ядра и данным модулей на диске/в выбранном backend-е.
 *
 * @author Евгений Платонов
 */

public final class FileKernelStorage implements KernelStorage {

    private final Path homeDir;
    private final Path stateFile;
    private final boolean readOnly;

    public FileKernelStorage(Path homeDir, boolean readOnly) {
        this.homeDir = Objects.requireNonNull(homeDir, "homeDir");
        this.stateFile = homeDir.resolve("state.json");
        this.readOnly = readOnly;
    }

    @Override
    public KernelState loadState() {
        try {
            if (!Files.exists(stateFile)) {
                return new KernelState().setUpdatedAt(Instant.now());
            }
            String json = Files.readString(stateFile, StandardCharsets.UTF_8);
            return KernelStateJson.fromJson(json);
        } catch (Exception e) {
            return new KernelState().setUpdatedAt(Instant.now());
        }
    }

    @Override
    public void saveState(KernelState state) {
        if (readOnly) return;
        try {
            Files.createDirectories(homeDir);
            KernelState st = (state == null) ? new KernelState() : state;
            st.setUpdatedAt(Instant.now());
            Files.writeString(stateFile, KernelStateJson.toJson(st), StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException ignored) {
        }
    }

    @Override
    public Path moduleDataDir(ModuleId moduleId) {
        Objects.requireNonNull(moduleId, "moduleId");
        String safe = sanitize(moduleId.value());
        Path dir = homeDir.resolve("modules").resolve(safe);
        if (!readOnly) {
            try {
                Files.createDirectories(dir);
            } catch (IOException ignored) {
            }
        }
        return dir;
    }

    @Override
    public Path homeDir() {
        return homeDir;
    }

    @Override
    public boolean isReadOnly() {
        return readOnly;
    }

    private static String sanitize(String s) {
        if (s == null || s.isEmpty()) return "unknown";
        StringBuilder out = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isLetterOrDigit(c) || c == '.' || c == '-' || c == '_') {
                out.append(c);
            } else {
                out.append('_');
            }
        }
        return out.toString();
    }
}
