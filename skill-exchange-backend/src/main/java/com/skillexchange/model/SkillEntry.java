package com.skillexchange.model;

import java.util.Objects;

/**
 * Backs both User_Skills_Offered and User_Skills_Wanted — the "direction"
 * (offered vs wanted) is determined by which list on User it lives in,
 * matching the schema's two separate tables.
 */
public class SkillEntry {
    private final String skillName;
    private final ProficiencyLevel level;

    public SkillEntry(String skillName, ProficiencyLevel level) {
        this.skillName = skillName;
        this.level = level;
    }

    public String getSkillName() {
        return skillName;
    }

    public ProficiencyLevel getLevel() {
        return level;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SkillEntry)) return false;
        SkillEntry that = (SkillEntry) o;
        return skillName.equalsIgnoreCase(that.skillName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(skillName.toLowerCase());
    }

    @Override
    public String toString() {
        return skillName + " (" + level + ")";
    }
}
