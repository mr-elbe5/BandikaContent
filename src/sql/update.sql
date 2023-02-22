alter table t_content drop column language;

CREATE TABLE IF NOT EXISTS t_link
(
    id            INTEGER       NOT NULL,
    link_url      VARCHAR(500)  NOT NULL DEFAULT '',
    link_icon     VARCHAR(255)  NOT NULL DEFAULT '',
    CONSTRAINT t_link_pk PRIMARY KEY (id),
    CONSTRAINT t_link_fk1 FOREIGN KEY (id) REFERENCES t_content (id) ON DELETE CASCADE
);

--alter TABLE t_link add link_icon     VARCHAR(255)  NOT NULL DEFAULT '';