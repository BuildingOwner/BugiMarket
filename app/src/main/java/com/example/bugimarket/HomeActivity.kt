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

class ListItem(val docId: String, val title: String, val date: String, val price: String, val imageUrl: String, val userId: String)
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
        holder.title.text = itemList[position].title
        holder.date.text = itemList[position].date
        holder.price.text = itemList[position].price

        Glide.with(holder.image.context)
            .load(itemList[position].imageUrl)
            .into(holder.image)

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val currentUserId = Firebase.auth.currentUser?.uid
            val itemUserId = itemList[position].userId

            if (currentUserId == itemUserId) {
                val intent = Intent(context, ProductViewActivity::class.java)
                intent.putExtra("docId", itemList[position].docId)
                context.startActivity(intent)
            } else {
                // TODO: 다른 액티비티로 이동하는 코드를 추가해주세요.
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
                    val docId = document.id
                    val title = document.getString("title") ?: ""
                    val date = document.getString("uploadTime") ?: ""
                    val price = document.getString("price") ?: ""
                    val imageUrlList = document.get("images") as List<String>
                    val imageUrl = imageUrlList.firstOrNull() ?: ""
                    val userId = document.getString("userId") ?: ""
                    addItemToList(docId, title, date, price, imageUrl, userId)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }

        findViewById<Button>(R.id.addItemBtn)?.setOnClickListener {
            val intent = Intent(this@HomeActivity, ProductRegistrationActivity::class.java)
            startActivity(intent)
        }

        // 로그아웃
        findViewById<Button>(R.id.signOutBtn)?.setOnClickListener {
            Firebase.auth.signOut()
            finish()
        }
    }

    // 아이템 추가 함수
    private fun addItemToList(docId: String, title: String, date: String, price: String, imageUrl: String, userId: String) {
        itemList.add(ListItem(docId, title, date, price, imageUrl, userId))
    }
}