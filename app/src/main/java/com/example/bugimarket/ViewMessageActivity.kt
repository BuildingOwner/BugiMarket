package com.example.chatproject

import androidx.appcompat.widget.Toolbar
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot

data class Message(
    val message: String,
    val senderName: String,
)
class ViewMessageActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var db: FirebaseFirestore
    private lateinit var query: Query

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_message)

        val toolbar: Toolbar = findViewById(R.id.toolbarMessage)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        recyclerView = findViewById(R.id.recyclerViewMessages)
        messageAdapter = MessageAdapter(emptyList()) // Pass your message list here
        recyclerView.adapter = messageAdapter

        db = FirebaseFirestore.getInstance()
        query = db.collection("sellers/sellerId/messages")

        fetchMessages()
    }

    private fun fetchMessages() {
        query.addSnapshotListener(object : EventListener<QuerySnapshot> {
            override fun onEvent(
                snapshots: QuerySnapshot?,
                e: FirebaseFirestoreException?
            ) {
                if (e != null) {
                    return
                }

                val messageList = snapshots!!.documentChanges.mapNotNull {
                    if (it.type == DocumentChange.Type.ADDED) {
                        Message(
                            message = it.document.get("message").toString(),
                            senderName = it.document.get("senderName").toString()
                        )
                    } else {
                        null
                    }
                }
                messageAdapter.updateData(messageList)
            }
        })
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
