package com.skillexchange.model;

/**
 * Sessions only run on weekends, so availability is modeled as a small
 * fixed set of slots rather than free-form time ranges.
 */
public enum TimeSlot {
    SAT_MORNING,
    SAT_AFTERNOON,
    SAT_EVENING,
    SUN_MORNING,
    SUN_AFTERNOON,
    SUN_EVENING
}
