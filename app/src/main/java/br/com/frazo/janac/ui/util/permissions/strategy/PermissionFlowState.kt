package br.com.frazo.janac.ui.util.permissions.strategy

data class PermissionFlowState<D>(
    val state: PermissionFlowStateEnum,
    val data: D
)