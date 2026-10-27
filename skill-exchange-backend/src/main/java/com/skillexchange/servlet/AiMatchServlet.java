package com.skillexchange.servlet;

import com.skillexchange.ai.AiMatchingClient;
import com.skillexchange.util.JsonUtil;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/** POST /api/ai-match - Java-to-Python deep-learning matchmaking bridge. */
@WebServlet("/api/ai-match")
public class AiMatchServlet extends HttpServlet {
    private final AiMatchingClient ai = new AiMatchingClient();

    @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        String s1 = req.getParameter("student1");
        String s2 = req.getParameter("student2");
        if (s1 == null || s2 == null) {
            resp.setStatus(400); write(resp, error("student1 and student2 are required")); return;
        }
        try {
            AiMatchingClient.AiResult r = ai.score(s1, s2);
            Map<String,Object> out = new LinkedHashMap<>();
            out.put("matchPercentage", r.getPercentage());
            out.put("recommendation", r.getRecommendation());
            out.put("model", "Sentence-BERT (all-MiniLM-L6-v2)");
            write(resp, out);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); resp.setStatus(503); write(resp, error("AI service request interrupted"));
        } catch (Exception e) {
            resp.setStatus(503); write(resp, error("AI service unavailable. Start Python app.py on port 5000."));
        }
    }
    private Map<String,Object> error(String msg) { Map<String,Object> m=new LinkedHashMap<>(); m.put("error",msg); return m; }
    private void write(HttpServletResponse resp, Object o) throws IOException { resp.getWriter().print(JsonUtil.toJson(o)); }
}
