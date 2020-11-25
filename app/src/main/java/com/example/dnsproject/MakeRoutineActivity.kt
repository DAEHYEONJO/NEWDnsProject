package com.example.dnsproject

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextClock
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_make_routine.*
import kotlinx.android.synthetic.main.activity_manage_routine.*
import kotlinx.android.synthetic.main.make_layout.*
import kotlinx.android.synthetic.main.make_layout.btnOk
import org.w3c.dom.Text
import java.io.DataOutputStream
import java.io.FileReader
import java.lang.Exception


class MakeRoutineActivity : AppCompatActivity() {

    private val database by lazy{ FirebaseDatabase.getInstance()}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_make_routine)


        //intent
        var userID = ""

        if (intent.hasExtra("nameKey")) {
            userID = intent.getStringExtra("nameKey").toString()
            /* "nameKey"라는 이름의 key에 저장된 값이 있다면
               textView의 내용을 "nameKey" key에서 꺼내온 값으로 바꾼다 */
            Toast.makeText(this, userID, Toast.LENGTH_SHORT).show()


        } else {
            Toast.makeText(this, "전달된 이름이 없습니다", Toast.LENGTH_SHORT).show()
        }

        //리스트 채우기
        val mylist = arrayListOf<String>();
        val adapter = MakeAdapter(mylist)
        savedExerlistView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false)
        savedExerlistView.adapter = adapter        // 내 리사이클러뷰랑 어뎁터 연결고리
        //리스트 채우기 끝

        val builder = DialogFrag.Builder(this)
        var exerName = ""
        var exerNum = 0
        var exerSet = 0


        val dialogView = layoutInflater.inflate(R.layout.make_layout, null)
        btnOk.setOnClickListener {
           builder.create().setOkButton(View.OnClickListener {
               exerName = builder.dialog.exerSpinner.selectedItem.toString()
               exerNum = builder.dialog.numSpinner.selectedItem.toString().toInt()
               exerSet = builder.dialog.setSpinner.selectedItem.toString().toInt()
               builder.dismissDialog()
               Toast.makeText(this, exerName+" "+exerNum.toString()+"  "+exerSet.toString(), Toast.LENGTH_SHORT).show()
               mylist.add(exerName+" "+exerNum.toString()+"  "+exerSet.toString())

               adapter.mData = mylist
               adapter.notifyDataSetChanged()
           })
               .show()

        }
        saveButton.setOnClickListener {
            //파베에 저장.

            //파베에 저장 끝.
            val nextIntent = Intent(this, ManageRoutineActivity::class.java)
            nextIntent.putExtra("nameKey", userID.toString())
            startActivity(nextIntent)
        }
    }
}