-- SEASON
create table season (
  id bigserial primary key,
  name varchar(120) not null,
  status varchar(20) not null,
  created_at timestamptz not null default now()
);

-- CONTESTANT  (Contestant.seasonId) :contentReference[oaicite:3]{index=3}
create table contestant (
  id bigserial primary key,
  season_id bigint not null references season(id),
  name varchar(120) not null,
  status varchar(20) not null,
  created_at timestamptz not null default now()
);

create index idx_contestant_season on contestant(season_id);

-- ROUND  (Round.seasonId) :contentReference[oaicite:4]{index=4}
create table round (
  id bigserial primary key,
  season_id bigint not null references season(id),
  name varchar(120) not null,
  starts_at timestamptz not null,
  ends_at timestamptz not null,
  status varchar(20) not null,
  rule_type varchar(40) not null,
  created_at timestamptz not null default now()
);

create index idx_round_season on round(season_id);
create index idx_round_status on round(status);

-- NOMINATION (Nomination.roundId, Nomination.contestantId) :contentReference[oaicite:5]{index=5}
create table nomination (
  id bigserial primary key,
  round_id bigint not null references round(id),
  contestant_id bigint not null references contestant(id),
  created_at timestamptz not null default now(),
  constraint uq_nomination unique (round_id, contestant_id)
);

create index idx_nomination_round on nomination(round_id);

-- VOTE (Vote.roundId, Vote.contestantId, Vote.voterId) :contentReference[oaicite:6]{index=6}
create table vote (
  id bigserial primary key,
  round_id bigint not null references round(id),
  contestant_id bigint not null references contestant(id),
  voter_id bigint null, -- Nivel 1: no se usa
  created_at timestamptz not null default now(),
  status varchar(20) not null,
  reject_reason varchar(200) null
);

create index idx_vote_round on vote(round_id);
create index idx_vote_round_contestant on vote(round_id, contestant_id);