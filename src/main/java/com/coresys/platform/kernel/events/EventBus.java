/**
 * CoreSys Platform Kernel
 * Copyright (c) 2026 Evgeniy Platonov
 * Licensed under the Apache License, Version 2.0
 * https://www.apache.org/licenses/LICENSE-2.0
 */

package com.coresys.platform.kernel.events;

import java.util.concurrent.Executor;

/**
 * Контракт: EventBus.
 *
 * Определяет публичный API компонента.
 *
 * @author Евгений Платонов
 */

public interface EventBus {

    <E> Subscription subscribe(Class<E> eventType, EventHandler<? super E> handler, SubscriptionOptions options);

    void publish(Object event);

    Executor executor();

    interface Subscription {
        void unsubscribe();
    }
}
