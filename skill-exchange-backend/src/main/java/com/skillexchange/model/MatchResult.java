package com.skillexchange.model;

import java.util.List;

/**
 * Output of MatchmakingService: a scored mentor for a given learner,
 * broken down by the three weighted pillars so the UI (and logs) can
 * show why a match was made, not just that it was.
 */
public class MatchResult {
    private final User mentor;
    private final User learner;
    private final double skillScore;      // 0.0 - 1.0
    private final double timeScore;       // 0.0 - 1.0
    private final double locationScore;   // 0.0 - 1.0
    private final double overallScore;    // 0.0 - 1.0, weighted
    private final List<String> matchedSkills;

    public MatchResult(User mentor, User learner, double skillScore, double timeScore,
                        double locationScore, double overallScore, List<String> matchedSkills) {
        this.mentor = mentor;
        this.learner = learner;
        this.skillScore = skillScore;
        this.timeScore = timeScore;
        this.locationScore = locationScore;
        this.overallScore = overallScore;
        this.matchedSkills = matchedSkills;
    }

    public User getMentor() { return mentor; }
    public User getLearner() { return learner; }
    public double getSkillScore() { return skillScore; }
    public double getTimeScore() { return timeScore; }
    public double getLocationScore() { return locationScore; }
    public double getOverallScore() { return overallScore; }
    public List<String> getMatchedSkills() { return matchedSkills; }

    public int getOverallPercent() {
        return (int) Math.round(overallScore * 100);
    }
}
