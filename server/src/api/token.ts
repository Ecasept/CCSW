import { DataPushSchema, TokenSendSchema } from "../types";
import { ensureRequest, ensureSchema, errorResponse, successResponse, supabase } from "../utils";

export async function routeTokenRequest(request: Request, env: Env, ctx: ExecutionContext): Promise<Response> {
    switch (request.method) {
        case "POST":
            return await register(request, env, ctx);
        case "UPDATE":
            return await updateToken(request, env, ctx);
        default:
            return errorResponse("Method Not Allowed", 405);
    }
}


async function register(request: Request, env: Env, ctx: ExecutionContext): Promise<Response> {
    const res = await ensureRequest(request, "POST", TokenSendSchema);
    if (res.success === false) {
        return res.response;
    }
    const { userId, fcmToken } = res.data;

    // Insert registration data into the database
    const { error } = await supabase
        .from("profiles")
        .upsert({
            id: userId,
            fcm_token: fcmToken,
        });

    if (error) {
        console.error("Error inserting registration:", error);
        return errorResponse("Failed to register device", 500);
    }

    // Return success response
    return successResponse("Device registered successfully");
}

async function updateToken(request: Request, env: Env, ctx: ExecutionContext): Promise<Response> {
    const res = await ensureRequest(request, "UPDATE", TokenSendSchema);
    if (res.success === false) {
        return res.response;
    }
    const { userId, fcmToken } = res.data;

    // Update the FCM token in the database
    const { error } = await supabase
        .from("profiles")
        .update({ fcm_token: fcmToken })
        .eq("id", userId);

    if (error) {
        console.error("Error updating token:", error);
        return errorResponse("Failed to update token", 500);
    }

    // Return success response
    return successResponse("Token updated successfully");
}
