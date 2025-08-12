package com.github.ecasept.ccsw.fcm

import android.util.Log
import com.github.ecasept.ccsw.data.Action
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

const val TAG = "CCSWFirebaseMessagingService"

@Serializable
data class NotificationData(
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("id")
    val id: String,
    @SerialName("actions")
    val actions: List<Action>,
)

class CCSWFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "From: ${remoteMessage.from}")

        val bytes = remoteMessage.rawData
        if (bytes == null) {
            Log.w(TAG, "Received message with no data")
            return
        }

        try {
            val decoded = Json.decodeFromString<NotificationData>(String(bytes))
            for (action in decoded.actions) {
                showActionNotification(action, this)
            }
        } catch (e: SerializationException) {
            Log.e(TAG, "Failed to decode notification data", e)
            return
        }
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
        // Here you can send the token to your server if needed
        // For example, using a repository or a data store
    }
}
