package com.example.bugimarket

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

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
        var senderName = "senderName"
        val messageText: String = "구매하고 싶습니다"
        var sellerId: String = ""
        val documentId = intent.getStringExtra("documentId")

        val user = Firebase.auth.currentUser
        user?.let {
            val uid = it.uid

            val docRef = db.collection("users").document(uid)
            docRef.get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        senderName = document.getString("name").toString()

                        val docRef = db.collection("items").document(documentId.toString())

                        docRef.get()
                            .addOnSuccessListener { document2 ->
                                if (document2 != null) {
                                    sellerId = document2.getString("userId").toString()
                                    val title = document2.getString("title").toString()

                                    val messageData = hashMapOf(
                                        "itemId" to documentId,
                                        "sellerId" to sellerId,
                                        "sender" to senderId,
                                        "senderName" to senderName,
                                        "message" to messageText,
                                        "title" to title
                                    )

                                    if (senderId != null) {
                                        db.collection("messages")
                                            .add(messageData)
                                            .addOnSuccessListener { documentReference ->
                                                Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
                                                Toast.makeText(this, "판매자에게 메세지를 보냈습니다.", Toast.LENGTH_SHORT).show()
                                            }
                                            .addOnFailureListener { e ->
                                                Toast.makeText(this, "메세지 전송에 실패했습니다.", Toast.LENGTH_SHORT).show()
                                            }
                                    }
                                } else {
                                    Log.d(TAG, "No such document")
                                }
                            }
                            .addOnFailureListener { exception ->
                                Log.d(TAG, "get failed with ", exception)
                            }

                    } else {
                        Log.d(TAG, "No such document")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "get failed with ", exception)
                }
        }
    }


    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
