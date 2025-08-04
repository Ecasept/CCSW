package com.github.ecasept.ccsw.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.OffsetDateTime

@Serializable
data class GoodHistory(
    @SerialName("items")
    val items: List<GoodHistoryItem>,
    @Serializable(with = OffsetDateTimeSerializer::class)
    @SerialName("created_at")
    val createdAt: OffsetDateTime,
)

@Serializable
data class GoodHistoryItem(
    val value: Double,
    val bought: Boolean
)