package br.com.frazo.janac.ui.util.permissions.providers.android

import android.Manifest

class AndroidRecordAudioPermissionProvider : AndroidPermissionProvider {
    override fun provide(): List<String> {
        return listOf( Manifest.permission.RECORD_AUDIO)
    }
}