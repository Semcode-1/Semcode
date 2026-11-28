package com.napier.sem;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DataModelTest {

    @Test
    void testCountryModel() {
        Country country = new Country(
            "ATG", "Antigua and Barbuda", "North America",
            "Caribbean", 100000
        );

        assertEquals("ATG", country.getCode());
        assertEquals("Antigua and Barbuda", country.getName());
        assertEquals(100000, country.getPopulation());
    }

    @Test
    void testCityModel() {
        City city = new City(1, "St John's", "ATG", "Saint John", 22000);

        assertEquals(1, city.getId());
        assertEquals("St John's", city.getName());
        assertEquals("ATG", city.getCountryCode());
        assertEquals(22000, city.getPopulation());
    }

    @Test
    void testLanguageModel() {
        Language language = new Language(
            "ATG", "English", true, new BigDecimal("95.7")
        );

        assertEquals("English", language.getLanguage());
        assertTrue(language.isOfficial());
        assertEquals(0, new BigDecimal("95.7").compareTo(
            language.getPercentage()
        ));
    }

    @Test
    void testPopulationReport() {
        PopulationReport report = new PopulationReport("Population Report");

        report.addHeader("Country");
        report.addHeader("Population");
        report.addRow(List.of("Antigua and Barbuda", "100000"));

        assertEquals("Population Report", report.getTitle());
        assertEquals(2, report.getHeaders().size());
        assertEquals(1, report.getRows().size());

        assertThrows(IllegalArgumentException.class,
            () -> report.addRow(List.of("Invalid")));
    }

    @Test
    void testConsoleTablePrinter() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream original = System.out;

        try {
            System.setOut(new PrintStream(output));

            ConsoleTablePrinter.print(
                List.of("Country", "Population"),
                List.of(List.of("Antigua", "100000"))
            );
        } finally {
            System.setOut(original);
        }

        assertTrue(output.toString().contains("Country"));
        assertTrue(output.toString().contains("Antigua"));
        assertTrue(output.toString().contains("100000"));
    }
}
