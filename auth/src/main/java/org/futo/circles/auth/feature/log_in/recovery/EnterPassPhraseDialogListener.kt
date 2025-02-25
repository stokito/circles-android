package org.futo.circles.auth.feature.log_in.recovery

import android.net.Uri

interface EnterPassPhraseDialogListener {
    fun onRestoreBackupWithPassphrase(passphrase: String)

    fun onRestoreBackupWithRawKey(key: String)
    fun onRestoreBackup(uri: Uri)
    fun onDoNotRestore()
    fun onSelectFileClicked()
}