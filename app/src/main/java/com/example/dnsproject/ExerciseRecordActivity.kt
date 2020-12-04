package com.example.dnsproject

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_exercise_record.*
import kotlinx.android.synthetic.main.activity_make_routine.*

class ExerciseRecordActivity : AppCompatActivity() {

    private lateinit var key:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercise_record)
        val toolbar = findViewById(R.id.recordtoolbar) as Toolbar
        setSupportActionBar(toolbar)
        val ab = supportActionBar!!
        ab.setDisplayShowTitleEnabled(false)

        key = intent.getStringExtra("IKEY").toString()
        val databaseReference= FirebaseDatabase.getInstance().reference.child(key)


        //i3일  i2월 i년
        MyCalendar.setOnDateChangeListener{ MyCalendar, i, i2, i3 ->
            //Toast.makeText(this@ExerciseRecordActivity, "Selected Date:$i3/$i2/$i", Toast.LENGTH_LONG).show()
            //i3는 일자   i2는 월

            var monthNum=i2+1
            var darStr = i3.toString()
            var monthStr = monthNum.toString()

            if(i3<10) darStr = "0$darStr"
            if(monthNum<10) monthStr = "0$monthStr"

            var ref = databaseReference.child("exerDate").child(i.toString()+"-"+monthStr+"-"+darStr)
            ref.addListenerForSingleValueEvent(object : ValueEventListener
            {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d("윤성테스트", snapshot.value.toString())
                    recordText.text=""
                    for(i in snapshot.children)
                    {
                        if(i.value.toString()!="0")
                        {
                            Log.d("윤성테스트2", i.key.toString()+"  "+i.value.toString())
                            recordText.text = recordText.text.toString() + i.key.toString()+"운동을 "+i.value.toString()+"세트 했습니다.\n"
                        }

                    }
                    if(recordText.text.toString()=="")
                    {
                        recordText.text = "이 날은 운동을 한 기록이 없네요"
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("윤성테스트", "nothing")

                }
            })

        }

    }
}