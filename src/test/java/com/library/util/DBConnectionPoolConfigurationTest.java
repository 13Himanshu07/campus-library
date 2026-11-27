package com.library.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DBConnectionPoolConfigurationTest {
    @Test void acceptsAValidPoolConfigurationIncludingZeroMinimumIdle() {
        assertDoesNotThrow(() -> DBConnection.validatePoolConfiguration(12, 0, 30000, 600000, 1800000, "library"));
    }

    @Test void rejectsInvalidPoolSizesAndTimeoutsBeforeHikariStarts() {
        assertThrows(IllegalArgumentException.class, () -> DBConnection.validatePoolConfiguration(0, 0, 30000, 1, 1, "library"));
        assertThrows(IllegalArgumentException.class, () -> DBConnection.validatePoolConfiguration(4, 5, 30000, 1, 1, "library"));
        assertThrows(IllegalArgumentException.class, () -> DBConnection.validatePoolConfiguration(4, 2, 249, 1, 1, "library"));
        assertThrows(IllegalArgumentException.class, () -> DBConnection.validatePoolConfiguration(4, 2, 30000, -1, 1, "library"));
        assertThrows(IllegalArgumentException.class, () -> DBConnection.validatePoolConfiguration(4, 2, 30000, 1, -1, "library"));
        assertThrows(IllegalArgumentException.class, () -> DBConnection.validatePoolConfiguration(4, 2, 30000, 1, 1, "  "));
    }
}
