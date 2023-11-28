package com.example.bugimarket

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import com.example.bugimarket.auth.LoginActivity
import com.example.chatproject.SendMessageActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (Firebase.auth.currentUser == null) {
            startActivity(
                Intent(this, LoginActivity::class.java)
            )
            finish()
        }
        findViewById<TextView>(R.id.textUID)?.text = Firebase.auth.currentUser?.uid ?: "No User"

        val db: FirebaseFirestore = Firebase.firestore
        val users = db.collection("users")

        val sendMessageButton: Button = findViewById(R.id.sendButton)
        val viewMessagesButton: Button = findViewById(R.id.viewMessagesButton)

        val uid = Firebase.auth.currentUser?.uid
        if (uid != null) {
            startActivity(
                Intent(this, HomeActivity::class.java)
            )
            finish()
        }

        sendMessageButton.setOnClickListener {
            // 판매 글 목록에서 판매자에게 메시지 보내기 버튼을 클릭하면 SendMessageActivity로 이동
            val intent = Intent(this, SendMessageActivity::class.java)
            startActivity(intent)
        }

        viewMessagesButton.setOnClickListener {
            // 판매 글 목록에서 메시지 보기 버튼을 클릭하면 ViewMessageActivity로 이동
            val intent = Intent(this, ViewMessageActivity::class.java)
            startActivity(intent)
        }

        findViewById<Button>(R.id.button_signout)?.setOnClickListener {
            Firebase.auth.signOut()
            finish()
        }
    }
}
