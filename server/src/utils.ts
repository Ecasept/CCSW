import { createClient, SupabaseClient } from "@supabase/supabase-js";
import { DataPushSchema, ErrorResponse } from "./types";
import { Database } from "./database.types";
import z from "zod";

export function errorResponse(message: string, status: number = 400): Response {
    return new Response(JSON.stringify({
        success: false,
        error: message,
    } satisfies ErrorResponse), {
        status,
        headers: { "Content-Type": "application/json" }
    });
}

export function successResponse(): Response {
    return new Response(JSON.stringify({
        success: true,
    }), {
        status: 200,
        headers: { "Content-Type": "application/json" }
    });
}

/** Validates the schema and returns a type-safe result,
 * or a 404 response */
export function ensureSchema<T>(schema: z.ZodType<T>, data: unknown): { success: true; data: T } | { success: false; response: Response } {
    const result = schema.safeParse(data);
    if (result.success) {
        return { success: true, data: result.data };
    } else {
        console.log("Invalid request data:", result.error.message);
        return {
            success: false,
            response: errorResponse(`Invalid request data: ${result.error.message}`)
        };
    }
}


export function createSupabaseClient(env: Env): SupabaseClient<Database> {
    return createClient<Database>(env.SUPABASE_URL, env.SUPABASE_KEY);
}

