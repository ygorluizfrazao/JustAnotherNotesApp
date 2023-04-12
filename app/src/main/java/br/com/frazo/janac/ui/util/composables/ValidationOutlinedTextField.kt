package br.com.frazo.janac.ui.util.composables

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
@SuppressLint
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ValidationOutlinedTextField(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    onValueChange: (String) -> Unit,
    singleLine: Boolean = false,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(),
    errorColors: TextFieldColors? = OutlinedTextFieldDefaults.colors(),
    maxLines: Int = 1,
    maxColumns: Int = -1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    errorMessage: String = "",
    errorTrailingIcon: @Composable (() -> Unit)? = {
        Icon(Icons.Default.Error, errorMessage)
    },
    successTrailingIcon: @Composable (() -> Unit)? = {
        Icon(Icons.Default.CheckCircle, "Success", tint = Color.Green)
    },
    loadingTrailingIcon: @Composable (() -> Unit)? = {
        CircularProgressIndicator(
            Modifier
                .wrapContentSize()
                .scale(0.5f))
    },
    successColors: TextFieldColors? = OutlinedTextFieldDefaults.colors(
        cursorColor = Color.Green,
        focusedLabelColor = Color.Green,
        unfocusedLabelColor = Color.Green,
        focusedBorderColor = Color.Green,
        unfocusedBorderColor = Color.Green,
    ),
    loadingColors: TextFieldColors? = OutlinedTextFieldDefaults.colors(),
    isError: Boolean = errorMessage.isNotEmpty(),
    isSuccess: Boolean = false,
    isLoading: Boolean = false
) {
    Column(modifier = modifier) {
        MyOutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            value = value,
            label = label,
            onValueChange = {
                if (maxColumns < 0 || it.length <= maxColumns)
                    onValueChange(it)
            },
            singleLine = singleLine,
            maxLines = maxLines,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            trailingIcon = {
                if (errorMessage.isNotEmpty())
                    errorTrailingIcon?.invoke() ?: trailingIcon?.invoke()
                else if (isSuccess)
                    successTrailingIcon?.invoke() ?: trailingIcon?.invoke()
                else if (isLoading)
                    loadingTrailingIcon?.invoke() ?: trailingIcon?.invoke()
                else
                    trailingIcon?.invoke()
            },
            visualTransformation = visualTransformation,
            colors =
            if (errorMessage.isNotEmpty())
                errorColors ?: colors
            else if (isSuccess)
                successColors ?: colors
            else if (isLoading)
                loadingColors ?: colors
            else
                colors,
            isError = isError
        )
        Row {
            AnimatedVisibility(
                modifier = Modifier
                    .weight(9f)
                    .wrapContentHeight(),
                visible = errorMessage.isNotEmpty()
            ) {
                Text(
                    textAlign = TextAlign.Start,
                    text = errorMessage,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
            AnimatedVisibility(
                modifier = Modifier
                    .weight(1f)
                    .wrapContentHeight(),
                visible = maxColumns >= 0
            ) {
                Text(
                    textAlign = TextAlign.End,
                    text = "${value.length}/$maxColumns",
                    style = MaterialTheme.typography.labelSmall,
                )
            }

        }
    }
}