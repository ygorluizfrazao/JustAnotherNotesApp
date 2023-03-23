package br.com.frazo.janac.ui.util.permissions.base.strategy

interface UserDrivenAskingStrategy<D> :
    PermissionAskingStrategy<D>{

    fun onUserManuallyDenied()
    fun onRequestedUserManualGrant()

}