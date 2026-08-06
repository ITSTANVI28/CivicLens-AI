package com.example.civiclensai.models;

public enum IssueCategory {
    POTHOLE("Pothole & Road Hazard", "🕳️", "Public Works Dept"),
    GARBAGE("Garbage & Waste", "🧹", "Sanitation Dept"),
    WATER_LEAK("Water Leak & Drainage", "💧", "Water Supply Board"),
    STREETLIGHT("Broken Streetlight", "💡", "Electrical Department"),
    MANHOLE("Open Manhole Hazard", "⚠️", "Infrastructure Dept"),
    OTHER("General Civic Issue", "🏛️", "Municipal Administration");

    private final String displayName;
    private final String emoji;
    private final String defaultDepartment;

    IssueCategory(String displayName, String emoji, String defaultDepartment) {
        this.displayName = displayName;
        this.emoji = emoji;
        this.defaultDepartment = defaultDepartment;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmoji() {
        return emoji;
    }

    public String getDefaultDepartment() {
        return defaultDepartment;
    }

    public static IssueCategory fromString(String text) {
        if (text == null) return OTHER;
        for (IssueCategory c : values()) {
            if (c.name().equalsIgnoreCase(text) || c.displayName.equalsIgnoreCase(text)) {
                return c;
            }
        }
        return OTHER;
    }
}
