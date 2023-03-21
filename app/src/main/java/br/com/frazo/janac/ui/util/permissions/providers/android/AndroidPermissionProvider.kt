package br.com.frazo.janac.ui.util.permissions.providers.android

import br.com.frazo.janac.ui.util.permissions.providers.PermissionProvider
import br.com.frazo.janac.util.capitalizeWords

interface AndroidPermissionProvider : PermissionProvider<List<String>> {

    val name: String
        get() {
            return provide().map { it.inHumanLanguage() + "\n" }.reduce { acc, s ->
                acc + s
            }
        }

    private fun String.inHumanLanguage(): String {
        return this.split(".").last().replace("_", " ").capitalizeWords()
    }
}