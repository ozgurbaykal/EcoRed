package com.ozgurbaykal.ecored.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ozgurbaykal.ecored.databinding.ItemProductImageBinding

class ProductImageAdapter(private val images: List<String>) : RecyclerView.Adapter<ProductImageAdapter.ProductImageViewHolder>() {

    inner class ProductImageViewHolder(val binding: ItemProductImageBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductImageViewHolder {
        val binding = ItemProductImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductImageViewHolder, position: Int) {
        val imageUrl = images[position]
        Glide.with(holder.binding.productImage.context)
            .load(imageUrl)
            .into(holder.binding.productImage)
    }

    override fun getItemCount(): Int = images.size
}
