package br.com.frazo.janac.ui.util.permissions.base.strategy

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import br.com.frazo.janac.ui.util.permissions.base.requesters.PermissionRequester
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class KeepAskingStrategy(
    private val permissionRequester: PermissionRequester<List<String>, String>,
    private val canStart: () -> Boolean = { true },
) :
    UserDrivenAskingStrategy<Map<String, Boolean>> {

    private constructor(
        permissionRequester: PermissionRequester<List<String>, String>,
        canStart: () -> Boolean = { true },
        savedData: DataHolder
    ) : this(permissionRequester, canStart) {
        _flowState.value =
            PermissionFlowState(savedData.state, permissionRequester.permissionsStatus())
    }

    private data class DataHolder(
        val state: PermissionFlowStateEnum,
    )

    private val _flowState =
        MutableStateFlow(
            PermissionFlowState(
                PermissionFlowStateEnum.NOT_STARTED,
                emptyMap<String, Boolean>()
            )
        )

    private val flowState = _flowState.asStateFlow()

    private var requestsMade = 0
    private var waitingSystemResponse = false
    private var waitingUserResponse = false
    private var lastPermissionMap = emptyMap<String, Boolean>()

    override fun onUserManuallyDenied() {
        waitingUserResponse = false
        assignNewState(
            PermissionFlowState(
                PermissionFlowStateEnum.NOT_STARTED,
                lastPermissionMap
            )
        )
    }

    override fun onRequestedUserManualGrant() {
        waitingUserResponse = false
        assignNewState(
            PermissionFlowState(
                PermissionFlowStateEnum.NOT_STARTED,
                lastPermissionMap
            )
        )
    }

    override fun resolveState() {
        lastPermissionMap = permissionRequester.permissionsStatus()

        if (lastPermissionMap.filter { (_, isGranted) ->
                !isGranted
            }.isEmpty() && _flowState.value.state != PermissionFlowStateEnum.TERMINAL_GRANTED) {
            assignNewState(
                PermissionFlowState(
                    PermissionFlowStateEnum.TERMINAL_GRANTED,
                    lastPermissionMap
                )
            )
        } else {
            when (flowState.value.state) {
                PermissionFlowStateEnum.NOT_STARTED -> {
                    if (canStart()) {
                        requestsMade = 0
                        assignNewState(
                            PermissionFlowState(
                                PermissionFlowStateEnum.STARTED,
                                lastPermissionMap
                            )
                        )
                    }
                }

                PermissionFlowStateEnum.STARTED -> {
                    if (requestsMade == 0) {
                        if (!waitingSystemResponse) {
                            waitingSystemResponse = true
                            permissionRequester.ask { permissionsMap ->
                                waitingSystemResponse = false
                                requestsMade++
                                val notGranted = permissionsMap.filter { (_, isGranted) ->
                                    !isGranted
                                }
                                if (notGranted.isEmpty()) {
                                    assignNewState(
                                        PermissionFlowState(
                                            PermissionFlowStateEnum.TERMINAL_GRANTED,
                                            permissionsMap
                                        )
                                    )
                                }
                            }
                        }
                    } else {
                        assignNewState(flowState.value.copy(state = PermissionFlowStateEnum.DENIED_BY_SYSTEM))
                    }
                }

                PermissionFlowStateEnum.DENIED_BY_SYSTEM, PermissionFlowStateEnum.APP_PROMPT -> {
                    if (!waitingUserResponse && !waitingSystemResponse) {
                        waitingUserResponse = true
                        assignNewState(
                            PermissionFlowState(
                                PermissionFlowStateEnum.APP_PROMPT,
                                lastPermissionMap
                            )
                        )
                    }
                }

                PermissionFlowStateEnum.TERMINAL_GRANTED -> {
                    if (lastPermissionMap.filter { (_, isGranted) ->
                            !isGranted
                        }.isNotEmpty()) {
                        assignNewState(
                            PermissionFlowState(
                                PermissionFlowStateEnum.NOT_STARTED,
                                lastPermissionMap
                            )
                        )
                    }
                }

                PermissionFlowStateEnum.TERMINAL_DENIED -> Unit
            }
        }
    }

    override fun flowState(): StateFlow<PermissionFlowState<Map<String, Boolean>>> {
        return flowState
    }

    private fun assignNewState(state: PermissionFlowState<Map<String, Boolean>>) {
        _flowState.value = state
        Log.d("New Flow State: ", state.toString())
    }

    companion object {

        @Composable
        fun rememberSavable(
            permissionRequester: PermissionRequester<List<String>, String>,
            canStart: () -> Boolean
        ): UserDrivenAskingStrategy<Map<String, Boolean>> {
            return rememberSaveable(
                permissionRequester,
                canStart,
                saver = saver(permissionRequester, canStart)
            ) {
                KeepAskingStrategy(
                    permissionRequester,
                    canStart
                )
            }
        }

        fun saver(
            permissionRequester: PermissionRequester<List<String>, String>,
            canStart: () -> Boolean
        ): Saver<KeepAskingStrategy, Any> {
            val flowStateKey = "flowStateKey"

            return mapSaver(
                save = {
                    mapOf(
                        flowStateKey to it.flowState.value.state,
                    )
                },
                restore = {
                    val data = DataHolder(
                        it[flowStateKey] as PermissionFlowStateEnum
                    )
                    KeepAskingStrategy(permissionRequester, canStart, data)
                }
            )
        }

    }

}