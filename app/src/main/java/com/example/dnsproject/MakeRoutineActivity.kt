package com.example.dnsproject

import android.R
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dnsproject.R.*
import com.example.dnsproject.adapter.MakeAdapter
import com.example.dnsproject.exeClasses.Exercise
import com.example.dnsproject.exeClasses.Routine
import com.example.dnsproject.exeClasses.User
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_make_routine.*
import kotlinx.android.synthetic.main.activity_manage_routine.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class MakeRoutineActivity : AppCompatActivity() {

    private lateinit var curRoutine:ArrayList<Routine>
    private lateinit var addedExerciseList:ArrayList<Exercise>
    private lateinit var exeName:String
    private lateinit var exeCount:String
    private lateinit var exeSetCount:String
    private lateinit var exeWeight:String
    private lateinit var ikey:String

    override fun onBackPressed() {
        Log.d("db","backpressed")
        val resultIntent=Intent()
        resultIntent.putExtra("addRoutine",curRoutine)
        setResult(0,resultIntent)
        finish()
    }

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_make_routine)
        addedExerciseList=ArrayList<Exercise>()
        if (intent.hasExtra("nameKey")) {
            curRoutine = intent.getSerializableExtra("nameKey") as ArrayList<Routine>
            ikey=intent.getStringExtra("IKEY") as String
            curRoutine.add(Routine("default", addedExerciseList))
            /* "nameKey"라는 이름의 key에 저장된 값이 있다면
               textView의 내용을 "nameKey" key에서 꺼내온 값으로 바꾼다 */
            //Toast.makeText(this, curRoutine[0].name, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "전달된 이름이 없습니다", Toast.LENGTH_SHORT).show()
        }

        exerSpinner.adapter=ArrayAdapter<String>(
            this, R.layout.simple_spinner_item, resources.getStringArray(
                array.exercise_entries
            )
        )
        exerSpinner.onItemSelectedListener=object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                exeName=p0?.getItemAtPosition(p2) as String
                when(exeName){
                    "벤치프레스"->{exeName="benchpress"}
                    "숄더프레스"->{exeName="shoulderpress"}
                    "바벨컬"->{exeName="barbellcurls"}
                    "데드리프트"->{exeName="deadlift"}
                    "스쿼트"->{exeName="squat"}
                    "크런치"->{exeName="crunch"}
                    "플랭크점프"->{exeName="plankJump"}
                    "버피테스트"->{exeName="burpee"}
                    "런지"->{exeName="lunge"}
                    "턱걸이"->{exeName="pullUp"}
                    else->{exeName="없음"}
                }
                Toast.makeText(this@MakeRoutineActivity, exeName, Toast.LENGTH_SHORT).show()
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
        }

        setSpinner.adapter=ArrayAdapter<String>(
            this, R.layout.simple_spinner_dropdown_item, resources.getStringArray(
                array.set_entries
            )
        )
        setSpinner.onItemSelectedListener=object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                exeSetCount=p0?.getItemAtPosition(p2) as String
                exeSetCount=exeSetCount.slice(IntRange(0, 0))
                Toast.makeText(this@MakeRoutineActivity, exeSetCount, Toast.LENGTH_SHORT).show()
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
        }
        numSpinner.adapter=ArrayAdapter<String>(
            this, R.layout.simple_spinner_dropdown_item, resources.getStringArray(
                array.num_entries
            )
        )
        numSpinner.onItemSelectedListener=object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                exeCount=p0?.getItemAtPosition(p2) as String
                exeCount=exeCount.slice(IntRange(0, 1))
                Toast.makeText(this@MakeRoutineActivity, exeCount, Toast.LENGTH_SHORT).show()
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
        }

        weightSpinner.adapter=ArrayAdapter<String>(
            this, R.layout.simple_spinner_dropdown_item, resources.getStringArray(
                array.weight_entries
            )
        )
        weightSpinner.onItemSelectedListener=object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                exeWeight=p0?.getItemAtPosition(p2) as String
                exeWeight=exeWeight.slice(IntRange(0, 2))
                Toast.makeText(this@MakeRoutineActivity, exeWeight, Toast.LENGTH_SHORT).show()
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
        }


        addExercise.setOnClickListener {
            //파베에 저장.
            if(addedExerciseList.size<5)
                addedExerciseList.add(Exercise(exeName, exeWeight, exeCount, exeSetCount))
            //리스트 채우기
            savedExerlistView.visibility=View.VISIBLE
            textview.visibility=View.GONE
            val adapter = MakeAdapter(
                addedExerciseList,
                LayoutInflater.from(this@MakeRoutineActivity)
            )
            savedExerlistView.layoutManager = LinearLayoutManager(
                this,
                LinearLayoutManager.VERTICAL,
                false
            )
            savedExerlistView.adapter = adapter        // 내 리사이클러뷰랑 어뎁터 연결고리
            
            if(addedExerciseList.size<=5){
                curRoutine[curRoutine.size - 1].exerciseList=addedExerciseList
                curRoutine[curRoutine.size - 1].name=getRoutineName(curRoutine.size - 1)
                Log.d("db", curRoutine[curRoutine.size - 1].name.toString())
                Log.d(
                    "db",
                    curRoutine[curRoutine.size - 1].exerciseList[addedExerciseList.size - 1].name
                )
                //리스트 채우기 끝
                val databaseReference=FirebaseDatabase.getInstance().reference.child(ikey)
                val croutine: MutableMap<String, ArrayList<Routine>> = HashMap()
                croutine["routine"]=curRoutine
                databaseReference.updateChildren(croutine as Map<String, ArrayList<Routine>>)
            }
            //파베에 저장 끝.
        }
    }
    fun getRoutineName(routineSize: Int):String{
        when(routineSize){
            0 -> {
                return "첫번째"
            }
            1 -> {
                return "두번째"
            }
            2 -> {
                return "세번째"
            }
            3 -> {
                return "네번째"
            }
            else->{return "no"}

        }
    }

}