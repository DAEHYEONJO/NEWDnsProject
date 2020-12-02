package com.example.dnsproject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dnsproject.adapter.ExerAdapter
import com.example.dnsproject.config.*
import com.example.dnsproject.config.HTWDConfigLoader
import com.example.dnsproject.engine.AsrManager
import com.example.dnsproject.engine.MicAudioSource
import com.example.dnsproject.engine.TriggerWordDetectionManager
import com.example.dnsproject.exeClasses.Exercise
import com.example.dnsproject.exeClasses.FixExercise
import com.example.dnsproject.exeClasses.Routine
import com.example.dnsproject.exeClasses.User
import com.example.dnsproject.helper.PermissionCheck
import com.example.dnsproject.model.TwdModelLoader
import com.google.firebase.BuildConfig
import com.google.gson.Gson
import com.lge.aip.engine.base.AIEngineReturn
import com.lge.aip.engine.speech.util.MyDevice
import kotlinx.android.synthetic.main.activity_execute.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.asr_button
import kotlinx.android.synthetic.main.activity_main.htwd_button
import kotlinx.android.synthetic.main.activity_make_routine.*
import kotlinx.android.synthetic.main.activity_manage_routine.*
import java.io.*
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

class MainActivity : AppCompatActivity() , AsrManager.UpdateResultListener, TriggerWordDetectionManager.UpdateResultListener{
    private val requestPermission=arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.RECORD_AUDIO)
    private lateinit var userData: User
    private lateinit var routineList:ArrayList<Routine>
    private lateinit var ikey:String
    private lateinit var fixExercise: FixExercise
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
        setContentView(R.layout.activity_main)
        //아 배고파..
        if (intent.hasExtra("nameKey")) {
            userData = intent.getSerializableExtra("nameKey") as User
            ikey= intent.getStringExtra("IKEY").toString()
            fixExercise=userData.fixExercise
            Log.d("db",fixExercise.barbellCurlsCount.toString())
            routineList =userData.routine
//            Log.d("db","rlist : "+routineList[0].name)

        }
        /*if(intent.hasExtra("fixExercise")){
            fixExercise=intent.getSerializableExtra("fixExercise") as FixExercise
            Log.d("db","execute to main"+fixExercise.benchPressCount.toString())
        }*/
        PermissionCheck(
            this@MainActivity,
            requestPermission
        )



        TimerButton.setOnClickListener {
            val nextIntent = Intent(this@MainActivity, StopWatchActivity::class.java)
            startActivity(nextIntent)
        }
        ScheduleButton.setOnClickListener {
            val nextIntent = Intent(this@MainActivity, ExerciseRecordActivity::class.java)
            nextIntent.putExtra("IKEY", ikey)
            startActivity(nextIntent)
        }
        RecommButton.setOnClickListener {
            val nextIntent = Intent(this@MainActivity, RecommendExerciseActivity::class.java)
            nextIntent.putExtra("IKEY", ikey)
            startActivity(nextIntent)
        }
        RoutineButton.setOnClickListener {
            val nextIntent = Intent(this@MainActivity, ManageRoutineActivity::class.java)
            nextIntent.putExtra("nameKey", routineList)
            nextIntent.putExtra("IKEY",ikey)
            startActivityForResult(nextIntent,2)
        }


    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(data!=null){
            when(resultCode){
                2->{
                    routineList= data.getSerializableExtra("addRoutine") as ArrayList<Routine>
                    Log.d("db",routineList.size.toString())
                    Log.d("db","전달됨")
                }
                3->{
                    fixExercise=data.getSerializableExtra("fixExercise") as FixExercise
                    Log.d("db","전달됐나?"+fixExercise.benchPressCount.toString())
                    Toast.makeText(this,fixExercise.benchPressCount.toString(),Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        initEngine()
        initButtonStart()
        /* htwd engine 가동 */
        htwd_button.isChecked = true
        htwd_button.callOnClick()
    }

    override fun onPause() {
        super.onPause()
        stopAndDestroyEngine()
        stophtwdListening()
    }

    private fun initEngine() {
        htwd_button.isEnabled = true
        loadConfig()
        loadHTWDConfig()
        // Creating a startup engine
        htwdEngineManager =
            TriggerWordDetectionManager(this)
        htwdEngineManager!!.create(this@MainActivity)
        mEngineManager =
            AsrManager(this, this)
    }
    private fun loadHTWDConfig() {
        val configLoader =
            HTWDConfigLoader(this)
        htwdConfig = configLoader.loadConfig(HybridTwdConfig::class.java)
        if (htwdConfig == null) {
            //Toast.makeText(this,"loadHTWDConfig실패", Toast.LENGTH_LONG).show()
            return
        }

        if (mModelLoader == null) {
            mModelLoader =
                TwdModelLoader(this)
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
            ConfigLoader(this@MainActivity)
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
    private fun initButtonStart() {
        asr_button.setOnClickListener {
            if (mEngineManager == null) {
                Log.d("TAG", "StartButton: EngineManager is not created.")
                mEngineManager =
                    AsrManager(this, this)
            }
            if (asr_button.isChecked) {
                Log.d("TAG", "StartButton: START")

                if (!MyDevice.isNetworkConnection(applicationContext)) {
                    Toast.makeText(
                        applicationContext,
                        "network_not_available",
                        Toast.LENGTH_SHORT
                    ).show()
                    asr_button.isChecked = false
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
                    Toast.makeText(this, "configure error", Toast.LENGTH_SHORT).show()
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
        htwd_button.setOnClickListener(View.OnClickListener {
            val currentFocus = currentFocus
            currentFocus?.clearFocus()
            if (htwdEngineManager == null) {
                Log.w(
                    "TAG",
                    "StartButton: EngineManager is not created."
                )
                return@OnClickListener
            }
            if (htwd_button.isChecked) {
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
                    Log.d("TAG","config error")
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
    override fun updateResult(str: String?) {
        Log.d("exeLoglOG","updateResult")
        runOnUiThread {
            asr_button.isChecked = false
            asr_button.callOnClick()
            setEnabledViewsForStart(true)
            //result_asr.text=str
        }
        checkAsrResult(str)
    }

    private fun checkAsrResult(str: String?){
        Log.d("exeLoglOG", "체크asrRESULT")
        var routineNum = 0
        if (str != null && !str.isEmpty()) {
            Log.d("exeLoglOG",str)
            if(str!!.contains("실행", true)||str!!.contains("루팅", true)||str!!.contains("루틴", true)){
                if(str!!.contains("첫번째", true)){
                    Toast.makeText(this, "첫번째 성공", Toast.LENGTH_SHORT).show()
                    Log.d("TAG", "첫번째 성공")
                    routineNum = 0
                }
                else if(str!!.contains("두번째", true)){
                    Log.d("TAG", "두번째 성공")
                    Toast.makeText(this, "두번째 성공", Toast.LENGTH_SHORT).show()
                    routineNum = 1
                }
                else if(str!!.contains("세번째", true)){
                    Log.d("TAG", "세번째 성공")
                    Toast.makeText(this, "세번째 성공", Toast.LENGTH_SHORT).show()
                    routineNum = 2
                }

                val intent = Intent(this@MainActivity, ExecuteActivity::class.java)

                intent.apply {
                    Log.d("exeLoglOG","익스큐트 액티비티 시작")
                    this.putExtra("fixExercise",fixExercise)
                    this.putExtra("routine", routineList[routineNum])
                    this.putExtra("IKEY", ikey)
                    startActivityForResult(intent,100)
                }
            }
            else
            {
                Toast.makeText(this, "루틴 가져오기 실패", Toast.LENGTH_SHORT).show()
                //htwd engine 다시 실행해야함
                Log.d("TAG",str)
                htwd_button.isChecked = true
                htwd_button.callOnClick()
            }
        }
        else{
            /* htwd engine 가동 */
            htwd_button.isChecked = true
            htwd_button.callOnClick()
        }
    }

    override fun updateKeyword(str: String?) {
        runOnUiThread {
            if (str != null && !str.isEmpty()) {
                // mScLog.updateKeyword(str)
            }
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
                    htwd_button.isChecked = false
                    htwd_button.callOnClick()
                    //여기서 asr engine실행
                    asr_button.isEnabled = true
                    asr_button.isChecked = true
                    asr_button.callOnClick()
                } else {

                }
                if (stopped) {

                    htwd_button.isChecked = false
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



}