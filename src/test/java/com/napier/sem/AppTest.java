package com.napier.sem;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class AppTest {
    @Test
    void defaultsToFiveCountriesAndAcceptsCustomLimit() {
        assertEquals(5, App.parseLimit(new String[0]));
        assertEquals(10, App.parseLimit(new String[]{"10"}));
    }

    @Test
    void rejectsInvalidReportSizes() {
        for (String size : new String[]{"0", "-1", "abc", "2147483648"}) {
            assertThrows(IllegalArgumentException.class, () -> App.parseLimit(new String[]{size}));
        }
        assertThrows(IllegalArgumentException.class, () -> App.parseLimit(new String[]{"5", "world", "extra"}));
    }

    @Test
    void missingDatabaseSettingsFailBeforeConnecting() {
        var errors = new ByteArrayOutputStream();
        int result = App.run(new String[0], Map.of(), System.out, new PrintStream(errors));
        assertEquals(1, result);
        assertTrue(errors.toString().contains("DB_URL must be configured"));
    }

    @Test
    void invalidArgumentsProduceFailureExitCode() {
        var errors = new ByteArrayOutputStream();
        int result = App.run(new String[]{"0"}, Map.of(), System.out, new PrintStream(errors));
        assertEquals(1, result);
        assertTrue(errors.toString().contains("positive integer"));
    }

    @Test
    void helpListsAvailableReportsWithoutDatabaseSettings() {
        var output = new ByteArrayOutputStream();
        assertEquals(0, App.run(new String[]{"--help"}, Map.of(),
                new PrintStream(output), System.err));
        assertTrue(output.toString().contains("world - Top N countries worldwide"));
        assertTrue(output.toString().contains("africa - Top N countries in Africa"));
    }

    @Test
    void unsupportedReportFailsBeforeConnectingToDatabase() {
        var errors = new ByteArrayOutputStream();
        assertEquals(1, App.run(new String[]{"10", "unknown"}, Map.of(),
                System.out, new PrintStream(errors)));
        assertTrue(errors.toString().contains("Unknown report"));
        assertFalse(errors.toString().contains("DB_URL"));
    }
}
