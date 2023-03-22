package br.com.frazo.janac.ui.util.permissions.base.strategy

import kotlinx.coroutines.flow.StateFlow

interface PermissionAskingStrategy<D> {
    fun resolveState()
    fun flowState(): StateFlow<PermissionFlowState<D>>
}

