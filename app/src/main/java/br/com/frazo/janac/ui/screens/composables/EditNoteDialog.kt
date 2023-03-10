package br.com.frazo.janac.ui.screens.composables

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.DialogProperties
import br.com.frazo.janac.R
import br.com.frazo.janac.ui.util.composables.ValidationTextField
import br.com.frazo.janac.ui.theme.spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNoteDialog(
    modifier: Modifier = Modifier,
    properties: DialogProperties = DialogProperties(),
    title: String,
    titleLabel: String = stringResource(R.string.title_label),
    titleHint: String = stringResource(R.string.note_title_hint),
    onTitleChanged: ((String) -> Unit)?,
    titleErrorMessage: String = "",
    text: String,
    textLabel: String = stringResource(R.string.text_label),
    textHint: String = stringResource(R.string.text_hint),
    textErrorMessage: String = "",
    onTextChanged: ((String) -> Unit)?,
    onDismissRequest: () -> Unit,
    onSaveClicked: () -> Unit,
    saveButtonEnabled: Boolean = false,
    dialogTitle: String
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        properties = properties
    ) {
        Surface(
            modifier = modifier,
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(MaterialTheme.spacing.medium)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = MaterialTheme.spacing.medium),
                    textAlign = TextAlign.Center,
                    text = dialogTitle,
                    style = MaterialTheme.typography.titleLarge)
                ValidationTextField(
                    value = title,
                    onValueChange = {
                        onTitleChanged?.invoke(it)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = MaterialTheme.spacing.medium),
                    singleLine = true,
                    hint = titleHint,
                    label = titleLabel,
                    errorMessage = titleErrorMessage
                )

                ValidationTextField(
                    value = text,
                    onValueChange = {
                        onTextChanged?.invoke(it)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = MaterialTheme.spacing.medium),
                    minLines = 5,
                    label = textLabel,
                    hint = textHint,
                    errorMessage = textErrorMessage
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    ElevatedButton(
                        modifier = Modifier.padding(MaterialTheme.spacing.medium),
                        onClick = onDismissRequest
                    ) {
                        Text(text = stringResource(R.string.cancel))
                    }

                    Button(
                        modifier = Modifier.padding(MaterialTheme.spacing.medium),
                        onClick = onSaveClicked,
                        enabled = saveButtonEnabled
                    ) {
                        Text(text = stringResource(R.string.save))
                    }
                }
            }
        }
    }
}