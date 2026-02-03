/**
 * CoreSys Platform Kernel
 * Copyright (c) 2026 Evgeniy Platonov
 * Licensed under the Apache License, Version 2.0
 * https://www.apache.org/licenses/LICENSE-2.0
 */

package com.coresys.platform.kernel.modules;

import java.util.Objects;

/**
 * Контракт: Requirement.
 *
 * Определяет публичный API компонента.
 *
 * @author Евгений Платонов
 */

public interface Requirement {

    final class ModuleRequirement implements Requirement {
        private final ModuleId moduleId;

        public ModuleRequirement(ModuleId moduleId) {
            this.moduleId = Objects.requireNonNull(moduleId, "moduleId");
        }

        public ModuleId moduleId() {
            return moduleId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ModuleRequirement)) return false;
            ModuleRequirement that = (ModuleRequirement) o;
            return moduleId.equals(that.moduleId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(moduleId);
        }

        @Override
        public String toString() {
            return "ModuleRequirement{moduleId=" + moduleId + "}";
        }
    }

    final class ServiceRequirement implements Requirement {
        private final Class<?> serviceType;
        private final boolean optional;

        public ServiceRequirement(Class<?> serviceType, boolean optional) {
            this.serviceType = Objects.requireNonNull(serviceType, "serviceType");
            this.optional = optional;
        }

        public Class<?> serviceType() {
            return serviceType;
        }

        public boolean optional() {
            return optional;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ServiceRequirement)) return false;
            ServiceRequirement that = (ServiceRequirement) o;
            return optional == that.optional && serviceType.equals(that.serviceType);
        }

        @Override
        public int hashCode() {
            return Objects.hash(serviceType, optional);
        }

        @Override
        public String toString() {
            return "ServiceRequirement{serviceType=" + serviceType.getName() + ", optional=" + optional + "}";
        }
    }
}
