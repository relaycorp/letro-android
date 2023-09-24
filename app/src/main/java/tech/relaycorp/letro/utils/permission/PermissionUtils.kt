package tech.relaycorp.letro.utils.permission

import android.Manifest
import android.os.Build
import androidx.compose.runtime.Composable
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun rememberNotificationPermissionStateCompat(
    onPermissionResult: (Boolean) -> Unit,
) = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    rememberPermissionState(
        permission = Manifest.permission.POST_NOTIFICATIONS,
        onPermissionResult = onPermissionResult,
    )
} else {
    object : PermissionState {
        override val permission: String
            get() = "Mock_For_Api_Before_33"

        override val status: PermissionStatus
            get() = PermissionStatus.Granted

        override fun launchPermissionRequest() {
            // Do nothing
        }
    }
}
