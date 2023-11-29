package com.example.bugimarket

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore

class ProductCorrectionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_correction)
        val closeButton: ImageView = findViewById(R.id.ic_close)
        closeButton.setOnClickListener {
            val intent = Intent(this@ProductCorrectionActivity, HomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }


        val saleSwitch = findViewById<Switch>(R.id.sale_switch)
        val saleIndicator = findViewById<TextView>(R.id.sale_state_indicator)
        val imageRecyclerView = findViewById<RecyclerView>(R.id.recyclerview_image)
        val updateButton: Button = findViewById(R.id.product_update_button)

        saleSwitch.setOnCheckedChangeListener { _, isChecked ->
            saleIndicator.text = if (isChecked) "판매중" else "판매완료"
        }

        // 데이터를 표시할 EditText 가져오기
        val titleEditText = findViewById<EditText>(R.id.title_edit)
        val priceEditText = findViewById<EditText>(R.id.price_edit)
        val explanationEditText = findViewById<EditText>(R.id.explanation_edit)

        // 사용자가 값을 변경하지 못하게 하기
        titleEditText.isEnabled = false
        explanationEditText.isEnabled = false

        // Intent에서 documentId 가져오기
        val documentId = intent.getStringExtra("documentId")

        if (documentId != null) {
            val db = FirebaseFirestore.getInstance()
            db.collection("items").document(documentId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val title = document.getString("title") ?: ""
                        val price = document.getString("price") ?: ""
                        val explanation = document.getString("explanation") ?: ""
                        val saleState = document.getBoolean("isSelling") ?: true
                        val imageUrlList = document.get("images") as List<String>

                        // 각 필드에 데이터 설정
                        titleEditText.setText(title)
                        priceEditText.setText(price)
                        explanationEditText.setText(explanation)
                        saleSwitch.isChecked = saleState

                        // 이미지 RecyclerView 설정
                        imageRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
                        imageRecyclerView.adapter = ImageAdapter(imageUrlList)
                    }
                }

            // 수정 버튼 클릭 리스너 설정
            updateButton.setOnClickListener {
                val price = priceEditText.text.toString()
                val saleState = saleIndicator.text.toString() == "판매중"
                val updates = hashMapOf<String, Any>(
                    "price" to price,
                    "isSelling" to saleState
                )
                db.collection("items").document(documentId)
                    .update(updates)
                    .addOnSuccessListener {
                        Toast.makeText(this, "데이터가 업데이트되었습니다.", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, HomeActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "데이터 업데이트에 실패했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        } else {
            finish()
        }
    }

    // 어댑터
    class ImageAdapter(private val imageUrlList: List<String>) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {
        inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val imageView: ImageView = itemView.findViewById(R.id.image_preview)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.rv_item_image, parent, false)
            return ImageViewHolder(view)
        }

        override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
            val imageUrl = imageUrlList[position]
            Glide.with(holder.imageView.context).load(imageUrl).into(holder.imageView)
        }

        override fun getItemCount(): Int {
            return imageUrlList.size
        }
    }
}
