package br.com.frazo.janac.ui.util.permissions.base.providers

interface PermissionProvider<out P> {

    fun provide(): P

}