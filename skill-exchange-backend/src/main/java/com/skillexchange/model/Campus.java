package com.skillexchange.model;

/**
 * The four participating institutions in the pilot cohort.
 * Order here defines the fixed rotation sequence used by CalendarManager —
 * each campus hosts exactly once every 4 weeks.
 */
public enum Campus {
    NORTHRIDGE("Northridge College"),
    LAKESIDE("Lakeside University"),
    HARBORVIEW("Harborview College"),
    SUMMIT("Summit Institute");

    private final String displayName;

    Campus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
