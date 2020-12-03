package com.example.dnsproject

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.widget.Toolbar
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
        val toolbar = findViewById(R.id.recomtoolbar) as Toolbar
        setSupportActionBar(toolbar)
        val ab = supportActionBar!!
        ab.setDisplayShowTitleEnabled(false)

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

                //sortedMyValueList[0].first.toString() == ??
                if(sortedMyValueList[0].first.toString() == "benchPressCount")
                {
                    recoText.text = "벤치프레스"
                    imageView2.setBackgroundResource(R.drawable.biceps)
                }
                else if(sortedMyValueList[0].first.toString() == "shoulderPressCount")
                {
                    recoText.text = "숄더프레스"
                    imageView2.setBackgroundResource(R.drawable.dumbbell)
                }
                else if(sortedMyValueList[0].first.toString() == "barbellCurlsCount")
                {
                    recoText.text = "바벨컬"
                    imageView2.setBackgroundResource(R.drawable.triceps)
                }
                else if(sortedMyValueList[0].first.toString() == "deadLiftCount")
                {
                    recoText.text = "데드리프트"
                    imageView2.setBackgroundResource(R.drawable.kettlebell)
                }
                else if(sortedMyValueList[0].first.toString() == "squatCount")
                {
                    recoText.text = "스쿼트"
                    imageView2.setBackgroundResource(R.drawable.squat)
                }
                else if(sortedMyValueList[0].first.toString() == "crunchCount")
                {
                    recoText.text = "크런치"
                    imageView2.setBackgroundResource(R.drawable.crunch)
                }
                else if(sortedMyValueList[0].first.toString() == "plankJumpCount")
                {
                    recoText.text = "플랭크점프"
                    imageView2.setBackgroundResource(R.drawable.plank)
                }
                else if(sortedMyValueList[0].first.toString() == "burpeeTestCount")
                {
                    recoText.text = "버피테스트"
                    imageView2.setBackgroundResource(R.drawable.burpee)
                }
                else if(sortedMyValueList[0].first.toString() == "lungeCount")
                {
                    recoText.text = "런지"
                    imageView2.setBackgroundResource(R.drawable.lunge)
                }
                else if(sortedMyValueList[0].first.toString() == "pullUpCount")
                {
                    recoText.text = "턱걸이"
                    imageView2.setBackgroundResource(R.drawable.chinning)
                }

                //recoText.text = sortedMyValueList[0].first.toString()

            }

            override fun onCancelled(error: DatabaseError) {


            }
        })
    }
}