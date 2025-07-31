import { RegisterSchema } from "../types";
import { ensureRequest, ensureSchema, errorResponse, successResponse, supabase } from "../utils";

export async function register(request: Request, env: Env, ctx: ExecutionContext): Promise<Response> {
    const res = await ensureRequest(request, "POST", RegisterSchema);
    if (res.success === false) {
        return res.response;
    }
    const { userId, fcmToken } = res.data;

    // Insert registration data into the database
    const { error } = await supabase
        .from("profiles")
        .insert({
            id: userId,
            fcm_token: fcmToken,
        });

    if (error) {
        console.error("Error inserting registration:", error);
        return errorResponse("Failed to register device", 500);
    }

    // Return success response
    return successResponse();
}
