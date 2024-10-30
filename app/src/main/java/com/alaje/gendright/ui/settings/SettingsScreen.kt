package com.alaje.gendright.ui.settings

import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.alaje.gendright.PermissionsUtils
import com.alaje.gendright.R
import com.alaje.gendright.accessibility.GendRightService
import com.alaje.gendright.ui.components.ActionButton
import com.alaje.gendright.ui.components.GendrightLogo
import kotlinx.coroutines.Job

@Composable
fun SettingsScreen() {

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = dimensionResource(id = R.dimen.padding_default))
                .verticalScroll(rememberScrollState())
        ) {

            GendrightLogo()

            Column(
                verticalArrangement = Arrangement.Center
            ) {

                ActionButton(
                    onClick = {

                    },
                    text = stringResource(id = R.string.get_started)
                )
            }


        }
    }

}

@Composable
fun ScreenContent(
    modifier: Modifier = Modifier,
) {
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val context = LocalContext.current

    var permissionCheckJob: Job? = remember { null }

    val canDrawOverApps = { Settings.canDrawOverlays(context) }

    var isFloatingWidgetEnabled by remember {
        mutableStateOf(canDrawOverApps())
    }

    val checkAccessibilityServiceState = {
        GendRightService.isAccessibilityServiceEnabled(context)
    }

    var isAccesibilityFeatureEnabled by remember {
        mutableStateOf(checkAccessibilityServiceState())
    }

    Column(
        modifier = modifier
    ) {
        SettingsItem(
            title = "Allow us to show you suggestions",
            info = "GendRight needs this permission to appear when you type in your other apps",
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null
                )
            },
            isSelected = isFloatingWidgetEnabled,
            onCheckChange = { shouldEnable ->
                // Ensure any existing coroutine is cancelled before starting a new one
                permissionCheckJob?.cancel()

                permissionCheckJob = PermissionsUtils.createDrawOverAppsPermissionJob(
                    canDrawOverApps,
                    context,
                    shouldEnable
                )

                PermissionsUtils.openPermissionToDrawOverOtherAppsSettings(context)
            }
        )

        SettingsItem(
            title = "Allow us to check text as you type",
            info = "GendRight needs access to your Android's accessibility features to check the text you type for biases",
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null
                )
            },
            isSelected = isAccesibilityFeatureEnabled,
            onCheckChange = { shouldEnable ->
                // Ensure any existing coroutine is cancelled before starting a new one
                permissionCheckJob?.cancel()

                permissionCheckJob = PermissionsUtils.createAccessibilityPermissionJob(
                    context,
                    hasAccessibilityPermission = checkAccessibilityServiceState,
                    shouldEnable,
                )

                PermissionsUtils.openAccessibilitySettings(context)

            }
        )
    }

    DisposableEffect(lifecycleOwner) {
        // Create an observer that triggers our remembered callbacks
        // for sending analytics events
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                permissionCheckJob?.cancel()
                isFloatingWidgetEnabled = canDrawOverApps()
                isAccesibilityFeatureEnabled = checkAccessibilityServiceState()
            }
        }

        // Add the observer to the lifecycle
        lifecycleOwner.lifecycle.addObserver(observer)

        // When the effect leaves the Composition, remove the observer
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

}


@Composable
fun SettingsItem(
    title: String,
    info: String,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean,
    onCheckChange: (Boolean) -> Unit
) {
    Column {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            icon()
            Text(
                text = title,
                modifier = modifier.weight(1f),
            )
            Switch(
                checked = isSelected,
                onCheckedChange = onCheckChange
            )

        }
        if (info.isNotBlank())
            Text(text = info)
    }


}

//private val gradientColors = listOf(Cyan, LightBlue, Purple /*...*/)
