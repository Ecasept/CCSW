package com.github.ecasept.ccsw.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.OffsetDateTime

@Serializable
data class Snapshot(
    @SerialName("goods")
    val goods: List<SnapshotEntry>,
    @Serializable(with = OffsetDateTimeSerializer::class)
    @SerialName("timestamp")
    val timestamp: OffsetDateTime,
)

@Serializable
data class SnapshotEntry(
    val value: Double,
    val bought: Boolean
)

typealias GoodHistory = List<GoodHistoryEntry>

data class GoodHistoryEntry(
    val timestamp: OffsetDateTime,
    val value: Double,
    val bought: Boolean,
)