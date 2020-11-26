package com.example.dnsproject

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_exercise_record.*

class ExerciseRecordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercise_record)
        MyCalendar.setOnDateChangeListener{ MyCalendar, i, i2, i3 ->
            Toast.makeText(this@ExerciseRecordActivity, "Selected Date:$i3/$i2/$i", Toast.LENGTH_LONG).show()
        }
    }
}