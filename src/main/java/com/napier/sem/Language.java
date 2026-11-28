package com.napier.sem;

import java.math.BigDecimal;

public class Language {
    private String countryCode;
    private String language;
    private boolean official;
    private BigDecimal percentage;

    public Language(String countryCode, String language,
                    boolean official, BigDecimal percentage) {
        this.countryCode = countryCode;
        this.language = language;
        this.official = official;
        this.percentage = percentage;
    }

    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public boolean isOfficial() { return official; }
    public void setOfficial(boolean official) { this.official = official; }

    public BigDecimal getPercentage() { return percentage; }
    public void setPercentage(BigDecimal percentage) {
        this.percentage = percentage;
    }
}
