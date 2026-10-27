package com.skillexchange.dao;

import com.skillexchange.db.DBConnection;
import com.skillexchange.model.Campus;
import com.skillexchange.model.ProficiencyLevel;
import com.skillexchange.model.TimeSlot;
import com.skillexchange.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDaoImpl implements UserDao {

    @Override
    public User create(User user, String passwordHash) throws SQLException {

        String sql =
                "INSERT INTO users " +
                "(name, email, password_hash, home_campus) " +
                "VALUES (?, ?, ?, ?)";

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps =
                        conn.prepareStatement(
                                sql,
                                Statement.RETURN_GENERATED_KEYS
                        )
        ) {

            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, passwordHash);
            ps.setString(4, user.getHomeCampus().name());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {

                if (keys.next()) {

                    return new User(
                            keys.getInt(1),
                            user.getName(),
                            user.getEmail(),
                            user.getHomeCampus()
                    );
                }
            }
        }

        throw new SQLException(
                "Insert into users failed, no generated key returned."
        );
    }


    @Override
    public Optional<User> findById(int id) throws SQLException {

        String sql =
                "SELECT id, name, email, home_campus " +
                "FROM users WHERE id = ?";

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    User user = mapRow(rs);

                    attachSkillsAndAvailability(conn, user);

                    return Optional.of(user);
                }
            }
        }

        return Optional.empty();
    }


    @Override
    public Optional<User> findByEmail(String email)
            throws SQLException {

        String sql =
                "SELECT id, name, email, home_campus " +
                "FROM users WHERE email = ?";

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    User user = mapRow(rs);

                    attachSkillsAndAvailability(conn, user);

                    return Optional.of(user);
                }
            }
        }

        return Optional.empty();
    }


    /**
     * Gets the SHA-256 password hash stored for a user.
     */
    @Override
    public String findPasswordHashByEmail(String email)
            throws SQLException {

        String sql =
                "SELECT password_hash " +
                "FROM users " +
                "WHERE email = ?";

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    return rs.getString("password_hash");
                }
            }
        }

        return null;
    }


    @Override
    public List<User> findAll() throws SQLException {

        List<User> users = new ArrayList<>();

        String sql =
                "SELECT id, name, email, home_campus " +
                "FROM users";

        try (
                Connection conn = DBConnection.getConnection();
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery(sql)
        ) {

            while (rs.next()) {

                User user = mapRow(rs);

                attachSkillsAndAvailability(conn, user);

                users.add(user);
            }
        }

        return users;
    }


    @Override
    public List<User> findCandidateMentors(
            int excludingUserId
    ) throws SQLException {

        List<User> users = new ArrayList<>();

        String sql =
                "SELECT id, name, email, home_campus " +
                "FROM users " +
                "WHERE id != ?";

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {

            ps.setInt(1, excludingUserId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    User user = mapRow(rs);

                    attachSkillsAndAvailability(conn, user);

                    users.add(user);
                }
            }
        }

        return users;
    }


    /**
     * Converts a database row into a User object.
     */
    private User mapRow(ResultSet rs)
            throws SQLException {

        return new User(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("email"),
                Campus.valueOf(
                        rs.getString("home_campus")
                )
        );
    }


    /**
     * Loads offered skills, wanted skills
     * and availability for a user.
     */
    private void attachSkillsAndAvailability(
            Connection conn,
            User user
    ) throws SQLException {

        // -----------------------------------------
        // OFFERED SKILLS
        // -----------------------------------------

        String offeredSql =
                "SELECT sc.name, uso.proficiency " +
                "FROM user_skills_offered uso " +
                "JOIN skills_catalog sc " +
                "ON sc.id = uso.skill_id " +
                "WHERE uso.user_id = ?";

        try (
                PreparedStatement ps =
                        conn.prepareStatement(offeredSql)
        ) {

            ps.setInt(1, user.getId());

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    user.offerSkill(
                            rs.getString("name"),
                            ProficiencyLevel.valueOf(
                                    rs.getString("proficiency")
                            )
                    );
                }
            }
        }


        // -----------------------------------------
        // WANTED SKILLS
        // -----------------------------------------

        String wantedSql =
                "SELECT sc.name, usw.min_proficiency " +
                "FROM user_skills_wanted usw " +
                "JOIN skills_catalog sc " +
                "ON sc.id = usw.skill_id " +
                "WHERE usw.user_id = ?";

        try (
                PreparedStatement ps =
                        conn.prepareStatement(wantedSql)
        ) {

            ps.setInt(1, user.getId());

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    user.wantSkill(
                            rs.getString("name"),
                            ProficiencyLevel.valueOf(
                                    rs.getString("min_proficiency")
                            )
                    );
                }
            }
        }


        // -----------------------------------------
        // AVAILABILITY
        // -----------------------------------------

        String availSql =
                "SELECT slot " +
                "FROM user_availability " +
                "WHERE user_id = ?";

        try (
                PreparedStatement ps =
                        conn.prepareStatement(availSql)
        ) {

            ps.setInt(1, user.getId());

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    user.availableAt(
                            TimeSlot.valueOf(
                                    rs.getString("slot")
                            )
                    );
                }
            }
        }
    }
}