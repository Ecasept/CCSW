# Installation/Usage
If someone is hosting this project, you can use their instance:
- Configure your preferred settings in `config.jsonc`
- Set the `serverUrl` value to their domain
- Install the requirements from `analyzer/requirements.txt`
- Execute `analyzer/main.py` for the monitoring
- Build the app in `app`


# Hosting yourself (haven't tested these instructions, some steps might be missing)
- Create
    - a new cloudflare worker
    - supabase project
    - firebase project
- Set the `name` property in `server/wrangler.jsonc` to the name of your cloudflare worker
- Create `server/.env.local` at put your supabase project url in it, following `server/.env.local.example`
- Create `server/.dev.vars` and put your supabase project url and secret key init, following `server/.dev.vars.example`
    - To create the secret key:
    - Go to your supabase project dashboard
    - Go to Project Settings > API Keys
    - You might need to switch to the API Keys tab from Legacy API Keys
    - Click "Add new API Key", enter the details and click "Create API Key"
    - Copy the key and put it into the file
- Create a new API key in the google a ai studio and add id to the `server/.dev.vars` file
- Execute `npx wrangler secret put SUPABASE_KEY` and `npx wrangler secret put SUPABASE_URL` and for each enter what you entered into the `.dev.vars` file respectively
- Configure your preferred settings in `config.jsonc`
- Set the `serverUrl` property to the url of the cloudflare worker
- Deploy the cloudflare worker with `npx wrangler deploy`
- Run `npx supabase link` to link your workspace with your remote project
- Deploy the supabase edge functions with `npx supabase functions deploy`
- Deploy the supabase database with `npx supabase db push`, or `npx supabse db reset --linked` if you have previously already pushed to the db.
- Add your app to firebase
- Download the `google-services.json` file and put it at `app/app/google-services.json`
- Create a new service account, ensure it has the necessary permissions, and put the `service-account.json` file at `server/supabase/functions/service-account.json`
- Install the requirements from `analyzer/requirements.txt`
- Execute `analyzer/main.py` for the monitoring
- Build the app in `app`




# Why have the Cloudflare Worker when you could use Supabase Edge Functions
- Cloudflare offers a more generous free plan
- Cloudflare offers more customizability for the domain
    - Cloudflare: https://<custom>.<username>.workers.dev/<custom>
    - Supabase: https://<project-id>.supabase.co/functions/v1/<custom>
- I ran into issues getting the Supabase edge runtime and docker containers to work locally

# Why use pycurl instead of requests
I had SSL problems using the `requests` library (it would not recognize that the hostname matched, I suspect there is a bug in the library with subdomain wildcards).
