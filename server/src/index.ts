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
import { actions } from "./api/actions";

const routeMap = new Map([
    ["/api/update", update],
    ["/api/token", addDeviceToken],
    ["/api/process", processImg],
    ["/api/goodHistory", goodHistory],
    ["/api/actions", actions],
    ["/api/auth/instance", createInstance],
    ["/api/auth/session", createSession],
]);

export default {
    async fetch(request, env, ctx): Promise<Response> {
        const url = new URL(request.url);
        if (url.pathname === "/") {
            return new Response("Hello, World!");
        }
        const routeHandler = routeMap.get(url.pathname);
        if (routeHandler) {
            initSupabaseClient(env);
            return await routeHandler(request, env, ctx);
        }
        return errorResponse("Not Found", 404);
    },
} satisfies ExportedHandler<Env>;
