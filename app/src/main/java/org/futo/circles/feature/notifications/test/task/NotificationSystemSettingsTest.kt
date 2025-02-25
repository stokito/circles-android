package org.futo.circles.feature.notifications.test.task

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import org.futo.circles.R
import org.futo.circles.core.model.TaskStatus
import javax.inject.Inject

class NotificationSystemSettingsTest @Inject constructor(
    @ApplicationContext private val context: Context
) : BaseNotificationTest(R.string.settings_troubleshoot_test_system_settings_title) {

    override fun perform() {
        if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            description =
                context.getString(R.string.settings_troubleshoot_test_system_settings_success)
            quickFix = null
            status = TaskStatus.SUCCESS
        } else {
            description =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && isNotificationsPermissionGranted(
                        context
                    ).not()
                ) {
                    context.getString(R.string.settings_troubleshoot_test_system_settings_permission_failed)
                } else {
                    context.getString(R.string.settings_troubleshoot_test_system_settings_failed)
                }
            status = TaskStatus.FAILED
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun isNotificationsPermissionGranted(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }
}