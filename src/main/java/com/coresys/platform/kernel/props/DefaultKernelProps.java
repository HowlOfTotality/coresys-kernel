/**
 * CoreSys Platform Kernel
 * Copyright (c) 2026 Evgeniy Platonov
 * Licensed under the Apache License, Version 2.0
 * https://www.apache.org/licenses/LICENSE-2.0
 */

package com.coresys.platform.kernel.props;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Свойства ядра: DefaultKernelProps.
 *
 * Источник параметров запуска и диагностических флагов.
 *
 * @author Евгений Платонов
 */

public final class DefaultKernelProps implements KernelProps {

    public static final String KERNEL_HOME = "coresys.kernel.home";
    public static final String KERNEL_READ_ONLY = "coresys.kernel.readOnly";
    public static final String KERNEL_TARGET_LEVEL = "coresys.kernel.targetLevel";
    public static final String KERNEL_PROPS_FILE = "coresys.kernel.propsFile";

    private final Map<String, String> values;
    private final Map<String, PropSource> sources;

    private DefaultKernelProps(Map<String, String> values, Map<String, PropSource> sources) {
        this.values = Map.copyOf(values);
        this.sources = Map.copyOf(sources);
    }

    public static DefaultKernelProps loadDefaults() {
        Map<String, String> defaults = new LinkedHashMap<>();

        String userHome = System.getProperty("user.home", ".");
        defaults.put(KERNEL_HOME, Path.of(userHome, ".coresys", "kernel").toString());
        defaults.put(KERNEL_READ_ONLY, "false");
        defaults.put(KERNEL_TARGET_LEVEL, "1");

        defaults.put("debug.lifecycle", "false");
        defaults.put("debug.modules", "false");
        defaults.put("debug.events", "false");
        defaults.put("debug.services", "false");

        defaults.put("coresys.kernel.events.threads", "4");
        defaults.put("coresys.kernel.events.queue", "10000");

        return load(defaults);
    }

    public static DefaultKernelProps load(Map<String, String> defaults) {
        Objects.requireNonNull(defaults, "defaults");

        Map<String, String> merged = new LinkedHashMap<>();
        Map<String, PropSource> src = new LinkedHashMap<>();

        for (var e : defaults.entrySet()) {
            merged.put(e.getKey(), e.getValue());
            src.put(e.getKey(), PropSource.DEFAULT);
        }

        Properties sys = System.getProperties();
        for (String name : sys.stringPropertyNames()) {
            String val = sys.getProperty(name);
            if (val != null) {
                merged.put(name, val);
                src.put(name, PropSource.SYSTEM);
            }
        }

        Map<String, String> env = System.getenv();
        for (var e : env.entrySet()) {
            String key = envToKey(e.getKey());
            if (key == null) continue;
            String val = e.getValue();
            if (val == null) continue;
            merged.put(key, val);
            src.put(key, PropSource.ENV);
        }

        String filePath = merged.get(KERNEL_PROPS_FILE);
        if (filePath != null && !filePath.isBlank()) {
            Path p = Path.of(filePath.trim());
            if (Files.isRegularFile(p)) {
                Properties fileProps = new Properties();
                try (BufferedReader r = new BufferedReader(new InputStreamReader(Files.newInputStream(p), StandardCharsets.UTF_8))) {
                    fileProps.load(r);
                } catch (IOException ex) {
                }
                for (String name : fileProps.stringPropertyNames()) {
                    String val = fileProps.getProperty(name);
                    if (val != null) {
                        merged.put(name, val);
                        src.put(name, PropSource.FILE);
                    }
                }
            }
        }

        return new DefaultKernelProps(merged, src);
    }

    private static String envToKey(String envKey) {
        if (envKey == null) return null;

        if (envKey.startsWith("CORESYS_")) {
            String rest = envKey.substring("CORESYS_".length());
            String lower = rest.toLowerCase(Locale.ROOT);
            lower = lower.replace("__", "-");
            lower = lower.replace('_', '.');
            if (!lower.startsWith("coresys.")) {
                lower = "coresys." + lower;
            }
            return lower;
        }

        if (envKey.startsWith("DEBUG_")) {
            String rest = envKey.substring("DEBUG_".length()).toLowerCase(Locale.ROOT).replace('_', '.');
            return "debug." + rest;
        }

        return null;
    }

    @Override
    public Map<String, String> values() {
        return values;
    }

    @Override
    public Map<String, PropSource> sources() {
        return sources;
    }

    @Override
    public Optional<String> find(String key) {
        if (key == null) return Optional.empty();
        return Optional.ofNullable(values.get(key));
    }

    @Override
    public String getString(String key, String defaultValue) {
        if (key == null) return defaultValue;
        String v = values.get(key);
        if (v == null) return defaultValue;
        return v;
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        String v = getString(key, null);
        if (v == null) return defaultValue;
        return Boolean.parseBoolean(v.trim());
    }

    @Override
    public int getInt(String key, int defaultValue) {
        String v = getString(key, null);
        if (v == null) return defaultValue;
        try {
            return Integer.parseInt(v.trim());
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    @Override
    public long getLong(String key, long defaultValue) {
        String v = getString(key, null);
        if (v == null) return defaultValue;
        try {
            return Long.parseLong(v.trim());
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    @Override
    public Path getPath(String key, Path defaultValue) {
        String v = getString(key, null);
        if (v == null || v.isBlank()) return defaultValue;
        try {
            return Path.of(v.trim());
        } catch (Exception ex) {
            return defaultValue;
        }
    }
}
