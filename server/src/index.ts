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

import { errorResponse, initSupabaseClient } from "./utils";
import { update } from "./api/update";
import { processImg } from "./api/process";
import { goodHistory } from "./api/goodHistory";
import { createInstance } from "./api/auth/instance";
import { createSession } from "./api/auth/session";
import { addDeviceToken } from "./api/token";

export default {
    async fetch(request, env, ctx): Promise<Response> {
        const url = new URL(request.url);
        switch (url.pathname) {
            case "/":
                return new Response("Hello, World!");
            case "/api/update":
                initSupabaseClient(env);
                return await update(request, env, ctx);
            case "/api/token":
                initSupabaseClient(env);
                return await addDeviceToken(request, env, ctx);
            case "/api/process":
                return await processImg(request, env, ctx);
            case "/api/goodHistory":
                initSupabaseClient(env);
                return await goodHistory(request, env, ctx);
            case "/api/auth/instance":
                initSupabaseClient(env);
                return await createInstance(request, env, ctx)
            case "/api/auth/session":
                initSupabaseClient(env);
                return await createSession(request, env, ctx)
            default:
                return errorResponse("Not Found", 404);
        }
    },
} satisfies ExportedHandler<Env>;
