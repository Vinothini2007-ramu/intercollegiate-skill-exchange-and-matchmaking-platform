package com.skillexchange.servlet;

import com.skillexchange.dao.UserDao;
import com.skillexchange.dao.UserDaoImpl;
import com.skillexchange.model.User;
import com.skillexchange.util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@WebServlet("/api/login")
public class LoginServlet extends HttpServlet {

    private final UserDao userDao = new UserDaoImpl();

    @Override
    protected void doPost(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String email = req.getParameter("email");
        String password = req.getParameter("password");

        // Validate input
        if (isBlank(email) || isBlank(password)) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeError(resp, "Email and password are required");
            return;
        }

        try {

            // Find user by email
            Optional<User> optionalUser = userDao.findByEmail(email);

            if (!optionalUser.isPresent()) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                writeError(resp, "Invalid email or password");
                return;
            }

            User user = optionalUser.get();

            // Hash entered password
            String enteredPasswordHash = hash(password);

            // Get stored password hash directly from database
            String storedPasswordHash = getStoredPasswordHash(email);

            if (!enteredPasswordHash.equalsIgnoreCase(storedPasswordHash)) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                writeError(resp, "Invalid email or password");
                return;
            }

            // Login successful
            Map<String, Object> payload = new LinkedHashMap<>();

            payload.put("success", true);
            payload.put("id", user.getId());
            payload.put("name", user.getName());
            payload.put("email", user.getEmail());
            payload.put(
                    "campus",
                    user.getHomeCampus().getDisplayName()
            );

            resp.setStatus(HttpServletResponse.SC_OK);

            try (PrintWriter out = resp.getWriter()) {
                out.print(JsonUtil.toJson(payload));
            }

        } catch (SQLException e) {

            e.printStackTrace();

            resp.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            writeError(
                    resp,
                    "Database error while logging in"
            );
        }
    }

    /**
     * Get password hash from database.
     */
    private String getStoredPasswordHash(String email)
            throws SQLException {

        String sql =
                "SELECT password_hash FROM users WHERE email = ?";

        try (
                java.sql.Connection conn =
                        com.skillexchange.db.DBConnection.getConnection();

                java.sql.PreparedStatement ps =
                        conn.prepareStatement(sql)
        ) {

            ps.setString(1, email);

            try (
                    java.sql.ResultSet rs =
                            ps.executeQuery()
            ) {

                if (rs.next()) {
                    return rs.getString("password_hash");
                }
            }
        }

        return "";
    }

    /**
     * SHA-256 password hashing.
     * This matches RegisterServlet.
     */
    private String hash(String raw) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hashed =
                    digest.digest(
                            raw.getBytes("UTF-8")
                    );

            StringBuilder sb =
                    new StringBuilder();

            for (byte b : hashed) {
                sb.append(
                        String.format("%02x", b)
                );
            }

            return sb.toString();

        } catch (Exception e) {

            throw new RuntimeException(e);
        }
    }

    private boolean isBlank(String value) {

        return value == null ||
               value.trim().isEmpty();
    }

    private void writeError(
            HttpServletResponse resp,
            String message)
            throws IOException {

        try (PrintWriter out = resp.getWriter()) {

            out.print(
                    JsonUtil.toJson(
                            Collections.singletonMap(
                                    "error",
                                    message
                            )
                    )
            );
        }
    }
}