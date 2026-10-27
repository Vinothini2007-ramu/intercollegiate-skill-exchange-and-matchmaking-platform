package com.skillexchange.servlet;

import com.skillexchange.dao.UserDao;
import com.skillexchange.dao.UserDaoImpl;
import com.skillexchange.model.Campus;
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
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * POST /api/register
 * Body (application/x-www-form-urlencoded): name, email, password, campus
 *
 * Skills and availability are added afterward via separate profile calls
 * (kept out of this servlet to keep sign-up fast and skill lists optional).
 */
@WebServlet("/api/register")
public class RegisterServlet extends HttpServlet {

    private final UserDao userDao = new UserDaoImpl();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String name = req.getParameter("name");
        String email = req.getParameter("email");
        String password = req.getParameter("password");
        String campusParam = req.getParameter("campus");

        if (isBlank(name) || isBlank(email) || isBlank(password) || isBlank(campusParam)) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeError(resp, "name, email, password, and campus are all required");
            return;
        }

        Campus campus;
        try {
            campus = Campus.valueOf(campusParam.toUpperCase());
        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeError(resp, "Unknown campus: " + campusParam);
            return;
        }

        try {
            if (userDao.findByEmail(email).isPresent()) {
                resp.setStatus(HttpServletResponse.SC_CONFLICT);
                writeError(resp, "An account with that email already exists");
                return;
            }

            User newUser = new User(0, name, email, campus);
            User created = userDao.create(newUser, hash(password));

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("id", created.getId());
            payload.put("name", created.getName());
            payload.put("email", created.getEmail());
            payload.put("campus", created.getHomeCampus().getDisplayName());

            resp.setStatus(HttpServletResponse.SC_CREATED);
            try (PrintWriter out = resp.getWriter()) {
                out.print(JsonUtil.toJson(payload));
            }
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writeError(resp, "Database error while creating account");
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    /** SHA-256 placeholder — swap for BCrypt/Argon2 in production. */
    private String hash(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(raw.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashed) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException | java.io.UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeError(HttpServletResponse resp, String message) throws IOException {
        try (PrintWriter out = resp.getWriter()) {
            out.print(JsonUtil.toJson(Collections.singletonMap("error", message)));
        }
    }
}
