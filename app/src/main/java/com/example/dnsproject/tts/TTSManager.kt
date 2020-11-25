package com.example.dnsproject.tts

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException

class TTSManager() {
    fun playPcmForFileModeStart(filepath: String) {
        Log.d("how load main", "playAudioFileViaAudioTrack")
        val thread = Thread(Runnable { playAudioFileViaAudioTrack(filepath) })
        thread.start()
    }
    private fun playAudioFileViaAudioTrack(filepath: String) {
        val SAMPLE_RATE_DEFAULT = 16000
        val MAX_BUFFER_SIZE = 512 * 1024
        Log.d("how load main", "playAudioFileViaAudioTrack")
        val file = File(filepath)
        val sampleRate = SAMPLE_RATE_DEFAULT
        val intSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT
        )
        val at = AudioTrack(
            AudioManager.STREAM_MUSIC,
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            intSize,
            AudioTrack.MODE_STREAM
        )
        var byteData: ByteArray? = null
        byteData = ByteArray(MAX_BUFFER_SIZE)
        var `in`: FileInputStream? = null
        try {
            `in` = FileInputStream(file)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        if (`in` == null) {
            Log.w("tagtag", "Could not read the file")
            return
        }
        var bytesread = 0
        var ret = 0
        at.play()
        try {
            while (bytesread < file.length()) {
                ret = `in`.read(byteData, 0, MAX_BUFFER_SIZE)
                bytesread += if (ret != -1) { // Write the byte array to the track
                    at.write(byteData, 0, ret)
                    ret
                } else break
            }
        } catch (e: IOException) {
            Log.w("tagtag", "Exception during play PCM", e)
        }
        try {
            `in`.close()
        } catch (e: IOException) {
            Log.w("tagtag", "Exception on closing file stream", e)
        }
        at.stop()
        at.release()
    }
}