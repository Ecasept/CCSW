package com.github.ecasept.ccsw.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
}

@Serializable
data class Action(
    @SerialName("good")
    val goodId: Int,
    @SerialName("value")
    val value: Double,
    @SerialName("thresh")
    val threshold: Double,
    @SerialName("type")
    val type: ActionType,
)