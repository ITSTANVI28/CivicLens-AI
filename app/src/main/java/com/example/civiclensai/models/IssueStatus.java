package com.example.civiclensai.models;

public enum IssueStatus {
    REPORTED("Reported", "#1976D2"),
    IN_PROGRESS("Work in Progress", "#F57C00"),
    RESOLVED("Resolved", "#388E3C");

    private final String label;
    private final String hexColor;

    IssueStatus(String label, String hexColor) {
        this.label = label;
        this.hexColor = hexColor;
    }

    public String getLabel() {
        return label;
    }

    public String getHexColor() {
        return hexColor;
    }

    public static IssueStatus fromString(String text) {
        if (text == null) return REPORTED;
        for (IssueStatus s : values()) {
            if (s.name().equalsIgnoreCase(text) || s.label.equalsIgnoreCase(text)) {
                return s;
            }
        }
        return REPORTED;
    }
}
