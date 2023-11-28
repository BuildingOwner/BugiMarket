package com.example.bugimarket

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import com.example.bugimarket.auth.LoginActivity
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

        val uid = Firebase.auth.currentUser?.uid
        if (uid != null) {
            startActivity(
                Intent(this, HomeActivity::class.java)
            )
            finish()
        }

        findViewById<Button>(R.id.button_signout)?.setOnClickListener {
            Firebase.auth.signOut()
            finish()
        }
    }
}
