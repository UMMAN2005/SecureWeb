DROP DATABASE IF EXISTS MidTerm;
CREATE DATABASE MidTerm;
USE MidTerm;

CREATE TABLE users
(
    id              int                               NOT NULL AUTO_INCREMENT,
    username        varchar(50)                       NOT NULL,
    password        varchar(255)                      NOT NULL,
    role            enum ('user','admin','moderator') NOT NULL,
    profile_picture varchar(255) DEFAULT 'default.jpg',
    PRIMARY KEY (id),
    UNIQUE KEY username (username)
) ENGINE = InnoDB
  AUTO_INCREMENT = 9
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE messages
(
    id          INT         NOT NULL AUTO_INCREMENT,
    username    VARCHAR(50) NOT NULL,
    message     TEXT        NOT NULL,
    date_posted TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;
CREATE TABLE LoginAttempts
(
    username            VARCHAR(255) PRIMARY KEY,
    failed_attempts     INT    DEFAULT 0,
    last_failed_attempt BIGINT DEFAULT 0
);

DELIMITER $$

CREATE TRIGGER before_users_insert
    BEFORE INSERT
    ON users
    FOR EACH ROW
BEGIN
    -- Check if the password is already hashed
    IF LENGTH(NEW.password) != 64 THEN
        SET NEW.password = SHA2(NEW.password, 256);
    END IF;
END$$

DELIMITER ;


DELIMITER $$

CREATE TRIGGER before_users_update
    BEFORE UPDATE
    ON users
    FOR EACH ROW
BEGIN
    -- Only hash the password if it has changed and is not already hashed
    IF LENGTH(NEW.password) != 64 THEN
        SET NEW.password = SHA2(NEW.password, 256);
    END IF;
END$$

DELIMITER ;


INSERT INTO users (username, password, role, profile_picture)
VALUES ('admin', 'admin', 'admin', 'default.jpg');

ALTER TABLE Users
    ADD COLUMN auth_token VARCHAR(255);

-- Table for Remember Me Tokens
CREATE TABLE RememberMeTokens
(
    username   VARCHAR(50),
    token      VARCHAR(255),
    expiration TIMESTAMP,
    PRIMARY KEY (username),
    FOREIGN KEY (username) REFERENCES Users (username)
);

-- Table for Session Logs
CREATE TABLE SessionLogs
(
    session_id  VARCHAR(255) PRIMARY KEY,
    username    VARCHAR(50),
    ip_address  VARCHAR(45),
    login_time  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    logout_time TIMESTAMP NULL,
    FOREIGN KEY (username) REFERENCES Users (username)
);