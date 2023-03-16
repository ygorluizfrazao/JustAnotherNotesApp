package br.com.frazo.janac.audio.recorder

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import androidx.test.rule.GrantPermissionRule
import br.com.frazo.janac.util.testDispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
@SmallTest
class AndroidAudioRecorderTest {

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.RECORD_AUDIO
    )

    private val dispatchers = testDispatchers
    private lateinit var tempRecordFile: File
    private lateinit var context: Context

    @Before
    fun createFile() {
        context = ApplicationProvider.getApplicationContext()
        tempRecordFile = File(context.cacheDir, "temp_record.aac")
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun startRecording_isFlowUpdatingResults() {
        runTest {
            val audioRecorder = AndroidAudioRecorder(context)
            val flow = audioRecorder.startRecording(tempRecordFile)
            val values = flow.take(3).toList()
            assert(values.size == 3)
        }
    }

    @Test
    fun stopRecording_isFlowCancelled() {
        runBlocking {
            val audioRecorder = AndroidAudioRecorder(context)
            val flow = audioRecorder.startRecording(tempRecordFile)
            var flowTerminated = false
            val values = mutableListOf<AudioRecordingData>()

            launch(dispatchers.unconfined) {
                flow.onCompletion { flowTerminated = true }.collectLatest {
                    values.add(it)
                }
            }

            delay(1000)
            assert(!flowTerminated)
            audioRecorder.stopRecording()
            delay(1000)
            assert(flowTerminated)
            assert(values.isNotEmpty())
            assert(values[2].cycleTime>200)
            assert(tempRecordFile.exists())
        }
    }

    @After
    fun tearDown() {
        if (tempRecordFile.exists()) tempRecordFile.delete()
    }

}