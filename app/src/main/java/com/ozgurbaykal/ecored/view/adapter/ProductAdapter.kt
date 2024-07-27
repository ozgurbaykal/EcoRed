package com.ozgurbaykal.ecored.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ozgurbaykal.ecored.R
import com.ozgurbaykal.ecored.databinding.ProductItemBinding
import com.ozgurbaykal.ecored.model.Product

class ProductAdapter(private val productList: List<Product>) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ProductItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]
        holder.bind(product)
    }

    override fun getItemCount(): Int = productList.size

    class ProductViewHolder(private val binding: ProductItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(product: Product) {
            with(binding) {
                Glide.with(root.context).load(product.images[0]).into(productImage)
                productTitle.text = product.title
                productPrice.text = "$${product.price}"
                if (product.discountPercentage > 0) {
                    productDiscountedPrice.visibility = View.VISIBLE
                    productDiscountPercentage.visibility = View.VISIBLE
                    productDiscountedPrice.text = "$${product.discountedPrice}"
                    productDiscountPercentage.text = "${product.discountPercentage}% OFF"
                } else {
                    productDiscountedPrice.visibility = View.GONE
                    productDiscountPercentage.visibility = View.GONE
                }
            }
        }
    }
}