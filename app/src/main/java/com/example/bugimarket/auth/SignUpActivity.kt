package com.example.bugimarket.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bugimarket.MainActivity
import com.example.bugimarket.R
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import java.util.Date

class SignUpActivity : AppCompatActivity() {
    val db: FirebaseFirestore = Firebase.firestore
    val users = db.collection("users")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sign_up_activity)
        findViewById<Button>(R.id.signUpBtn)?.setOnClickListener {
            val username = findViewById<EditText>(R.id.signUpName)?.text.toString()
            val userEmail = findViewById<EditText>(R.id.signUpEmail)?.text.toString()
            val password = findViewById<EditText>(R.id.signUpPassword)?.text.toString()
            val datePicker = findViewById<DatePicker>(R.id.signUpDate)
            val birthDay =
                datePicker.year.toString() + "/" + (datePicker.month + 1).toString() + "/" + datePicker.dayOfMonth.toString()

            signUp(username, userEmail, password, birthDay)

        }
    }

    //회원가입 함수
    fun signUp(name: String, email: String, password: String, birthDay: String) {
        Firebase.auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    //Firebase DB에 저장 되어 있는 계정이 아닐 경우
                    //입력한 계정을 새로 등록한다
                    Firebase.auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this) { // it: Task<AuthResult!>
                            if (it.isSuccessful) {
                                val user = Firebase.auth.currentUser
                                user?.let {
                                    val uid = it.uid
                                    val userData = hashMapOf(
                                        "name" to name,
                                        "birthDay" to birthDay
                                    )

                                    users.document(uid).set(userData)
                                        .addOnSuccessListener {
                                            Log.d(
                                                "SignUpActivity",
                                                "DocumentSnapshot successfully written!"
                                            )
                                        }
                                        .addOnFailureListener { e ->
                                            Log.w(
                                                "SignUpActivity",
                                                "Error writing document",
                                                e
                                            )
                                        }
                                }
                            } else {
                                Log.w("LoginActivity", "signInWithEmail", it.exception)
                                Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT)
                                    .show()
                            }
                            startActivity(
                                Intent(this, MainActivity::class.java)
                            )
                            finish()
                        }
                } else if (task.exception?.message.isNullOrEmpty()) {
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                } else {
                    //입력한 계정 정보가 이미 Firebase DB에 있는 경우
                    Toast.makeText(this, "중복된 이메일입니다.", Toast.LENGTH_LONG).show()
                }
            }
    }
}