package com.github.ecasept.ccsw.data.preferences

/**
 * Data class representing the application preferences
 */
data class AppPreferences(
    val instanceId: String?,
    val sessionToken: String?,
    val serverUrl: String,
)
