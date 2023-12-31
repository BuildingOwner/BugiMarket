package com.example.bugimarket

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.firebase.firestore.FirebaseFirestore

class ProductViewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_view)
        val closeButton: ImageView = findViewById(R.id.ic_close)
        closeButton.setOnClickListener {
            val intent = Intent(this@ProductViewActivity, HomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        val titleText = findViewById<TextView>(R.id.product_title_text)
        val priceText = findViewById<TextView>(R.id.product_won_text)
        val explanationText = findViewById<TextView>(R.id.product_content_text)
        val uploadText = findViewById<TextView>(R.id.product_time_text)
        val userText = findViewById<TextView>(R.id.product_owner_text)
        val viewPager = findViewById<ViewPager>(R.id.viewPager)
        val saleText = findViewById<TextView>(R.id.product_sale_text)

        val documentId = intent.getStringExtra("documentId")

        val saveButton: Button = findViewById(R.id.product_save_button)
        saveButton.setOnClickListener {
            val intent = Intent(this@ProductViewActivity, SendMessageActivity::class.java).apply {
                putExtra("documentId", documentId)
            }
            startActivity(intent)
        }

        if (documentId != null) {
            val db = FirebaseFirestore.getInstance()
            db.collection("items").document(documentId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val title = document.getString("title") ?: ""
                        val explanation = document.getString("explanation") ?: ""
                        val price = document.getString("price") ?: ""
                        val uploadTime = document.getString("uploadTime") ?: ""
                        val userId = document.getString("userId")
                        val isSelling = document.getBoolean("isSelling") ?: false
                        val images = document.get("images") as? List<String> ?: emptyList()
                        viewPager.adapter = ImageViewPagerAdapter(this, images)

                        titleText.setText(title)
                        priceText.setText(price+"원")
                        explanationText.setText(explanation)
                        uploadText.setText(uploadTime)
                        saleText.text = if (isSelling) "판매중" else "판매완료"

                        if (userId != null) {
                            db.collection("users").document(userId).get()
                                .addOnSuccessListener { userDocument ->
                                    val userName = userDocument.getString("name") ?: ""
                                    userText.setText(userName)
                                }
                        }
                    }
                }
        } else {
            // documentId가 null인 경우에 대한 처리
            finish()
        }
    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

}
