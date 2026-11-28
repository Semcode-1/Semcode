package com.napier.sem;

import java.io.PrintStream;
import java.util.List;

/** Tabular output keeps all required columns, including empty-result headings. */
public record ReportResult(List<String> columns, List<List<String>> rows) {
    public void print(PrintStream out) {
        out.println(String.join(" | ", columns));
        if (rows.isEmpty()) out.println("No matching records.");
        for (var row : rows) out.println(String.join(" | ", row));
    }
}
