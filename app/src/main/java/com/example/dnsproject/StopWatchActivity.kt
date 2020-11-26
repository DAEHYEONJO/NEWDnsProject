package com.example.dnsproject

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.dnsproject.config.*
import com.example.dnsproject.config.HTWDConfigLoader
import com.example.dnsproject.engine.AsrManager
import com.example.dnsproject.engine.MicAudioSource
import com.example.dnsproject.engine.TriggerWordDetectionManager
import com.example.dnsproject.model.TwdModelLoader
import com.google.firebase.BuildConfig
import com.google.gson.Gson
import com.lge.aip.engine.base.AIEngineReturn
import com.lge.aip.engine.speech.util.MyDevice
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_stop_watch.*
import java.io.*
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.concurrent.timer

class StopWatchActivity : AppCompatActivity() , AsrManager.UpdateResultListener, TriggerWordDetectionManager.UpdateResultListener{
    private var timerTask: Timer? = null
    private var isRunning = false
    private var time = 0

    /* engine */
    private var mEngineManager: AsrManager? = null
    private var mConfig: SpeechConfig? = null
    private var htwdEngineManager: TriggerWordDetectionManager? = null
    private var htwdConfig: HybridTwdConfig? = null
    private var mModelLoader: TwdModelLoader? = null
    private var mTimeFull: Long = 0
    private val mTimeAsr: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stop_watch)
        initEngine()
        initButtonStart()
        watch_htwd_button.isChecked = true
        watch_htwd_button.callOnClick()
        onClick()
    }

    private fun onClick() {
        start.setOnClickListener {
            //stophtwdListening()
            isRunning = !isRunning
            if (isRunning) start()
            else pause()
        }
        end.setOnClickListener { reset() }
    }

    private fun start() {
        timerTask = timer(period = 10) {
            time++
            val min = time / 6000
            var sec = time / 100
            sec %= 60
            var secString=if(sec in 0..9) {
               "0"+sec.toString()
            }else{
                sec.toString()
            }
            val milli = time % 100
            var minString = if (min in 0..9) {
                "0" + min.toString()
            } else {
                min.toString()
            }
            var milliString = if (milli in 0..9) {
                "0" + milli.toString()
            } else {
                milli.toString()
            }
            runOnUiThread {
                setUIText(minString, secString, milliString)
            }
        }
    }

    private fun pause() {
        timerTask?.cancel()
    }

    private fun reset() {
        timerTask?.cancel()
        time = 0
        isRunning = false
        setUIText("00", "00", "00")
    }

    private fun setUIText(min: String, sec: String, milli: String) {
        minTV.text = min
        secTV.text = sec
        milliTV.text = milli
    }
    /* engine */
    private fun initEngine() {
        watch_htwd_button.isEnabled = true
        loadConfig()
        loadHTWDConfig()
        // Creating a startup engine
        htwdEngineManager =
            TriggerWordDetectionManager(this@StopWatchActivity)
        htwdEngineManager!!.create(this@StopWatchActivity)
        mEngineManager =
            AsrManager(this@StopWatchActivity, this@StopWatchActivity)
    }
    private fun loadHTWDConfig() {
        val configLoader =
            HTWDConfigLoader(this@StopWatchActivity)
        htwdConfig = configLoader.loadConfig(HybridTwdConfig::class.java)
        if (htwdConfig == null) {
            //Toast.makeText(this,"loadHTWDConfig실패", Toast.LENGTH_LONG).show()
            return
        }

        if (mModelLoader == null) {
            mModelLoader =
                TwdModelLoader(this@StopWatchActivity)
        }
        try {
            mModelLoader!!.load(htwdConfig!!.triggerWord!!, htwdConfig!!.locale!!)
        } catch (e: IllegalArgumentException) {
            //Toast.makeText(this,"loadHTWDConfig실패", Toast.LENGTH_LONG).show()
            mModelLoader = null
            return
        }
    }
    private fun loadConfig() {
        val configLoader =
            ConfigLoader(this@StopWatchActivity)
        mConfig = configLoader.loadConfig(SpeechConfig::class.java)

        if (mConfig == null) {
            //Toast.makeText(this,"loadConfig실패", Toast.LENGTH_LONG).show()
            return
        }
        if (mConfig!!.enableHttp2) {
            //Toast.makeText(this,"loadConfig실패", Toast.LENGTH_LONG).show()
            return
        }
        val asrConfig: AsrConfig = mConfig!!.asrConfig

        if (asrConfig == null) {
            //Toast.makeText(this,"loadConfig실패", Toast.LENGTH_LONG).show()
            return
        }
    }
    private fun stophtwdListening() {
        if (htwdEngineManager != null) {
            htwdEngineManager!!.stopListening()
            Log.d(
                "TAG",
                "Hybrid Twd Engine: destroy called."
            )
            htwdEngineManager!!.destroy()
        }
    }

    private fun stopListening(reason: String) {
        Log.d("TAG", "stopListening")
        mEngineManager!!.stopListening()
        setEnabledViewsForStart(true)
    }
    private fun updateDeviceTime() {
        if (mConfig == null) {
            return
        }
        val currentTime = System.currentTimeMillis()
    }
    private fun getEncryptionKey(): String? {
        return getBuildConfigField("ENCRYPTION_KEY")
    }
    private fun getBuildConfigField(name: String): String? {
        var key: String? = null
        try {
            val f = BuildConfig::class.java.getField(name)
            key = f[null] as String

        } catch (e: Exception) {
            Log.w(
                "TAG", "Cannot found on BuildConfig $name"
            )
            //Toast.makeText(this, "getBuildConfigField 오류", Toast.LENGTH_LONG).show()
        }
        return key
    }

    private fun loadCertificate(): String? {
        var stream: InputStream? = null
        var isr: InputStreamReader? = null
        var reader: Reader? = null
        var sb: StringBuilder? = null
        try {
            stream = assets.open("asr-certificate.crt")
            sb = StringBuilder()
            isr = InputStreamReader(
                stream,
                Charset.forName(StandardCharsets.UTF_8.name())
            )
            reader = BufferedReader(isr)
            var c = 0
            while (reader.read().also { c = it } != -1) {
                sb.append(c.toChar())
            }
        } catch (e: IOException) {
            Log.e(
                "TAG",
                "Exception during loading a certificate",
                e
            )
        } finally {
            try {
                reader?.close()
            } catch (e: IOException) {
                e.message?.let { Log.e("TAG", it) }
            }
            try {
                isr?.close()
            } catch (e: IOException) {
                e.message?.let { Log.e("TAG", it) }
            }
            try {
                stream?.close()
            } catch (e: IOException) {
                e.message?.let { Log.e("TAG", it) }
            }
        }
        return sb?.toString()
    }

    private fun setEnabledViewsForStart(enabled: Boolean) {
        //시작상태
        //엔진 두개 나눠야 될지도 모름
//        findViewById(R.id.text_input_type).setEnabled(enabled)
//        mRadioButtonFile.setEnabled(enabled)
//        mRadioButtonMic.setEnabled(enabled)
    }

    private fun initButtonStart() {
        watch_asr_button.setOnClickListener {
            if (mEngineManager == null) {
                Log.d("TAG", "StartButton: EngineManager is not created.")
                mEngineManager =
                    AsrManager(this@StopWatchActivity, this@StopWatchActivity)
            }
            if (watch_asr_button.isChecked) {
                Log.d("TAG", "StartButton: START")

                if (!MyDevice.isNetworkConnection(applicationContext)) {
                    Toast.makeText(
                        applicationContext,
                        "network_not_available",
                        Toast.LENGTH_SHORT
                    ).show()
                    watch_asr_button.isChecked = false
                    return@setOnClickListener
                }

                //모르겠음
                updateDeviceTime()
                mEngineManager!!.create()

                // Create Config in Json format. In this case, we use Gson to dynamically configure
                // to change the setting by the UI, but it is also possible to read from a fixed
                // file or to use a hard-coded string.

                if (!mConfig!!.enableHttp2) {
                    mConfig!!.asrConfig!!.encryptionKey = getEncryptionKey()
                } else {
                    //  Update Certificate for HTTP/2 connection.
                    mConfig!!.setCertificate(loadCertificate())
                }
                val jsonConfig: String = Gson().toJson(mConfig, SpeechConfig::class.java)
                val res: Int = mEngineManager!!.configure(jsonConfig)
                if (res != AIEngineReturn.LGAI_ASR_SUCCESS) {
                    //mScLog.append("configure error : $res")
                    Toast.makeText(this@StopWatchActivity, "configure error", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                setEnabledViewsForStart(false)
                val ret: Int = mEngineManager!!.startListening(MicAudioSource())
                if (AIEngineReturn.LGAI_ASR_SUCCESS != ret) {
                    Log.d("TAG","Unable to start. error")
                    stopListening("Unable to start. error = $ret")
                }
            } else {
                Log.d("TAG","button uncheck")
                stopListening("버튼 uncheck")
            }


        }
        watch_htwd_button.setOnClickListener(View.OnClickListener {
            val currentFocus = currentFocus
            currentFocus?.clearFocus()
            if (htwdEngineManager == null) {
                Log.w(
                    "TAG",
                    "StartButton: EngineManager is not created."
                )
                return@OnClickListener
            }
            if (watch_htwd_button.isChecked) {
                Log.d(
                    "TAG",
                    "StartButton: START"
                )

                val sensitivity: String = "10"
                var isFileMode = false
                val keywordId: Int = 1 //hilg
                val isHybridMode: Boolean = true

                htwdConfig!!.serverConfig!!.encryptionKey = getEncryptionKey()
                val jsonConfig: String =
                    Gson().toJson(htwdConfig, HybridTwdConfig::class.java)
                Log.d(
                    "TAG",
                    "onClick: $jsonConfig"
                )
                val res: Int = htwdEngineManager!!.configure(jsonConfig)
                if (res != AIEngineReturn.LGAI_HTWD_SUCCESS) {
                    Log.d("TAG","config error $res")
                    return@OnClickListener
                }

                // Passing enableHybrid to the Manager is for turning off the microphone and handling
                // the UI. The processing of the engine is sufficient to be passed through configure.
                if (htwdConfig != null) {
                    htwdEngineManager!!.enableHybrid(htwdConfig!!.enableHybrid)
                }
                if (mModelLoader == null) {
                    //showJsonErrorDialog()
                    return@OnClickListener
                }
                try {
                    htwdEngineManager!!.injectModels(
                        mModelLoader!!.readAmAsset(),
                        mModelLoader!!.readNetAsset()
                    )
                } catch (e: IllegalStateException) {
                    //showJsonErrorDialog(e.message)
                    return@OnClickListener
                }

                setEnabledViewsForStart(false)
                mTimeFull = System.currentTimeMillis()

                htwdEngineManager!!.startListening(MicAudioSource())
            } else {
                Log.d(
                    "TAG",
                    "StartButton: STOP"
                )
                htwdEngineManager!!.stopListening()
                setEnabledViewsForStart(true)
                //Toast.makeText(this,"htwdEngine stop", Toast.LENGTH_SHORT).show()
            }
        })
    }
    //asr engine
    override fun updateResult(str: String?) {

        runOnUiThread {
            watch_asr_button.isChecked = false
            watch_asr_button.callOnClick()
            setEnabledViewsForStart(true)
        }
        checkAsrResult(str)
    }

    fun checkAsrResult(str: String?){
        if (str != null && !str.isEmpty()) {

            if(str!!.contains("시작", true) && !isRunning){
                isRunning = !isRunning
                if (isRunning) start()
            }
            else
            {
                Toast.makeText(this@StopWatchActivity, "루틴 가져오기 실패", Toast.LENGTH_SHORT).show()
                //htwd engine 다시 실행해야함
                watch_htwd_button.isChecked = true
                watch_htwd_button.callOnClick()
            }
        }
    }

    override fun updateKeyword(str: String?) {
        runOnUiThread {
            if (str != null && !str.isEmpty()) {
                // mScLog.updateKeyword(str)
            }
        }
    }

    //asr 멈추기
    private fun stopAndDestroyEngine() {
        if (mEngineManager != null) {
            mEngineManager!!.stopListening()
            Log.d(
                "TAG",
                "ASR Engine: destroy called."
            )
            mEngineManager!!.destroy()
            mEngineManager = null
        }
    }
    //htwd engine
    override fun updateResult(
        str: String?,
        detected: Boolean,
        fromServer: Boolean,
        stopped: Boolean
    ) {
        runOnUiThread {
            if (str != null && !str.isEmpty()) {
                if (detected) {
                    Toast.makeText(
                        applicationContext,
                        "기동어 검출 성공!",
                        Toast.LENGTH_LONG
                    ).show()
                    watch_htwd_button.isChecked = false
                    watch_htwd_button.callOnClick()
                    //여기서 asr engine실행
                    watch_asr_button.isEnabled = true
                    watch_asr_button.isChecked = true
                    watch_asr_button.callOnClick()
                } else {

                }
                if (stopped) {
                    watch_htwd_button.isChecked = false
                    setEnabledViewsForStart(true)
                }
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        stopAndDestroyEngine()
        stophtwdListening()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        stopAndDestroyEngine()
        stophtwdListening()
    }

    override fun onStop() {
        super.onStop()
        stopAndDestroyEngine()
        stophtwdListening()
    }



}