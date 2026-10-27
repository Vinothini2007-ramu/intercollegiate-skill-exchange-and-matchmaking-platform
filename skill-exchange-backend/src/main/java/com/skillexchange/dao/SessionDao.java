package com.skillexchange.dao;

import com.skillexchange.db.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SessionDao {

    public int schedule(int matchId, String hostCampus, LocalDate date,
                        String time, String room, String skillTaught,
                        String meetingLink) throws SQLException {

        String sql = "INSERT INTO sessions "
                + "(match_id, host_campus, session_date, session_time, room, skill_taught, meeting_link) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, matchId);
            ps.setString(2, hostCampus);
            ps.setDate(3, Date.valueOf(date));
            ps.setString(4, time);
            ps.setString(5, room);
            ps.setString(6, skillTaught);
            ps.setString(7, meetingLink);

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }

        throw new SQLException(
                "Insert into sessions failed, no generated key returned.");
    }

    /**
     * Checks whether the selected campus is already hosting
     * a session on the given date.
     */
    public boolean hostAlreadyBooked(String hostCampus, LocalDate date)
            throws SQLException {

        String sql = "SELECT COUNT(*) FROM sessions "
                + "WHERE host_campus = ? AND session_date = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, hostCampus);
            ps.setDate(2, Date.valueOf(date));

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * Returns upcoming sessions for a learner.
     */
    public List<UpcomingSession> upcomingForUser(int userId)
            throws SQLException {

        List<UpcomingSession> sessions = new ArrayList<>();

        String sql = "SELECT s.host_campus, s.session_date, "
                + "s.session_time, s.room, s.skill_taught, "
                + "s.meeting_link, "
                + "mentor.name AS mentor_name, "
                + "mentor.email AS mentor_email "
                + "FROM sessions s "
                + "JOIN matches m ON m.id = s.match_id "
                + "JOIN users mentor ON mentor.id = m.mentor_id "
                + "WHERE m.learner_id = ? "
                + "AND s.session_date >= CURDATE() "
                + "ORDER BY s.session_date ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    sessions.add(new UpcomingSession(
                            rs.getString("host_campus"),
                            rs.getDate("session_date").toLocalDate(),
                            rs.getString("session_time"),
                            rs.getString("room"),
                            rs.getString("skill_taught"),
                            rs.getString("meeting_link"),
                            rs.getString("mentor_name"),
                            rs.getString("mentor_email")
                    ));
                }
            }
        }

        return sessions;
    }

    /**
     * Data object used to display upcoming sessions.
     */
    public static class UpcomingSession {

        public final String hostCampus;
        public final LocalDate date;
        public final String time;
        public final String room;
        public final String skillTaught;
        public final String meetingLink;
        public final String mentorName;
        public final String mentorEmail;

        public UpcomingSession(
                String hostCampus,
                LocalDate date,
                String time,
                String room,
                String skillTaught,
                String meetingLink,
                String mentorName,
                String mentorEmail) {

            this.hostCampus = hostCampus;
            this.date = date;
            this.time = time;
            this.room = room;
            this.skillTaught = skillTaught;
            this.meetingLink = meetingLink;
            this.mentorName = mentorName;
            this.mentorEmail = mentorEmail;
        }
    }
}