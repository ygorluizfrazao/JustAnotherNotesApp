package br.com.frazo.janac.ui.util.permissions

import br.com.frazo.janac.ui.util.permissions.providers.PermissionProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class StateBasedPermissionAdapter(private val permissionProvider: PermissionProvider<String>) :
    PermissionAdapter<PermissionProvider<String>> {

    data class PermissionAdapterState(
        val inPermissionRequisition: Boolean,
        val permissionRequisitionStatus: PermissionRequisitionStatus? = null
    )

    enum class PermissionRequisitionStatus {
        PERMISSION_GRANTED,
        PERMISSION_DENIED_BY_SYSTEM,
        PERMISSION_DENIED_BY_USER,
        PERMISSION_MANUALLY_REQUESTED,
    }

    private val _permissionAdapterState = MutableStateFlow(PermissionAdapterState(false))
    val permissionAdapterState = _permissionAdapterState.asStateFlow()

    override fun onRequestPermission() {
        _permissionAdapterState.value = PermissionAdapterState(true)
    }

    override fun onPermissionGranted() {
        _permissionAdapterState.value = _permissionAdapterState.value.copy(
            inPermissionRequisition = false,
            permissionRequisitionStatus = PermissionRequisitionStatus.PERMISSION_GRANTED
        )
    }

    override fun onPermissionDeniedBySystem() {
        _permissionAdapterState.value = _permissionAdapterState.value.copy(
            inPermissionRequisition = true,
            permissionRequisitionStatus = PermissionRequisitionStatus.PERMISSION_DENIED_BY_SYSTEM
        )
    }

    override fun onPermissionDeniedByUser() {
        _permissionAdapterState.value = _permissionAdapterState.value.copy(
            inPermissionRequisition = false,
            permissionRequisitionStatus = PermissionRequisitionStatus.PERMISSION_DENIED_BY_USER
        )
    }

    override fun onPermissionManuallyRequested() {
        _permissionAdapterState.value = _permissionAdapterState.value.copy(
            inPermissionRequisition = true,
            permissionRequisitionStatus = PermissionRequisitionStatus.PERMISSION_MANUALLY_REQUESTED
        )
    }

    override fun getTargetPermission(): PermissionProvider<String> {
        return permissionProvider
    }
}
