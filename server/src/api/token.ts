import { TokenSendSchema } from "../types";
import { ensureRequest, errorResponse, successResponse, supabase } from "../utils";
import { verifyJWT } from "./auth/verify";

export async function addDeviceToken(request: Request, env: Env, ctx: ExecutionContext): Promise<Response> {
    const res = await ensureRequest(request, "POST", TokenSendSchema);
    if (res.success === false) {
        return res.response;
    }
    const { instanceId, fcmToken } = res.data;
    const authRes = await verifyJWT(request, env, "accessCode", instanceId);
    if (authRes.success === false) {
        return authRes.response;
    }

    const { error } = await supabase
        .from("devices")
        .insert({
            instance_id: instanceId,
            fcm_token: fcmToken,
        })
    if (error) {
        if (error.code = "23505") {
            return errorResponse("Duplicate token", 400)
        }
        return errorResponse("Failed to register device token", 500);
    }
    return successResponse("Device token registered successfully");
}
