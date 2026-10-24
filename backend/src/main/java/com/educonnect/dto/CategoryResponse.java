package com.educonnect.dto;

import com.educonnect.enums.ResourceCategory;

/**
 * Category metadata DTO for frontend category filters and navigation badges.
 */
public class CategoryResponse {

    private String id;
    private String label;
    private String badgeClass;
    private String name;

    public CategoryResponse() {
    }

    public CategoryResponse(String id, String label, String badgeClass, String name) {
        this.id = id;
        this.label = label;
        this.badgeClass = badgeClass;
        this.name = name;
    }

    public static CategoryResponse fromCategory(ResourceCategory category) {
        return new CategoryResponse(
                category.getSlug(),
                category.getLabel(),
                category.getBadgeClass(),
                category.name()
        );
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getBadgeClass() {
        return badgeClass;
    }

    public void setBadgeClass(String badgeClass) {
        this.badgeClass = badgeClass;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
