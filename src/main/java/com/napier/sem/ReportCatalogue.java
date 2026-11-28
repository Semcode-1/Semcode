package com.napier.sem;

import java.util.List;

/** Numbering follows the 32 requirements in the coursework specification. */
public final class ReportCatalogue {
    private ReportCatalogue() { }
    public record Definition(int id, String title, String filterType) { }
    public static final List<Definition> REPORTS = List.of(
            new Definition(1, "All countries in the world", ""),
            new Definition(2, "All countries in the continent", "continent"),
            new Definition(3, "All countries in the region", "region"),
            new Definition(4, "Top N countries in the world", ""),
            new Definition(5, "Top N countries in the continent", "continent"),
            new Definition(6, "Top N countries in the region", "region"),
            new Definition(7, "All cities in the world", ""),
            new Definition(8, "All cities in the continent", "continent"),
            new Definition(9, "All cities in the region", "region"),
            new Definition(10, "All cities in the country", "country"),
            new Definition(11, "All cities in the district", "district"),
            new Definition(12, "Top N cities in the world", ""),
            new Definition(13, "Top N cities in the continent", "continent"),
            new Definition(14, "Top N cities in the region", "region"),
            new Definition(15, "Top N cities in the country", "country"),
            new Definition(16, "Top N cities in the district", "district"),
            new Definition(17, "All capital cities in the world", ""),
            new Definition(18, "All capital cities in the continent", "continent"),
            new Definition(19, "All capital cities in the region", "region"),
            new Definition(20, "Top N capital cities in the world", ""),
            new Definition(21, "Top N capital cities in the continent", "continent"),
            new Definition(22, "Top N capital cities in the region", "region"),
            new Definition(23, "Population living in cities and outside cities by continent", ""),
            new Definition(24, "Population living in cities and outside cities by region", ""),
            new Definition(25, "Population living in cities and outside cities by country", ""),
            new Definition(26, "Population of the world", ""),
            new Definition(27, "Population of the continent", "continent"),
            new Definition(28, "Population of the region", "region"),
            new Definition(29, "Population of the country", "country"),
            new Definition(30, "Population of the district", "district"),
            new Definition(31, "Population of the city", "city"),
            new Definition(32, "Chinese, English, Hindi, Spanish and Arabic speakers", ""));

    public static Definition get(int id) {
        if (id < 1 || id > 32) throw new IllegalArgumentException("Report must be between 1 and 32, or all");
        return REPORTS.get(id - 1);
    }

    /** Reproducible demonstration inputs; --all is a demo, not every possible filter. */
    public static ReportRequest example(int id) {
        String filter = switch (get(id).filterType()) {
            case "continent" -> "Africa";
            case "region" -> "Eastern Africa";
            case "country" -> "Nigeria";
            case "district", "city" -> "Lagos";
            default -> "";
        };
        return new ReportRequest(id, 10, filter,
                get(id).filterType().equals("district") || get(id).filterType().equals("city") ? "NGA" : "");
    }
}
