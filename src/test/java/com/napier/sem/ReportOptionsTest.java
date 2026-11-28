package com.napier.sem;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class ReportOptionsTest {
    @Test void catalogueHasAll32AndExamplesValidate() {
        assertEquals(32, ReportCatalogue.REPORTS.size());
        for (int id = 1; id <= 32; id++) assertEquals(id, ReportCatalogue.example(id).id());
    }
    @Test void rejectsMissingInvalidAndDuplicateInputs() {
        for (String id : List.of("0", "33", "wrong")) {
            assertThrows(IllegalArgumentException.class, () -> ReportOptions.parse(new String[]{"--report", id}));
        }
        for (String n : List.of("0", "-1", "x", "2147483648")) {
            assertThrows(IllegalArgumentException.class, () -> ReportOptions.parse(new String[]{"--limit", n}));
        }
        assertThrows(IllegalArgumentException.class, () -> ReportOptions.parse(new String[]{"--report", "5"}));
        assertThrows(IllegalArgumentException.class, () -> ReportOptions.parse(new String[]{"--report"}));
        assertThrows(IllegalArgumentException.class, () -> ReportOptions.parse(new String[]{"--bad", "x"}));
        assertThrows(IllegalArgumentException.class, () -> ReportOptions.parse(new String[]{"--limit", "5", "--limit", "6"}));
        assertThrows(IllegalArgumentException.class, () -> ReportOptions.parse(new String[]{"--report", "all", "--filter", "Africa"}));
        assertThrows(IllegalArgumentException.class, () -> new ReportRequest(4, 5, "", "NGA"));
    }
    @Test void allUsesExamplesWithChosenLimitAndKeepsParametersSeparate() {
        var options = ReportOptions.parse(new String[]{"--report", "all", "--limit", "3"});
        assertEquals(32, options.requests().size());
        assertTrue(options.requests().stream().allMatch(r -> r.limit() == 3));
        var africa = ReportOptions.parse(new String[]{"--report", "5", "--filter", "Africa", "--limit", "10"});
        assertEquals(new ReportRequest(5, 10, "Africa", ""), africa.requests().get(0));
    }
    @Test void helpNeedsNoDatabaseAndEmptyResultsKeepColumns() {
        var output = new ByteArrayOutputStream();
        assertEquals(0, App.run(new String[]{"--help"}, Map.of(), new PrintStream(output), System.err));
        assertTrue(output.toString().contains("32:"));
        output.reset();
        new ReportResult(List.of("Name", "Population"), List.of()).print(new PrintStream(output));
        assertTrue(output.toString().contains("Name | Population"));
        assertTrue(output.toString().contains("No matching records."));
    }
}
