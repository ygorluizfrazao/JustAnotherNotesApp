package br.com.frazo.janac.ui.util.permissions.base.requesters

import br.com.frazo.janac.ui.util.permissions.base.providers.PermissionProvider

interface PermissionRequester<D,R> {

    val permissionProvider: PermissionProvider<D>

    fun ask(resultCallback: (Map<R, Boolean>) -> Unit)

    fun permissionsStatus(): Map<R, Boolean>

}