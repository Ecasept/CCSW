package com.github.ecasept.ccsw.fcm

import android.app.NotificationManager
import android.content.Context
import android.graphics.drawable.Icon
import androidx.core.app.NotificationCompat
import com.github.ecasept.ccsw.R
import com.github.ecasept.ccsw.data.Action
import com.github.ecasept.ccsw.data.ActionType
import com.github.ecasept.ccsw.data.getGood

fun uuidToInt(uuid: String): Int {
    // TODO: Actually use db
    return uuid.hashCode() and 0x7FFFFFFF // Ensure positive integer
}

const val ACTION_NOTIFICATION_CHANNEL_ID = "action"
const val ACTION_NOTIFICATION_CHANNEL_NAME = "Actions"
const val ACTION_NOTIFICATION_GROUP_ID = "action_group"

fun showActionNotification(action: Action, uuid: String, context: Context) {
    val (building, name, symbol, res) = getGood(action.goodId)

    val text = when (action.type) {
        ActionType.SELL -> "$name has risen to ${action.value}$"
        ActionType.BUY -> "$name has fallen to ${action.value}$"
    }
    val title = when (action.type) {
        ActionType.SELL -> "Sell $symbol"
        ActionType.BUY -> "Buy $symbol"
    }

    val largeIcon = Icon.createWithResource(context, res)

    val notification =
        NotificationCompat.Builder(context, ACTION_NOTIFICATION_CHANNEL_ID).setContentTitle(title)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setLargeIcon(largeIcon)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(text)
            )
            .setGroup(ACTION_NOTIFICATION_GROUP_ID)
            .setGroupSummary(true)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setColor(context.getColor(R.color.purple_500))
            .setColorized(true)
            .build()

    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val id = uuidToInt(uuid)
    notificationManager.notify(id, notification)
}