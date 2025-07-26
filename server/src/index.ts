/**
 * Welcome to Cloudflare Workers! This is your first worker.
 *
 * - Run `npm run dev` in your terminal to start a development server
 * - Open a browser tab at http://localhost:8787/ to see your worker in action
 * - Run `npm run deploy` to publish your worker
 *
 * Bind resources to your worker in `wrangler.jsonc`. After adding bindings, a type definition for the
 * `Env` object can be regenerated with `npm run cf-typegen`.
 *
 * Learn more at https://developers.cloudflare.com/workers/
 */

import { SupabaseClient } from "@supabase/supabase-js";
import { Database } from "./database.types";
import { createSupabaseClient } from "./utils";
import { update } from "./api/update";
import { register } from "./api/register";

export let supabase: SupabaseClient<Database>;

export default {
    async fetch(request, env, ctx): Promise<Response> {
        const url = new URL(request.url);
        switch (url.pathname) {
            case "/":
                return new Response("Hello, World!");
            case "/api/update":
                supabase = createSupabaseClient(env);
                return update(request, env, ctx);
            case "/api/register":
                supabase = createSupabaseClient(env);
                return register(request, env, ctx);
            default:
                return new Response("Not Found", { status: 404 });
        }
    },
} satisfies ExportedHandler<Env>;
