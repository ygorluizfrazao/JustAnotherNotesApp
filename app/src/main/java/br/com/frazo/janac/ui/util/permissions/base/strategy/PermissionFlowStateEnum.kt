package br.com.frazo.janac.ui.util.permissions.base.strategy

enum class PermissionFlowStateEnum {
    NOT_STARTED, STARTED, DENIED_BY_SYSTEM, APP_PROMPT, TERMINAL_GRANTED, TERMINAL_DENIED
}