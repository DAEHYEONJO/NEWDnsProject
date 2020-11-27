package com.example.dnsproject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dnsproject.adapter.ExerAdapter
import com.example.dnsproject.exeClasses.Routine
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_manage_routine.*

class ManageRoutineActivity : AppCompatActivity() {
    private lateinit var curRoutine:ArrayList<Routine>
    private lateinit var ikey:String
    private var adapter:ExerAdapter?=null
    override fun onBackPressed() {
        Log.d("db","backpressed")
        val resultIntent=Intent()
        resultIntent.putExtra("addRoutine",curRoutine)
        setResult(0,resultIntent)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_routine)


        if (intent.hasExtra("nameKey")) {
            curRoutine = intent.getSerializableExtra("nameKey") as ArrayList<Routine>
            ikey= intent.getStringExtra("IKEY").toString()
            //Toast.makeText(this, curRoutine[0].name, Toast.LENGTH_SHORT).show()

        } else {
            Toast.makeText(this, "전달된 이름이 없습니다", Toast.LENGTH_SHORT).show()
        }

        val addExerbut : FloatingActionButton = findViewById(R.id.addExerButton)
        adapter = ExerAdapter(
            curRoutine,
            LayoutInflater.from(this@ManageRoutineActivity)
        )
        routineRecyclerView.adapter = adapter        // 내 리사이클러뷰랑 어뎁터 연결고리
        routineRecyclerView.layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
        addExerbut.setOnClickListener {
            //인텐트
            if (curRoutine.size<=4){
                val nextIntent = Intent(this, MakeRoutineActivity::class.java)
                nextIntent.putExtra("nameKey", curRoutine)
                nextIntent.putExtra("IKEY",ikey)
                startActivityForResult(nextIntent,1)
                //인텐트끝
                //adapter.notifyDataSetChanged()
            }

        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode==0){
            if(data!=null){
                curRoutine= data.getSerializableExtra("addRoutine") as ArrayList<Routine>
                Log.d("db",curRoutine[0].name.toString())
                adapter = ExerAdapter(
                    curRoutine,
                    LayoutInflater.from(this@ManageRoutineActivity)
                )
                routineRecyclerView.adapter = adapter        // 내 리사이클러뷰랑 어뎁터 연결고리
                routineRecyclerView.layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
            }else{
                Log.d("db","null data")
            }
        }
    }

}