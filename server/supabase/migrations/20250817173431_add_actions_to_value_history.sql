-- Add actions column with NOT NULL constraint and default in one step
ALTER TABLE public.value_history 
ADD COLUMN actions jsonb NOT NULL DEFAULT '[]'::jsonb;

-- Remove default after column is created
ALTER TABLE public.value_history 
ALTER COLUMN actions DROP DEFAULT;

