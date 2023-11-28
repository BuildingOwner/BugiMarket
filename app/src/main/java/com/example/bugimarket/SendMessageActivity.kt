package com.example.bugimarket

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.firestore.FirebaseFirestore

class SendMessageActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()

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
        val sellerId = "sellerId" // 바꾸기

        val messageText: String = "구매하고 싶습니다"

        val senderId = "senderId"

        db.collection("sellers").document(sellerId)
            .collection("messages")
            .whereEqualTo("sender", senderId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    val message = Message(messageText, senderId)

                    db.collection("sellers").document(sellerId)
                        .collection("messages")
                        .add(message)
                    Toast.makeText(this, "판매자에게 메세지를 보냈습니다.", Toast.LENGTH_SHORT).show()

                } else {
                    Toast.makeText(this, "이미 메세지를 전송했습니다", Toast.LENGTH_SHORT).show()
                }
            }
    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
