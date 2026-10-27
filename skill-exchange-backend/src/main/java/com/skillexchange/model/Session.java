package com.skillexchange.model;

import java.time.LocalDate;

public class Session {
    private int id;
    private final MatchResult match;
    private final Campus hostCampus;
    private final LocalDate date;
    private final String time;
    private final String room;
    private final String skillTaught;
    private final String meetingLink;

    public Session(int id, MatchResult match, Campus hostCampus,
                   LocalDate date, String time, String room,
                   String skillTaught, String meetingLink) {
        this.id = id;
        this.match = match;
        this.hostCampus = hostCampus;
        this.date = date;
        this.time = time;
        this.room = room;
        this.skillTaught = skillTaught;
        this.meetingLink = meetingLink;
    }

    public int getId() {
        return id;
    }

    public MatchResult getMatch() {
        return match;
    }

    public Campus getHostCampus() {
        return hostCampus;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getRoom() {
        return room;
    }

    public String getSkillTaught() {
        return skillTaught;
    }

    public String getMeetingLink() {
        return meetingLink;
    }
}