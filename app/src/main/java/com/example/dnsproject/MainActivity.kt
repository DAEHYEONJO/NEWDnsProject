package com.example.dnsproject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.example.dnsproject.exeClasses.Exercise
import com.example.dnsproject.exeClasses.Routine
import com.example.dnsproject.exeClasses.User
import com.example.dnsproject.helper.PermissionCheck
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_make_routine.*

class MainActivity : AppCompatActivity() {
    //private val filePath="/sdcard/dnsTTS/10_seconds.pcm"
    private val requestPermission=arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
    private lateinit var userData: User
    private lateinit var routineList:ArrayList<Routine>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //아 배고파..

        if (intent.hasExtra("nameKey")) {
            userData = intent.getSerializableExtra("nameKey") as User
            /* "nameKey"라는 이름의 key에 저장된 값이 있다면
               textView의 내용을 "nameKey" key에서 꺼내온 값으로 바꾼다 --????????????*/
            routineList =userData.routine
            Log.d("db","rlist : "+routineList[0].name)

        } else {
            Toast.makeText(this, "전달된 이름이 없습니다", Toast.LENGTH_SHORT).show()
        }
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
            startActivity(nextIntent)
        }
        RecommButton.setOnClickListener {
            val nextIntent = Intent(this@MainActivity, RecommendExerciseActivity::class.java)
            startActivity(nextIntent)
        }
        RoutineButton.setOnClickListener {
            val nextIntent = Intent(this@MainActivity, ManageRoutineActivity::class.java)
            nextIntent.putExtra("nameKey", routineList)
            startActivity(nextIntent)
        }


    }

}