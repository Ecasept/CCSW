create table public.value_history (
  "userId" text not null,
  timestamp timestamp with time zone not null,
  values
    double precision[] not null,
    bought boolean[] not null,
    constraint value_history_pkey primary key ("userId", "timestamp")
) TABLESPACE pg_default;
