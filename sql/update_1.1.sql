alter table t_content drop column language;

CREATE TABLE IF NOT EXISTS t_link
(
    id            INTEGER       NOT NULL,
    link_url      VARCHAR(500)  NOT NULL DEFAULT '',
    link_icon     VARCHAR(255)  NOT NULL DEFAULT '',
    CONSTRAINT t_link_pk PRIMARY KEY (id),
    CONSTRAINT t_link_fk1 FOREIGN KEY (id) REFERENCES t_content (id) ON DELETE CASCADE
);

ALTER TABLE t_content ALTER COLUMN type TYPE varchar(255);
ALTER TABLE t_file ALTER COLUMN type TYPE varchar(255);

UPDATE t_content set type = 'de.elbe5.content.ContentData' where type = 'ContentData';
UPDATE t_content set type = 'de.elbe5.link.LinkData' where type = 'LinkData';

UPDATE t_file set type = 'de.elbe5.file.FileData' where type = 'FileData';
UPDATE t_file set type = 'de.elbe5.file.DocumentData' where type = 'DocumentData';
UPDATE t_file set type = 'de.elbe5.file.ImageData' where type = 'ImageData';
UPDATE t_file set type = 'de.elbe5.file.MediaData' where type = 'MediaData';



