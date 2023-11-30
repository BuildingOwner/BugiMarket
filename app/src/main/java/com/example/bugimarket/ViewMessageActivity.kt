package com.example.bugimarket

import android.content.ContentValues.TAG
import androidx.appcompat.widget.Toolbar
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.DialogTitle
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

data class Message(
    val message: String,
    val senderName: String,
    val title: String
)

class ViewMessageActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_message)

        val toolbar: Toolbar = findViewById(R.id.toolbarMessage)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        recyclerView = findViewById(R.id.recyclerViewMessages)
        messageAdapter = MessageAdapter(emptyList())
        recyclerView.adapter = messageAdapter

        auth = FirebaseAuth.getInstance()

        val currentUser = auth.currentUser
        val userId = currentUser?.uid

        db = FirebaseFirestore.getInstance()

        if (userId != null) {
            fetchMessages(userId)
        } else {
            // Handle the case where the user is not signed in.
        }
    }

    private fun fetchMessages(userId: String) {
        val messages = mutableListOf<Message>()
        db.collection("messages")
            .whereEqualTo("sellerId", userId)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshots!!.isEmpty) {
                    Log.w(TAG, "No matching documents.")
                    return@addSnapshotListener
                }

                Log.w(TAG, "messages")
                for (doc in snapshots!!) {
                    val message = doc.getString("message")
                    val senderName = doc.getString("senderName")
                    val title = doc.getString("title")
                    if (message != null && senderName != null && title != null) {
                        messages.add(Message(message, senderName, title))
                    }
                }
                messageAdapter.updateData(messages)
            }
    }


    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}

