package com.example.bugimarket

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.net.URI

class ImageAdapter(private val imageList: ArrayList<Uri>) :
    RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.image_preview)
        val closeView: ImageView = itemView.findViewById(R.id.image_close) // close 이미지의 id를 입력하세요.
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.rv_item_image, parent, false)
        return ImageViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val currentItem = imageList[position]

        Glide.with(holder.imageView.context)
            .load(currentItem)
            .into(holder.imageView)

        // close 이미지에 클릭 리스너를 설정합니다.
        holder.closeView.setOnClickListener {
            // 클릭 시 해당 아이템을 리스트에서 제거합니다.
            imageList.removeAt(position)
            // 데이터가 변경되었음을 알려줍니다.
            notifyDataSetChanged()
        }
    }

    override fun getItemCount() = imageList.size
}

