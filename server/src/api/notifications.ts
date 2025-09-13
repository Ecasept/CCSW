import { Action, InstanceId } from "../types";
import { errorResponse, supabase } from "../utils";

export async function sendPushNotification(instanceId: InstanceId, actions: Action[]) {
    // Only notify for new actions
    const notifyActions = actions.filter(a =>
        a.type === 'buy' || a.type === 'sell' || a.type === 'missed_buy' || a.type === 'missed_sell'
    );

    if (notifyActions.length === 0) {
        return; // Nothing to notify
    }

    const { error } = await supabase
        .from("notifications")
        .insert({
            instance_id: instanceId,
            actions: notifyActions
        });
    if (error) {
        console.error("Error inserting notification:", error);
        return errorResponse("Failed to store notification", 500);
    }
}
