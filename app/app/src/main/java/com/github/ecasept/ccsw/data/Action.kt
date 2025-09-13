package com.github.ecasept.ccsw.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.OffsetDateTime

@Serializable
enum class PushActionType {
    @SerialName("buy")
    BUY,

    @SerialName("sell")
    SELL,

    @SerialName("missed_buy")
    MISSED_BUY,

    @SerialName("missed_sell")
    MISSED_SELL,
}

@Serializable
enum class ActionType {
    @SerialName("buy")
    BUY,

    @SerialName("sell")
    SELL,

    @SerialName("missed_buy")
    MISSED_BUY,

    @SerialName("missed_sell")
    MISSED_SELL,

    @SerialName("still_buy")
    STILL_BUY,

    @SerialName("still_sell")
    STILL_SELL,
}

fun ActionType.isMissed(): Boolean {
    return this == ActionType.MISSED_BUY || this == ActionType.MISSED_SELL
}

@Serializable
data class GeneralizedAction<ActionT>(
    @SerialName("symbol")
    val symbol: String,
    @SerialName("value")
    val value: Double,
    @SerialName("thresh")
    val threshold: Double,
    @SerialName("type")
    val type: ActionT,
)

/** Represents a limited subset of [Action] actions that can be received via FCM */
typealias PushAction = GeneralizedAction<PushActionType>
/** Represents any kind of action that can be received, eg. from the actions endpoint */
typealias Action = GeneralizedAction<ActionType>

@Serializable
data class ActionsSnapshot(
    @SerialName("actions")
    val actions: List<Action>,
    @SerialName("timestamp")
    @Serializable(with = OffsetDateTimeSerializer::class)
    val createdAt: OffsetDateTime,
)