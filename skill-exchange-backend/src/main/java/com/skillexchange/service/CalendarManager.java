package com.skillexchange.service;

import com.skillexchange.model.Campus;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

/**
 * Rotates the host campus on a fixed weekly cycle so travel responsibility
 * is spread evenly across all participating colleges. The cycle length
 * equals the number of Campus enum constants, so adding a fifth campus
 * to the enum automatically extends the rotation — no other code changes.
 */
public class CalendarManager {

    private final Campus[] rotation = Campus.values();
    private final LocalDate cycleStart; // the Saturday the rotation began

    public CalendarManager(LocalDate cycleStart) {
        // normalize to the Saturday of that week so week-math is exact
        this.cycleStart = cycleStart.with(TemporalAdjusters.previousOrSame(DayOfWeek.SATURDAY));
    }

    /** Which campus hosts on the given date's weekend. */
    public Campus hostFor(LocalDate date) {
        LocalDate saturday = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.SATURDAY));
        long weeksElapsed = ChronoUnit.WEEKS.between(cycleStart, saturday);
        int index = (int) (((weeksElapsed % rotation.length) + rotation.length) % rotation.length);
        return rotation[index];
    }

    /** Which campus hosts N weeks from the cycle start (0-indexed). */
    public Campus hostForWeek(int weekNumber) {
        int index = ((weekNumber % rotation.length) + rotation.length) % rotation.length;
        return rotation[index];
    }

    /** The Saturday date of the weekend that a given week number falls on. */
    public LocalDate dateForWeek(int weekNumber) {
        return cycleStart.plusWeeks(weekNumber);
    }

    /** Full rotation schedule for the next N weeks, for display or seeding Sessions. */
    public List<ScheduledWeek> upcomingSchedule(int numberOfWeeks) {
        List<ScheduledWeek> schedule = new ArrayList<>();
        for (int w = 0; w < numberOfWeeks; w++) {
            schedule.add(new ScheduledWeek(w + 1, dateForWeek(w), hostForWeek(w)));
        }
        return schedule;
    }

    /** Guards against double-booking: true if a campus is already hosting that weekend. */
    public boolean isHostConflict(LocalDate date, Campus proposedHost) {
        return hostFor(date) != proposedHost;
    }

    public static class ScheduledWeek {
        public final int weekNumber;
        public final LocalDate date;
        public final Campus host;

        public ScheduledWeek(int weekNumber, LocalDate date, Campus host) {
            this.weekNumber = weekNumber;
            this.date = date;
            this.host = host;
        }

        @Override
        public String toString() {
            return "Week " + weekNumber + " (" + date + ") -> " + host.getDisplayName();
        }
    }
}
