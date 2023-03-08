package br.com.frazo.janac.ui.util.composables

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.VisualTransformation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTextField(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    onValueChange: (String) -> Unit,
    singleLine: Boolean = false,
    maxLines: Int = 1,
    minLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    leadingIcon: @Composable (() -> Unit)? = {
        IconButton(onClick = { onValueChange("") }) {
            Icon(imageVector = Icons.Rounded.Clear, contentDescription = "Clear Text")
        }
    },
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    colors: TextFieldColors = TextFieldDefaults.textFieldColors(),
    isError: Boolean = false,
    hint: String = ""
) {

    TextField(
        value = value,
        label = {
            Text(text = label)
        },
        singleLine = singleLine,
        maxLines = if (singleLine) 1 else maxLines,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        onValueChange = onValueChange,
        modifier = modifier,
        leadingIcon = if (value.isNotEmpty()) leadingIcon else null,
        trailingIcon = trailingIcon,
        visualTransformation = visualTransformation,
        colors = colors,
        isError = isError,
        placeholder = {
            Text(text = hint)
        },
        minLines = minLines
    )
}