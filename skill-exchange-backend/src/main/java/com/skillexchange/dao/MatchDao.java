package com.skillexchange.dao;

import com.skillexchange.db.DBConnection;
import com.skillexchange.model.MatchResult;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;

/**
 * Persists the output of MatchmakingService so a match doesn't need to be
 * recomputed every time a dashboard loads, and so history can be audited.
 */
public class MatchDao {

    public int save(MatchResult match) throws SQLException {
        String sql = "INSERT INTO matches (mentor_id, learner_id, skill_score, time_score, "
                + "location_score, overall_score, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, match.getMentor().getId());
            ps.setInt(2, match.getLearner().getId());
            ps.setDouble(3, match.getSkillScore());
            ps.setDouble(4, match.getTimeScore());
            ps.setDouble(5, match.getLocationScore());
            ps.setDouble(6, match.getOverallScore());
            ps.setTimestamp(7, Timestamp.from(Instant.now()));
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        throw new SQLException("Insert into matches failed, no generated key returned.");
    }
}
