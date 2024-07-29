package com.ozgurbaykal.ecored.view.adapter

import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ozgurbaykal.ecored.R
import com.ozgurbaykal.ecored.databinding.ProductListItemBinding
import com.ozgurbaykal.ecored.model.Product
import com.ozgurbaykal.ecored.view.ProductViewActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ListAdapter
class ProductBigListAdapter() : ListAdapter<Product, ProductBigListAdapter.ProductBigListViewHolder>(ProductDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductBigListViewHolder {
        val binding = ProductListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductBigListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductBigListViewHolder, position: Int) {
        val product = getItem(position)
        holder.bind(product)
    }

    class ProductBigListViewHolder(private val binding: ProductListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(product: Product) {
            with(binding) {
                productDiscountedPrice.visibility = View.GONE
                productDiscountPercentageLayout.visibility = View.GONE
                productPrice.paintFlags = productPrice.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                productPrice.setTextColor(Color.BLACK)
                productPrice.textSize = 14F

                Glide.with(root.context).load(product.images[0]).into(productImage)
                productTitle.text = product.title
                productPrice.text = "$${product.price}"
                productDesc.text = product.description
                productDesc.visibility = View.VISIBLE

                if (product.discountPercentage > 0) {
                    productDiscountedPrice.visibility = View.VISIBLE
                    productDiscountPercentageLayout.visibility = View.VISIBLE
                    productDiscountedPrice.text = "$${product.discountedPrice}"
                    productDiscountPercentage.text = "${product.discountPercentage}% OFF"
                    productPrice.paintFlags = productPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    productPrice.setTextColor(Color.RED)
                    productPrice.textSize = 11F
                    productDiscountedPrice.textSize = 15F
                    productDesc.visibility = View.GONE
                }

                root.setOnClickListener {
                    val intent = Intent(root.context, ProductViewActivity::class.java)
                    intent.putExtra("product", product)
                    root.context.startActivity(intent)
                }
            }
        }
    }
}

class ProductDiffCallback : DiffUtil.ItemCallback<Product>() {
    override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
        return oldItem == newItem
    }
}

class MarginItemDecoration(private val spaceHeight: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view)
        val spanCount = (parent.layoutManager as GridLayoutManager).spanCount

        if (position % spanCount == 0) {
            outRect.right = spaceHeight / 2
        } else {
            outRect.left = spaceHeight / 2
        }
    }
}

