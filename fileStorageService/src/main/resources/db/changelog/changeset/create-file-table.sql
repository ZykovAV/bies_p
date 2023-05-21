-- liquibase formatted sql

-- changeset egorbacheva:1
CREATE TABLE file(
                 id        UUID PRIMARY KEY,
                 idea_id   BIGINT NOT NULL,
                 content_type  VARCHAR,
                 file_name VARCHAR NOT NULL CHECK (file_name <> ''),
                 file_size BIGINT
);