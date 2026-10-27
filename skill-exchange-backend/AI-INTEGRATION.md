# Deep-learning integration

Run the Python service in `ai-service` first. It exposes POST `/ai/match` and uses Sentence-BERT (`all-MiniLM-L6-v2`) plus cosine similarity.

Run the Java WAR on Tomcat. The browser uses `/api/ai-match`, so frontend -> Java -> Python -> Java -> frontend. The existing `/api/matches?userId=` endpoint also attempts to add `aiMatchPercent`, `aiRecommendation`, and `aiModel` to each traditional match.

Optional JVM property for a different AI URL:
`-Dai.url=http://127.0.0.1:5000`
