import { ensureRequest, supabase } from "../utils";
import { DataPushSchema } from "../types";
import { ensureSchema, errorResponse, successResponse } from "../utils";
import { sendPushNotification } from "./notifications";
import { verifyJWT } from "./auth/verify";

export async function update(request: Request, env: Env, ctx: ExecutionContext): Promise<Response> {
    const res = await ensureRequest(request, "POST", DataPushSchema);
    if (res.success === false) {
        return res.response;
    }
    const data = res.data;

    const authRes = await verifyJWT(request, env, "apiKey", data.instanceId);
    if (authRes.success === false) {
        return authRes.response;
    }


    // Insert data into the database
    const { error } = await supabase
        .from("value_history")
        .insert({
            instance_id: data.instanceId,
            timestamp: data.timestamp,
            goods: data.goods,
            actions: data.actions,
        });
    if (error) {
        console.error("Error inserting data:", error);
        return errorResponse("Failed to store values", 500);
    }
    if (data.actions.length > 0) {
        const response = await sendPushNotification(data.instanceId, data.actions);
        if (response !== undefined) {
            return response;
        }
    }
    return successResponse("Data updated successfully");
}
