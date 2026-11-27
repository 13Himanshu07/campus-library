package com.library.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LogFieldsTest {
    @Test void quotedValuesEscapeCharactersThatCouldForgeLogFields() {
        assertEquals("\"path\\\"\\nstatus=200\"", LogFields.quoted("path\"\nstatus=200"));
        assertEquals("\"\"", LogFields.quoted(null));
    }
}
