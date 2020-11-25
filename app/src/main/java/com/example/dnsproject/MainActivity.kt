package com.example.dnsproject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.example.dnsproject.exeClasses.Exercise
import com.example.dnsproject.exeClasses.Routine
import com.example.dnsproject.helper.PermissionCheck

class MainActivity : AppCompatActivity() {
    //private val filePath="/sdcard/dnsTTS/10_seconds.pcm"
    private val requestPermission=arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val TimerBut : Button = findViewById(R.id.TimerButton)
        val RecommBut : Button = findViewById(R.id.RecommButton)
        val RoutineBut : Button = findViewById(R.id.RoutineButton)
        val ScheduleBut :  Button = findViewById(R.id.ScheduleButton)
        var userID = ""

        if (intent.hasExtra("nameKey")) {
            userID = intent.getStringExtra("nameKey").toString()
            /* "nameKey"라는 이름의 key에 저장된 값이 있다면
               textView의 내용을 "nameKey" key에서 꺼내온 값으로 바꾼다 */

        } else {
            Toast.makeText(this, "전달된 이름이 없습니다", Toast.LENGTH_SHORT).show()
        }
        PermissionCheck(
            this@MainActivity,
            requestPermission
        )

        TimerBut.setOnClickListener {
            val nextIntent = Intent(this@MainActivity, StopWatchActivity::class.java)
            startActivity(nextIntent)
        }
        ScheduleBut.setOnClickListener {
            val nextIntent = Intent(this@MainActivity, ExerciseRecordActivity::class.java)
            startActivity(nextIntent)
        }
        RecommBut.setOnClickListener {
            val nextIntent = Intent(this@MainActivity, RecommendExerciseActivity::class.java)
            startActivity(nextIntent)
        }
        RoutineBut.setOnClickListener {
            val nextIntent = Intent(this@MainActivity, ManageRoutineActivity::class.java)
            nextIntent.putExtra("nameKey", userID.toString())
            startActivity(nextIntent)
        }


    }

}