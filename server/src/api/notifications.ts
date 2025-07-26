import { Action, DataPushSchema } from "../types";
import { errorResponse, supabase } from "../utils";

export async function sendPushNotification(userId: string, actions: Action[]) {
    const { error } = await supabase
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
