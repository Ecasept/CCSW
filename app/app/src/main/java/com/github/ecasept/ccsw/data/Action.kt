package com.github.ecasept.ccsw.data

import org.json.JSONObject

enum class ActionType {
    SELL, BUY
}

class ActionException(override val message: String) : Exception(message)

data class Action(
    val goodId: Int,
    val value: Double,
    val threshold: Double,
    val type: ActionType,
) {
    companion object {
        fun fromJson(json: JSONObject): Action {
            val goodId = json.getInt("good")
            val value = json.getDouble("value")
            val thresh = json.getDouble("thresh")
            val typeStr = json.getString("type")
            val type = when (typeStr) {
                "sell" -> ActionType.SELL
                "buy" -> ActionType.BUY
                else -> throw ActionException("Unknown action type: $typeStr")
            }
            return Action(
                goodId = goodId,
                value = value,
                threshold = thresh,
                type = type
            )
        }
    }
}