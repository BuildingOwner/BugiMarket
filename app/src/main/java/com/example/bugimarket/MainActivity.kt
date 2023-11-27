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
            val docRef = users.document(uid)
            docRef.get().addOnSuccessListener { document ->
                if (document != null) {
                    val name = document.getString("name")
                    val birthDay = document.getString("birthDay")
                    // 이제 name과 birthDay를 사용할 수 있습니다.
                    findViewById<TextView>(R.id.userName)?.text = name
                    findViewById<TextView>(R.id.userBirthDay)?.text = birthDay
                } else {
                    Log.d("MainActivity", "No such document")
                }
            }.addOnFailureListener { exception ->
                Log.d("MainActivity", "get failed with ", exception)
            }
        }

        findViewById<Button>(R.id.button_signout)?.setOnClickListener {
            Firebase.auth.signOut()
            finish()
        }
    }
}
