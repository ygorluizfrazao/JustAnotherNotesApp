package br.com.frazo.janac.ui.util.permissions.base.requesters.android

import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import br.com.frazo.janac.ui.util.permissions.base.providers.android.AndroidPermissionProvider
import br.com.frazo.janac.ui.util.permissions.base.requesters.PermissionRequester

class AndroidPermissionRequester private constructor(
    private val activityResultCaller: ActivityResultCaller,
    private val context: Context,
    override val permissionProvider: AndroidPermissionProvider
) : PermissionRequester<List<String>, String> {

    private var onPermissionResult: ((Map<String, Boolean>) -> Unit)? = null
    private var activityResultLauncher: ActivityResultLauncher<*>? = null


    private fun registerLauncher() {
        activityResultLauncher?.unregister()

        activityResultLauncher = if (permissionProvider.isSinglePermission()) {
            activityResultCaller.registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                onPermissionResult(mapOf(Pair(permissionProvider.provide().first(), it)))
            }
        } else {
            activityResultCaller.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
                onPermissionResult(it)
            }
        }
    }

    override fun ask(resultCallback: (Map<String, Boolean>) -> Unit) {

        onPermissionResult = resultCallback
        if (permissionProvider.isSinglePermission()) {
            (activityResultLauncher as ActivityResultLauncher<String>).launch(
                permissionProvider.provide().first()
            )
        } else {
            (activityResultLauncher as ActivityResultLauncher<Array<String>>).launch(
                permissionProvider.provide().toTypedArray()
            )
        }
    }

    override fun permissionsStatus(): Map<String, Boolean> {
        return permissionProvider.provide().associateWith {
            context.checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun onPermissionResult(resultMap: Map<String, Boolean>) {
        onPermissionResult?.invoke(resultMap)
    }

    companion object {

        fun ActivityResultCaller.register(
            permissionProvider: AndroidPermissionProvider,
            context: Context
        ): AndroidPermissionRequester {
            return AndroidPermissionRequester(this, context,permissionProvider).apply { registerLauncher() }
        }

        @Composable
        fun ActivityResultCaller.rememberAndroidPermissionRequester(
            context: Context = LocalContext.current,
            permissionProvider: AndroidPermissionProvider
        ): AndroidPermissionRequester {

            val permissionState = rememberUpdatedState(newValue = permissionProvider)
            val contextState = rememberUpdatedState(newValue = context)
            val activityResultCallerState = rememberUpdatedState(newValue = this)

            val androidPermissionRequester = remember {
                AndroidPermissionRequester(activityResultCallerState.value, contextState.value, permissionState.value)
            }

            androidPermissionRequester.activityResultLauncher =
                if (permissionProvider.isSinglePermission()) {
                    rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestPermission(),
                        onResult = {
                            androidPermissionRequester.onPermissionResult(
                                mapOf(
                                    Pair(
                                        permissionProvider.provide().first(), it
                                    )
                                )
                            )
                        })
                } else {
                    rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestMultiplePermissions(),
                        onResult = {
                            androidPermissionRequester.onPermissionResult(it)
                        })
                }

            return androidPermissionRequester
        }
    }
}

private fun AndroidPermissionProvider.isSinglePermission(): Boolean {
    if (this.provide().size > 1)
        return false
    return true
}
