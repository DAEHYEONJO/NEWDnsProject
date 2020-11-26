package com.example.dnsproject

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.dnsproject.exeClasses.Exercise
import com.example.dnsproject.exeClasses.Routine
import kotlinx.android.synthetic.main.activity_execute.*
import java.util.ArrayList
import kotlin.properties.Delegates


/*
    routine을 실행하는 Activity
 */
class ExecuteActivity : AppCompatActivity() {
    val COUNTTIME : Long = 500 //운동 count시간 0.5초로 해둠
    lateinit var mRoutine:Routine
    lateinit var routineArray: ArrayList<Exercise>
    private lateinit var myTimer : MyTimer
    private var routineSize:Int = 3
    var rNum : Int = 0

    var restFlag : Boolean by Delegates.observable(true, { _, old, new ->
        if(rNum>=routineSize){
            //routine 끝!
            Toast.makeText(this@ExecuteActivity,
                "루틴 끝임 알아서 새로운 창 띄워서 보여줘유", Toast.LENGTH_SHORT).show()


        }
        else{
            if(restFlag){ // 휴식타이머
                rNum--
                //tts 휴식 시작합니다
                current_action.text = "휴식^^"
                myTimer = MyTimer(3000, 1000)
                myTimer.start()
            }
            else{
                current_action.text = routineArray[rNum].name
                Log.d("FFINDD", "start $rNum action 운동")
                //tts routineArray[rNum].name운동 시작합니다
                val futureTime = routineArray[rNum].count.toLong()*COUNTTIME
                myTimer = MyTimer(futureTime, COUNTTIME)
                myTimer.start()
            }
        }

    })


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_execute)
        mRoutine = intent.getSerializableExtra("routine") as Routine
        //Toast.makeText(this,mRoutine.name, Toast.LENGTH_SHORT).show()
        routineArray = ArrayList()
        for(i in 0 until mRoutine.exerciseList.size)
        {
            for(j in 0 until mRoutine.exerciseList[i].setCount.toInt()){
                routineArray.add(mRoutine.exerciseList[i])
            }
        }
        routineSize = routineArray.size
        restFlag = false // 루틴시작

    }

    inner class MyTimer(
        millisInFuture: Long,
        countDownInterval: Long
    ) :
        CountDownTimer(millisInFuture, countDownInterval) {
        override fun onTick(millisUntilFinished: Long) {
            if(restFlag)//휴식 타이머 일때
            {
                if(millisUntilFinished.equals(10000)){
                    //tts 10초 남았습니다.
                }
                remain_time.text = (millisUntilFinished / 1000.toLong()).toString() + " 초"
            }
            else{//운동 타이머
                remain_time.text = (millisUntilFinished / COUNTTIME.toLong()).toString() + " 세트"
            }

        }

        override fun onFinish() {
            remain_time.text = "finish"
            rNum += 1
            restFlag = !restFlag
        }

    }

}