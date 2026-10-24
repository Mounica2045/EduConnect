package com.educonnect.enums;

/**
 * Educational Resource Categories for EduConnect.
 * Provides slug mapping and labels aligned with Member 1's frontend.
 */
public enum ResourceCategory {
    NOTES("notes", "Subject Notes", "badge-notes"),
    QP("qp", "Previous Question Papers", "badge-qp"),
    LAB("lab", "Lab Manuals", "badge-lab"),
    PROG("prog", "Programming Resources", "badge-prog"),
    TUTORIALS("tutorials", "Tutorials and Reference Materials", "badge-tutorial"),
    OTHER("other", "Other Study Materials", "badge-other");

    private final String slug;
    private final String label;
    private final String badgeClass;

    ResourceCategory(String slug, String label, String badgeClass) {
        this.slug = slug;
        this.label = label;
        this.badgeClass = badgeClass;
    }

    public String getSlug() {
        return slug;
    }

    public String getLabel() {
        return label;
    }

    public String getBadgeClass() {
        return badgeClass;
    }

    /**
     * Resolves category from case-insensitive slug or enum name.
     */
    public static ResourceCategory fromString(String text) {
        if (text == null || text.trim().isEmpty()) {
            return OTHER;
        }
        String clean = text.trim().toLowerCase();
        for (ResourceCategory cat : values()) {
            if (cat.slug.equalsIgnoreCase(clean) || cat.name().equalsIgnoreCase(clean) || cat.label.equalsIgnoreCase(clean)) {
                return cat;
            }
        }
        return OTHER;
    }
}
