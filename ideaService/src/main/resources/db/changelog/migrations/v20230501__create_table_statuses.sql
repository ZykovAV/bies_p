
create table idea_db.statuses
(
    id   bigserial not null,
    name varchar(255),

    constraint pk_statuses primary key (id)

);