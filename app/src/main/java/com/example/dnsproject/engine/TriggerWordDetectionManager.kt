package com.example.dnsproject.engine

import android.content.Context
import android.util.Log
import com.lge.aip.engine.base.AIEngineReturn
import com.lge.aip.engine.base.AudioInputData
import com.lge.aip.engine.base.IEngineListener
import com.lge.aip.engine.hybridtwd.AI_HybridTWDEngineAPI
import com.lge.aip.engine.hybridtwd.AI_HybridTWDEngineListener

class TriggerWordDetectionManager
/**
 * The constructor of TriggerWordDetectionManager.
 * It receives an UpdateResultListener to display the results on the screen.
 *
 * @param updateResultListener Used to display the result on the screen
 */(
    /**
     * Listener to return result to Activity after Callback
     */
    private val mUpdateResultListener: UpdateResultListener
) {
    /**
     * Flag for Thread termination processing
     */
    private var mEnded = false

    /**
     * Thread for voice processing
     */
    private var mThread: Thread? = null

    /**
     * TWD Engine, JNI
     */
    private var mHtwdEngine: AI_HybridTWDEngineAPI? = null

    /**
     * Set status of server operation
     */
    private var enableASR = true

    /**
     * Create engine
     */
    fun create(context: Context?) {
        mHtwdEngine = AI_HybridTWDEngineAPI(context)
        mHtwdEngine!!.create()
    }

    /**
     * Configure engine with Json config
     *
     * @param jsonConfig json string
     */
    fun configure(jsonConfig: String?): Int {
        val res = mHtwdEngine!!.configure(jsonConfig)
        if (res != AIEngineReturn.LGAI_HTWD_SUCCESS) {
            return res
        }
        mHtwdEngine!!.setListener(mTwdListener)
        return AIEngineReturn.LGAI_HTWD_SUCCESS
    }

    /**
     * Set model data with byte array. Do not set amModelFile and netModelFile on config json, if want
     * to use this method.
     *
     * @param am byte array of am file
     * @param net byte array of net file
     */
    fun injectModels(am: ByteArray?, net: ByteArray?) {
        mHtwdEngine!!.injectModels(am, net)
    }

    fun enableHybrid(enableASR: Boolean) {
        this.enableASR = enableASR
    }

    /**
     * Start engine using the specified audio source
     *
     * @param audioSource Implementation that implements IAudioSource
     */
    fun startListening(audioSource: MicAudioSource) {
        Log.d("TAG", "startListening함수 들어옴")

        // Condition for Thread termination
        mEnded = false
        // Engine start, engine must be started and must be initialized by create in advance
        mHtwdEngine!!.start()
        Log.d("TAG", "thread")
        // Sound data reading thread
        mThread = Thread(Runnable {
            if (!audioSource.prepare()) {
                Log.d("TAG", "prepare문제")
                audioSource.release()
                return@Runnable
            }
            val buffer = ByteArray(BUFFER_SIZE)
            while (audioSource.read(buffer) === BUFFER_SIZE && !mEnded) {
                val audioInput = AudioInputData(buffer)
                // Deliver data to the engine
                mHtwdEngine!!.process(audioInput, null)
                try {
                    Thread.sleep(20)
                } catch (e: InterruptedException) {
                    Log.w(
                        "TAG",
                        "startListening:InterruptedException: " + e.message
                    )
                }
            }
            audioSource.release()
            Log.d(
                "TAG",
                "startListening : loop end."
            )
        })
        mThread!!.start()
    }

    /**
     * Stop engine and voice input thread.
     */
    fun stopListening() {
        // Stop thread
        mEnded = true
        Log.d("TAG", "Twd Engine: stop")
        try {
            if (mThread != null) {
                mThread!!.join()
            }
        } catch (e: InterruptedException) {
            Log.w(
                "TAG",
                "stopListening:InterruptedException",
                e
            )
        }
        // Stop engine
        if (mHtwdEngine != null) {
            mHtwdEngine!!.stop()
        }
    }

    /**
     * Invoked when the engine is destroyed. Call when activity ends.
     */
    fun destroy() {
        if (mHtwdEngine != null) {
            Log.d(TAG, "Twd Engine: destroy")

            // Destroy engine when user finish app
            mHtwdEngine!!.destroy()
            mHtwdEngine = null
        }
    }

    /**
     * Listener for receiving results from the engine.
     */
    private val mTwdListener: IEngineListener = object : AI_HybridTWDEngineListener {
        override fun onDetected(id: Int, length: Int, keyword: String) {
            Log.d("TAG", "detect")
            // This is the result through the native engine without a server.
            // It is always received before the onResults Callback.
            // If you only use server results, there is nothing to handle in this callback.
            mUpdateResultListener.updateResult(keyword, true, false, !enableASR)
            if (!enableASR) {
                stopListening()
            }
        }

        override fun onError(error: Int, errorText: String) {
            Log.d(
                "TAG",
                "onError: $error $errorText"
            )
            // Stop the engine
            mUpdateResultListener.updateResult("$errorText [$error]", false, false, true)
            stopListening()
        }

        override fun onResults(success: Boolean, text: String) {
            // Receiving results through the server. The success parameter determines success.
            // Stop the engine
            mUpdateResultListener.updateResult(text, success, true, true)
            stopListening()
            Log.d("TAG", "onResults: $text")
        }
    }

    interface UpdateResultListener {
        /**
         * Update the results in a text view to display the results.
         *
         * @param str      String to update
         * @param detected Detected or not
         */
        fun updateResult(
            str: String?,
            detected: Boolean,
            fromServer: Boolean,
            stopped: Boolean
        )
    }

    companion object {
        private val TAG = TriggerWordDetectionManager::class.java.simpleName

        /**
         * The size of buffer delivered to engine
         */
        private const val BUFFER_SIZE = 800
    }

}