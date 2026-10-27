package com.skillexchange.demo;

import com.skillexchange.model.*;
import com.skillexchange.service.CalendarManager;
import com.skillexchange.service.MatchmakingService;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * Runs the matching algorithm and the rotation manager against in-memory
 * sample data — no database required. Useful for demoing/testing the
 * business logic independent of the servlet/JDBC layer.
 *
 * Run with:  java -cp out com.skillexchange.demo.MatchDemo
 */
public class MatchDemo {

    public static void main(String[] args) {
        System.out.println("=== Skill Exchange — backend logic demo ===\n");
        demoMatching();
        System.out.println();
        demoRotation();
    }

    private static void demoMatching() {
        User priya = new User(1, "Priya Nandakumar", "priya.n@harborview.edu", Campus.HARBORVIEW)
                .offerSkill("Java Programming", ProficiencyLevel.ADVANCED)
                .offerSkill("Robotics", ProficiencyLevel.INTERMEDIATE)
                .offerSkill("Public Speaking", ProficiencyLevel.ADVANCED)
                .availableAt(TimeSlot.SAT_MORNING, TimeSlot.SUN_AFTERNOON);

        User arjun = new User(2, "Arjun Mehta", "arjun.m@lakeside.edu", Campus.LAKESIDE)
                .offerSkill("Graphic Design", ProficiencyLevel.ADVANCED)
                .offerSkill("Content Creation", ProficiencyLevel.INTERMEDIATE)
                .availableAt(TimeSlot.SAT_AFTERNOON);

        User jonah = new User(3, "Jonah Ferreira", "jonah.f@summit.edu", Campus.SUMMIT)
                .wantSkill("Java Programming", ProficiencyLevel.BEGINNER)
                .wantSkill("Public Speaking", ProficiencyLevel.BEGINNER)
                .availableAt(TimeSlot.SAT_MORNING, TimeSlot.SUN_AFTERNOON, TimeSlot.SUN_EVENING);

        MatchmakingService matchmaking = new MatchmakingService();
        List<MatchResult> matches = matchmaking.findMatches(jonah, Arrays.asList(priya, arjun));

        System.out.println("Learner: " + jonah.getName() + " (" + jonah.getHomeCampus().getDisplayName() + ")");
        System.out.println("Wants: Java Programming, Public Speaking\n");

        if (matches.isEmpty()) {
            System.out.println("No matches found above threshold.");
        }
        for (MatchResult m : matches) {
            System.out.printf(
                "-> %s (%s) | overall %d%%  [skill %.0f%% x50%%, time %.0f%% x30%%, location %.0f%% x20%%]  matched: %s%n",
                m.getMentor().getName(),
                m.getMentor().getHomeCampus().getDisplayName(),
                m.getOverallPercent(),
                m.getSkillScore() * 100,
                m.getTimeScore() * 100,
                m.getLocationScore() * 100,
                m.getMatchedSkills()
            );
        }
    }

    private static void demoRotation() {
        CalendarManager calendar = new CalendarManager(LocalDate.of(2026, 8, 1));
        System.out.println("Upcoming host-campus rotation:");
        for (CalendarManager.ScheduledWeek week : calendar.upcomingSchedule(6)) {
            System.out.println("  " + week);
        }

        LocalDate checkDate = LocalDate.of(2026, 9, 12);
        System.out.println("\nHost on " + checkDate + ": " + calendar.hostFor(checkDate).getDisplayName());
    }
}
