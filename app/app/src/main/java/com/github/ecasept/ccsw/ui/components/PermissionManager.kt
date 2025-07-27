package com.github.ecasept.ccsw.ui.screens.login

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


// You can request permissions twice, and after that the system will automatically reject the request
// If the denial took shorter than this time, it was probably auto-denied by the system
// so we can open the settings screen
const val SYSTEM_DENY_THRESHOLD = 200

enum class RequestSource {
    BUTTON, RATIONALE
}

open class PermissionManager(private val onContinue: () -> Unit, private val skipRequest: Boolean) :
    ViewModel() {

    private var startPermissionRequest = 0L
    private var requestSource: RequestSource? = null

    /** Called when the user has granted or denied the permission */
    fun onResult(granted: Boolean) {
        _permissionState.update { it.copy(askPermission = false) }
        val rs = requestSource
        requestSource = null
        if (granted) {
            Log.d("PermissionManager", "Permission granted")
            onContinue()
        } else {
            if (System.currentTimeMillis() - startPermissionRequest < SYSTEM_DENY_THRESHOLD && rs == RequestSource.RATIONALE) {
                Log.d("PermissionManager", "Permission request was auto-denied by the system")
                // The system automatically denied the request
                // So we open the settings screen
                // - to allow the user to enable the permission manually
                // - to provide feedback for the users action that led to the permission request
                // We don't want open settings if the user clicked the button to request the permission
                // as the user doesn't even know which permission was requested at that point

                _permissionState.update { it.copy(navigateToSettings = true) }
            } else {
                Log.d("PermissionManager", "Permission request was denied by the user")
                // The user denied the request, show rationale
                _permissionState.update { it.copy(showRationale = true) }
            }
        }
    }

    /** Called when the user clicks a button on the rationale dialog
     * If requested is true, the user wants to request permission again
     * If requested is false, the user wants to continue without permission
     */
    fun onRationaleClick(requested: Boolean) {
        _permissionState.update { it.copy(showRationale = false) }
        if (requested) {
            // User wants to request the permission again
            requestSource = RequestSource.RATIONALE
            requestPermission()
        } else {
            // User wants to continue without permission
            onContinue()
        }
    }

    /** Called when the user clicks a button to request the permission */
    fun onButtonClick() {
        requestSource = RequestSource.BUTTON
        requestPermission()
    }

    /** Called when the settings screen was opened */
    fun onSettingsOpened() {
        _permissionState.update { it.copy(navigateToSettings = false) }
        //
        _permissionState.update { it.copy(askPermission = true) }
    }

    /** Requests the permission, or continues without it if skipRequest is true */
    private fun requestPermission() {
        if (skipRequest) {
            onContinue()
            return
        }
        _permissionState.update { it.copy(askPermission = true) }
        startPermissionRequest = System.currentTimeMillis()
    }

    private val _permissionState = MutableStateFlow(PermissionState())
    val permissionState = _permissionState.asStateFlow()
}

class PermissionManagerFactory(
    private val onContinue: () -> Unit,
    private val skipRequest: Boolean
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PermissionManager::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PermissionManager(onContinue, skipRequest) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

data class PermissionState(
    val showRationale: Boolean = false,
    val askPermission: Boolean = false,
    val navigateToSettings: Boolean = false
)

/** Opens the app settings screen for the current application. */
fun Activity.openAppSetting() {
    Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", packageName, null)
    ).also(::startActivity)
}


/**
 * A composable that can be used when a permission is required to continue.
 *
 * @param onContinue Callback to be invoked when the permission is granted or the user chooses to continue without it.
 * @param permission The permission to request, or null if no permission is required (eg. when the permission does not exist in the current SDK version).
 * @param content Composable content that will be shown
 *
 * ### Content composable parameters:
 * - `showRationale`: Boolean indicating whether the rationale dialog should be shown.
 * - `onRationaleClick`: Callback to be invoked when the user clicks a button on the rationale dialog. Pass `true` to request the permission again, or `false` to continue without it.
 * - `onButtonClick`: Callback to be invoked when the permission should initially be requested
 */
@Composable
fun Permission(
    onContinue: () -> Unit,
    permission: String?,
    viewModel: PermissionManager = viewModel(
        factory = PermissionManagerFactory(
            onContinue,
            permission == null
        )
    ),
    content: @Composable (showRationale: Boolean, onRationaleClick: (Boolean) -> Unit, onButtonClick: () -> Unit) -> Unit
) {
    val activity = LocalActivity.current
        ?: throw IllegalStateException("LoginScreen must be used within an Activity context")

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.onResult(isGranted)
    }

    val state = viewModel.permissionState.collectAsStateWithLifecycle().value

    LaunchedEffect(key1 = state.askPermission) {
        if (state.askPermission) {
            permissionLauncher.launch(permission!!)
        }
    }
    LaunchedEffect(key1 = state.navigateToSettings) {
        if (state.navigateToSettings) {
            activity.openAppSetting()
            viewModel.onSettingsOpened()
        }
    }

    content(
        state.showRationale,
        viewModel::onRationaleClick,
        viewModel::onButtonClick
    )
}