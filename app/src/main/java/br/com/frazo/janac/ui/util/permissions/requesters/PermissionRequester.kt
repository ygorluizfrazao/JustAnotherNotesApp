package br.com.frazo.janac.ui.util.permissions.requesters

import br.com.frazo.janac.ui.util.permissions.providers.PermissionProvider

interface PermissionRequester<D,R> {

    val permissionProvider: PermissionProvider<D>

    fun ask(resultCallback: (Map<R, Boolean>) -> Unit)

    fun permissionsStatus(): Map<R, Boolean>

}