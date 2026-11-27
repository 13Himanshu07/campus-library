package com.library.util;

/** Escapes untrusted text for quoted key-value fields in operational logs. */
public final class LogFields {
    private LogFields() { }

    public static String quoted(String value) {
        if (value == null) return "\"\"";
        return "\"" + value.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\r", "\\r")
            .replace("\n", "\\n")
            .replace("\t", "\\t") + "\"";
    }
}
