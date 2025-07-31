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

type ValidationResult<T> = { success: true; data: T } | { success: false; response: Response };

/** Validates the schema and returns a type-safe result,
 * or a 404 response */
export function ensureSchema<T>(schema: z.ZodType<T>, data: unknown): ValidationResult<T> {
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

/** Ensures the request method matches the expected method,
 * and validates the request body against the provided schema.
 * Returns a type-safe result, or an appropriate response if validation fails. */
export async function ensureRequest<T>(request: Request, method: string, schema: z.ZodType<T>): Promise<ValidationResult<T>> {
    if (request.method !== method) {
        return {
            success: false,
            response: new Response(null, { status: 405 })
        };
    }
    try {
        const data = await request.json();
        return ensureSchema(schema, data);
    } catch (error) {
        console.error("Error parsing request body:", error);
        return {
            success: false,
            response: errorResponse("Invalid JSON body")
        };
    }
}


export function createSupabaseClient(env: Env): SupabaseClient<Database> {
    return createClient<Database>(env.SUPABASE_URL, env.SUPABASE_KEY);
}

export let supabase: SupabaseClient<Database>;

export function initSupabaseClient(env: Env) {
    supabase = createSupabaseClient(env);
}

