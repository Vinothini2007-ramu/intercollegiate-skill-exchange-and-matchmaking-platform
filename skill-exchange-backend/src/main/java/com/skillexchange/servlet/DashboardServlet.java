package com.skillexchange.servlet;

import com.skillexchange.dao.SessionDao;
import com.skillexchange.util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * GET /api/dashboard?userId=123
 *
 * Returns the learner's upcoming sessions.
 *
 * The response contains:
 * - campus
 * - date
 * - time
 * - room
 * - skill
 * - mentor details
 * - video meeting link
 */
@WebServlet("/api/dashboard")
public class DashboardServlet extends HttpServlet {

    private final SessionDao sessionDao = new SessionDao();

    @Override
    protected void doGet(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String userIdParam = req.getParameter("userId");

        // Check userId
        if (userIdParam == null || userIdParam.trim().isEmpty()) {

            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);

            try (PrintWriter out = resp.getWriter()) {
                out.print(
                        JsonUtil.toJson(
                                Collections.singletonMap(
                                        "error",
                                        "userId is required"
                                )
                        )
                );
            }

            return;
        }

        try {

            int userId = Integer.parseInt(userIdParam);

            List<SessionDao.UpcomingSession> sessions =
                    sessionDao.upcomingForUser(userId);

            List<Map<String, Object>> payload =
                    new ArrayList<>();

            for (SessionDao.UpcomingSession s : sessions) {

                Map<String, Object> row =
                        new LinkedHashMap<>();

                // Session information
                row.put("hostCampus", s.hostCampus);
                row.put("date", s.date.toString());
                row.put("time", s.time);
                row.put("room", s.room);
                row.put("skillTaught", s.skillTaught);

                // Video call information
                row.put("meetingLink", s.meetingLink);

                // Mentor information
                row.put("mentorName", s.mentorName);
                row.put("mentorEmail", s.mentorEmail);

                payload.add(row);
            }

            // Send JSON response
            try (PrintWriter out = resp.getWriter()) {
                out.print(JsonUtil.toJson(payload));
            }

        } catch (NumberFormatException e) {

            resp.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
            );

            try (PrintWriter out = resp.getWriter()) {
                out.print(
                        JsonUtil.toJson(
                                Collections.singletonMap(
                                        "error",
                                        "userId must be numeric"
                                )
                        )
                );
            }

        } catch (SQLException e) {

            e.printStackTrace();

            resp.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            try (PrintWriter out = resp.getWriter()) {
                out.print(
                        JsonUtil.toJson(
                                Collections.singletonMap(
                                        "error",
                                        "Database error"
                                )
                        )
                );
            }
        }
    }
}