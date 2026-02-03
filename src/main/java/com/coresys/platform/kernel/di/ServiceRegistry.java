/**
 * CoreSys Platform Kernel
 * Copyright (c) 2026 Evgeniy Platonov
 * Licensed under the Apache License, Version 2.0
 * https://www.apache.org/licenses/LICENSE-2.0
 */

package com.coresys.platform.kernel.di;

import com.coresys.platform.kernel.modules.ModuleId;

import java.util.List;
import java.util.Objects;

/**
 * Реестр Service.
 *
 * Отвечает за регистрацию и получение объектов по ключу.
 *
 * @author Евгений Платонов
 */

public interface ServiceRegistry {

    <T> void register(ModuleId owner, Class<T> type, T instance, ServiceRegistrationOptions options);

    <T> void registerProvider(ModuleId owner, Class<T> type, ServiceProvider<? extends T> provider, ServiceRegistrationOptions options);

    void unregister(ModuleId owner, Class<?> type);

    <T> T getFor(ModuleId consumer, Class<T> type);

    <T> List<T> getAllFor(ModuleId consumer, Class<T> type);

    boolean has(Class<?> type);

    default <T> T get(Class<T> type) {
        Objects.requireNonNull(type, "type");
        return getFor(null, type);
    }

    default <T> List<T> getAll(Class<T> type) {
        Objects.requireNonNull(type, "type");
        return getAllFor(null, type);
    }
}
