package org.futo.circles.feature.settings

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.futo.circles.auth.feature.log_in.log_out.LogoutDataSource
import org.futo.circles.auth.feature.token.RefreshTokenManager
import org.futo.circles.core.base.SingleEventLiveData
import org.futo.circles.core.extensions.Response
import org.futo.circles.core.extensions.launchBg
import org.futo.circles.core.feature.workspace.SharedCircleDataSource
import org.futo.circles.core.provider.MatrixSessionProvider
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsDataSource: SettingsDataSource,
    private val logoutDataSource: LogoutDataSource,
    private val sharedCircleDataSource: SharedCircleDataSource,
    private val refreshTokenManager: RefreshTokenManager
) : ViewModel() {

    val profileLiveData = settingsDataSource.profileLiveData
    val passPhraseLoadingLiveData = settingsDataSource.passPhraseLoadingLiveData
    val startReAuthEventLiveData = settingsDataSource.startReAuthEventLiveData
    val logOutLiveData = SingleEventLiveData<Response<Unit?>>()
    val deactivateLiveData = SingleEventLiveData<Response<Unit?>>()
    val navigateToMatrixChangePasswordEvent = SingleEventLiveData<Unit>()
    val changePasswordResponseLiveData = SingleEventLiveData<Response<Unit?>>()

    fun logOut() {
        launchBg {
            MatrixSessionProvider.currentSession?.let { refreshTokenManager.cancelTokenRefreshing(it) }
            val result = logoutDataSource.logOut()
            logOutLiveData.postValue(result)
        }
    }

    fun deactivateAccount() {
        launchBg {
            MatrixSessionProvider.currentSession?.let { refreshTokenManager.cancelTokenRefreshing(it) }
            val deactivateResult = settingsDataSource.deactivateAccount()
            deactivateLiveData.postValue(deactivateResult)
        }
    }

    fun handleChangePasswordFlow() {
        launchBg {
            when (settingsDataSource.changePasswordUIA()) {
                is Response.Error -> navigateToMatrixChangePasswordEvent.postValue(Unit)
                is Response.Success -> createNewBackupInNeeded()
            }
        }
    }

    private suspend fun createNewBackupInNeeded() {
        val createBackupResult = settingsDataSource.createNewBackupIfNeeded()
        changePasswordResponseLiveData.postValue(createBackupResult)
    }

    fun getSharedCircleSpaceId(): String? = sharedCircleDataSource.getSharedCirclesSpaceId()
}