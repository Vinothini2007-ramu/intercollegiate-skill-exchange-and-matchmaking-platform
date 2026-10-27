package com.skillexchange.model;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class User {
    private int id;
    private String name;
    private String email;
    private Campus homeCampus;
    private final Set<SkillEntry> skillsOffered = new LinkedHashSet<>();
    private final Set<SkillEntry> skillsWanted = new LinkedHashSet<>();
    private final Set<TimeSlot> availability = new HashSet<>();

    public User(int id, String name, String email, Campus homeCampus) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.homeCampus = homeCampus;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public Campus getHomeCampus() { return homeCampus; }
    public Set<SkillEntry> getSkillsOffered() { return skillsOffered; }
    public Set<SkillEntry> getSkillsWanted() { return skillsWanted; }
    public Set<TimeSlot> getAvailability() { return availability; }

    public User offerSkill(String skillName, ProficiencyLevel level) {
        skillsOffered.add(new SkillEntry(skillName, level));
        return this;
    }

    public User wantSkill(String skillName, ProficiencyLevel minLevel) {
        skillsWanted.add(new SkillEntry(skillName, minLevel));
        return this;
    }

    public User availableAt(TimeSlot... slots) {
        for (TimeSlot slot : slots) availability.add(slot);
        return this;
    }
}
