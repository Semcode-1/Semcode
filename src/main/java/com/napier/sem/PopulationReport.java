package com.napier.sem;

import java.util.ArrayList;
import java.util.List;

public class PopulationReport {
    private String title;
    private List<String> headers;
    private List<List<String>> rows;

    public PopulationReport(String title) {
        this.title = title;
        this.headers = new ArrayList<>();
        this.rows = new ArrayList<>();
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public List<String> getHeaders() { return headers; }
    public void setHeaders(List<String> headers) {
        this.headers = new ArrayList<>(headers);
    }

    public List<List<String>> getRows() { return rows; }
    public void setRows(List<List<String>> rows) {
        this.rows = new ArrayList<>(rows);
    }

    public void addHeader(String header) {
        headers.add(header);
    }

    public void addRow(List<String> row) {
        if (row.size() != headers.size()) {
            throw new IllegalArgumentException(
                "Row must contain the same number of values as headers."
            );
        }
        rows.add(new ArrayList<>(row));
    }
}
