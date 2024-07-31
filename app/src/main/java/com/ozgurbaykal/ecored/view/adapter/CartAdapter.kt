package com.ozgurbaykal.ecored.view.adapter

import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ozgurbaykal.ecored.databinding.CartProductItemBinding
import com.ozgurbaykal.ecored.model.CartItem
import com.ozgurbaykal.ecored.model.Product
import com.ozgurbaykal.ecored.view.ProductViewActivity

class CartAdapter(
    private var items: List<CartItem>,
    private val onIncreaseClick: (String) -> Unit,
    private val onDecreaseClick: (String) -> Unit,
    private val fetchProductDetails: (String, (Product?) -> Unit) -> Unit
) : RecyclerView.Adapter<CartAdapter.ViewHolder>() {

    class ViewHolder(val binding: CartProductItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = CartProductItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.productAmount.text = item.quantity.toString()

        holder.binding.addProduct.setOnClickListener {
            onIncreaseClick(item.productId)
        }

        holder.binding.removeProduct.setOnClickListener {
            onDecreaseClick(item.productId)
        }

        // Fetch product details
        fetchProductDetails(item.productId) { product ->
            product?.let {
                with(holder.binding) {
                    Glide.with(root.context).load(it.images[0]).into(productImage)
                    productTitle.text = it.title
                    productPrice.text = "$${it.price}"
                    productDesc.text = it.description

                    if (it.discountPercentage > 0) {
                        productDiscountedPrice.visibility = View.VISIBLE
                        productDiscountPercentageLayout.visibility = View.VISIBLE
                        productDiscountedPrice.text = "$${it.discountedPrice}"
                        productDiscountPercentage.text = "${it.discountPercentage}% OFF"
                        productPrice.apply {
                            paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                            textSize = 11F
                        }
                        productDiscountedPrice.apply {
                            textSize = 15F
                        }
                        productDesc.visibility = View.GONE
                    } else {
                        productDiscountedPrice.visibility = View.GONE
                        productDiscountPercentageLayout.visibility = View.GONE
                        productPrice.setTextColor(Color.BLACK)
                        productDesc.visibility = View.VISIBLE
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

    override fun getItemCount() = items.size

    fun updateItems(newItems: List<CartItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}
