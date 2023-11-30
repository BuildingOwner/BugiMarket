package com.example.bugimarket

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bugimarket.auth.LoginActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class ListItem(
    val title: String,
    val date: String,
    val price: String,
    val imageUrl: String,
    val userId: String,
    val documentId: String,
    val isSelling: Boolean
)

class ListAdapter(var itemList: List<ListItem>) :
    RecyclerView.Adapter<ListAdapter.MyViewHolder>() {

    class MyViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val title: TextView = v.findViewById(R.id.listItemTitle)
        val date: TextView = v.findViewById(R.id.listItemDate)
        val price: TextView = v.findViewById(R.id.listItemPrice)
        val isSellingText: TextView = v.findViewById(R.id.isSelling)
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
        val isSell = itemList[position].isSelling
        if(isSell){
            holder.isSellingText.text = "판매중"
        }
        else{
            holder.isSellingText.text = "판매 완료"
        }

        Glide.with(holder.image.context)
            .load(itemList[position].imageUrl)
            .into(holder.image)

        // 아이템 클릭 시 새로운 액티비티로 이동
        holder.itemView.setOnClickListener {
            val currentUser = Firebase.auth.currentUser

            val context = holder.itemView.context
            if (currentUser != null && itemList[position].userId == currentUser.uid) {
                val intent = Intent(context, ProductCorrectionActivity::class.java).apply {
                    putExtra("documentId", itemList[position].documentId)
                }
                context.startActivity(intent)
            } else {
                val intent = Intent(context, ProductViewActivity::class.java).apply {
                    putExtra("documentId", itemList[position].documentId)
                }
                context.startActivity(intent)
            }
        }
    }

}


class HomeActivity : AppCompatActivity() {
    private lateinit var adapter: ListAdapter
    private var itemList: ArrayList<ListItem> = ArrayList()
    private lateinit var recyclerView: RecyclerView
    private lateinit var homeBtn: Button
    // 드롭다운 메뉴에서 선택한 옵션을 저장할 변수
    private var selectedOption: String = ""
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

        // 드롭다운 메뉴 초기화
        homeBtn = findViewById<Button>(R.id.filterBtn)
        initDropdownMenu()

        val db = FirebaseFirestore.getInstance()


        // 실시간 업데이트를 위한 스냅샷 리스너 추가
        db.collection("items")
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    Log.w(TAG, "리스닝 실패.", exception)
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    // 기존 데이터 지우기
                    itemList.clear()

                    // 새 데이터 처리
                    for (document in snapshot.documents) {
                        val title = document.getString("title") ?: ""
                        val date = document.getString("uploadTime") ?: ""
                        val price = document.getString("price") ?: ""
                        val imageUrlList = document.get("images") as List<String>
                        val imageUrl = imageUrlList.firstOrNull() ?: ""
                        val userId = document.getString("userId") ?: ""
                        val documentId = document.id
                        val isSelling = document.getBoolean("isSelling") ?: false
                        addItemToList(title, date, price, imageUrl, userId, documentId, isSelling)
                    }

                    // 어댑터에 데이터 변경 알림
                    adapter.notifyDataSetChanged()
                }
            }

        findViewById<Button>(R.id.addItemBtn)?.setOnClickListener {
            val intent = Intent(this@HomeActivity, ProductRegistrationActivity::class.java)
            startActivity(intent)
//            finish()
        }

        // 로그아웃
        findViewById<Button>(R.id.signOutBtn)?.setOnClickListener {
            Firebase.auth.signOut()
            startActivity(
                Intent(this, LoginActivity::class.java)
            )
            finish()
        }

        val chatButton = findViewById<Button>(R.id.chatBtn)
        chatButton?.setOnClickListener {
            startActivity(
                Intent(this@HomeActivity, ViewMessageActivity::class.java)
            )
        }
    }

    // 아이템 추가 함수
    private fun addItemToList(
        title: String,
        date: String,
        price: String,
        imageUrl: String,
        userId: String,
        documentId: String,
        isSelling: Boolean
    ) {
        itemList.add(ListItem(title, date, price, imageUrl, userId, documentId, isSelling))
    }

    private fun initDropdownMenu() {
        val popupMenu = PopupMenu(this, homeBtn)
        popupMenu.menuInflater.inflate(R.menu.dropdown_menu, popupMenu.menu)

        // 드롭다운 메뉴에서 선택한 옵션을 처리하는 리스너
        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_selling -> {
                    selectedOption = "판매중"
                    filterItems()
                    true
                }
                R.id.menu_sold_out -> {
                    selectedOption = "판매 완료"
                    filterItems()
                    true
                }
                R.id.menu_all -> {
                    selectedOption = "모두 보기"
                    filterItems()
                    true
                }
                else -> false
            }
        }

        // 드롭다운 메뉴 버튼 클릭 시 팝업 메뉴 표시
        homeBtn.setOnClickListener {
            popupMenu.show()
        }
    }

    // itemList를 선택한 옵션에 따라 필터링하는 함수
    private fun filterItems() {
        val filteredList = when (selectedOption) {
            "판매중" -> itemList.filter { it.isSelling }
            "판매 완료" -> itemList.filter { !it.isSelling }
            else -> itemList
        }

        adapter.itemList = filteredList
        adapter.notifyDataSetChanged()
    }
}