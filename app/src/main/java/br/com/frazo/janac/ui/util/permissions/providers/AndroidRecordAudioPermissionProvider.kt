package br.com.frazo.janac.ui.util.permissions.providers

import android.Manifest

class AndroidRecordAudioPermissionProvider : AndroidPermissionProvider {

    override fun provide(): String {
        return Manifest.permission.RECORD_AUDIO
    }
}