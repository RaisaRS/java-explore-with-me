--CREATE TABLE IF NOT EXISTS users
--(
--    id    bigint                 NOT NULL GENERATED BY DEFAULT AS IDENTITY,
--    email character varying(254) NOT NULL,
--    name  character varying(250) NOT NULL,
--    CONSTRAINT users_pkey PRIMARY KEY (id),
--    CONSTRAINT users_email_key UNIQUE (email)
--);

CREATE TABLE IF NOT EXISTS users
(
    id    BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    name  VARCHAR(255)                            NOT NULL,
    email VARCHAR(512)                            NOT NULL,
    CONSTRAINT pk_user PRIMARY KEY (id),
    CONSTRAINT UQ_USER_EMAIL UNIQUE (email)
);


--CREATE TABLE IF NOT EXISTS categories
--(
--    id   bigint                NOT NULL GENERATED BY DEFAULT AS IDENTITY,
--    name character varying(50) NOT NULL,
--    CONSTRAINT categories_pkey PRIMARY KEY (id),
--    CONSTRAINT categories_name_key UNIQUE (name)
--);

CREATE TABLE IF NOT EXISTS categories
(
    id   BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    name varchar(50)                             NOT NULL,
    CONSTRAINT pk_categories PRIMARY KEY (id),
    CONSTRAINT UQ_CATEGORY_NAME UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS locations
(
    id  bigint  NOT NULL GENERATED BY DEFAULT AS IDENTITY,
    lat numeric NOT NULL,
    lon numeric NOT NULL,
    CONSTRAINT pk_locations PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS events
(
    id                 BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    annotation         VARCHAR(2000)                           NOT NULL,
    category_id        BIGINT,
--   confirmed_requests  BIGINT,
    created_on         TIMESTAMP WITHOUT TIME ZONE             NOT NULL,
    description        VARCHAR(7000)                           NOT NULL,
    event_date         TIMESTAMP WITHOUT TIME ZONE             NOT NULL,
    initiator_id       BIGINT,
    location_id        BIGINT,
    paid               BOOLEAN                                 NOT NULL,
    participant_limit  INT,
    published_on       TIMESTAMP WITHOUT TIME ZONE,
    request_moderation BOOLEAN,
    state              VARCHAR(128),
    title              VARCHAR(120)                            NOT NULL,
    views              BIGINT,
--   request               BIGINT,
    CONSTRAINT pk_id PRIMARY KEY (id),
    CONSTRAINT fk_events_initiator FOREIGN KEY (initiator_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_events_location_id FOREIGN KEY (location_id) REFERENCES locations (id) ON DELETE CASCADE,
    CONSTRAINT fk_events_category FOREIGN KEY (category_id) REFERENCES categories (id) ON DELETE CASCADE,
    CONSTRAINT UQ_EVENT_EVENT_INITIATOR_DATE UNIQUE (id, initiator_id, event_date)
);

--CREATE TABLE IF NOT EXISTS events
--(
--    id                 bigint                  NOT NULL GENERATED BY DEFAULT AS IDENTITY,
--    title              character varying(120)  NOT NULL,
--    description        character varying(7000) NOT NULL,
--    annotation         character varying(2000) NOT NULL,
--    event_date         timestamp without time zone,
--    created_on         timestamp without time zone,
--    published_on       timestamp without time zone,
--    participant_limit  integer,
--    request_moderation boolean default true,
--    paid               boolean,
--    state              character varying,
--    category_id        bigint,
--    initiator_id       bigint,
--    location_id        bigint,
--    CONSTRAINT events_pkey PRIMARY KEY (id),
--    CONSTRAINT events_category_id_fkey FOREIGN KEY (category_id)
--        REFERENCES public.categories (id),
--    CONSTRAINT events_initiator_id_fkey FOREIGN KEY (initiator_id)
----    CONSTRAINT events_location_id_fkey FOREIGN KEY (location_id)
--        REFERENCES public.locations (id)
--);

--CREATE TABLE IF NOT EXISTS compilations
--(
--    id     bigint                NOT NULL GENERATED BY DEFAULT AS IDENTITY,
--    title  character varying(50) NOT NULL,
--    pinned boolean,
--    CONSTRAINT compilation_pkey PRIMARY KEY (id)
--);
--CREATE TABLE IF NOT EXISTS events_compilations
--(
--    event_id       bigint NOT NULL,
--    compilation_id bigint NOT NULL,
--    CONSTRAINT events_compilations_pkey PRIMARY KEY (event_id, compilation_id),
--    CONSTRAINT events_compilations_compilation_id_fkey FOREIGN KEY (compilation_id)
--        REFERENCES public.compilations (id),
--    CONSTRAINT events_compilations_event_id_fkey FOREIGN KEY (event_id)
--        REFERENCES public.events (id)
--);
CREATE TABLE IF NOT EXISTS compilations
(
    id     BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    title  VARCHAR(50)                             NOT NULL,
    pinned BOOL                                    NOT NULL,
    CONSTRAINT pk_compilations PRIMARY KEY (id),
    CONSTRAINT UQ_COMPILATION_TITLE UNIQUE (title)
);
CREATE TABLE IF NOT EXISTS events_compilations
(
    event_id       BIGINT,
    compilation_id BIGINT,
    PRIMARY KEY (event_id, compilation_id),
    CONSTRAINT fk_events_compilations_event FOREIGN KEY (event_id) REFERENCES events (id) ON DELETE CASCADE,
    CONSTRAINT fk_events_compilations_compilation FOREIGN KEY (compilation_id) REFERENCES compilations (id) ON DELETE CASCADE,
    CONSTRAINT UQ_EVENT_WITH_COMPILATION UNIQUE (event_id, compilation_id)
);



CREATE TABLE IF NOT EXISTS requests
(
    id           BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    created      TIMESTAMP WITHOUT TIME ZONE,
    event_id     BIGINT                                  NOT NULL,
    requester_id BIGINT                                  NOT NULL,
    status       VARCHAR(255),
    CONSTRAINT pk_requests PRIMARY KEY (id),
    CONSTRAINT fk_requests_event FOREIGN KEY (event_id) REFERENCES events (id) ON DELETE CASCADE,
    CONSTRAINT fk_requests_requester FOREIGN KEY (requester_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT UQ_REQUEST_EVENT_REQUESTER UNIQUE (event_id, requester_id)
);