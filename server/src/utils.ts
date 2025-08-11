import { createClient, SupabaseClient } from "@supabase/supabase-js";
import { DataPushSchema, ErrorResponse, ValidationResult } from "./types";
import { Database } from "./database.types";
import z from "zod";

export function errorResponse(message: string, status: number): Response {
    return new Response(JSON.stringify({
        success: false,
        error: message,
    } satisfies ErrorResponse), {
        status,
        statusText: message,
        headers: { "Content-Type": "application/json" }
    });
}

export function successResponse(data: any): Response {
    return new Response(JSON.stringify({
        success: true,
        data: data,
    }), {
        status: 200,
        headers: { "Content-Type": "application/json" }
    });
}


/** Validates the schema and returns a type-safe result,
 * or a 404 response */
export function ensureSchema<T>(schema: z.ZodType<T>, data: unknown): ValidationResult<T> {
    const result = schema.safeParse(data);
    if (result.success) {
        return { success: true, data: result.data };
    } else {
        console.error("Schema validation error:", result.error);
        return {
            success: false,
            response: errorResponse("Bad Request", 400)
        };
    }
}

/** Ensures the request method matches the expected method.
 * Returns a type-safe result, or a 405 response if the method does not match. */
export function ensureMethod(request: Request, method: string): ValidationResult<null> {
    if (request.method !== method) {
        return {
            success: false,
            response: errorResponse("Method Not Allowed", 405)
        }
    }
    return { success: true, data: null };
}

/** Ensures the request method matches the expected method,
 * and validates the request body against the provided schema.
 * Returns a type-safe result, or an appropriate response if validation fails. */
export async function ensureRequest<T>(request: Request, method: string, schema: z.ZodType<T>): Promise<ValidationResult<T>> {
    const methodResult = ensureMethod(request, method);
    if (methodResult.success === false) {
        return methodResult;
    }
    try {
        const data = await request.json();
        return ensureSchema(schema, data);
    } catch (error) {
        console.error("Error parsing request body:", error);
        return {
            success: false,
            response: errorResponse("Invalid JSON body", 400)
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

