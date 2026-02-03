/**
 * CoreSys Platform Kernel
 * Copyright (c) 2026 Evgeniy Platonov
 * Licensed under the Apache License, Version 2.0
 * https://www.apache.org/licenses/LICENSE-2.0
 */

package com.coresys.platform.kernel.props;

import java.util.Objects;

/**
 * DebugFlags.
 *
 * Инфраструктурный компонент ядра CoreSys.
 *
 * @author Евгений Платонов
 */

public final class DebugFlags {

    private final KernelProps props;

    public DebugFlags(KernelProps props) {
        this.props = Objects.requireNonNull(props, "props");
    }

    public boolean lifecycle() {
        return props.getBoolean("debug.lifecycle", false);
    }

    public boolean modules() {
        return props.getBoolean("debug.modules", false);
    }

    public boolean events() {
        return props.getBoolean("debug.events", false);
    }

    public boolean services() {
        return props.getBoolean("debug.services", false);
    }

    public boolean anyEnabled() {
        return lifecycle() || modules() || events() || services();
    }
}
