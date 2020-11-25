package com.example.dnsproject.engine

import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.util.Log
import com.lge.aip.engine.base.AIEngineReturn
import com.lge.aip.engine.base.AudioInputData
import com.lge.aip.engine.speech.*
import java.io.File
import java.io.FileOutputStream

class AsrManager {
    private val TAG = AsrManager::class.java.simpleName

    /**
     * Flag for Thread termination processing
     */
    private var mEnded = false

    /**
     * Thread for voice processing
     */
    private var mThread: Thread? = null

    /**
     * ASR + NLP Engine
     */
    private var mAsrEngine: AI_ASREngineAPI? = null

    /**
     * The size of buffer delivered to engine
     */
    private val BUFFER_SIZE = 800

    /**
     * Listener to return result to Activity after Callback
     */
    private var mUpdateResultListener: UpdateResultListener? = null

    /**
     * Handler for processing event
     */
    private val sHandler = Handler()

    constructor(
        context: Context?,
        updateResultListener: UpdateResultListener?
    ) {
        mUpdateResultListener = updateResultListener
        mAsrEngine = AI_ASREngineAPI(context)
    }

    /**
     * Creates engine
     */
    fun create(): Int {
        return mAsrEngine!!.create()
    }

    /**
     * Configures engine by json string
     * @param jsonConfig json string
     */
    fun configure(jsonConfig: String?): Int {
        var res: Int = mAsrEngine!!.configure(jsonConfig)
        if (res != AIEngineReturn.LGAI_ASR_SUCCESS) {
            return res
        }
        res = mAsrEngine!!.setListener(mAsrListener)
        return if (res != AIEngineReturn.LGAI_ASR_SUCCESS) {
            res
        } else AIEngineReturn.LGAI_ASR_SUCCESS
    }

    /**
     * Drives the engine using the specified audio source
     *
     * When audio data is transmitted in ASR mode,
     * the implementation conforming to IAudioSource is passed as an argument.
     * If the nlp signal mode or text mode does not require an audio source, pass it as null.
     *
     * @param audioSource Implementation that implements IAudioSource
     */
    fun startListening(audioSource: IAudioSource): Int {
        // If there is no audio source, terminate without listening thread operation.
        // Corresponds to NLP mode.
        if (audioSource == null) {
            return AIEngineReturn.LGAI_ASR_ERROR_INVALID_ARGUMENT
        }
        // Engine start, engine must be started and must be initialized by create in advance
        val ret: Int = mAsrEngine!!.start()
        if (ret != AIEngineReturn.LGAI_ASR_SUCCESS) {
            return ret
        }
        Log.d("TAG", "startListening")
        // Thread end condition
        mEnded = false
        // Sound data reading thread
        mThread = Thread(Runnable {
            if (!audioSource.prepare()) {
                audioSource.release()
                return@Runnable
            }
            val buffer = Array(
                audioSource.getChannel()
            ) { ByteArray(BUFFER_SIZE) }
            while (audioSource.read(buffer) === BUFFER_SIZE && !mEnded) {
                val audioData = AudioInputData(buffer)

                // Delivering data to the engine
                mAsrEngine!!.process(audioData, null)
                try {
                    Thread.sleep(1)
                } catch (e: InterruptedException) {
                    Log.w(TAG, "startListening:InterruptedException: " + e.message)
                }
            }
            audioSource.release()
            Log.d("TAG", "startListening : loop end.")
        })
        mThread!!.start()
        return AIEngineReturn.LGAI_ASR_SUCCESS
    }

    /**
     * Invoked when the engine is no longer in use.
     * Call it mainly when Activity finish
     */
    fun destroy() {
        if (mAsrEngine != null) {
            Log.d(TAG, "Engine: destroy")

            // Destroy the engine when exiting the app.
            mAsrEngine!!.destroy()
        }
    }

    /**
     * Listener to receive results from ASR/NLP engine
     */
    private val mAsrListener: AI_ASREngineListener = object : AI_ASREngineListener {
        /**
         * Speech engine ready state callback
         */
        override fun onReadyForSpeech() {
            Log.v("TAG", "onReadyForSpeech")
        }

        /**
         * Speech engine speech recognition start point detection callback
         */
        override fun onBeginningOfSpeech() {
            Log.v("TAG", "onBeginningOfSpeech")
        }

        /**
         * Speech engine speech recognition endpoint detection callback
         */
        override fun onEndOfSpeech() {
            Log.v("TAG", "onEndOfSpeech")
        }

        /**
         * Speech engine result callback
         * @param results Receive result as true/false to [Settings.RESULT_BOOLEAN] from Bundle object
         * Receive result text with [ResultBundleKey.RESULT_STRING] as a key
         */
        override fun onResults(results: Bundle) {
            mEnded = results.getBoolean(ResultBundleKey.RESULT_BOOLEAN)
            sHandler.post {
                val resultString =
                    results.getString(ResultBundleKey.RESULT_STRING)
                Log.v("TAG", "onResults:$resultString")
                mUpdateResultListener!!.updateResult("onResults [isFinal:$mEnded] : $resultString")
                if (mEnded) {
                    stopListening()
                }
            }
        }

        /**
         * Speech engine ASR result callback
         * @param results Receive result text with [ResultBundleKey.RESULT_STRING] as a key from Bundle object
         */
        override fun onAsrResults(results: Bundle) {
            sHandler.post {
                val resultString =
                    results.getString(ResultBundleKey.RESULT_STRING)
                mUpdateResultListener!!.updateResult("\nonAsrResults: $resultString")
                Log.d("TAG", "onASRResults:$resultString")

            }
        }

        /**
         * Keyword detection result callback via Speech engine
         * @param results Receive result text with [ResultBundleKey.RESULT_STRING] as a key from Bundle object
         */
        override fun onKeywordResults(results: Bundle) {
            sHandler.post {
                val resultString =
                    results.getString(ResultBundleKey.RESULT_STRING)
                mUpdateResultListener!!.updateResult("onKeywordResults: $resultString")
                Log.d("TAG", "onKeywordResults:$resultString")
            }
        }

        /**
         * Speech engine result error callback
         * @param error Result of error code
         * @param errorText Result of error text
         */
        override fun onError(error: Int, errorText: String) {
            Log.v(TAG, "onError code=$error: $errorText")
            sHandler.post {
                mUpdateResultListener!!.updateResult("onError: $errorText")
                stopListening()
            }
        }

        /**
         * Speech engine voice recognition audio level callback
         * @param rmsdB The value for the size of the input voice
         */
        override fun onRmsChanged(rmsdB: Float) {
            Log.d(TAG, "onRmsChanged...($rmsdB)")
            sHandler.post(Runnable { // This sample does not handle UI processing.
                if (!mEnded) {
                    return@Runnable
                }
                if (rmsdB >= 88) {
                } else {
                }
            })
        }

        /**
         * Callback for storing voice data
         * @param data Voice data
         */
        override fun onBufferReceived(data: ByteArray?) {
            Log.d(TAG, "onBufferReceived...")
            val dirPath =
                Environment.getExternalStorageDirectory().path + "/Download"
            val file = File(dirPath)
            if (!file.exists()) {
                if (!file.mkdirs()) {
                    Log.d(TAG, "Failed to create a directory.")
                }
            }
            try {
                val pcmFile =
                    FileOutputStream("$dirPath/dumpPcm.pcm", true)
                pcmFile.write(data)
                pcmFile.close()
            } catch (re: RuntimeException) {
                println("[Runtime Exception] Check FileOutputStream")
            } catch (e: Exception) {
                println("[danby] dump error")
            }
        }
    }

    /**
     * Stop engine and voice input thread.
     */
    fun stopListening() {
        Log.w("TAG", "stopListening!!")
        mAsrEngine!!.stop()
        mEnded = true
        if (mThread != null) {
            try {
                mThread!!.join()
            } catch (e: InterruptedException) {
                Log.w(TAG, "stopListening: ", e)
            }
        }
    }

    interface UpdateResultListener {
        /**
         * Update the results in a text view to display the results.
         *
         * @param str      String to update.
         */
        fun updateResult(str: String?)
        fun updateKeyword(str: String?)
    }
}