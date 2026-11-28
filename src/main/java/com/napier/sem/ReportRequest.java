package com.napier.sem;

/** Validated inputs; filters remain values and never become SQL identifiers. */
public record ReportRequest(int id, int limit, String filter, String country) {
    public ReportRequest {
        var definition = ReportCatalogue.get(id);
        if (limit < 1) throw new IllegalArgumentException("N must be a positive integer");
        filter = filter == null ? "" : filter.trim();
        country = country == null ? "" : country.trim();
        if (!definition.filterType().isEmpty() && filter.isEmpty()) {
            throw new IllegalArgumentException("Report " + id + " requires --filter " + definition.filterType());
        }
        if (!country.isEmpty() && !definition.filterType().equals("district")
                && !definition.filterType().equals("city")) {
            throw new IllegalArgumentException("--country only applies to district or city filters");
        }
    }
}
