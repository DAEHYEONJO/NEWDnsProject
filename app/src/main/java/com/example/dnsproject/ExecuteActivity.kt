package com.example.dnsproject

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.*
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.dnsproject.config.*
import com.example.dnsproject.config.HTWDConfigLoader
import com.example.dnsproject.engine.AsrManager
import com.example.dnsproject.engine.MicAudioSource
import com.example.dnsproject.engine.TriggerWordDetectionManager
import com.example.dnsproject.exeClasses.Exercise
import com.example.dnsproject.exeClasses.FixExercise
import com.example.dnsproject.exeClasses.Routine
import com.example.dnsproject.model.TwdModelLoader
import com.example.dnsproject.tts.TTSManager
import com.google.firebase.database.*
import com.google.gson.Gson
import com.lge.aip.engine.base.AIEngineReturn
import com.lge.aip.engine.speech.util.MyDevice
import kotlinx.android.synthetic.main.activity_execute.*
import kotlinx.android.synthetic.main.activity_login.*
import org.junit.rules.ExternalResource
import java.io.*
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import kotlin.collections.ArrayList
import kotlin.properties.Delegates

/*
    routine을 실행하는 Activity
 */
@RequiresApi(Build.VERSION_CODES.N)
class ExecuteActivity : AppCompatActivity() , AsrManager.UpdateResultListener, TriggerWordDetectionManager.UpdateResultListener {
    val COUNTTIME : Long = 500 //운동 count시간 0.5초로 해둠->0.5로바꿈
    lateinit var mRoutine:Routine
    lateinit var routineArray: ArrayList<Exercise>
    lateinit var fixExercise: FixExercise
    private lateinit var myTimer : MyTimer
    private var routineSize:Int = 3
    var rNum : Int = 0
    private var setNum=0
    private var curExerciseName=""


    /* tts & pcm file path */
    private val tenSecPcm="/sdcard/dnsTTS/10seconds.pcm"
    private val defaultPcmPath="/sdcard/dnsTTS/"
    private val restStartPcm="/sdcard/dnsTTS/rest_start.pcm"
    private val ttsManager=TTSManager()

    /* engine */
    private var mEngineManager: AsrManager? = null
    private var mConfig: SpeechConfig? = null
    private var htwdEngineManager: TriggerWordDetectionManager? = null
    private var htwdConfig: HybridTwdConfig? = null
    private var mModelLoader: TwdModelLoader? = null
    private var mTimeFull: Long = 0
    private val mTimeAsr: Long = 0

    /* time */
    @SuppressLint("NewApi")
    @RequiresApi(Build.VERSION_CODES.O)
    public val onlyDate: LocalDate = LocalDate.now()

    /* database */
    private lateinit var database: DatabaseReference
    private lateinit var key:String

    var restFlag : Boolean by Delegates.observable(true, { _, old, new ->
        if(rNum>=routineSize){
            //routine 끝!
            finishRoutine(true)
        }
        else{
            if(restFlag){ // 휴식타이머
                Log.d("운동","휴식타이머 : "+setNum.toString())
                rNum--
                ttsManager.playPcmForFileModeStart(restStartPcm)
                runOnUiThread{
                    current_action.text = "휴식^^"
                    remain_time.visibility=View.GONE
                   /* progress_circular.setClockwise(true)
                    progress_circular.foregroundProgressColor= Color.BLACK
                    progress_circular.backgroundProgressColor=Color.WHITE
                    val listener=progress_circular.setProgressWithAnimation(0F,500)*/
                }

                myTimer = MyTimer(10000, 1000)
                myTimer.start()

            }
            else{
                Log.d("운동","운동 : "+setNum.toString())
                if(curExerciseName==routineArray[rNum].name){
                    setNum++
                }else{
                    setNum=1
                    curExerciseName=routineArray[rNum].name
                }
                runOnUiThread{
                    current_action.text = routineArray[rNum].name
                    set_num.text=setNum.toString()+" / "+routineArray[rNum].setCount+"세트"
                }
                Log.d("FFINDD", "start $rNum action 운동")
                val exePath=defaultPcmPath+routineArray[rNum].name+"_start.pcm"
                ttsManager.playPcmForFileModeStart(exePath)
                val futureTime = routineArray[rNum].count.toLong()*COUNTTIME
                //mytimer.start()
                myTimer = MyTimer(futureTime, COUNTTIME)
                myTimer.start()

            }
        }
    })


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_execute)
        val toolbar = findViewById(R.id.exertoolbar) as Toolbar
        setSupportActionBar(toolbar)
        val ab = supportActionBar!!
        ab.setDisplayShowTitleEnabled(false)
        mRoutine = intent.getSerializableExtra("routine") as Routine
        key = intent.getStringExtra("IKEY").toString()
        fixExercise = intent.getSerializableExtra("fixExercise") as FixExercise
        routineArray = ArrayList()


        for(i in 0 until mRoutine.exerciseList.size)
        {
            for(j in 0 until mRoutine.exerciseList[i].setCount.toInt()){
                routineArray.add(mRoutine.exerciseList[i])
            }
        }
        routineSize = routineArray.size
        curExerciseName=routineArray[0].name
        restFlag = false // 루틴시작
    }


    private fun finishRoutine(end:Boolean){
        //엔진과 timer 멈춰야함
        //지금까지 실행한 루틴을 저장해야 함(db에)
        //그리고 창으로 띄워서 보여줘야함
        //그 뒤에는 메인으로 돌아가야함
        //end true:끝까지 실행 false 중간에 멈춤
        Log.d("routine","finishiRoutine db저장할곳")
        myTimer.cancel()
        stopAndDestroyEngine()
        stophtwdListening()
        var curFixExercise = FixExercise()
        var exerciseArray :ArrayList<shortExercise> = ArrayList()
        exerciseArray.add(shortExercise("barbellcurls",fixExercise.barbellCurlsCount))
        exerciseArray.add(shortExercise("benchpress",fixExercise.benchPressCount))
        exerciseArray.add(shortExercise("deadlift",fixExercise.deadLiftCount))
        exerciseArray.add(shortExercise("shoulderpress",fixExercise.shoulderPressCount))
        exerciseArray.add(shortExercise("squat",fixExercise.squatCount))
        for(i in 0 until rNum){
            Log.d("routine name",routineArray[i].name+"/"+routineArray[i].count)
            for(element in exerciseArray){
                Log.d("routine name","element name : "+element.name)
                if(element.name == routineArray[i].name){
                    element.count += routineArray[i].count.toInt()
                    Log.d("routine name","in if element name : "+element.name.toString())
                    Log.d("routine count","in if element name : "+element.count.toString())
                    break
                }
            }
        }
        for(i in exerciseArray.indices) {
            curFixExercise.setExerciseCount(exerciseArray[i].name, exerciseArray[i].count)
            Log.d("윤성테스트1204",exerciseArray[i].name+exerciseArray[i].count)

        }
        Log.d("routine","finishiRoutine db저장할곳2")
        val databaseReference=FirebaseDatabase.getInstance().reference.child(key)
        val toDBFixExercise: MutableMap<String, FixExercise> = HashMap()
        toDBFixExercise["fixExercise"]=curFixExercise
//윤성 작업
        val todayExer : MutableMap<String, String> = HashMap()

        //하드코딩 ㅈㅅ
        var exerSetCount =arrayOf(0,0,0,0,0)
        var exerCount =arrayOf(0,0,0,0,0)
        for(i in 0 until rNum)
        {

            //<<<<<<< bye
            if(routineArray[i].name=="benchpress")
            {
                //idx=0
                exerSetCount[0]+=1
               // exerSetCount[0] = exerSetCount[0]+routineArray[i].setCount.toString().toInt()
                //exerSetCount[0] =  exerCount[0]+routineArray[i].count.toString().toInt()
            }
            else if(routineArray[i].name=="shoulderpress")
            {
                //idx=1
                //exerSetCount[1] = exerSetCount[1]+routineArray[i].setCount.toString().toInt()
                exerSetCount[1]+=1
               // exerSetCount[1] = exerCount[1]+routineArray[i].count.toString().toInt()
            }
            else if(routineArray[i].name=="barbellcurls")
            {
                //idx=2
                //exerSetCount[2] = exerSetCount[2]+routineArray[i].setCount.toString().toInt()
                exerSetCount[2]+=1
                //exerSetCount[2] = exerCount[2]+routineArray[i].count.toString().toInt()
            }
            else if(routineArray[i].name=="deadlift")
            {
                //idx=3
                //exerSetCount[3] = exerSetCount[3]+routineArray[i].setCount.toString().toInt()
                exerSetCount[3]+=1
                //exerSetCount[3] = exerCount[3]+routineArray[i].count.toString().toInt()
            }
            else if(routineArray[i].name=="squat")
            {
                //idx=4
                //exerSetCount[4] = exerSetCount[4]+routineArray[i].setCount.toString().toInt()
                exerSetCount[4]+=1
                //exerSetCount[4] = exerCount[4]+routineArray[i].count.toString().toInt()
            }

        }

        //파베 가져오기

        var ref = databaseReference.child("exerDate").child(onlyDate.toString())
        /*
        ref.child("benchpress").setValue(0)
        ref.child("shoulderpress").setValue(0)
        ref.child("barbellcurls").setValue(0)
        ref.child("deadlift").setValue(0)
        ref.child("squat").setValue(0)
        */
        ref.addListenerForSingleValueEvent(object : ValueEventListener
        {
            override fun onDataChange(snapshot: DataSnapshot) {

                if(snapshot.value.toString()!=null)
                {
                    var ref3 = databaseReference.child("exerDate").child(onlyDate.toString()).child("benchpress")
                    ref3.setValue((snapshot.child("benchpress").value.toString().toInt()+(exerSetCount[0])).toString())
                    var ref4 = databaseReference.child("exerDate").child(onlyDate.toString()).child("shoulderpress")
                    ref4.setValue((snapshot.child("shoulderpress").value.toString().toInt()+(exerSetCount[1])).toString())
                    var ref5 = databaseReference.child("exerDate").child(onlyDate.toString()).child("barbellcurls")
                    ref5.setValue((snapshot.child("barbellcurls").value.toString().toInt()+(exerSetCount[2])).toString())
                    var ref6 = databaseReference.child("exerDate").child(onlyDate.toString()).child("deadlift")
                    ref6.setValue((snapshot.child("deadlift").value.toString().toInt()+(exerSetCount[3])).toString())
                    var ref7 = databaseReference.child("exerDate").child(onlyDate.toString()).child("squat")
                    ref7.setValue((snapshot.child("squat").value.toString().toInt()+(exerSetCount[4])).toString())

                }
                else
                {
                    //오늘은 처음 저장
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
        //윤성 작업 끝
        databaseReference.updateChildren(toDBFixExercise as Map<String, FixExercise>)
        val toMainIntent=Intent()
        toMainIntent.putExtra("fixExercise",curFixExercise)
        setResult(3,toMainIntent)
        onBackPressed()
        finish()
    }
    class shortExercise(val name: String, var count:Int){

    }

    inner class MyTimer(
        millisInFuture: Long,
        countDownInterval: Long
    ) : CountDownTimer(millisInFuture, countDownInterval) {

        @SuppressLint("SetTextI18n")
        override fun onTick(millisUntilFinished: Long) {

            Log.d("timer", " millisUntilFinished : "+millisUntilFinished.toString())
            if(restFlag)//휴식 타이머 일때
            {
                Log.d("timer", " 휴식타이머 : ")
                if(millisUntilFinished.toInt() in 10000..10999){
                    //tts 10초 남았습니다.
                    Log.d("timer", " 10초남았을때 : "+millisUntilFinished.toString())
                    Toast.makeText(this@ExecuteActivity,"10초남음",Toast.LENGTH_SHORT).show()
                    ttsManager.playPcmForFileModeStart(tenSecPcm)
                }
                runOnUiThread{set_num.text = (millisUntilFinished / 1000.toLong()).toString() + " 초"}
            }
            else{//운동 타이머
                runOnUiThread{
                    remain_time.visibility=View.VISIBLE
                    remain_time.text = (millisUntilFinished / COUNTTIME.toLong()).toString() + " 개"}
            }
        }


        override fun onFinish() {
            Log.d("timer","onfinish timer")
            remain_time.text=""
            remain_time.text = "finish"
            rNum += 1
            Log.d("timer",rNum.toString())
            restFlag = !restFlag

        }

    }

    override fun onResume() {
        Log.d("TAG","ONRESUME 온리쥼")
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
        htwdEngineManager!!.create(this@ExecuteActivity)
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
            ConfigLoader(this@ExecuteActivity)
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

        runOnUiThread {
            asr_button.isChecked = false
            asr_button.callOnClick()
            setEnabledViewsForStart(true)
        }
        checkAsrResult(str)
    }


    private fun checkAsrResult(str: String?){
        var routineNum = 0
        if (str != null && !str.isEmpty()) {
            if(str.contains("그만")){
                finishRoutine(false)
            }
            else{
                /* htwd engine 가동 */
                htwd_button.isChecked = true
                htwd_button.callOnClick()
            }

        }else{
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

    }

}
