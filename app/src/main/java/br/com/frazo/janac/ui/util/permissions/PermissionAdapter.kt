package br.com.frazo.janac.ui.util.permissions

interface PermissionAdapter<P> {

    fun onRequestPermission()

    fun onPermissionGranted()

    fun onPermissionDeniedBySystem()

    fun onPermissionDeniedByUser()

    fun onPermissionManuallyRequested()

    fun getTargetPermission(): P
}