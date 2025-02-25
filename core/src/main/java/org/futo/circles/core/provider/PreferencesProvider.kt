package org.futo.circles.core.provider

import android.content.Context
import android.content.Context.MODE_PRIVATE
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import org.futo.circles.core.model.CircleRoomTypeArg
import javax.inject.Inject

class PreferencesProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private fun getSharedPreferences() =
        context.getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE)

    fun setDeveloperMode(isEnabled: Boolean) {
        getSharedPreferences().edit { putBoolean(DEV_MODE_KEY, isEnabled) }
    }

    fun isDeveloperModeEnabled(): Boolean = getSharedPreferences().getBoolean(DEV_MODE_KEY, false)

    fun getFcmToken(): String? {
        return getSharedPreferences().getString(FCM_TOKEN, null)
    }

    fun storeFcmToken(token: String?) {
        getSharedPreferences().edit { putString(FCM_TOKEN, token) }
    }

    fun getEndpoint(): String? {
        return getSharedPreferences().getString(ENDPOINT_OR_TOKEN, null)
    }

    fun storeUpEndpoint(endpoint: String?) {
        getSharedPreferences().edit { putString(ENDPOINT_OR_TOKEN, endpoint) }
    }

    fun getPushGateway(): String? {
        return getSharedPreferences().getString(PUSH_GATEWAY, null)
    }

    fun storePushGateway(gateway: String?) {
        getSharedPreferences().edit { putString(PUSH_GATEWAY, gateway) }
    }

    fun setFdroidBackgroundSyncEnabled(isEnabled: Boolean) {
        getSharedPreferences().edit { putBoolean(FDROID_BACKGROUND_SYNC, isEnabled) }
    }

    fun isFdroidBackgroundSyncEnabled(): Boolean =
        getSharedPreferences().getBoolean(FDROID_BACKGROUND_SYNC, true)


    fun setShouldShowAllExplanations(shouldShow: Boolean = true) {
        setShouldShowExplanation(shouldShow, CircleRoomTypeArg.Circle)
        setShouldShowExplanation(shouldShow, CircleRoomTypeArg.Group)
    }

    fun setShouldShowExplanation(shouldShow: Boolean, roomType: CircleRoomTypeArg) {
        getSharedPreferences().edit {
            putBoolean(
                if (roomType == CircleRoomTypeArg.Circle) SHOULD_SHOW_CIRCLES_EXPLANATION
                else SHOULD_SHOW_GROUPS_EXPLANATION,
                shouldShow
            )
        }
    }

    fun shouldShowExplanation(roomType: CircleRoomTypeArg): Boolean =
        getSharedPreferences().getBoolean(
            if (roomType == CircleRoomTypeArg.Circle) SHOULD_SHOW_CIRCLES_EXPLANATION
            else SHOULD_SHOW_GROUPS_EXPLANATION,
            true
        )

    companion object {
        private const val PREFERENCES_NAME = "circles_preferences"
        private const val DEV_MODE_KEY = "developer_mode"
        private const val FCM_TOKEN = "fcm_token"
        private const val ENDPOINT_OR_TOKEN = "unified_push_endpoint_or_token"
        private const val PUSH_GATEWAY = "push_gateway"
        private const val FDROID_BACKGROUND_SYNC = "fdroid_background_sync"
        private const val SHOULD_SHOW_CIRCLES_EXPLANATION = "should_show_circles_explanation"
        private const val SHOULD_SHOW_GROUPS_EXPLANATION = "should_show_groups_explanation"
    }
}