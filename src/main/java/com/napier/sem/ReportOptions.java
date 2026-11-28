package com.napier.sem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.nio.file.Path;

/** Small command-line selector shared by local Compose and GitHub Actions. */
public record ReportOptions(List<ReportRequest> requests, Path output, boolean help) {
    public static ReportOptions parse(String[] args) {
        if (args.length == 1 && args[0].equals("--help")) return new ReportOptions(List.of(), null, true);
        Map<String, String> values = new HashMap<>();
        for (int i = 0; i < args.length; i += 2) {
            if (!List.of("--report", "--limit", "--filter", "--country", "--output").contains(args[i])) {
                throw new IllegalArgumentException("Unknown option: " + args[i] + ". Use --help.");
            }
            if (i + 1 >= args.length) throw new IllegalArgumentException("Missing value for " + args[i]);
            if (values.putIfAbsent(args[i], args[i + 1]) != null) {
                throw new IllegalArgumentException("Duplicate option: " + args[i]);
            }
        }
        int limit = positive(values.getOrDefault("--limit", "10"));
        String selected = values.getOrDefault("--report", "4");
        String filter = values.getOrDefault("--filter", "");
        String country = values.getOrDefault("--country", "");
        Path output = values.containsKey("--output") ? Path.of(values.get("--output")) : null;
        if (selected.equals("all")) {
            if (!filter.isBlank() || !country.isBlank()) {
                throw new IllegalArgumentException("Report all uses documented example filters; leave filter and country blank");
            }
            return new ReportOptions(ReportCatalogue.REPORTS.stream().map(d -> {
                var example = ReportCatalogue.example(d.id());
                return new ReportRequest(d.id(), limit, example.filter(), example.country());
            }).toList(), output, false);
        }
        try {
            return new ReportOptions(List.of(new ReportRequest(Integer.parseInt(selected), limit, filter, country)), output, false);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Report must be between 1 and 32, or all");
        }
    }

    static int positive(String value) {
        try {
            int number = Integer.parseInt(value);
            if (number > 0) return number;
        } catch (NumberFormatException ignored) { }
        throw new IllegalArgumentException("N must be a positive integer");
    }
}
