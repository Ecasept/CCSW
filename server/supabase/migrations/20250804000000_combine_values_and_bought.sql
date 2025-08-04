-- Add new column for combined goods structure
alter table public.value_history 
add column goods jsonb[];

-- Migrate existing data to new format
update public.value_history 
set goods = (
  select array_agg(
    jsonb_build_object(
      'value', values[i],
      'bought', bought[i]
    )
  )
  from generate_subscripts(values, 1) as i
);

-- Make the new column not null after migration
alter table public.value_history 
alter column goods set not null;

-- Drop old columns
alter table public.value_history 
drop column values,
drop column bought;
