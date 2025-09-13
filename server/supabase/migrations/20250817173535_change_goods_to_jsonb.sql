-- Change goods column from jsonb[] to jsonb (single JSONB value)
-- Preserve existing data by converting the array to jsonb via to_jsonb(goods)
alter table public.value_history
alter column goods type jsonb using to_jsonb(goods);
