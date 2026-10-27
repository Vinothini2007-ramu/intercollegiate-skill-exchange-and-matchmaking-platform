package com.skillexchange.service;

import com.skillexchange.model.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Core matching engine.
 *
 * Score = 0.50 * skillOverlap + 0.30 * timeCompatibility + 0.20 * locationLogistics
 *
 * - skillOverlap:   fraction of the learner's wanted skills that a mentor can
 *                    actually teach at a sufficient proficiency level.
 * - timeCompatibility: fraction of the learner's available slots that the
 *                    mentor also has free.
 * - locationLogistics: rewards cross-campus pairs, since the platform's whole
 *                    purpose is intercollegiate exchange; same-campus pairs
 *                    still count (in case of a small nearby cohort) but score lower.
 *
 * A mentor is only surfaced as a candidate match if overallScore meets
 * MIN_MATCH_THRESHOLD and there is at least one overlapping skill and time slot.
 */
public class MatchmakingService {

    public static final double SKILL_WEIGHT = 0.50;
    public static final double TIME_WEIGHT = 0.30;
    public static final double LOCATION_WEIGHT = 0.20;
    public static final double MIN_MATCH_THRESHOLD = 0.35;

    private static final double SAME_CAMPUS_LOCATION_SCORE = 0.6;
    private static final double CROSS_CAMPUS_LOCATION_SCORE = 1.0;

    /**
     * Scores and ranks every candidate mentor for a given learner.
     * Returns only mentors that clear MIN_MATCH_THRESHOLD, sorted best-first.
     */
    public List<MatchResult> findMatches(User learner, List<User> candidateMentors) {
        List<MatchResult> results = new ArrayList<>();

        for (User mentor : candidateMentors) {
            if (mentor.getId() == learner.getId()) continue;

            List<String> overlappingSkills = matchedSkills(learner, mentor);
            if (overlappingSkills.isEmpty()) continue;

            double skillScore = skillOverlapScore(learner, mentor);
            double timeScore = timeCompatibilityScore(learner, mentor);
            if (timeScore == 0.0) continue; // no shared weekend availability, no point matching

            double locationScore = locationScore(learner, mentor);

            double overall = SKILL_WEIGHT * skillScore
                    + TIME_WEIGHT * timeScore
                    + LOCATION_WEIGHT * locationScore;

            if (overall >= MIN_MATCH_THRESHOLD) {
                results.add(new MatchResult(mentor, learner, skillScore, timeScore,
                        locationScore, overall, overlappingSkills));
            }
        }

        results.sort(Comparator.comparingDouble(MatchResult::getOverallScore).reversed());
        return results;
    }

    private List<String> matchedSkills(User learner, User mentor) {
        List<String> matched = new ArrayList<>();
        for (SkillEntry wanted : learner.getSkillsWanted()) {
            for (SkillEntry offered : mentor.getSkillsOffered()) {
                if (offered.getSkillName().equalsIgnoreCase(wanted.getSkillName())
                        && offered.getLevel().meetsOrExceeds(wanted.getLevel())) {
                    matched.add(offered.getSkillName());
                    break;
                }
            }
        }
        return matched;
    }

    private double skillOverlapScore(User learner, User mentor) {
        if (learner.getSkillsWanted().isEmpty()) return 0.0;
        long satisfied = matchedSkills(learner, mentor).size();
        return satisfied / (double) learner.getSkillsWanted().size();
    }

    private double timeCompatibilityScore(User learner, User mentor) {
        if (learner.getAvailability().isEmpty()) return 0.0;
        Set<TimeSlot> shared = new HashSet<>(learner.getAvailability());
        shared.retainAll(mentor.getAvailability());
        return shared.size() / (double) learner.getAvailability().size();
    }

    private double locationScore(User learner, User mentor) {
        return learner.getHomeCampus() == mentor.getHomeCampus()
                ? SAME_CAMPUS_LOCATION_SCORE
                : CROSS_CAMPUS_LOCATION_SCORE;
    }

    /** Convenience for reporting: what % of a candidate pool actually matched. */
    public double successRate(List<User> learners, List<User> mentorPool) {
        long matched = learners.stream()
                .filter(l -> !findMatches(l, mentorPool).isEmpty())
                .count();
        return learners.isEmpty() ? 0.0 : matched / (double) learners.size();
    }
}
