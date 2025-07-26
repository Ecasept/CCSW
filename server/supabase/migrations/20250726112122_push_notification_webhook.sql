-- Create a private schema for secure functions
create schema if not exists private;

-- Create a secure function to retrieve secrets from vault
create or replace function private.get_secret(secret_name text)
returns text
security definer
set search_path = ''
language plpgsql
as $$
declare 
   secret text;
begin
   select decrypted_secret into secret 
   from vault.decrypted_secrets 
   where name = secret_name;
   return secret;
end;
$$;

-- Create a function to set up the dynamic trigger
create or replace function private.setup_notification_trigger()
returns void
security definer
set search_path = ''
language plpgsql
as $$
declare
  project_url text;
  function_endpoint text;
  trigger_sql text;
begin
  -- Get the project URL from vault
  project_url := private.get_secret('project_url');
  function_endpoint := project_url || '/functions/v1/push';

  -- Drop existing trigger if it exists
  drop trigger if exists "fcm_forwarding" on "public"."notifications";

  -- Build the dynamic trigger SQL with the variable endpoint
  trigger_sql := format('
    create trigger "fcm_forwarding" after insert
    on "public"."notifications" for each row
    execute function "supabase_functions"."http_request"(
      %L,
      %L,
      %L,
      %L,
      %L
    )', function_endpoint, 'POST', '{"Content-Type":"application/json"}', '{}', '5000');

  -- Execute the dynamic SQL
  execute trigger_sql;
end;
$$;

-- Run the setup function to create the trigger
select private.setup_notification_trigger();
