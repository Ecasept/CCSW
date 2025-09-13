package com.github.ecasept.ccsw.utils

// --- Time constants & helpers ---
const val MINUTE = 60
const val HOUR = MINUTE * 60
const val DAY = HOUR * 24
const val WEEK = DAY * 7
const val MONTH = WEEK * 4
const val YEAR = MONTH * 12

/** Formats a difference in seconds into a human-readable relative time string.
 *
 * @param d How many seconds ago the event happened.
 * @return A string representing the time difference, e.g. "5m"
 */
fun formatRelative(d: Long): String {
    val format = { unit: Int -> d / unit }
    return when {
        d < MINUTE -> "now"
        d < HOUR -> "${format(MINUTE)}m"
        d < DAY -> "${format(HOUR)}h"
        d < WEEK -> "${format(DAY)}d"
        d < MONTH -> "${format(WEEK)}w"
        d < YEAR -> "${format(MONTH)}mo"
        else -> "${format(YEAR)}y"
    }
}

/** [formatRelative] with "ago" suffix. */
fun formatRelativeAgo(d: Long): String {
    val relative = formatRelative(d)
    return if (relative == "now") relative else "$relative ago"
}

val Int.m get() = this * MINUTE.toLong()
val Int.h get() = this * HOUR.toLong()
val Int.d get() = this * DAY.toLong()
val Int.w get() = this * WEEK.toLong()
val Int.mo get() = this * MONTH.toLong()
val Int.y get() = this * YEAR.toLong()
