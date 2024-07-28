package com.ozgurbaykal.ecored.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ozgurbaykal.ecored.R
import com.ozgurbaykal.ecored.databinding.ItemGalleryBinding

class GalleryImageAdapter(
    private val images: List<String>,
    private val onImageClick: (position: Int) -> Unit
) : RecyclerView.Adapter<GalleryImageAdapter.GalleryImageViewHolder>() {

    inner class GalleryImageViewHolder(val binding: ItemGalleryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(imageUrl: String) {
            Glide.with(binding.img.context)
                .load(imageUrl)
                .into(binding.img)

            binding.img.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onImageClick(position)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryImageViewHolder {
        val binding = ItemGalleryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GalleryImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GalleryImageViewHolder, position: Int) {
        holder.bind(images[position])
    }

    override fun getItemCount(): Int = images.size
}