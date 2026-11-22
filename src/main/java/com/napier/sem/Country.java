package com.napier.sem;

/** One country report row, including its capital city where available. */
public record Country(String code, String name, String continent, String region,
                      long population, String capital) {
}
