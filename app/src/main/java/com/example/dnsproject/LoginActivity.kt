package com.example.dnsproject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LoginActivity : AppCompatActivity() {
    private val database by lazy{FirebaseDatabase.getInstance()}
    private val userRef = database.getReference("user")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val LoginBut : Button = findViewById(R.id.LoginButton)
        val SignUpBut : Button = findViewById(R.id.SignUpButton)
        val IdText : EditText = findViewById(R.id.IdText)
        val PwText : EditText = findViewById(R.id.PwText)

        LoginBut.setOnClickListener{
            FirebaseDatabase.getInstance().reference
                .child("user")
                .orderByChild("userID").equalTo(IdText.text.toString())
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {

                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        var map = snapshot.children.first().value as Map<String, Any>
                        if(map["userPW"].toString()==PwText.text.toString())
                        {
                            Toast.makeText(this@LoginActivity, "로그인 완료! "+map["userPW"].toString(), Toast.LENGTH_LONG).show()
                            //로그인 성공
                            val nextIntent = Intent(this@LoginActivity, MainActivity::class.java)
                            nextIntent.putExtra("nameKey", IdText.text.toString())
                            startActivity(nextIntent)
                        }
                        else
                        {
                            Toast.makeText(this@LoginActivity, "로그인 실패", Toast.LENGTH_LONG).show()
                        }
                    }
                })
        }

        SignUpBut.setOnClickListener{
            var map = mutableMapOf<String, Any>()
            map["userID"] = IdText.text.toString()
            map["userPW"] = PwText.text.toString()
            map["userEXER"] = ""
            map["userEXERNUM"] = 0
            userRef.push().setValue(map)


            Toast.makeText(this@LoginActivity, "회원가입 완료 아이디 : "+IdText.text.toString(), Toast.LENGTH_LONG).show()
            IdText.text.clear()
            PwText.text.clear()
        }
    }
}