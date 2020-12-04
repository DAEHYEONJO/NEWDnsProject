package com.example.dnsproject

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.dnsproject.exeClasses.Exercise
import com.example.dnsproject.exeClasses.FixExercise
import com.example.dnsproject.exeClasses.Routine
import com.example.dnsproject.exeClasses.User
import com.google.firebase.database.*
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_login.*
import java.time.LocalDate


class LoginActivity : AppCompatActivity() {
    private val userRef = FirebaseDatabase.getInstance().reference
    private var flag = false
//   private var sp=getSharedPreferences("isLogin", Context.MODE_PRIVATE)
//   val edit : SharedPreferences.Editor=sp.edit()

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        LoginButton.setOnClickListener {
            Toast.makeText(this@LoginActivity,"로그인버튼",Toast.LENGTH_SHORT).show()
            flag=false
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {

                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (i in snapshot.children) {
                            Log.d("db",i.child("pw").toString())
                            Log.d("db", i.child("id").toString())
                            if (i.child("pw").value == PwText.text.toString() && i.child("id").value == IdText.text.toString()) {
                                Log.d("db",i.value.toString())
                                val user = i.value as HashMap<Any, User>
                                Log.d("db", "맞음 ")
                                flag =!flag
                                var userData= Gson().fromJson(i.value.toString(),User::class.java)
                                Toast.makeText(this@LoginActivity, "로그인 성공", Toast.LENGTH_LONG).show()
                                val databaseReference=FirebaseDatabase.getInstance().reference.child(i.key.toString())
                                //디폴트로 11/15, 11/18, 11/27 넣어놓음
                                var refDefalut1 = databaseReference.child("exerDate").child("2020-11-15")
                                refDefalut1.child("benchpress").setValue(2)
                                refDefalut1.child("shoulderpress").setValue(4)
                                refDefalut1.child("barbellcurls").setValue(0)
                                refDefalut1.child("deadlift").setValue(3)
                                refDefalut1.child("squat").setValue(0)
                                var refDefalut2 = databaseReference.child("exerDate").child("2020-11-18")
                                refDefalut2.child("benchpress").setValue(0)
                                refDefalut2.child("shoulderpress").setValue(3)
                                refDefalut2.child("barbellcurls").setValue(3)
                                refDefalut2.child("deadlift").setValue(0)
                                refDefalut2.child("squat").setValue(5)
                                var refDefalut3 = databaseReference.child("exerDate").child("2020-11-27")
                                refDefalut3.child("benchpress").setValue(0)
                                refDefalut3.child("shoulderpress").setValue(2)
                                refDefalut3.child("barbellcurls").setValue(2)
                                refDefalut3.child("deadlift").setValue(0)
                                refDefalut3.child("squat").setValue(3)


                                val onlyDate: LocalDate = LocalDate.now()

                                var ref = databaseReference.child("exerDate").child(onlyDate.toString())

                                ref.child("benchpress").setValue(0)
                                ref.child("shoulderpress").setValue(0)
                                ref.child("barbellcurls").setValue(0)
                                ref.child("deadlift").setValue(0)
                                ref.child("squat").setValue(0)

                                if(flag){
                                    val nextIntent = Intent(this@LoginActivity, MainActivity::class.java)
                                    nextIntent.putExtra("nameKey", userData)
                                    nextIntent.putExtra("IKEY", i.key.toString())
                                    startActivity(nextIntent)
                                }
                                break
                            }
                        }

                    }
            })
        }

        SignUpButton.setOnClickListener {
            if (IdText.text.isNotEmpty() && PwText.text.isNotEmpty()) {
                val exerciseList = ArrayList<Exercise>()
                exerciseList.add(Exercise("benchpress", "0kg", "15","2"))
                exerciseList.add(Exercise("squat", "20kg", "20","3"))

                val routineList=ArrayList<Routine>()
                routineList.add(Routine("첫번째", exerciseList))
                val fixExercise=FixExercise()
                val user = User( IdText.text.toString(),
                    PwText.text.toString(),
                    routineList,
                    fixExercise)
                userRef.push().setValue(user)
                IdText.text.clear()
                PwText.text.clear()
            }
        }
    }
}

