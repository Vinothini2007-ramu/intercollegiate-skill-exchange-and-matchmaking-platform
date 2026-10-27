package com.skillexchange.servlet;

import com.skillexchange.model.Campus;
import com.skillexchange.service.CalendarManager;
import com.skillexchange.util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.*;

/**
 * GET /api/rotation?weeks=4
 * Returns the upcoming host-campus rotation, driving the interactive dial
 * on the frontend's "Campus rotation" section.
 */
@WebServlet("/api/rotation")
public class RotationServlet extends HttpServlet {

    // Semester start date — in production this would come from a config table.
    private static final LocalDate SEMESTER_START = LocalDate.of(2026, 8, 1);
    private final CalendarManager calendarManager = new CalendarManager(SEMESTER_START);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        int weeks = 4;
        String weeksParam = req.getParameter("weeks");
        if (weeksParam != null) {
            try {
                weeks = Math.max(1, Math.min(52, Integer.parseInt(weeksParam)));
            } catch (NumberFormatException ignored) {
                // fall back to default
            }
        }

        List<CalendarManager.ScheduledWeek> schedule = calendarManager.upcomingSchedule(weeks);
        List<Map<String, Object>> payload = new ArrayList<>();
        Campus currentHost = calendarManager.hostFor(LocalDate.now());

        for (CalendarManager.ScheduledWeek w : schedule) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("weekNumber", w.weekNumber);
            row.put("date", w.date.toString());
            row.put("campusCode", w.host.name());
            row.put("campusName", w.host.getDisplayName());
            row.put("isCurrentHost", w.host == currentHost);
            payload.add(row);
        }

        try (PrintWriter out = resp.getWriter()) {
            out.print(JsonUtil.toJson(payload));
        }
    }
}
