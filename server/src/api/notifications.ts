import { Action, DataPushSchema, InstanceId } from "../types";
import { errorResponse, supabase } from "../utils";

export async function sendPushNotification(instanceId: InstanceId, actions: Action[]) {
    const { error } = await supabase
        .from("notifications")
        .insert({
            instance_id: instanceId,
            actions: actions
        });
    if (error) {
        console.error("Error inserting notification:", error);
        return errorResponse("Failed to store notification", 500);
    }
}
