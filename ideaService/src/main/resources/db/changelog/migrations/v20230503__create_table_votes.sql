
create table idea_db.votes
(
    idea_id bigint not null,
    user_id uuid not null,
    is_like boolean,
    constraint pk_votes primary key (idea_id, user_id)
);