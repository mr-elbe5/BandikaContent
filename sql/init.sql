
CREATE SEQUENCE IF NOT EXISTS s_content_id START 1000;

CREATE TABLE IF NOT EXISTS t_content
(
    id            INTEGER       NOT NULL,
    type          VARCHAR(30)   NOT NULL,
    creation_date TIMESTAMP     NOT NULL DEFAULT now(),
    change_date   TIMESTAMP     NOT NULL DEFAULT now(),
    parent_id     INTEGER       NULL,
    ranking       INTEGER       NOT NULL DEFAULT 0,
    name          VARCHAR(60)   NOT NULL,
    display_name  VARCHAR(100)  NOT NULL,
    description   VARCHAR(2000) NOT NULL DEFAULT '',
    creator_id    INTEGER       NOT NULL DEFAULT 1,
    changer_id    INTEGER       NOT NULL DEFAULT 1,
    access_type   VARCHAR(10)   NOT NULL DEFAULT 'OPEN',
    nav_type      VARCHAR(10)   NOT NULL DEFAULT 'NONE',
    active        BOOLEAN       NOT NULL DEFAULT TRUE,
    CONSTRAINT t_content_pk PRIMARY KEY (id),
    CONSTRAINT t_content_fk1 FOREIGN KEY (parent_id) REFERENCES t_content (id) ON DELETE CASCADE,
    CONSTRAINT t_content_fk2 FOREIGN KEY (creator_id) REFERENCES t_user (id) ON DELETE SET DEFAULT,
    CONSTRAINT t_content_fk3 FOREIGN KEY (changer_id) REFERENCES t_user (id) ON DELETE SET DEFAULT,
    CONSTRAINT t_content_un1 UNIQUE (id, parent_id, name)
);

CREATE TABLE IF NOT EXISTS t_link
(
    id            INTEGER       NOT NULL,
    link_url      VARCHAR(500)  NOT NULL DEFAULT '',
    CONSTRAINT t_link_pk PRIMARY KEY (id),
    CONSTRAINT t_link_fk1 FOREIGN KEY (id) REFERENCES t_content (id) ON DELETE CASCADE
);

CREATE SEQUENCE IF NOT EXISTS s_file_id START 1000;

CREATE TABLE IF NOT EXISTS t_file
(
    id            INTEGER       NOT NULL,
    type          VARCHAR(30)   NOT NULL,
    creation_date TIMESTAMP     NOT NULL DEFAULT now(),
    change_date   TIMESTAMP     NOT NULL DEFAULT now(),
    parent_id     INTEGER       NULL,
    file_name     VARCHAR(60)   NOT NULL,
    display_name  VARCHAR(100)  NOT NULL,
    description   VARCHAR(2000) NOT NULL DEFAULT '',
    creator_id    INTEGER       NOT NULL DEFAULT 1,
    changer_id    INTEGER       NOT NULL DEFAULT 1,
    content_type  VARCHAR(255)  NOT NULL DEFAULT '',
    file_size     INTEGER       NOT NULL DEFAULT 0,
    bytes         BYTEA         NOT NULL,
    CONSTRAINT t_file_pk PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS t_image
(
    id            INTEGER       NOT NULL,
    width         INTEGER       NOT NULL DEFAULT 0,
    height        INTEGER       NOT NULL DEFAULT 0,
    preview_bytes BYTEA         NULL,
    CONSTRAINT t_image_pk PRIMARY KEY (id),
    CONSTRAINT t_image_fk1 FOREIGN KEY (id) REFERENCES t_file (id) ON DELETE CASCADE
);

CREATE OR REPLACE VIEW v_preview_file as (
                                         select t_file.id,file_name,content_type,preview_bytes
                                         from t_file, t_image
                                         where t_file.id=t_image.id
                                             );

CREATE TABLE IF NOT EXISTS t_content_right
(
    content_id INTEGER     NOT NULL,
    group_id   INTEGER     NOT NULL,
    value      VARCHAR(20) NOT NULL,
    CONSTRAINT t_content_right_pk PRIMARY KEY (content_id, group_id),
    CONSTRAINT t_content_right_fk1 FOREIGN KEY (content_id) REFERENCES t_content (id) ON DELETE CASCADE,
    CONSTRAINT t_content_right_fk2 FOREIGN KEY (group_id) REFERENCES t_group (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS t_content_log
(
    content_id INTEGER     NOT NULL,
    day        DATE        NOT NULL,
    count      INTEGER 	   NOT NULL,
    CONSTRAINT t_content_log_pk PRIMARY KEY (content_id, day),
    CONSTRAINT t_content_log_fk1 FOREIGN KEY (content_id) REFERENCES t_content (id) ON DELETE CASCADE
);
