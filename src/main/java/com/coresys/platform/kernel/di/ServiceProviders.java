/**
 * CoreSys Platform Kernel
 * Copyright (c) 2026 Evgeniy Platonov
 * Licensed under the Apache License, Version 2.0
 * https://www.apache.org/licenses/LICENSE-2.0
 */

package com.coresys.platform.kernel.di;

import com.coresys.platform.kernel.modules.ModuleId;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

/**
 * ServiceProviders.
 *
 * Инфраструктурный компонент ядра CoreSys.
 *
 * @author Евгений Платонов
 */

public final class ServiceProviders {

    private ServiceProviders() {
    }

    public static <T> ServiceProvider<T> singleton(T instance) {
        Objects.requireNonNull(instance, "instance");
        return consumer -> instance;
    }

    public static <T> ServiceProvider<T> scoped(Function<ModuleId, ? extends T> factory) {
        Objects.requireNonNull(factory, "factory");
        return new ScopedProvider<>(factory);
    }

    private static final class ScopedProvider<T> implements ServiceProvider<T> {
        private final Function<ModuleId, ? extends T> factory;
        private final ConcurrentMap<ModuleId, T> cache = new ConcurrentHashMap<>();

        private ScopedProvider(Function<ModuleId, ? extends T> factory) {
            this.factory = factory;
        }

        @Override
        public T get(ModuleId consumer) {
            if (consumer == null) {
                return Objects.requireNonNull(factory.apply(null), "scoped factory returned null");
            }
            return cache.computeIfAbsent(consumer, k -> Objects.requireNonNull(factory.apply(k), "scoped factory returned null"));
        }

        @Override
        public void close() {
            cache.clear();
        }
    }
}
