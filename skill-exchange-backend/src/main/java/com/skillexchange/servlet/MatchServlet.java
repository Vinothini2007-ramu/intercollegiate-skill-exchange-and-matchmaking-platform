package com.skillexchange.servlet;

import com.skillexchange.ai.AiMatchingClient;
import com.skillexchange.dao.MatchDao;
import com.skillexchange.dao.UserDao;
import com.skillexchange.dao.UserDaoImpl;
import com.skillexchange.model.MatchResult;
import com.skillexchange.model.User;
import com.skillexchange.service.MatchmakingService;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@WebServlet("/api/matches")
public class MatchServlet extends HttpServlet {

    private final UserDao userDao = new UserDaoImpl();
    private final MatchDao matchDao = new MatchDao();
    private final MatchmakingService matchmakingService =
            new MatchmakingService();
    private final AiMatchingClient aiMatchingClient =
            new AiMatchingClient();

    @Override
    protected void doGet(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String userIdParam = req.getParameter("userId");

        if (userIdParam == null ||
                userIdParam.trim().isEmpty()) {

            resp.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
            );

            writeError(
                    resp,
                    "userId query parameter is required"
            );

            return;
        }

        try {

            int userId = Integer.parseInt(userIdParam);

            Optional<User> learnerOpt =
                    userDao.findById(userId);

            if (!learnerOpt.isPresent()) {

                resp.setStatus(
                        HttpServletResponse.SC_NOT_FOUND
                );

                writeError(
                        resp,
                        "No user with id " + userId
                );

                return;
            }

            User learner = learnerOpt.get();

            List<User> candidates =
                    userDao.findCandidateMentors(userId);

            List<MatchResult> matches =
                    matchmakingService.findMatches(
                            learner,
                            candidates
                    );

            List<Map<String, Object>> payload =
                    new ArrayList<>();

            for (MatchResult match : matches) {

                /*
                 * Save the match so that a session can later
                 * use the generated matchId.
                 */
                int matchId = matchDao.save(match);

                Map<String, Object> row =
                        new LinkedHashMap<>();

                row.put(
                        "matchId",
                        matchId
                );

                row.put(
                        "mentorId",
                        match.getMentor().getId()
                );

                row.put(
                        "mentorName",
                        match.getMentor().getName()
                );

                row.put(
                        "mentorEmail",
                        match.getMentor().getEmail()
                );

                row.put(
                        "mentorCampus",
                        match.getMentor()
                                .getHomeCampus()
                                .getDisplayName()
                );

                row.put(
                        "overallPercent",
                        Math.round(
                                match.getOverallPercent()
                        * 100.0
                        ) / 100.0
                );

                row.put(
                        "skillScore",
                        match.getSkillScore()
                );

                row.put(
                        "timeScore",
                        match.getTimeScore()
                );

                row.put(
                        "locationScore",
                        match.getLocationScore()
                );

                row.put(
                        "matchedSkills",
                        match.getMatchedSkills()
                );

                /*
                 * AI semantic matching.
                 */
                String learnerText =
                        match.getLearner()
                                .getSkillsWanted()
                                .toString();

                String mentorText =
                        match.getMentor()
                                .getSkillsOffered()
                                .toString();

                try {

                    AiMatchingClient.AiResult ai =
                            aiMatchingClient.score(
                                    learnerText,
                                    mentorText
                            );

                    row.put(
                            "aiMatchPercent",
                            Math.round(
                                    ai.getPercentage()
                                    * 100.0
                            ) / 100.0
                    );

                    row.put(
                            "aiRecommendation",
                            ai.getRecommendation()
                    );

                    row.put(
                            "aiModel",
                            "Sentence-BERT (all-MiniLM-L6-v2)"
                    );

                } catch (Exception ignored) {

                    row.put(
                            "aiMatchPercent",
                            null
                    );

                    row.put(
                            "aiRecommendation",
                            "AI service unavailable"
                    );
                }

                payload.add(row);
            }

            try (PrintWriter out = resp.getWriter()) {

                out.print(
                        JsonUtil.toJson(payload)
                );
            }

        } catch (NumberFormatException e) {

            resp.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
            );

            writeError(
                    resp,
                    "userId must be numeric"
            );

        } catch (SQLException e) {

            e.printStackTrace();

            resp.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            writeError(
                    resp,
                    "Database error while computing matches"
            );
        }
    }

    private void writeError(
            HttpServletResponse resp,
            String message)
            throws IOException {

        Map<String, Object> error =
                new LinkedHashMap<>();

        error.put(
                "error",
                message
        );

        try (PrintWriter out =
                     resp.getWriter()) {

            out.print(
                    JsonUtil.toJson(error)
            );
        }
    }
}
