DROP TABLE IF EXISTS users;
CREATE TABLE users(
    id BIGINT NOT NULL,
    nickname VARCHAR(100) NOT NULL,
    profile_image VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    password VARCHAR(200) NOT NULL,
    isVerified SMALLINT DEFAULT false NOT NULL,
    created_date DATETIME NOT NULL,
    modified_date DATETIME NOT NULL,
    PRIMARY KEY (id)
);