-- src/main/resources/db/migration/V2__add_users.sql
-- Usamos app_user en lugar de user porque user es una palabra reservada en PostgreSQL.
CREATE TABLE app_user (
    id        bigserial    PRIMARY KEY,
    username  varchar(80)  NOT NULL UNIQUE,
    password  varchar(200) NOT NULL,
    role      varchar(20)  NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now()
);