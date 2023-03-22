package br.com.frazo.janac.ui.util.permissions.base.strategy

import android.util.Log
import androidx.compose.runtime.Composable
import br.com.frazo.janac.ui.util.permissions.base.requesters.android.AndroidPermissionRequester
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface PermissionAskingStrategy<D> {
    fun resolveState()
    fun flowState(): StateFlow<PermissionFlowState<D>>
}

abstract class AndroidPermissionAskingStrategy(
    protected val androidPermissionRequester: AndroidPermissionRequester,
    protected val canStart: () -> Boolean = { true },
    protected val initialStateContent: @Composable () -> Unit,
    protected val rationalePrompt: @Composable (state: PermissionFlowStateEnum, permissionsStatus: Map<String, Boolean>, callMeWhen: RationaleCallback) -> Unit,
    protected val terminalStateContent: @Composable (state: PermissionFlowStateEnum, permissionsStatus: Map<String, Boolean>) -> Unit
) :
    PermissionAskingStrategy<@Composable () -> Unit>

data class RationaleCallback(
    val requestedUserManualGrant: () -> Unit,
    val manuallyDeniedByUser: () -> Unit,
)

enum class AskingStrategy {
    KEEP_ASKING, ONLY_ASK_SYSTEM, STOP_ASKING_ON_USER_DENIAL
}


fun androidPermissionAskingStrategyFactory(
    type: AskingStrategy,
    androidPermissionRequester: AndroidPermissionRequester,
    canStart: () -> Boolean = { true },
    initialStateContent: @Composable () -> Unit,
    rationalePrompt: @Composable (state: PermissionFlowStateEnum, permissionsStatus: Map<String, Boolean>, callMeWhen: RationaleCallback) -> Unit,
    terminalStateContent: @Composable (state: PermissionFlowStateEnum, permissionsStatus: Map<String, Boolean>) -> Unit
): AndroidPermissionAskingStrategy {
    return when (type) {
        AskingStrategy.STOP_ASKING_ON_USER_DENIAL -> StopOnUserDenial(
            androidPermissionRequester,
            canStart,
            initialStateContent,
            rationalePrompt,
            terminalStateContent
        )
        else ->
            StopOnUserDenial(
                androidPermissionRequester,
                canStart,
                initialStateContent,
                rationalePrompt,
                terminalStateContent
            )
    }
}

class StopOnUserDenial(
    androidPermissionRequester: AndroidPermissionRequester,
    canStart: () -> Boolean = { true },
    initialStateContent: @Composable () -> Unit,
    rationalePrompt: @Composable (state: PermissionFlowStateEnum, permissionsStatus: Map<String, Boolean>, callMeWhen: RationaleCallback) -> Unit,
    terminalStateContent: @Composable (state: PermissionFlowStateEnum, permissionsStatus: Map<String, Boolean>) -> Unit
) :
    AndroidPermissionAskingStrategy(
        androidPermissionRequester,
        canStart,
        initialStateContent,
        rationalePrompt,
        terminalStateContent
    ) {

    private val _flowState =
        MutableStateFlow(
            PermissionFlowState(
                PermissionFlowStateEnum.NOT_STARTED,
                initialStateContent
            )
        )
    private val flowState = _flowState.asStateFlow()
    private var lastPermissionMap = mapOf<String, Boolean>()


    private var userManuallyDenied = false
    private var requestsMade = 0
    private var waitingSystemResponse = false
    private var waitingUserResponse = false

    override fun flowState(): StateFlow<PermissionFlowState<() -> Unit>> = flowState

    override fun resolveState() {

        lastPermissionMap = androidPermissionRequester.permissionsStatus()

        if (lastPermissionMap.filter { (_, isGranted) ->
                !isGranted
            }.isEmpty() && _flowState.value.state != PermissionFlowStateEnum.TERMINAL_GRANTED) {
            assignNewState(
                PermissionFlowState(
                    PermissionFlowStateEnum.TERMINAL_GRANTED,
                    prepareTerminalComposable(
                        PermissionFlowStateEnum.TERMINAL_GRANTED,
                        lastPermissionMap
                    )
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
                                initialStateContent
                            )
                        )
                    }
                }

                PermissionFlowStateEnum.STARTED -> {
                    if (requestsMade == 0) {
                        if (!waitingSystemResponse) {
                            waitingSystemResponse = true
                            androidPermissionRequester.ask { permissionsMap ->
                                waitingSystemResponse = false
                                requestsMade++
                                lastPermissionMap = permissionsMap
                                val notGranted = permissionsMap.filter { (_, isGranted) ->
                                    !isGranted
                                }
                                if (notGranted.isEmpty()) {
                                    assignNewState(
                                        PermissionFlowState(
                                            PermissionFlowStateEnum.TERMINAL_GRANTED,
                                            prepareTerminalComposable(
                                                PermissionFlowStateEnum.TERMINAL_GRANTED,
                                                permissionsMap
                                            )
                                        )
                                    )
                                }
                            }
                        }
                    } else {
                        assignNewState(flowState.value.copy(state = PermissionFlowStateEnum.DENIED_BY_SYSTEM))
                    }
                }

                PermissionFlowStateEnum.DENIED_BY_SYSTEM, PermissionFlowStateEnum.SHOW_RATIONALE -> {
                    if (!userManuallyDenied && !waitingUserResponse && !waitingSystemResponse) {
                        waitingUserResponse = true
                        val rationaleComposable: @Composable () -> Unit = {
                            initialStateContent()
                            rationalePrompt(
                                PermissionFlowStateEnum.DENIED_BY_SYSTEM,
                                lastPermissionMap,
                                RationaleCallback(
                                    requestedUserManualGrant = {
                                        waitingUserResponse = false
                                        assignNewState(
                                            PermissionFlowState(
                                                PermissionFlowStateEnum.NOT_STARTED,
                                                initialStateContent
                                            )
                                        )
                                    },
                                    manuallyDeniedByUser = {
                                        waitingUserResponse = false
                                        userManuallyDenied = true
                                        assignNewState(
                                            PermissionFlowState(
                                                PermissionFlowStateEnum.TERMINAL_DENIED,
                                                prepareTerminalComposable(
                                                    PermissionFlowStateEnum.TERMINAL_DENIED,
                                                    lastPermissionMap
                                                )
                                            )
                                        )
                                    })
                            )
                        }
                        assignNewState(
                            PermissionFlowState(
                                PermissionFlowStateEnum.SHOW_RATIONALE,
                                rationaleComposable
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
                                initialStateContent
                            )
                        )
                    }
                }

                PermissionFlowStateEnum.TERMINAL_DENIED -> {
                }
            }
        }

    }

    private fun prepareTerminalComposable(
        state: PermissionFlowStateEnum,
        permissionsStatus: Map<String, Boolean>
    ): @Composable () -> Unit {

        val composable: @Composable () -> Unit = {
            terminalStateContent(
                state,
                permissionsStatus
            )
        }
        return composable
    }

    private fun assignNewState(state: PermissionFlowState<@Composable () -> Unit>) {
        _flowState.value = state
        Log.d("New Flow State: ", state.toString())
    }
}