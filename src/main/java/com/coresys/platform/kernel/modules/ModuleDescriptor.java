/**
 * CoreSys Platform Kernel
 * Copyright (c) 2026 Evgeniy Platonov
 * Licensed under the Apache License, Version 2.0
 * https://www.apache.org/licenses/LICENSE-2.0
 */

package com.coresys.platform.kernel.modules;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * ModuleDescriptor.
 *
 * Инфраструктурный компонент ядра CoreSys.
 *
 * @author Евгений Платонов
 */

public final class ModuleDescriptor {

    private final ModuleId id;
    private final String displayName;
    private final ServiceMode serviceMode;
        private final int startLevel;
    private final List<Requirement> requirements;
    private final List<Provision> provisions;

    private ModuleDescriptor(Builder b) {
        this.id = Objects.requireNonNull(b.id, "id");
        this.displayName = (b.displayName == null || b.displayName.isBlank()) ? id.value() : b.displayName.trim();
        this.serviceMode = b.serviceMode == null ? ServiceMode.STATIC : b.serviceMode;
        int sl = (b.startLevel == null) ? 1 : b.startLevel;
        if (sl <= 0) {
            throw new IllegalArgumentException("startLevel must be > 0, is " + sl);
        }
        this.startLevel = sl;
        this.requirements = b.requirements == null ? List.of() : List.copyOf(b.requirements);
        this.provisions = b.provisions == null ? List.of() : List.copyOf(b.provisions);
    }

    public ModuleId id() {
        return id;
    }

    public String displayName() {
        return displayName;
    }

    public ServiceMode serviceMode() {
        return serviceMode;
    }

    public int startLevel() {
        return startLevel;
    }

    public List<Requirement> requirements() {
        return Collections.unmodifiableList(requirements);
    }

    public List<Provision> provisions() {
        return Collections.unmodifiableList(provisions);
    }

    public static Builder builder(ModuleId id) {
        return new Builder(id);
    }

    public static final class Builder {
        private final ModuleId id;
        private String displayName;
        private ServiceMode serviceMode;
        private Integer startLevel;
        private List<Requirement> requirements;
        private List<Provision> provisions;

        public Builder(ModuleId id) {
            this.id = Objects.requireNonNull(id, "id");
        }

        public Builder displayName(String name) {
            this.displayName = name;
            return this;
        }

        public Builder serviceMode(ServiceMode mode) {
            this.serviceMode = mode;
            return this;
        }

        public Builder startLevel(int startLevel) {
            this.startLevel = startLevel;
            return this;
        }

        public Builder requirements(List<Requirement> requirements) {
            this.requirements = requirements;
            return this;
        }

        public Builder provisions(List<Provision> provisions) {
            this.provisions = provisions;
            return this;
        }

        public ModuleDescriptor build() {
            return new ModuleDescriptor(this);
        }
    }
}
