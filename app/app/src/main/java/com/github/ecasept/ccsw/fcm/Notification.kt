package com.github.ecasept.ccsw.fcm

import android.app.NotificationManager
import android.content.Context
import android.graphics.drawable.Icon
import android.util.Log
import androidx.core.app.NotificationCompat
import com.github.ecasept.ccsw.R
import com.github.ecasept.ccsw.data.PushAction
import com.github.ecasept.ccsw.data.PushActionType
import com.github.ecasept.ccsw.data.getGood
import com.github.ecasept.ccsw.data.id
import com.github.ecasept.ccsw.utils.formatPrice

const val ACTION_NOTIFICATION_CHANNEL_ID = "action"
const val ACTION_NOTIFICATION_CHANNEL_NAME = "Actions"
const val ACTION_NOTIFICATION_GROUP_ID = "action_group"

fun showActionNotification(action: PushAction, context: Context) {
    val good = getGood(action.symbol)
    if (good == null) {
        Log.e("Notification", "No good found for symbol: ${action.symbol}")
        return
    }
    val id = good.id
    val (building, name, symbol, res) = good

    val thresholdStr = formatPrice(action.threshold)
    val valueStr = formatPrice(action.value)

    val text = when (action.type) {
        PushActionType.BUY -> "$name has fallen to ${valueStr}$, passing the threshold of ${thresholdStr}$"
        PushActionType.SELL -> "$name has risen to ${valueStr}$, passing the threshold of ${thresholdStr}$"
        PushActionType.MISSED_BUY -> "$name was above the threshold of ${thresholdStr}$"
        PushActionType.MISSED_SELL -> "$name was  below the threshold of ${thresholdStr}$"
    }
    val title = when (action.type) {
        PushActionType.SELL -> "Sell $symbol"
        PushActionType.BUY -> "Buy $symbol"
        PushActionType.MISSED_BUY -> "Missed Buying $symbol"
        PushActionType.MISSED_SELL -> "Missed Selling $symbol"
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
