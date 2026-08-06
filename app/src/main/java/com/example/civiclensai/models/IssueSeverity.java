package com.example.civiclensai.models;

public enum IssueSeverity {
    CRITICAL("CRITICAL", "Emergency Hazard (24h SLA)", "#D32F2F"),
    HIGH("HIGH", "High Severity (72h SLA)", "#F57C00"),
    MEDIUM("MEDIUM", "Moderate Severity (7 Days SLA)", "#FBC02D"),
    LOW("LOW", "Minor Issue (14 Days SLA)", "#388E3C");

    private final String label;
    private final String slaDescription;
    private final String hexColor;

    IssueSeverity(String label, String slaDescription, String hexColor) {
        this.label = label;
        this.slaDescription = slaDescription;
        this.hexColor = hexColor;
    }

    public String getLabel() {
        return label;
    }

    public String getSlaDescription() {
        return slaDescription;
    }

    public String getHexColor() {
        return hexColor;
    }

    public static IssueSeverity fromString(String text) {
        if (text == null) return MEDIUM;
        for (IssueSeverity s : values()) {
            if (s.name().equalsIgnoreCase(text) || s.label.equalsIgnoreCase(text)) {
                return s;
            }
        }
        return MEDIUM;
    }
}
