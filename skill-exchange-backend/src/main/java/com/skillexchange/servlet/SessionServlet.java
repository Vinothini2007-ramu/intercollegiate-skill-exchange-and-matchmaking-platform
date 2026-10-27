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
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@WebServlet("/api/session")
public class SessionServlet extends HttpServlet {

    private final SessionDao sessionDao = new SessionDao();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            String matchIdParam = req.getParameter("matchId");
            String hostCampus = req.getParameter("hostCampus");
            String dateParam = req.getParameter("date");
            String time = req.getParameter("time");
            String room = req.getParameter("room");
            String skillTaught = req.getParameter("skillTaught");

            if (matchIdParam == null || hostCampus == null ||
                    dateParam == null || time == null ||
                    skillTaught == null) {

                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                writeResponse(resp, "error",
                        "matchId, hostCampus, date, time and skillTaught are required");
                return;
            }

            int matchId = Integer.parseInt(matchIdParam);
            LocalDate date = LocalDate.parse(dateParam);

            /*
             * Create a unique video room.
             *
             * Example:
             * skill-exchange-8f31a2c9
             */
            String roomId = "skill-exchange-" +
                    UUID.randomUUID().toString().substring(0, 8);

            /*
             * Jitsi Meet allows us to use the room directly
             * without building a separate video server.
             */
            String meetingLink =
                    "https://meet.jit.si/" + roomId;

            if (sessionDao.hostAlreadyBooked(hostCampus, date)) {
                resp.setStatus(HttpServletResponse.SC_CONFLICT);
                writeResponse(resp, "error",
                        "This campus already has a session scheduled for this date");
                return;
            }

            int sessionId = sessionDao.schedule(
                    matchId,
                    hostCampus,
                    date,
                    time,
                    room,
                    skillTaught,
                    meetingLink
            );

            Map<String, Object> result = new LinkedHashMap<>();

            result.put("success", true);
            result.put("sessionId", sessionId);
            result.put("matchId", matchId);
            result.put("date", date.toString());
            result.put("time", time);
            result.put("room", room);
            result.put("skillTaught", skillTaught);
            result.put("meetingLink", meetingLink);

            try (PrintWriter out = resp.getWriter()) {
                out.print(JsonUtil.toJson(result));
            }

        } catch (NumberFormatException e) {

            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeResponse(resp, "error", "matchId must be numeric");

        } catch (java.time.format.DateTimeParseException e) {

            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeResponse(resp, "error",
                    "date must be in YYYY-MM-DD format");

        } catch (Exception e) {

            e.printStackTrace();

            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writeResponse(resp, "error",
                    "Unable to create session");
        }
    }

    private void writeResponse(
            HttpServletResponse resp,
            String key,
            Object value) throws IOException {

        Map<String, Object> response = new LinkedHashMap<>();
        response.put(key, value);

        try (PrintWriter out = resp.getWriter()) {
            out.print(JsonUtil.toJson(response));
        }
    }
}
