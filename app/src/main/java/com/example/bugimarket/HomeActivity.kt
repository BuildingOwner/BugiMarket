package com.example.bugimarket

import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class ListItem(val userId: String, val title: String, val date: String, val price: String, val imageUrl: String)
class ListAdapter(val itemList: List<ListItem>) :
    RecyclerView.Adapter<ListAdapter.MyViewHolder>() {

    class MyViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val title: TextView = v.findViewById(R.id.listItemTitle)
        val date: TextView = v.findViewById(R.id.listItemDate)
        val price: TextView = v.findViewById(R.id.listItemPrice)
        val image: ImageView = v.findViewById(R.id.listItemImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListAdapter.MyViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.home_list_item, parent, false)
        val viewHolder = MyViewHolder(view)
        return viewHolder
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: ListAdapter.MyViewHolder, position: Int) {
        val item = itemList[position]
        holder.title.text = item.title
        holder.date.text = item.date
        holder.price.text = item.price

        Glide.with(holder.image.context)
            .load(item.imageUrl)
            .into(holder.image)

        // 항목 클릭 리스너 설정
        holder.itemView.setOnClickListener {
            val currentUser = Firebase.auth.currentUser
            if (currentUser != null && item.userId == currentUser.uid) {
                // userId가 현재 로그인한 사용자의 ID와 같다면 ProductViewActivity로 이동
                val intent = Intent(holder.itemView.context, ProductCorrectionActivity::class.java)
                holder.itemView.context.startActivity(intent)
            } else {
                // userId가 현재 로그인한 사용자의 ID와 다르다면 ProductViewActivity로 이동
                val intent = Intent(holder.itemView.context, ProductViewActivity::class.java)
                holder.itemView.context.startActivity(intent)
            }
        }
    }

}


class HomeActivity : AppCompatActivity() {
    private lateinit var adapter: ListAdapter
    private var itemList: ArrayList<ListItem> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_activity)

        // 리사이클러뷰와 어댑터 연결
        val recyclerView = findViewById<RecyclerView>(R.id.itemList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ListAdapter(itemList)
        recyclerView.adapter = adapter
        val dividerItemDecoration =
            DividerItemDecoration(recyclerView.context, LinearLayoutManager(this).orientation)
        recyclerView.addItemDecoration(dividerItemDecoration)

        val db = FirebaseFirestore.getInstance()

        db.collection("items")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val userId = document.getString("userId") ?: ""
                    val title = document.getString("title") ?: ""
                    val date = document.getString("uploadTime") ?: ""
                    val price = document.getString("price") ?: ""
                    val imageUrlList = document.get("images") as List<String>
                    val imageUrl = imageUrlList.firstOrNull() ?: ""
                    addItemToList(userId, title, date, price, imageUrl)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }

        findViewById<Button>(R.id.addItemBtn)?.setOnClickListener {
            val intent = Intent(this@HomeActivity, ProductRegistrationActivity::class.java)
            startActivity(intent)
            finish()
        }

        // 로그아웃
        findViewById<Button>(R.id.signOutBtn)?.setOnClickListener {
            Firebase.auth.signOut()
            finish()
        }

        val chatButton = findViewById<Button>(R.id.chatBtn)
        chatButton?.setOnClickListener {
            Log.d("HomeActivity", "chatBtn clicked")
            startActivity(
                Intent(this@HomeActivity, ViewMessageActivity::class.java)
            )
        }
    }

    // 아이템 추가 함수
    private fun addItemToList(userId: String, title: String, date: String, price: String, imageUrl: String) {
        itemList.add(ListItem(userId, title, date, price, imageUrl))
    }
}