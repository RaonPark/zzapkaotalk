CREATE TABLE group_chat_message(
    id BIGINT NOT NULL,
    content VARCHAR(200) NOT NULL,
    from_user_id BIGINT NOT NULL,
    chat_room_id BIGINT NOT NULL,
    checked INT NOT NULL,
    created_date DATETIME NOT NULL,
    modified_date DATETIME NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE direct_chat_message(
    id BIGINT NOT NULL,
    message VARCHAR(200) NOT NULL,
    from_user_id BIGINT NOT NULL,
    to_user_id BIGINT NOT NULL,
    checked TINYINT(1) NOT NULL,
    created_at DATETIME NOT NULL,
    last_modified_at DATETIME NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE chatroom(
    id BIGINT NOT NULL,
    room_name VARCHAR(20) NOT NULL,
    room_image VARCHAR(100) NOT NULL,
    room_description VARCHAR(100) NOT NULL,
    created_date DATETIME NOT NULL,
    modified_date DATETIME NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE chatroom_users(
    id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    chatroom_id BIGINT NOT NULL,
    role VARCHAR(100) NOT NULL,
    created_date DATETIME NOT NULL,
    modified_date DATETIME NOT NULL,
    PRIMARY KEY (id)
);

CREATE INDEX idx_group_chat_message_chat_room_id_created_date ON group_chat_message(chat_room_id, created_date desc);

CREATE INDEX idx_direct_chat_message_user_id_created_date ON direct_chat_message(to_user_id, created_at);