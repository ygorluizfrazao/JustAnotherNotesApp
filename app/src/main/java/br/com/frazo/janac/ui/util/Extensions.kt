package br.com.frazo.janac.ui.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

fun Context.goToAppSettings() {
    Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", packageName, null)
    ).also { startActivity(it) }
}