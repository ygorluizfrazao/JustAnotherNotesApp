package br.com.frazo.janac.ui.util.permissions.providers

import br.com.frazo.janac.util.capitalizeWords

interface AndroidPermissionProvider : PermissionProvider<String> {
    val name: String
        get() {
            return provide().inHumanLanguage()
        }
}

private fun String.inHumanLanguage(): String {
    return this.split(".").last().replace("_", " ").capitalizeWords()
}