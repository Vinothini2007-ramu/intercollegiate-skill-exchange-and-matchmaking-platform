# Final AI-Powered Skill Exchange Project

## 1. Start AI service
Open Command Prompt in `ai-service`:

    python -m venv venv
    venv\Scripts\activate.bat
    pip install -r requirements.txt
    python app.py

Keep this window open. AI runs at http://127.0.0.1:5000.

## 2. Build Java backend
Open a second terminal at this project root:

    mvn clean package

The WAR will be created at `target/skill-exchange.war`.

## 3. Database
Run `sql/schema.sql` in MySQL and configure the JVM properties described in README.md:

    -Ddb.url=jdbc:mysql://localhost:3306/skill_exchange?useSSL=false&serverTimezone=UTC
    -Ddb.user=skillexchange_app
    -Ddb.password=YOUR_PASSWORD

## 4. Deploy
Copy `target/skill-exchange.war` to Tomcat 9 `webapps` and start Tomcat.

Open:

    http://localhost:8080/skill-exchange/

The Java frontend calls `/api/register` and `/api/ai-match` on the same Tomcat application.

## 5. AI demo
Use the Deep Learning Match Demo on the home page. The browser sends the text to Java `/api/ai-match`; Java calls Python `/ai/match`; Python uses Sentence-BERT (`all-MiniLM-L6-v2`) and cosine similarity; Java returns the score to the browser.

## 6. Existing matching API
`GET /api/matches?userId=...` keeps the original 50/30/20 Java score and adds AI fields when the Python service is available:
- `aiMatchPercent`
- `aiRecommendation`
- `aiModel`
