package tech.relaycorp.letro.push

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

interface PushPermissionManager {

    fun isPermissionGranted(): Boolean
}

class PushPermissionManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : PushPermissionManager {

    override fun isPermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
}
