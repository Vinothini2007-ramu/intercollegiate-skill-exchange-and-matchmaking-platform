-- Skill Exchange platform schema (MySQL 8+)
-- Matches the tables named in the project report:
-- Users, Skills_Catalog, User_Skills_Offered, User_Skills_Wanted, Matches, Sessions

CREATE DATABASE IF NOT EXISTS skill_exchange
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE skill_exchange;

CREATE TABLE users (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(120)  NOT NULL,
    email         VARCHAR(160)  NOT NULL UNIQUE,
    password_hash CHAR(64)      NOT NULL,
    home_campus   ENUM('NORTHRIDGE','LAKESIDE','HARBORVIEW','SUMMIT') NOT NULL,
    created_at    TIMESTAMP     DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE skills_catalog (
    id   INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(120) NOT NULL UNIQUE,
    category VARCHAR(60)
) ENGINE=InnoDB;

CREATE TABLE user_skills_offered (
    user_id     INT NOT NULL,
    skill_id    INT NOT NULL,
    proficiency ENUM('BEGINNER','INTERMEDIATE','ADVANCED') NOT NULL,
    PRIMARY KEY (user_id, skill_id),
    FOREIGN KEY (user_id)  REFERENCES users(id)          ON DELETE CASCADE,
    FOREIGN KEY (skill_id) REFERENCES skills_catalog(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE user_skills_wanted (
    user_id         INT NOT NULL,
    skill_id        INT NOT NULL,
    min_proficiency ENUM('BEGINNER','INTERMEDIATE','ADVANCED') NOT NULL DEFAULT 'BEGINNER',
    PRIMARY KEY (user_id, skill_id),
    FOREIGN KEY (user_id)  REFERENCES users(id)          ON DELETE CASCADE,
    FOREIGN KEY (skill_id) REFERENCES skills_catalog(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE user_availability (
    user_id INT NOT NULL,
    slot    ENUM('SAT_MORNING','SAT_AFTERNOON','SAT_EVENING',
                 'SUN_MORNING','SUN_AFTERNOON','SUN_EVENING') NOT NULL,
    PRIMARY KEY (user_id, slot),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE matches (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    mentor_id      INT NOT NULL,
    learner_id     INT NOT NULL,
    skill_score    DECIMAL(4,3) NOT NULL,
    time_score     DECIMAL(4,3) NOT NULL,
    location_score DECIMAL(4,3) NOT NULL,
    overall_score  DECIMAL(4,3) NOT NULL,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (mentor_id)  REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (learner_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE sessions (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    match_id      INT NOT NULL,
    host_campus   ENUM('NORTHRIDGE','LAKESIDE','HARBORVIEW','SUMMIT') NOT NULL,
    session_date  DATE NOT NULL,
    session_time  VARCHAR(20),
    meeting_link  VARCHAR(500),
    room          VARCHAR(120),
    skill_taught  VARCHAR(120) NOT NULL,
    FOREIGN KEY (match_id) REFERENCES matches(id) ON DELETE CASCADE,
    -- one campus can only host one session per weekend date at the platform level
    UNIQUE KEY uniq_host_per_weekend (host_campus, session_date)
) ENGINE=InnoDB;

-- Seed skill catalog referenced in the report
INSERT INTO skills_catalog (name, category) VALUES
    ('Java Programming', 'Programming'),
    ('Data Science', 'Programming'),
    ('Graphic Design', 'Design'),
    ('Public Speaking', 'Communication'),
    ('Robotics', 'Engineering'),
    ('Content Creation', 'Communication');
