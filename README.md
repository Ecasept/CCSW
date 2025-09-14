<h1 align="center"><img src="https://github.com/Ecasept/CCSW/blob/main/app/app/src/main/res/cookie_icon.svg" width=64 height=64"> <span>Cookie Clicker Stock Watcher</span></h1>

# About
Have you ever wanted to get the Gaseous Assets achievement in Cookie Clicker but not spend your whole day staring at the stock market waiting for prices to change? Then this project is for you!

Cookie Clicker Stock Watcher consists of a client side analyzer that periodically takes screenshots of your game and analyzes which stocks you should buy or sell, a (possibly self hosted) server, and an android app that can notify you whenever you need to run to your computer to complete a purchase or sale! Everything works fully automated, and you can't say this is cheating because the program does not issue any inputs to the game itself, and does not use methods like extracting the html from the page to get the current prices.

# Usage

## Analyzer
On the first run, the program will simulate the stock market to get average values for it, which will later be used to recommend specific actions. The simulation will only run the first time you start the analyzer. After you start the analyzer (and the simulation is completed), it will instantly take a screenshot of your computer. Make sure you have Cookie Clicker opened and the stock market visible. The program will print a URL that you should visit. Here you will see the details of the instance that you have configured.
- If there is no instance configured, you will be shown options for creating a new instance or logging in to an already existing one. Follow the instructions to continue.
- If you have already configured an instance, the analyzer will have tried to log in when it was started, and the results will be shown on the website.
If the login was successful, you should be able to continue to the bounds selection. Here you will see the screenshot that was taken earlier. Select the region of your screen where the different values of the stocks are displayed, and click submit. After this, the analyzer will immediately start screenshotting the area, processing it and uploading the data. You can take a look at the output of the program to see what it is currently doing.

## App
You need to download the app or build it yourself. Depending on who builds the app, it will default to a different server URL, but it can always be changed. When you open the app, you will need to enter the access code that you were provided with when the instance was created, as well as the ID of the instance. If you want to use an instance on a different server than the one the app was built for, you can change the server URL. Once you log in, the device will register with the instance and receive notifications for important updates. You can also view the history of your goods right in the app.

# Installation

If someone is hosting this project, you can use their instance:
- Configure your preferred settings in `config.jsonc`
- Set the `serverUrl` value to their domain
- Install the analyzer dependencies using [pyproject.toml](https://peps.python.org/pep-0621/):
    ```sh
    cd analyzer
    pip install .
    ```
- Build the app in `app`, or get a prebuilt app with the correct server URL automatically configured
- Run the analyzer with:
    ```sh
    python analyzer/main.py
    ```


# Hosting yourself (haven't tested these instructions, some steps might be missing)
- Create
    - a new cloudflare worker
    - supabase project
    - firebase project
- Set the `name` property in `server/wrangler.jsonc` to the name of your cloudflare worker
- Create `server/.env.local` and put your supabase project url in it, following `server/.env.local.example`
- Create `server/.dev.vars` and put your supabase project url and secret key in it, following `server/.dev.vars.example`
    - To create the secret key:
    - Go to your supabase project dashboard
    - Go to Project Settings > API Keys
    - You might need to switch to the API Keys tab from Legacy API Keys
    - Click "Add new API Key", enter the details and click "Create API Key"
    - Copy the key and put it into the file
- Execute `npx wrangler secret put SUPABASE_KEY` and `npx wrangler secret put SUPABASE_URL` and for each enter what you entered into the `.dev.vars` file respectively
- Now do the same for `JWT_SECRET` and ensure you generate the secret in a secure and random way, eg. using `openssl`.
- Create a new API key in the Google AI Studio and add it to the `server/.dev.vars` file and save it to wrangler with `AI_STUDIO_API_KEY`
- Configure your preferred settings in `config.jsonc`
- Set the `serverUrl` property to the url of the cloudflare worker
- Deploy the cloudflare worker with `npx wrangler deploy`
- Run `npx supabase link` to link your workspace with your remote project
- Deploy the supabase edge functions with `npx supabase functions deploy`
- Deploy the supabase database with `npx supabase db push`, or `npx supabase db reset --linked` if you have previously already pushed to the db.
- Add your app to firebase
- Download the `google-services.json` file and put it at `app/app/google-services.json`
- Create a new service account, ensure it has the necessary permissions, and put the `service-account.json` file at `server/supabase/functions/service-account.json`
- Build the app in `app` for a version of the app with your server url as the default




# Why have the Cloudflare Worker when you could use Supabase Edge Functions
- Cloudflare offers a more generous free plan
- Cloudflare offers more customizability for the domain
    - Cloudflare: https://<custom>.<username>.workers.dev/<custom>
    - Supabase: https://<project-id>.supabase.co/functions/v1/<custom>
- I ran into issues getting the Supabase edge runtime and docker containers to work locally
- Allows the supabase notification endpoint to be unsecured

# Why use pycurl instead of requests
I had SSL problems using the `requests` library (it would not recognize that the hostname matched, I suspect there is a bug in the library with subdomain wildcards).


# Problems
This project is not important and a priority for me. It started as a joke and I only continued it because I realized that I could learn a few things from it. That is why there are some flaws that I don't think I will fix, as the time I would need to invest far outweighs any benefit I would gain from doing so.

- The flow for setting up the project is bad. Ideally the user starts the app and opens the web interface. Then everything can be controlled from there. The current structure makes this difficult. Implementing this would require:
    - Separating the server for the web interface and the actual analyzer
    - Using a real js framework instead of raw html
- I have my own instance hosted at `cookie-clicker-stock-watcher.ecasept.workers.dev`, but the supabase side will be paused after a while and therefore unavailable
- The notification endpoint for the supabase backend is open to everyone. As long as the supabase project id is not exposed, this should not be a problem.
- Session tokens are valid indefinitely. Ideally they should expire after a while and the app or analyzer would respond to the expiration. Another part of this would be making access tokens only valid for like 5 minutes so you can share them. There would be an interface in the analyzer to create a new session token.

# License
This project is licensed under the GNU General Public License v3.0 or later.
