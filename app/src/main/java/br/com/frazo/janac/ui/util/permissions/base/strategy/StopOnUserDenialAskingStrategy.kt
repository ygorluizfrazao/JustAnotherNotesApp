package br.com.frazo.janac.ui.util.permissions.base.strategy

import android.util.Log
import androidx.compose.runtime.saveable.SaverScope
import br.com.frazo.janac.ui.util.permissions.base.requesters.PermissionRequester
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class StopOnUserDenialAskingStrategy(
    private val permissionRequester: PermissionRequester<List<String>, String>,
    private val canStart: () -> Boolean = { true },
) :
    UserDrivenAskingStrategy<Map<String, Boolean>> {

    private constructor(
        permissionRequester: PermissionRequester<List<String>, String>,
        canStart: () -> Boolean = { true },
        state: PermissionFlowStateEnum,
    ) : this(permissionRequester, canStart) {
        _flowState.value = PermissionFlowState(state, emptyMap())
    }

    private val _flowState =
        MutableStateFlow(
            PermissionFlowState(
                PermissionFlowStateEnum.NOT_STARTED,
                emptyMap<String, Boolean>()
            )
        )
    private val flowState = _flowState.asStateFlow()


    private var userManuallyDenied = false
    private var requestsMade = 0
    private var waitingSystemResponse = false
    private var waitingUserResponse = false
    private var lastPermissionMap = emptyMap<String, Boolean>()

    override fun onUserDenied() {
        waitingUserResponse = false
        userManuallyDenied = true
        assignNewState(
            PermissionFlowState(
                PermissionFlowStateEnum.TERMINAL_DENIED,
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
                        userManuallyDenied = false
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
                    if (!userManuallyDenied && !waitingUserResponse && !waitingSystemResponse) {
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

}