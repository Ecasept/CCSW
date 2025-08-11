create table
  public.instances (
    id text not null,
    created_at timestamp with time zone not null default now(),
    name text not null,
    api_key_hash text not null,
    access_code_hash text not null,
    constraint instances_pkey primary key (id)
  );

-- Enable row level security for instances
alter table public.instances enable row level security;

-- Change name of the table to devices
alter table public.profiles rename to devices;

-- Make fcm_token not nullable before using it in the primary key
alter table public.devices
alter column fcm_token set not null;

-- Remove old user_id column and add instance_id
alter table public.devices
drop column id;
alter table public.devices
add column instance_id text not null references public.instances(id) on update cascade;

-- Change primary key
alter table public.devices
add constraint devices_pkey
primary key (fcm_token, instance_id);

-- Drop old user_id column and add instance_id to notifications
alter table public.notifications
drop column user_id;
alter table public.notifications
add column instance_id text not null references public.instances(id) on update cascade;

-- Drop old userId column and add instance_id to value updates
alter table public.value_history
drop column "userId";
alter table public.value_history
add column instance_id text not null references public.instances(id) on update cascade;
