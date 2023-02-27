package br.com.frazo.janac.ui.util

import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

sealed class TextResource {

    data class RuntimeString(val value: String) : TextResource()

    class StringResource(
        @StringRes val resId: Int,
        vararg val args: Any
    ) : TextResource()


    @Composable
    fun asString(): String {
        return when (this) {
            is RuntimeString -> value
            is StringResource -> stringResource(id = resId, *args)
        }
    }

    @SuppressLint
    fun asString(context: Context): String {
        return when (this) {
            is RuntimeString -> value
            is StringResource -> context.getString(resId, *args)
        }
    }
}
