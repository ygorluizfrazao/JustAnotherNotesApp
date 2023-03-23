package br.com.frazo.janac.ui.util.permissions.base.providers.android

import br.com.frazo.janac.ui.util.permissions.base.providers.PermissionProvider
import br.com.frazo.janac.util.capitalizeWords

interface AndroidPermissionProvider : PermissionProvider<List<String>> {

    val name: String
        get() {
            return provide().map { it.toHumanLanguage() + "\n" }.reduce { acc, s ->
                acc + s
            }
        }


    companion object {

        fun String.toHumanLanguage(): String {
            return this.split(".").last().replace("_", " ").capitalizeWords()
        }

        fun of(vararg permissions: String): AndroidPermissionProvider {
            return of(permissions.toList())
        }

        fun of(permissions: List<String>): AndroidPermissionProvider {
            return object : AndroidPermissionProvider {
                val permissionsList = permissions.distinct()
                override fun provide(): List<String> {
                    return permissionsList
                }

            }
        }
    }
}