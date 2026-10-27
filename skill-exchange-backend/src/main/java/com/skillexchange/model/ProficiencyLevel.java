package com.skillexchange.model;

public enum ProficiencyLevel {
    BEGINNER(1),
    INTERMEDIATE(2),
    ADVANCED(3);

    private final int rank;

    ProficiencyLevel(int rank) {
        this.rank = rank;
    }

    public int getRank() {
        return rank;
    }

    public boolean meetsOrExceeds(ProficiencyLevel other) {
        return this.rank >= other.rank;
    }
}
