import { RegisterSchema } from "../types";
import { ensureSchema, errorResponse, successResponse, supabase } from "../utils";

export async function register(request: Request, env: Env, ctx: ExecutionContext): Promise<Response> {
    // Parse and validate request body
    const data = await request.json();
    const res = ensureSchema(RegisterSchema, data);
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
