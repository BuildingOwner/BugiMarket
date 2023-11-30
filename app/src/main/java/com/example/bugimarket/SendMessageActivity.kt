package com.example.chatproject

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth

class SendMessageActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_message)

        val toolbar: Toolbar = findViewById(R.id.toolbarSendMessage)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val sendMessageButton: Button = findViewById(R.id.sendMessageButton)

        sendMessageButton.setOnClickListener {
            sendMessage()
        }
    }


    private fun sendMessage() {
        val currentUser = auth.currentUser
        val senderId = currentUser?.uid
        val senderName = "senderName"
        val messageText: String = "구매하고 싶습니다"

        val messageData = hashMapOf(
            "sender" to senderId,
            "senderName" to senderName,
            "message" to messageText
        )

        if (senderId != null) {
            db.collection("sellers").document("sellerId")
                .collection("messages")
                .add(messageData)
                .addOnSuccessListener {
                    Toast.makeText(this, "판매자에게 메세지를 보냈습니다.", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "메세지 전송에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
        }
    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
