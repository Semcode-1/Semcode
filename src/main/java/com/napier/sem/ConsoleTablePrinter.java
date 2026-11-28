package com.napier.sem;

import java.util.List;

public class ConsoleTablePrinter {

    public static void print(List<String> headers,
                             List<List<String>> rows) {

        if (headers == null || headers.isEmpty()) {
            throw new IllegalArgumentException("Table headers cannot be empty.");
        }

        int columns = headers.size();
        int[] widths = new int[columns];

        for (int i = 0; i < columns; i++) {
            widths[i] = headers.get(i).length();
        }

        for (List<String> row : rows) {
            if (row.size() != columns) {
                throw new IllegalArgumentException(
                    "Every row must have the same number of columns."
                );
            }

            for (int i = 0; i < columns; i++) {
                String value = row.get(i) == null ? "" : row.get(i);
                widths[i] = Math.max(widths[i], value.length());
            }
        }

        printSeparator(widths);
        printRow(headers, widths);
        printSeparator(widths);

        for (List<String> row : rows) {
            printRow(row, widths);
        }

        printSeparator(widths);
    }

    private static void printRow(List<String> values, int[] widths) {
        for (int i = 0; i < widths.length; i++) {
            String value = values.get(i) == null ? "" : values.get(i);
            System.out.printf("| %-" + widths[i] + "s ", value);
        }
        System.out.println("|");
    }

    private static void printSeparator(int[] widths) {
        for (int width : widths) {
            System.out.print("+-");
            System.out.print("-".repeat(width));
            System.out.print("-");
        }
        System.out.println("+");
    }
}
