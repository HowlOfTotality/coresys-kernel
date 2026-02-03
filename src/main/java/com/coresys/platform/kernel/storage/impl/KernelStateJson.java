/**
 * CoreSys Platform Kernel
 * Copyright (c) 2026 Evgeniy Platonov
 * Licensed under the Apache License, Version 2.0
 * https://www.apache.org/licenses/LICENSE-2.0
 */

package com.coresys.platform.kernel.storage.impl;

import com.coresys.platform.kernel.storage.KernelState;

import java.time.Instant;
import java.util.*;

/**
 * KernelStateJson.
 *
 * Инфраструктурный компонент ядра CoreSys.
 *
 * @author Евгений Платонов
 */

final class KernelStateJson {

    private KernelStateJson() {
    }

    static String toJson(KernelState st) {
        StringBuilder sb = new StringBuilder(512);
        sb.append('{');

        appendField(sb, "updatedAt", st.getUpdatedAt() == null ? null : st.getUpdatedAt().toString());
        sb.append(',');
        appendField(sb, "currentLevel", st.getCurrentLevel());
        sb.append(',');
        appendField(sb, "targetLevel", st.getTargetLevel());
        sb.append(',');

        sb.append("\"lastStartOrder\":");
        appendStringArray(sb, st.getLastStartOrder());
        sb.append(',');

        sb.append("\"lastFailureChain\":");
        appendStringArray(sb, st.getLastFailureChain());
        sb.append(',');

        sb.append("\"moduleStates\":");
        appendStringMap(sb, st.getModuleStates());

        sb.append('}');
        return sb.toString();
    }

    static KernelState fromJson(String json) {
        KernelState st = new KernelState();
        if (json == null) return st;

        String s = json.trim();
        if (s.isEmpty()) return st;

        st.setUpdatedAt(parseInstant(extractString(s, "updatedAt")));
        st.setCurrentLevel(extractInt(s, "currentLevel", 0));
        st.setTargetLevel(extractInt(s, "targetLevel", 0));
        st.setLastStartOrder(extractStringArray(s, "lastStartOrder"));
        st.setLastFailureChain(extractStringArray(s, "lastFailureChain"));
        st.setModuleStates(extractStringMap(s, "moduleStates"));

        return st;
    }

    private static void appendField(StringBuilder sb, String key, String value) {
        sb.append('"').append(escape(key)).append("\":");
        if (value == null) {
            sb.append("null");
        } else {
            sb.append('"').append(escape(value)).append('"');
        }
    }

    private static void appendField(StringBuilder sb, String key, int value) {
        sb.append('"').append(escape(key)).append("\":").append(value);
    }

    private static void appendStringArray(StringBuilder sb, List<String> arr) {
        sb.append('[');
        if (arr != null) {
            boolean first = true;
            for (String v : arr) {
                if (!first) sb.append(',');
                first = false;
                sb.append('"').append(escape(v == null ? "" : v)).append('"');
            }
        }
        sb.append(']');
    }

    private static void appendStringMap(StringBuilder sb, Map<String, String> map) {
        sb.append('{');
        if (map != null) {
            boolean first = true;
            for (Map.Entry<String, String> e : map.entrySet()) {
                if (!first) sb.append(',');
                first = false;
                sb.append('"').append(escape(e.getKey())).append("\":");
                sb.append('"').append(escape(e.getValue())).append('"');
            }
        }
        sb.append('}');
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private static String extractString(String json, String key) {
        String marker = "\"" + key + "\":";
        int i = json.indexOf(marker);
        if (i < 0) return null;
        int p = i + marker.length();
        while (p < json.length() && Character.isWhitespace(json.charAt(p))) p++;
        if (p >= json.length()) return null;
        if (json.startsWith("null", p)) return null;
        if (json.charAt(p) != '"') return null;
        int start = p + 1;
        int end = start;
        boolean esc = false;
        StringBuilder out = new StringBuilder();
        while (end < json.length()) {
            char c = json.charAt(end++);
            if (esc) {
                switch (c) {
                    case 'n': out.append('\n'); break;
                    case 'r': out.append('\r'); break;
                    case 't': out.append('\t'); break;
                    case '"': out.append('"'); break;
                    case '\\': out.append('\\'); break;
                    default: out.append(c); break;
                }
                esc = false;
                continue;
            }
            if (c == '\\') {
                esc = true;
                continue;
            }
            if (c == '"') break;
            out.append(c);
        }
        return out.toString();
    }

    private static int extractInt(String json, String key, int def) {
        String marker = "\"" + key + "\":";
        int i = json.indexOf(marker);
        if (i < 0) return def;
        int p = i + marker.length();
        while (p < json.length() && Character.isWhitespace(json.charAt(p))) p++;
        int start = p;
        while (p < json.length() && (Character.isDigit(json.charAt(p)) || json.charAt(p) == '-')) p++;
        try {
            return Integer.parseInt(json.substring(start, p));
        } catch (Exception e) {
            return def;
        }
    }

    private static List<String> extractStringArray(String json, String key) {
        String marker = "\"" + key + "\":";
        int i = json.indexOf(marker);
        if (i < 0) return List.of();
        int p = json.indexOf('[', i + marker.length());
        if (p < 0) return List.of();
        int end = findMatching(json, p, '[', ']');
        if (end < 0) return List.of();
        String body = json.substring(p + 1, end).trim();
        if (body.isEmpty()) return List.of();
        List<String> out = new ArrayList<>();
        int idx = 0;
        while (idx < body.length()) {
            while (idx < body.length() && (body.charAt(idx) == ',' || Character.isWhitespace(body.charAt(idx)))) idx++;
            if (idx >= body.length()) break;
            if (body.charAt(idx) != '"') break;
            int j = idx + 1;
            StringBuilder val = new StringBuilder();
            boolean esc = false;
            while (j < body.length()) {
                char c = body.charAt(j++);
                if (esc) {
                    switch (c) {
                        case 'n': val.append('\n'); break;
                        case 'r': val.append('\r'); break;
                        case 't': val.append('\t'); break;
                        case '"': val.append('"'); break;
                        case '\\': val.append('\\'); break;
                        default: val.append(c); break;
                    }
                    esc = false;
                    continue;
                }
                if (c == '\\') {
                    esc = true;
                    continue;
                }
                if (c == '"') break;
                val.append(c);
            }
            out.add(val.toString());
            idx = j;
        }
        return out;
    }

    private static Map<String, String> extractStringMap(String json, String key) {
        String marker = "\"" + key + "\":";
        int i = json.indexOf(marker);
        if (i < 0) return Map.of();
        int p = json.indexOf('{', i + marker.length());
        if (p < 0) return Map.of();
        int end = findMatching(json, p, '{', '}');
        if (end < 0) return Map.of();
        String body = json.substring(p + 1, end).trim();
        if (body.isEmpty()) return Map.of();

        Map<String, String> out = new LinkedHashMap<>();
        int idx = 0;
        while (idx < body.length()) {
            while (idx < body.length() && (body.charAt(idx) == ',' || Character.isWhitespace(body.charAt(idx)))) idx++;
            if (idx >= body.length()) break;
            if (body.charAt(idx) != '"') break;
            int keyEnd = body.indexOf('"', idx + 1);
            while (keyEnd > 0 && body.charAt(keyEnd - 1) == '\\') keyEnd = body.indexOf('"', keyEnd + 1);
            if (keyEnd < 0) break;
            String k = unescape(body.substring(idx + 1, keyEnd));
            int colon = body.indexOf(':', keyEnd + 1);
            if (colon < 0) break;
            int vStart = colon + 1;
            while (vStart < body.length() && Character.isWhitespace(body.charAt(vStart))) vStart++;
            if (vStart >= body.length() || body.charAt(vStart) != '"') break;
            int vEnd = body.indexOf('"', vStart + 1);
            while (vEnd > 0 && body.charAt(vEnd - 1) == '\\') vEnd = body.indexOf('"', vEnd + 1);
            if (vEnd < 0) break;
            String v = unescape(body.substring(vStart + 1, vEnd));
            out.put(k, v);
            idx = vEnd + 1;
        }

        return out;
    }

    private static int findMatching(String s, int start, char open, char close) {
        int depth = 0;
        boolean inString = false;
        boolean esc = false;
        for (int i = start; i < s.length(); i++) {
            char c = s.charAt(i);
            if (inString) {
                if (esc) {
                    esc = false;
                } else if (c == '\\') {
                    esc = true;
                } else if (c == '"') {
                    inString = false;
                }
                continue;
            }
            if (c == '"') {
                inString = true;
                continue;
            }
            if (c == open) depth++;
            else if (c == close) {
                depth--;
                if (depth == 0) return i;
            }
        }
        return -1;
    }

    private static String unescape(String s) {
        if (s == null) return "";
        StringBuilder out = new StringBuilder(s.length());
        boolean esc = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (esc) {
                switch (c) {
                    case 'n': out.append('\n'); break;
                    case 'r': out.append('\r'); break;
                    case 't': out.append('\t'); break;
                    case '"': out.append('"'); break;
                    case '\\': out.append('\\'); break;
                    default: out.append(c); break;
                }
                esc = false;
            } else if (c == '\\') {
                esc = true;
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }

    private static Instant parseInstant(String s) {
        try {
            return s == null ? Instant.now() : Instant.parse(s);
        } catch (Exception e) {
            return Instant.now();
        }
    }
}
