
create table idea_db.ideas
(
    id          bigserial not null,
    name        varchar(255) not null,
    text        varchar not null,
    rating      integer,
    user_id     uuid   not null,
    status_id   bigint,

    constraint pk_ideas primary key (id)

);
