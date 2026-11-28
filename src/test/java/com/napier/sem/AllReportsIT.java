package com.napier.sem;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import java.sql.*;
import java.util.*;
import java.math.*;
import java.nio.file.*;
import java.io.*;
import java.util.stream.*;
import static org.junit.jupiter.api.Assertions.*;

/** Independent oracle: read base tables once and calculate expected reports in Java. */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AllReportsIT {
    record Nation(String code, String name, String continent, String region, long population, int capital) { }
    record Town(int id, String name, String code, String district, long population) { }
    record Language(String code, String language, BigDecimal percentage) { }
    Connection connection;
    List<Nation> nations = new ArrayList<>();
    List<Town> towns = new ArrayList<>();
    List<Language> languages = new ArrayList<>();
    Map<String,Nation> byCode = new HashMap<>();
    Map<Integer,Town> byId = new HashMap<>();

    @BeforeAll void readFixture() throws Exception {
        connection = DriverManager.getConnection(System.getenv("DB_URL"), System.getenv("DB_USER"), System.getenv("DB_PASSWORD"));
        try (var s = connection.createStatement(); var r = s.executeQuery("SELECT * FROM country")) {
            while (r.next()) {
                var n = new Nation(r.getString("Code"), r.getString("Name"), r.getString("Continent"),
                        r.getString("Region"), r.getLong("Population"), r.getInt("Capital"));
                nations.add(n); byCode.put(n.code(), n);
            }
        }
        try (var s = connection.createStatement(); var r = s.executeQuery("SELECT * FROM city")) {
            while (r.next()) {
                var t = new Town(r.getInt("ID"), r.getString("Name"), r.getString("CountryCode"),
                        r.getString("District"), r.getLong("Population"));
                towns.add(t); byId.put(t.id(), t);
            }
        }
        try (var s = connection.createStatement(); var r = s.executeQuery("SELECT * FROM countrylanguage")) {
            while (r.next()) languages.add(new Language(r.getString("CountryCode"),r.getString("Language"),r.getBigDecimal("Percentage")));
        }
        assertEquals(239, nations.size()); assertEquals(4079, towns.size()); assertEquals(984, languages.size());
    }
    @AfterAll void close() throws Exception { if (connection != null) connection.close(); }

    @TestFactory Stream<DynamicTest> everyAssessmentReportMatchesIndependentCalculations() {
        return IntStream.rangeClosed(1,32).mapToObj(id -> DynamicTest.dynamicTest("Requirement " + id, () -> {
            var request = ReportCatalogue.example(id);
            var actual = new ReportRepository(connection).run(request);
            assertFalse(actual.rows().isEmpty());
            assertEquals(expected(request), actual.rows());
            if (id <= 6) assertEquals(List.of("Code","Name","Continent","Region","Population","Capital"), actual.columns());
            else if (id <=16) assertEquals(List.of("Name","Country","District","Population"), actual.columns());
            else if (id <=22) assertEquals(List.of("Name","Country","Population"), actual.columns());
            else if (id <=25) assertEquals(List.of("Name","TotalPopulation","CityPopulation","CityPercentage","NonCityPopulation","NonCityPercentage"), actual.columns());
            else if (id <=29) assertEquals(List.of("Population"), actual.columns());
            else if (id ==30) assertEquals(List.of("Country","District","Population"), actual.columns());
            else if (id ==31) assertEquals(List.of("ID","Name","Country","District","Population"), actual.columns());
            else assertEquals(List.of("Language","Speakers","WorldPercentage"), actual.columns());
        }));
    }

    @Test void filtersAreParametersAndNIsRespected() throws Exception {
        for (int id : List.of(2,3,5,6,8,9,10,11,13,14,15,16,18,19,21,22,27,28,29,30,31)) {
            String filter = switch(ReportCatalogue.get(id).filterType()) {
                case "continent" -> "Asia"; case "region" -> "Caribbean";
                case "country" -> "USA"; case "district" -> "California"; default -> "San Jose";
            };
            String country = List.of("city","district").contains(ReportCatalogue.get(id).filterType()) ? "USA" : "";
            var request = new ReportRequest(id,1,filter,country);
            assertEquals(expected(request), new ReportRepository(connection).run(request).rows(), "Alternate filter for " + id);
            assertTrue(new ReportRepository(connection).run(new ReportRequest(id,10,"' OR 1=1 --",country)).rows().isEmpty());
        }
        assertEquals(239, new ReportRepository(connection).run(new ReportRequest(4,Integer.MAX_VALUE,"","")).rows().size());
    }

    @Test void zeroPopulationsNullCapitalsAndLargeTotalsArePreserved() throws Exception {
        var repo = new ReportRepository(connection);
        assertTrue(repo.run(new ReportRequest(1,10,"","")).rows().stream().anyMatch(r -> r.get(5).equals("N/A")));
        var breakdown = repo.run(new ReportRequest(25,10,"",""));
        var zero = breakdown.rows().stream().filter(r -> r.get(1).equals("0")).toList();
        assertFalse(zero.isEmpty());
        assertTrue(zero.stream().allMatch(r -> r.get(3).equals("N/A") && r.get(5).equals("N/A")));
        long total = Long.parseLong(repo.run(new ReportRequest(26,10,"","")).rows().get(0).get(0));
        assertTrue(total > Integer.MAX_VALUE);
    }

    @Test void allReportsExportAndDatabaseFailureReturnsNonzero(@TempDir Path directory) throws Exception {
        var output = new ByteArrayOutputStream(); var errors = new ByteArrayOutputStream();
        assertEquals(0, App.run(new String[]{"--report","all","--output",directory.toString()},
                System.getenv(), new PrintStream(output), new PrintStream(errors)), errors.toString());
        for (int id=1;id<=32;id++) {
            String text = Files.readString(directory.resolve(String.format("report-%02d.txt",id)));
            assertTrue(text.startsWith("Report " + id + ":"));
            assertTrue(text.contains("Rows:"));
        }
        assertTrue(Files.exists(directory.resolve("index.md")));
        var bad = new HashMap<>(System.getenv()); bad.put("DB_PASSWORD","wrong-password");
        assertEquals(1, App.run(new String[]{"--report","26"},bad,new PrintStream(output),new PrintStream(errors)));
    }

    boolean nationMatches(Nation n, String type, String filter) {
        return switch(type) {
            case "continent" -> n.continent().equals(filter);
            case "region" -> n.region().equals(filter);
            case "country" -> n.code().equals(filter) || n.name().equals(filter);
            default -> true;
        };
    }
    boolean townMatches(Town t, ReportRequest r) {
        String type = ReportCatalogue.get(r.id()).filterType();
        return nationMatches(byCode.get(t.code()),type,r.filter())
                && (!type.equals("district") || t.district().equals(r.filter()))
                && (!type.equals("city") || t.name().equals(r.filter()))
                && (r.country().isEmpty() || nationMatches(byCode.get(t.code()),"country",r.country()));
    }
    String percent(BigDecimal part, BigDecimal total) {
        return total.signum()==0 ? "N/A" : part.multiply(BigDecimal.valueOf(100)).divide(total,2,RoundingMode.HALF_UP).toPlainString();
    }
    List<List<String>> expected(ReportRequest r) {
        int id = r.id(); String type=ReportCatalogue.get(id).filterType();
        if (id<=6) {
            var stream=nations.stream().filter(n -> nationMatches(n,type,r.filter()))
                    .sorted(Comparator.comparingLong(Nation::population).reversed().thenComparing(Nation::code));
            if(id>=4) stream=stream.limit(r.limit());
            return stream.map(n -> List.of(n.code(),n.name(),n.continent(),n.region(),""+n.population(),
                    byId.containsKey(n.capital()) ? byId.get(n.capital()).name() : "N/A")).toList();
        }
        if(id<=22) {
            var stream=towns.stream().filter(t -> townMatches(t,r));
            if(id>=17) stream=stream.filter(t -> byCode.get(t.code()).capital()==t.id());
            stream=stream.sorted(Comparator.comparingLong(Town::population).reversed().thenComparingInt(Town::id));
            if((id>=12 && id<=16)||id>=20) stream=stream.limit(r.limit());
            return stream.map(t -> id<=16 ? List.of(t.name(),byCode.get(t.code()).name(),t.district(),""+t.population())
                    : List.of(t.name(),byCode.get(t.code()).name(),""+t.population())).toList();
        }
        if(id<=25) {
            Map<String,long[]> sums=new HashMap<>();
            for(var n:nations) {
                String name=id==23?n.continent():id==24?n.region():n.name();
                long[] sum=sums.computeIfAbsent(name,k -> new long[2]);
                sum[0]+=n.population();
                sum[1]+=towns.stream().filter(t->t.code().equals(n.code())).mapToLong(Town::population).sum();
            }
            return sums.entrySet().stream().sorted(Comparator.<Map.Entry<String,long[]>>comparingLong(e->e.getValue()[0]).reversed().thenComparing(Map.Entry::getKey))
                    .map(e->{long[] v=e.getValue();return List.of(e.getKey(),""+v[0],""+v[1],
                            percent(BigDecimal.valueOf(v[1]),BigDecimal.valueOf(v[0])),""+(v[0]-v[1]),
                            percent(BigDecimal.valueOf(v[0]-v[1]),BigDecimal.valueOf(v[0])));}).toList();
        }
        if(id<=29) {
            var selected=nations.stream().filter(n->nationMatches(n,type,r.filter())).toList();
            return selected.isEmpty()?List.of():List.of(List.of(""+selected.stream().mapToLong(Nation::population).sum()));
        }
        if(id==30) {
            Map<String,Long> sums=new TreeMap<>();
            towns.stream().filter(t->townMatches(t,r)).forEach(t->sums.merge(byCode.get(t.code()).name(),t.population(),Long::sum));
            return sums.entrySet().stream().map(e->List.of(e.getKey(),r.filter(),""+e.getValue())).toList();
        }
        if(id==31) return towns.stream().filter(t->townMatches(t,r))
                .sorted(Comparator.comparing((Town t)->byCode.get(t.code()).name()).thenComparing(Town::district).thenComparingInt(Town::id))
                .map(t->List.of(""+t.id(),t.name(),byCode.get(t.code()).name(),t.district(),""+t.population())).toList();
        Map<String,BigDecimal> sums=new HashMap<>();
        for(var language:languages) if(List.of("Chinese","English","Hindi","Spanish","Arabic").contains(language.language())) {
            sums.merge(language.language(),BigDecimal.valueOf(byCode.get(language.code()).population())
                    .multiply(language.percentage()).divide(BigDecimal.valueOf(100)),BigDecimal::add);
        }
        BigDecimal total=BigDecimal.valueOf(nations.stream().mapToLong(Nation::population).sum());
        return sums.entrySet().stream().sorted(Map.Entry.<String,BigDecimal>comparingByValue().reversed())
                .map(e->List.of(e.getKey(),e.getValue().setScale(0,RoundingMode.HALF_UP).toPlainString(),percent(e.getValue(),total))).toList();
    }
}
