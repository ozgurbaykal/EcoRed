package com.ozgurbaykal.ecored.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.Timestamp
import com.ozgurbaykal.ecored.model.Order
import com.ozgurbaykal.ecored.databinding.OrderHistoryItemBinding
import com.ozgurbaykal.ecored.model.Product
import java.text.SimpleDateFormat
import java.util.Locale

class OrderHistoryAdapter(
    private val fetchProductDetails: (String, (Product?) -> Unit) -> Unit,
    private val onItemClicked: (Order) -> Unit
) : RecyclerView.Adapter<OrderHistoryAdapter.ViewHolder>() {

    private var items: List<Order> = emptyList()

    class ViewHolder(val binding: OrderHistoryItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = OrderHistoryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.apply {
            orderNumber.text = item.orderId
            orderDate.text = formatDate(item.orderDate)
            orderTotalPrice.text = String.format("$%.2f", item.totalPrice)

            firstImageCard.visibility = View.GONE
            secondImageCard.visibility = View.GONE
            isMoreThanTwoProduct.visibility = View.GONE

            if (item.items.isNotEmpty()) {
                fetchProductDetails(item.items[0].productId) { product ->
                    product?.let {
                        firstImageCard.visibility = View.VISIBLE
                        Glide.with(firstImage.context).load(it.images.firstOrNull()).into(firstImage)
                    }
                }
            }

            if (item.items.size > 1) {
                fetchProductDetails(item.items[1].productId) { product ->
                    product?.let {
                        secondImageCard.visibility = View.VISIBLE
                        Glide.with(secondImage.context).load(it.images.firstOrNull()).into(secondImage)
                    }
                }
            }

            if (item.items.size > 2) {
                isMoreThanTwoProduct.visibility = View.VISIBLE
                ifMoreThanTwoProduct.text = "+${item.items.size - 2}"
            }

            root.setOnClickListener {
                onItemClicked(item)
            }
        }
    }

    private fun formatDate(timestamp: Timestamp): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return sdf.format(timestamp.toDate())
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun updateItems(newItems: List<Order>) {
        items = newItems
        notifyDataSetChanged()
    }
}
