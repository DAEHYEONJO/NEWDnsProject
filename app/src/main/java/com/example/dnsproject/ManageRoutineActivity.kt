package com.example.dnsproject

import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dnsproject.exeClasses.Routine
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_manage_routine.*
import java.io.DataInput
import java.io.DataInputStream

class ManageRoutineActivity : AppCompatActivity() {
    private val database by lazy{FirebaseDatabase.getInstance()}
    private val userRef = database.getReference("user")
    private lateinit var curRoutine:ArrayList<Routine>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_routine)


        if (intent.hasExtra("nameKey")) {
            curRoutine = intent.getSerializableExtra("nameKey") as ArrayList<Routine>
            Toast.makeText(this, curRoutine[0].name, Toast.LENGTH_SHORT).show()
            /* "nameKey"라는 이름의 key에 저장된 값이 있다면
               textView의 내용을 "nameKey" key에서 꺼내온 값으로 바꾼다 */

        } else {
            Toast.makeText(this, "전달된 이름이 없습니다", Toast.LENGTH_SHORT).show()
        }

        val addExerbut : FloatingActionButton = findViewById(R.id.addExerButton)
        val adapter = ExerAdapter(curRoutine, LayoutInflater.from(this@ManageRoutineActivity))
        routineRecyclerView.adapter = adapter        // 내 리사이클러뷰랑 어뎁터 연결고리
        routineRecyclerView.layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
        addExerbut.setOnClickListener {

            //mylist.add(mylist.size+1)
            //adapter.mData = mylist

            //인텐트
            val nextIntent = Intent(this, MakeRoutineActivity::class.java)
            //nextIntent.putExtra("nameKey", userID)
            startActivity(nextIntent)
            //인텐트끝
            //adapter.notifyDataSetChanged()
        }
    }
}