package com.github.ecasept.ccsw.fcm

import android.util.Log
import com.github.ecasept.ccsw.data.Action
import com.github.ecasept.ccsw.data.ActionException
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.json.JSONArray
import org.json.JSONException
import java.time.OffsetDateTime
import java.time.format.DateTimeParseException

const val TAG = "CCSWFirebaseMessagingService"


class CCSWFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val data = remoteMessage.data

        val createdAtStr = data["created_at"]
        val notificationUUID = data["id"]
        val actionsStr = data["actions"]
        if (createdAtStr == null || notificationUUID == null || actionsStr == null) {
            Log.e(TAG, "Received message with missing fields: $data")
            return
        }

        val createdAt: OffsetDateTime
        try {
            createdAt = OffsetDateTime.parse(createdAtStr)
        } catch (e: DateTimeParseException) {
            Log.e(TAG, "Invalid created_at date: $createdAtStr", e)
            return
        }

        val actions: List<Action>
        try {
            val actionsJson = JSONArray(actionsStr)
            actions = List(actionsJson.length()) { index ->
                Action.fromJson(actionsJson.getJSONObject(index))
            }
        } catch (e: JSONException) {
            Log.e(TAG, "Failed to parse actions: $actionsStr", e)
            return
        } catch (e: ActionException) {
            Log.e(TAG, "Failed to parse actions - ${e.message}", e)
            return
        }

        for (action in actions) {
            showActionNotification(action, notificationUUID, this)
        }
    }
    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
        // Here you can send the token to your server if needed
        // For example, using a repository or a data store
    }
}
