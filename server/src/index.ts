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
import { initSupabaseClient } from "./utils";
import { update } from "./api/update";
import { register } from "./api/register";
import { processImg } from "./api/process";
import { goodHistory } from "./api/goodHistory";

export default {
    async fetch(request, env, ctx): Promise<Response> {
        const url = new URL(request.url);
        switch (url.pathname) {
            case "/":
                return new Response("Hello, World!");
            case "/api/update":
                initSupabaseClient(env);
                return await update(request, env, ctx);
            case "/api/register":
                initSupabaseClient(env);
                return await register(request, env, ctx);
            case "/api/process":
                return await processImg(request, env, ctx);
            case "/api/goodHistory":
                initSupabaseClient(env);
                return await goodHistory(request, env, ctx);
            default:
                return new Response("Not Found", { status: 404 });
        }
    },
} satisfies ExportedHandler<Env>;
