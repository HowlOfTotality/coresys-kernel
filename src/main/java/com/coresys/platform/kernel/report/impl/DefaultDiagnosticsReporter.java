/**
 * CoreSys Platform Kernel
 * Copyright (c) 2026 Evgeniy Platonov
 * Licensed under the Apache License, Version 2.0
 * https://www.apache.org/licenses/LICENSE-2.0
 */

package com.coresys.platform.kernel.report.impl;

import com.coresys.platform.kernel.modules.Module;
import com.coresys.platform.kernel.modules.ModuleDescriptor;
import com.coresys.platform.kernel.modules.ModuleId;
import com.coresys.platform.kernel.modules.ModuleRegistry;
import com.coresys.platform.kernel.modules.Provision;
import com.coresys.platform.kernel.modules.Requirement;
import com.coresys.platform.kernel.report.DiagnosticsReport;
import com.coresys.platform.kernel.report.DiagnosticsReporter;

import java.util.*;

/**
 * Формирователь отчётов: DefaultDiagnosticsReporter.
 *
 * Собирает диагностическую информацию и предоставляет удобное текстовое представление.
 *
 * @author Евгений Платонов
 */

public final class DefaultDiagnosticsReporter implements DiagnosticsReporter {

    @Override
    public DiagnosticsReport buildReport(ModuleRegistry registry) {
        Objects.requireNonNull(registry, "registry");

        Map<Class<?>, List<ModuleId>> providersByService = new HashMap<>();
        Map<Class<?>, Boolean> exclusiveByService = new HashMap<>();
        for (Module m : registry.all()) {
            ModuleDescriptor d = m.descriptor();
            for (Provision p : d.provisions()) {
                providersByService.computeIfAbsent(p.serviceType(), k -> new ArrayList<>()).add(d.id());
                if (p.exclusive()) {
                    exclusiveByService.put(p.serviceType(), true);
                }
            }
        }

        Map<ModuleId, List<ModuleId>> graph = new LinkedHashMap<>();
        List<String> missing = new ArrayList<>();
        List<String> conflicts = new ArrayList<>();

        for (Module m : registry.all()) {
            ModuleDescriptor d = m.descriptor();
            LinkedHashSet<ModuleId> deps = new LinkedHashSet<>();

            for (Requirement r : d.requirements()) {
                if (r instanceof Requirement.ModuleRequirement) {
                    Requirement.ModuleRequirement mr = (Requirement.ModuleRequirement) r;
                    deps.add(mr.moduleId());

                    Module dep = registry.find(mr.moduleId());
                    if (dep == null) {
                        missing.add(d.id() + " requires missing module " + mr.moduleId());
                    } else {
                        checkStartLevelConflict(d, dep.descriptor(), conflicts);
                    }
                    continue;
                }

                if (r instanceof Requirement.ServiceRequirement) {
                    Requirement.ServiceRequirement sr = (Requirement.ServiceRequirement) r;

                    List<ModuleId> providers = providersByService.getOrDefault(sr.serviceType(), List.of());
                    if (providers.isEmpty()) {
                        if (!sr.optional()) {
                            missing.add(d.id() + " requires missing service " + sr.serviceType().getName());
                        }
                        continue;
                    }

                    if (Boolean.TRUE.equals(exclusiveByService.get(sr.serviceType())) && providers.size() > 1) {
                        conflicts.add("Exclusive service conflict: " + sr.serviceType().getName()
                                + " has multiple providers " + providers);
                    }

                    List<ModuleId> sortedProviders = new ArrayList<>(providers);
                    sortedProviders.sort(providerComparator(registry));

                    for (ModuleId pid : sortedProviders) {
                        if (pid.equals(d.id())) {
                            continue;
                        }
                        deps.add(pid);

                        Module dep = registry.find(pid);
                        if (dep != null) {
                            checkStartLevelConflict(d, dep.descriptor(), conflicts);
                        }
                    }
                }
            }

            graph.put(d.id(), List.copyOf(deps));
        }

        List<ModuleId> order = topoSortByStartLevel(graph, registry, conflicts);

        return new DiagnosticsReport(graph, order, missing, conflicts);
    }

    @Override
    public String format(DiagnosticsReport report) {
        Objects.requireNonNull(report, "report");

        StringBuilder sb = new StringBuilder(2048);

        sb.append("Dependency graph:").append('\n');
        for (var e : report.dependencyGraph().entrySet()) {
            sb.append(" - ").append(e.getKey()).append(" -> ").append(e.getValue()).append('\n');
        }

        sb.append('\n').append("Start order:").append('\n');
        for (int i = 0; i < report.startOrder().size(); i++) {
            sb.append(" ").append(i + 1).append(") ").append(report.startOrder().get(i)).append('\n');
        }

        if (!report.missingRequirements().isEmpty()) {
            sb.append('\n').append("Missing requirements:").append('\n');
            for (String s : report.missingRequirements()) {
                sb.append(" - ").append(s).append('\n');
            }
        }

        if (!report.conflicts().isEmpty()) {
            sb.append('\n').append("Conflicts:").append('\n');
            for (String s : report.conflicts()) {
                sb.append(" - ").append(s).append('\n');
            }
        }

        return sb.toString();
    }

    private static void checkStartLevelConflict(ModuleDescriptor me, ModuleDescriptor dep, List<String> conflicts) {
        int depLevel = dep.startLevel();
        int myLevel = me.startLevel();
        if (depLevel > myLevel) {
            conflicts.add("StartLevel conflict: " + me.id() + "(level " + myLevel
                    + ") requires " + dep.id() + "(level " + depLevel + ")");
        }
    }

    private static Comparator<ModuleId> providerComparator(ModuleRegistry registry) {
        return (a, b) -> {
            int la = 1;
            int lb = 1;
            Module ma = registry.find(a);
            Module mb = registry.find(b);
            if (ma != null) la = ma.descriptor().startLevel();
            if (mb != null) lb = mb.descriptor().startLevel();
            int c = Integer.compare(la, lb);
            if (c != 0) return c;
            return a.value().compareTo(b.value());
        };
    }

    private static List<ModuleId> topoSortByStartLevel(
            Map<ModuleId, List<ModuleId>> graph,
            ModuleRegistry registry,
            List<String> conflicts
    ) {
        Map<ModuleId, Integer> indeg = new HashMap<>();
        Map<ModuleId, List<ModuleId>> rev = new HashMap<>();

        for (var e : graph.entrySet()) {
            indeg.putIfAbsent(e.getKey(), 0);
            for (ModuleId dep : e.getValue()) {
                indeg.putIfAbsent(dep, 0);
                indeg.put(e.getKey(), indeg.get(e.getKey()) + 1);
                rev.computeIfAbsent(dep, k -> new ArrayList<>()).add(e.getKey());
            }
        }

        Comparator<ModuleId> cmp = providerComparator(registry);

        PriorityQueue<ModuleId> q = new PriorityQueue<>(cmp);
        for (var e : indeg.entrySet()) {
            if (e.getValue() == 0) q.add(e.getKey());
        }

        List<ModuleId> order = new ArrayList<>();
        while (!q.isEmpty()) {
            ModuleId n = q.remove();
            if (graph.containsKey(n)) {
                order.add(n);
            }
            for (ModuleId out : rev.getOrDefault(n, List.of())) {
                int d = indeg.computeIfPresent(out, (k, v) -> v - 1);
                if (d == 0) q.add(out);
            }
        }

        if (order.size() != graph.size()) {
            List<ModuleId> remaining = new ArrayList<>();
            for (ModuleId id : graph.keySet()) {
                if (!order.contains(id)) remaining.add(id);
            }
            remaining.sort(cmp);

            conflicts.add("Dependency cycle detected (topoSort incomplete). Remaining=" + remaining);

            for (ModuleId id : remaining) {
                List<ModuleId> deps = graph.getOrDefault(id, List.of());
                List<ModuleId> depsInRemaining = new ArrayList<>();
                for (ModuleId dep : deps) {
                    if (remaining.contains(dep)) depsInRemaining.add(dep);
                }
                if (!depsInRemaining.isEmpty()) {
                    depsInRemaining.sort(cmp);
                    conflicts.add(" - " + id + " depends on " + depsInRemaining);
                }
            }

            List<ModuleId> fallback = new ArrayList<>(graph.keySet());
            fallback.sort(cmp);
            return fallback;
        }

        return order;
    }
}
