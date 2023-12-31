CREATE TABLE IF NOT EXISTS public.hits
(
    id
              BIGINT
                                          NOT
                                              NULL
        GENERATED
            BY
            DEFAULT AS
            IDENTITY,
    app
              VARCHAR(512)                NOT NULL,
    uri       VARCHAR(512)                NOT NULL,
    ip        VARCHAR(255)                NOT NULL, --проверить
    date_time TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT stats_pkey PRIMARY KEY
        (
         id
            )
);