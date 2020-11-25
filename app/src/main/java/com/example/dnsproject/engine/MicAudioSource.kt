package com.example.dnsproject.engine

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log

class MicAudioSource : IAudioSource {
    private val TAG = MicAudioSource::class.java.simpleName

    /**
     * Sampling rate
     */
    private val SAMPLE_RATE = 16000 // Hz


    /**
     * To calculate buffer size for mic
     */
    private val SUB_BUFFER = 15

    /**
     * Gain the audio resource from Mic
     */
    private var mRecorder: AudioRecord? = null

    /**
     * Creates the AudioRecord instance and start recording from it.
     *
     * @return true, if the source is available.
     */
    override fun prepare(): Boolean {
        // Acquired Mic Resource
        Log.d("TAG", "prepare")
        mRecorder = AudioRecord(
            MediaRecorder.AudioSource.VOICE_RECOGNITION, SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
            SAMPLE_RATE * SUB_BUFFER
        )

        mRecorder!!.startRecording()

        // Microphone status check required. Not available when voice input is in use by another app.
        // For example, you can not use this feature while running a voice recording app.
        if (mRecorder!!.recordingState != AudioRecord.RECORDSTATE_RECORDING) {
            Log.e("TAG", "Cannot startRecording!!")
            return false
        }
        return true
    }

    override fun read(buffer: ByteArray): Int {
        return mRecorder!!.read(buffer, 0, buffer.size)
    }

    override fun read(buffers: Array<ByteArray>): Int {
        return mRecorder!!.read(buffers[0], 0, buffers[0].size)
    }

    /**
     * Read the audio data from source and write on the buffer given by parameter.
     *
     * @param buffer byte buffer to write the audio data.
     * @return the size of buffer. See the return part of the [AudioRecord.read]
     */


    override fun getChannel(): Int {
        return 1
    }

    override fun release() {
        // release mic
        if (mRecorder != null) {
            mRecorder!!.stop()
            mRecorder!!.release()
            mRecorder = null
        }
    }
}