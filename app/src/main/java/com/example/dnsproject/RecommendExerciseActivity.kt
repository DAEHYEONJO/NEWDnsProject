package com.example.dnsproject

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_exercise_record.*
import kotlinx.android.synthetic.main.activity_recommend_exercise.*

class RecommendExerciseActivity : AppCompatActivity() {
    private lateinit var key:String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recommend_exercise)

        key = intent.getStringExtra("IKEY").toString()
        val databaseReference= FirebaseDatabase.getInstance().reference.child(key)

        var ref = databaseReference.child("fixExercise")

        var map = mutableMapOf<String, Int>()
        ref.addListenerForSingleValueEvent(object : ValueEventListener
        {
            override fun onDataChange(snapshot: DataSnapshot) {
               for(i in snapshot.children)
               {
                   map[i.key.toString()] = i.value.toString().toInt()
               }

                var sortedByValue = map.toList().sortedWith(compareBy({it.second})).toMap()
                var sortedMyValueList = sortedByValue.toList()

                recoText.text = sortedMyValueList[0].first.toString()

            }

            override fun onCancelled(error: DatabaseError) {


            }
        })
    }
}