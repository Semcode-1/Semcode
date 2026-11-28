package com.napier.sem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;

/** Stores actual application output for downloads and assessment evidence. */
public final class ReportExporter {
    private final Path directory;
    private final StringBuilder index = new StringBuilder("# Population report output\n\n");
    public ReportExporter(Path directory) throws IOException {
        this.directory = directory;
        Files.createDirectories(directory);
    }
    public void save(ReportRequest request, String text) throws IOException {
        String name = String.format("report-%02d.txt", request.id());
        Files.writeString(directory.resolve(name), text, StandardCharsets.UTF_8);
        index.append("- [Report ").append(request.id()).append("](").append(name).append(")\n");
        Files.writeString(directory.resolve("index.md"), index.toString(), StandardCharsets.UTF_8);
    }
}
