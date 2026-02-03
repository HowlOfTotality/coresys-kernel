/**
 * CoreSys Platform Kernel
 * Copyright (c) 2026 Evgeniy Platonov
 * Licensed under the Apache License, Version 2.0
 * https://www.apache.org/licenses/LICENSE-2.0
 */

package com.coresys.platform.kernel;

import com.coresys.platform.kernel.modules.Module;
import com.coresys.platform.kernel.report.KernelPropsReporter;
import com.coresys.platform.kernel.modules.impl.InMemoryModuleRegistry;

import java.util.ServiceLoader;

/**
 * Точка входа (launcher) ядра CoreSys.
 *
 * Загружает модули через ServiceLoader, создаёт Kernel и переводит его на целевой start level.
 * Печатает свойства и диагностический отчёт, а также добавляет shutdown-hook для корректной остановки.
 *
 * @author Евгений Платонов (29.01.2026)
 */

public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        int targetLevel = readTargetLevel(args);

        InMemoryModuleRegistry registry = new InMemoryModuleRegistry();

        int loaded = 0;
        for (Module module : ServiceLoader.load(Module.class)) {
            registry.register(module);
            loaded++;
        }
        if (loaded == 0) {
            System.out.println("No modules found via ServiceLoader. Hint: add META-INF/services/"
                    + Module.class.getName() + " or register modules programmatically.");
        }

        Kernel kernel = KernelFactory.create(registry);

        KernelPropsReporter propsReporter = new KernelPropsReporter();
        System.out.println(propsReporter.format(propsReporter.build(kernel.kernelProps())));
        System.out.println(kernel.diagnostics().format(kernel.diagnostics().buildReport(kernel.modules())));

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                kernel.close();
            } catch (Exception ignored) {
            }
        }, "coresys-kernel-shutdown"));

        kernel.startLevels().setTargetLevel(targetLevel);

        System.out.println("CoreSys Kernel started. targetLevel=" + targetLevel
                + ", moduleCount=" + kernel.modules().all().size()
                + ", currentLevel=" + kernel.startLevels().getCurrentLevel());
    }

    private static int readTargetLevel(String[] args) {
        Integer fromCli = parseArgInt(args, "--level");
        if (fromCli != null) return fromCli;

        String prop = System.getProperty("coresys.kernel.targetLevel");
        if (prop != null && !prop.trim().isEmpty()) {
            try {
                return Integer.parseInt(prop.trim());
            } catch (NumberFormatException ignored) {
            }
        }
        return 10;
    }

    private static Integer parseArgInt(String[] args, String key) {
        if (args == null) return null;

        for (int i = 0; i < args.length; i++) {
            String a = args[i];
            if (a == null) continue;

            if (a.startsWith(key + "=")) {
                String v = a.substring((key + "=").length()).trim();
                if (!v.isEmpty()) {
                    try {
                        return Integer.parseInt(v);
                    } catch (NumberFormatException ignored) {
                        return null;
                    }
                }
                return null;
            }

            if (a.equals(key) && i + 1 < args.length) {
                String v = args[i + 1];
                if (v != null) {
                    v = v.trim();
                    try {
                        return Integer.parseInt(v);
                    } catch (NumberFormatException ignored) {
                        return null;
                    }
                }
                return null;
            }
        }
        return null;
    }
}
