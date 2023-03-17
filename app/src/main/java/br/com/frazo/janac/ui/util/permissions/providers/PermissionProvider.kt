package br.com.frazo.janac.ui.util.permissions.providers

interface PermissionProvider<out P> {

    fun provide(): P

}