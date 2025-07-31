create table public.profiles (
  id text not null primary key,
  fcm_token text
);

create table public.notifications (
  id uuid not null default gen_random_uuid() primary key,
  user_id text not null,
  created_at timestamp with time zone not null default now(),
  actions jsonb not null
);

alter table public.profiles
enable row level security;

alter table public.notifications
enable row level security;
