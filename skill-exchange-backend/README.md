# Skill Exchange — Backend

Java backend for the Intercollegiate Skill Exchange and Matchmaking Platform.
MVC architecture: Servlets (controller) → Service layer (business logic) →
DAO/JDBC (persistence) → MySQL, with model classes shared across all layers.
Servlets return JSON, so the existing `skill-exchange-frontend.html` can call
them directly with `fetch()`.

## Project layout

```
src/main/java/com/skillexchange/
  model/      User, Campus, SkillEntry, TimeSlot, ProficiencyLevel,
              MatchResult, Session — plain data classes, no framework code.
  service/    MatchmakingService — the weighted 50/30/20 matching algorithm.
              CalendarManager    — rotating host-campus scheduling.
  dao/        UserDao / UserDaoImpl, MatchDao, SessionDao — JDBC against MySQL.
  db/         DBConnection — JDBC connection factory.
  servlet/    MatchServlet, RotationServlet, DashboardServlet, RegisterServlet
              — thin controllers: parse request, call a service/DAO, write JSON.
  util/       JsonUtil — small dependency-free JSON writer.
  demo/       MatchDemo — runs the algorithm + rotation against in-memory
              sample data, no database needed. Good for a quick sanity check
              or a viva demo.
src/main/webapp/WEB-INF/web.xml   Servlet mappings for Tomcat 9.
sql/schema.sql                    Users / Skills_Catalog / User_Skills_Offered /
                                   User_Skills_Wanted / Matches / Sessions.
pom.xml                           Maven build, packaged as a .war.
```

## Try the logic right now (no DB, no server)

```
javac -d out $(find src/main/java/com/skillexchange/model \
  src/main/java/com/skillexchange/service \
  src/main/java/com/skillexchange/demo -name "*.java")
java -cp out com.skillexchange.demo.MatchDemo
```

Sample output:

```
Learner: Jonah Ferreira (Summit Institute)
Wants: Java Programming, Public Speaking

-> Priya Nandakumar (Harborview College) | overall 90%
   [skill 100% x50%, time 67% x30%, location 100% x20%]
   matched: [Java Programming, Public Speaking]

Upcoming host-campus rotation:
  Week 1 (2026-08-01) -> Northridge College
  Week 2 (2026-08-08) -> Lakeside University
  Week 3 (2026-08-15) -> Harborview College
  Week 4 (2026-08-22) -> Summit Institute
  Week 5 (2026-08-29) -> Northridge College
  Week 6 (2026-09-05) -> Lakeside University

Host on 2026-09-12: Harborview College
```

This confirms the two pieces of "core IP" the report calls out — the
weighted matchmaking score and the no-double-booking rotation — both work
independent of the web/DB layer.

## Running the full thing (Tomcat + MySQL)

1. **Database**
   ```
   mysql -u root -p < sql/schema.sql
   ```
2. **Build**
   ```
   mvn clean package
   ```
   This needs internet access to Maven Central (not available in this sandbox,
   which is why only the `model`/`service`/`demo` packages were compiled and
   run here — they have zero external dependencies). On a normal machine with
   Maven installed this produces `target/skill-exchange.war`.
3. **Deploy** — drop the WAR into Tomcat 9's `webapps/` folder, or point your
   IDE's Tomcat run configuration at this project.
4. **Configure the DB connection** — pass these as JVM system properties (or
   move them into a `context.xml` resource for production):
   ```
   -Ddb.url=jdbc:mysql://localhost:3306/skill_exchange
   -Ddb.user=skillexchange_app
   -Ddb.password=yourpassword
   ```

## API endpoints (what the frontend calls)

| Method | Path              | Purpose                                         |
|--------|-------------------|--------------------------------------------------|
| POST   | `/api/register`   | Create an account (name, email, password, campus)|
| GET    | `/api/matches?userId=` | Ranked mentor matches for a learner        |
| GET    | `/api/rotation?weeks=` | Upcoming host-campus schedule              |
| GET    | `/api/dashboard?userId=` | This student's upcoming sessions         |

Example: wiring the frontend's rotation dial to real data instead of the
hardcoded 4-week array —

```js
const res = await fetch('/api/rotation?weeks=4');
const schedule = await res.json();
// schedule[i] = { weekNumber, date, campusCode, campusName, isCurrentHost }
```

## Design notes

- **Matching weights** (`MatchmakingService`): skill overlap 50%, time
  compatibility 30%, location logistics 20% — directly from the report's
  methodology section. Location scores cross-campus pairs higher than
  same-campus, since cross-campus exchange is the platform's whole point.
- **Rotation** (`CalendarManager`): built on `java.time.LocalDate` and the
  `Campus` enum, exactly as described — the cycle length is
  `Campus.values().length`, so adding a fifth campus to the enum extends the
  rotation automatically.
- **No external JSON/ORM library**: `JsonUtil` is hand-rolled so the servlet
  layer only depends on the JDK + servlet API, keeping the WAR small and the
  build simple for a Tomcat-only deployment.
- **Passwords** are SHA-256 hashed as a placeholder in `RegisterServlet` —
  swap for BCrypt/Argon2 before any real deployment.
