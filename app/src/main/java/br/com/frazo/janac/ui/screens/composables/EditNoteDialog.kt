package br.com.frazo.janac.ui.screens.composables

import android.Manifest
import android.graphics.DashPathEffect
import android.graphics.Paint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.DialogProperties
import br.com.frazo.easy_permissions.base.strategy.AskingStrategy
import br.com.frazo.easy_permissions.base.strategy.PermissionFlowStateEnum
import br.com.frazo.easy_permissions.base.strategy.rememberUserDrivenAskingStrategy
import br.com.frazo.easy_permissions_ext.materialv3.WithPermission
import br.com.frazo.janac.R
import br.com.frazo.janac.audio.player.AudioPlayingData
import br.com.frazo.janac.audio.recorder.AudioRecordingData
import br.com.frazo.janac.audio.recorder.visualizer.MirrorWaveRecordingVisualizer
import br.com.frazo.janac.ui.screens.notes.editnote.EditNoteViewModel
import br.com.frazo.janac.ui.util.composables.ValidationTextField
import br.com.frazo.janac.ui.theme.spacing
import br.com.frazo.janac.ui.util.IconResource
import br.com.frazo.janac.ui.util.composables.audio.AudioPlayer
import br.com.frazo.janac.ui.util.composables.audio.AudioRecorder

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
    dialogTitle: String,
    audioRecordingData: List<AudioRecordingData>,
    onAudioRecordStartRequested: () -> Unit,
    onAudioRecordStopRequested: () -> Unit,
    audioNoteStatus: EditNoteViewModel.AudioNoteStatus,
    audioPlayingData: AudioPlayingData,
    onAudioNotePlayRequest: () -> Unit,
    onAudioNotePauseRequest: () -> Unit,
    onAudioNoteDeleteRequest: (() -> Unit)? = null
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
                    style = MaterialTheme.typography.titleLarge
                )
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

                Divider(
                    color = LocalContentColor.current,
                    modifier = Modifier.padding(MaterialTheme.spacing.medium)
                )

                Text(
                    text = stringResource(R.string.audio_note),
                    Modifier.padding(bottom = MaterialTheme.spacing.small)
                )

                var canStart by rememberSaveable {
                    mutableStateOf(false)
                }

                val userDrivenAskingStrategy =
                    rememberUserDrivenAskingStrategy(
                        type = AskingStrategy.STOP_ASKING_ON_USER_DENIAL,
                        permissions = listOf(
                            Manifest.permission.RECORD_AUDIO
                        ),
                        canStart = { canStart }
                    )

                WithPermission(
                    userDrivenAskingStrategy = userDrivenAskingStrategy,
                    initialStateContent = {
                        IconButton(onClick = { canStart = true }) {
                            IconResource.fromImageVector(
                                Icons.Default.Mic,
                                stringResource(id = R.string.audio_note)
                            ).ComposeIcon()
                        }
                    }) { state: PermissionFlowStateEnum, _: Map<String, Boolean> ->
                    if (state == PermissionFlowStateEnum.TERMINAL_GRANTED) {
                        if (audioNoteStatus == EditNoteViewModel.AudioNoteStatus.HAVE_TO_RECORD) {
                            AudioRecorder(
                                modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium),
                                recordIcon = {
                                    IconResource.fromImageVector(Icons.Default.Mic).ComposeIcon()
                                },
                                stopIcon = {
                                    IconResource.fromImageVector(Icons.Default.Stop).ComposeIcon()
                                },
                                onRecordRequested = onAudioRecordStartRequested,
                                onStopRequested = onAudioRecordStopRequested,
                                audioRecordingData = audioRecordingData,
                                recordingWaveVisualizer = MirrorWaveRecordingVisualizer(
                                    wavePaint = Paint().apply {
                                        color = LocalContentColor.current.toArgb()
                                        strokeWidth = 2f
                                        style = Paint.Style.STROKE
                                        strokeCap = Paint.Cap.ROUND
                                        flags = Paint.ANTI_ALIAS_FLAG
                                        strokeJoin = Paint.Join.BEVEL
                                    },
                                    middleLinePaint = Paint().apply {
                                        color =
                                            LocalTextSelectionColors.current.handleColor.toArgb()
                                        style = Paint.Style.FILL_AND_STROKE
                                        strokeWidth = 2f
                                        pathEffect =
                                            DashPathEffect(arrayOf(4f, 4f).toFloatArray(), 0f)
                                    }
                                )
                            )
                        } else {
                            AudioPlayer(
                                modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium),
                                audioPlayingData = audioPlayingData,
                                playIcon = {
                                    IconResource.fromImageVector(Icons.Default.PlayArrow)
                                        .ComposeIcon()
                                },
                                pauseIcon = {
                                    IconResource.fromImageVector(Icons.Default.Pause).ComposeIcon()
                                },
                                deleteIcon =
                                if (onAudioNoteDeleteRequest != null) {
                                    {
                                        IconResource.fromImageVector(Icons.Default.Delete)
                                            .ComposeIcon()
                                    }
                                } else null,
                                onPlay = onAudioNotePlayRequest,
                                onPause = onAudioNotePauseRequest,
                                onDelete = onAudioNoteDeleteRequest
                            )
                        }
                    } else {
                        IconResource.fromImageVector(Icons.Default.MicOff)
                    }
                }

                Divider(
                    color = LocalContentColor.current,
                    modifier = Modifier.padding(MaterialTheme.spacing.medium)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
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