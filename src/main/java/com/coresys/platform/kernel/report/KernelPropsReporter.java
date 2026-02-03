package com.coresys.platform.kernel.report;

import com.coresys.platform.kernel.props.KernelProps;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Формирователь отчётов: KernelPropsReporter.
 *
 * Собирает диагностическую информацию и предоставляет удобное текстовое представление.
 *
 * @author Евгений Платонов
 */

public final class KernelPropsReporter {

    public KernelPropsReport build(KernelProps props) {
        Objects.requireNonNull(props, "props");

        Map<String, String> filtered = new LinkedHashMap<>();
        for (var e : props.values().entrySet()) {
            String k = e.getKey();
            if (k == null) continue;
            if (k.startsWith("coresys.kernel.") || k.startsWith("debug.")) {
                filtered.put(k, e.getValue());
            }
        }

        return new KernelPropsReport(Instant.now(), filtered, props.sources());
    }

    public String format(KernelPropsReport report) {
        Objects.requireNonNull(report, "report");

        StringBuilder sb = new StringBuilder(1024);
        sb.append("Kernel props (loadedAt=").append(report.loadedAt()).append(")").append('\n');

        for (var e : report.values().entrySet()) {
            String key = e.getKey();
            String val = e.getValue();
            var src = report.sources().getOrDefault(key, null);
            sb.append(" - ").append(key).append('=').append(val);
            if (src != null) {
                sb.append(" [").append(src).append(']');
            }
            sb.append('\n');
        }

        return sb.toString();
    }
}
