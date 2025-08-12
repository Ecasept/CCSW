package com.github.ecasept.ccsw.fcm

import android.app.NotificationManager
import android.content.Context
import android.graphics.drawable.Icon
import android.util.Log
import androidx.core.app.NotificationCompat
import com.github.ecasept.ccsw.R
import com.github.ecasept.ccsw.data.Action
import com.github.ecasept.ccsw.data.ActionType
import com.github.ecasept.ccsw.data.getGood
import com.github.ecasept.ccsw.data.id

const val ACTION_NOTIFICATION_CHANNEL_ID = "action"
const val ACTION_NOTIFICATION_CHANNEL_NAME = "Actions"
const val ACTION_NOTIFICATION_GROUP_ID = "action_group"

fun showActionNotification(action: Action, context: Context) {
    val good = getGood(action.symbol)
    if (good == null) {
        Log.e("Notification", "No good found for symbol: ${action.symbol}")
        return
    }
    val id = good.id
    val (building, name, symbol, res) = good

    val text = when (action.type) {
        ActionType.BUY -> "$name has fallen to ${action.value}$, passing the threshold of ${action.threshold}$"
        ActionType.SELL -> "$name has risen to ${action.value}$, passing the threshold of ${action.threshold}$"
        ActionType.MISSED_BUY -> "$name risen above the threshold of ${action.threshold}$"
        ActionType.MISSED_SELL -> "$name fallen below the threshold of ${action.threshold}$"
    }
    val title = when (action.type) {
        ActionType.SELL -> "Sell $symbol"
        ActionType.BUY -> "Buy $symbol"
        ActionType.MISSED_BUY -> "Missed Buying $symbol"
        ActionType.MISSED_SELL -> "Missed Selling $symbol"
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
            .build()

    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    notificationManager.notify(id, notification)
}
