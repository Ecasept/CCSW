import { createClient, SupabaseClient } from "@supabase/supabase-js";
import { Action, DataPushSchema, type DataPushRequest, type DataPushResponse, type ErrorResponse } from "./types";
import z from "zod";
import { errorResponse } from "./utils";
import { sendPushNotification } from "./notify";




/** Validates the schema and returns a type-safe response,
 * or a 404 response */
function ensureSchema<T>(schema: z.ZodType<T>, data: unknown): { success: true; data: T } | { success: false; response: Response } {
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

export async function pushNotification(userId: string, actions: Action[], client: SupabaseClient) {
    const { error } = await client
        .from("notifications")
        .insert({
            user_id: userId,
            actions: actions
        });
    if (error) {
        console.error("Error inserting notification:", error);
        return errorResponse("Failed to store notification", 500);
    }
}

export async function dataPush(request: Request, env: Env, ctx: ExecutionContext): Promise<Response> {
    // Parse and validate request body
    const body = await request.json();
    const res = ensureSchema(DataPushSchema, body);
    if (res.success === false) {
        return res.response;
    }
    const data = res.data;

    // Create Supabase client
    const client = createClient(env.SUPABASE_URL, env.SUPABASE_KEY);

    // Insert data into the database
    const { error } = await client
        .from("value_history")
        .insert({
            userId: data.userId,
            timestamp: data.timestamp,
            values: data.values,
            bought: data.bought,
        });
    if (error) {
        console.error("Error inserting data:", error);
        return errorResponse("Failed to store values", 500);
    }
    // Push notifications to users
    const response = await pushNotification(data.userId, data.actions, client);
    if (response !== undefined) {
        // If there was an error sending the notification, return it
        return response;
    }
    // Return success response
    return new Response(JSON.stringify({
        success: true,
        message: "Data pushed successfully",
    } satisfies DataPushResponse), {
        status: 200,
        headers: { "Content-Type": "application/json" }
    });
}
