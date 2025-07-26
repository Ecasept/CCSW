import { supabase } from "../utils";
import { DataPushSchema } from "../types";
import { ensureSchema, errorResponse, successResponse } from "../utils";
import { sendPushNotification } from "./notifications";

export async function update(request: Request, env: Env, ctx: ExecutionContext): Promise<Response> {
    // Parse and validate request body
    const body = await request.json();
    const res = ensureSchema(DataPushSchema, body);
    if (res.success === false) {
        return res.response;
    }
    const data = res.data;

    // Create Supabase client

    // Insert data into the database
    const { error } = await supabase
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
    const response = await sendPushNotification(data.userId, data.actions);
    if (response !== undefined) {
        // If there was an error sending the notification, return it
        return response;
    }
    // Return success response
    return successResponse();
}
