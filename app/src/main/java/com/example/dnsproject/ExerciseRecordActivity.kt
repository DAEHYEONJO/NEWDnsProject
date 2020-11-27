package com.example.dnsproject

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
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

        key = intent.getStringExtra("IKEY").toString()
        val databaseReference= FirebaseDatabase.getInstance().reference.child(key)


        //i3일  i2월 i년
        MyCalendar.setOnDateChangeListener{ MyCalendar, i, i2, i3 ->
            Toast.makeText(this@ExerciseRecordActivity, "Selected Date:$i3/$i2/$i", Toast.LENGTH_LONG).show()
            var ref = databaseReference.child("exerDate").child(i.toString()+"-"+(i2.toInt()+1).toString()+"-"+i3.toString())
            ref.addListenerForSingleValueEvent(object : ValueEventListener
            {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d("윤성테스트", snapshot.value.toString())
                    for(i in snapshot.children)
                    {
                        if(i.value!="0")
                        {
                            Log.d("윤성테스트2", i.key.toString()+"  "+i.value.toString())
                            recordText.text = recordText.text.toString() + i.key.toString()+"운동을 "+i.value.toString()+"세트 했습니다.\n"
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("윤성테스트", "nothing")

                }
            })

        }

    }
}