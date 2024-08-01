package com.ozgurbaykal.ecored.view.adapter

import android.os.Parcelable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ozgurbaykal.ecored.R
import com.ozgurbaykal.ecored.databinding.ItemAddressOrCardBinding
import com.ozgurbaykal.ecored.model.Address
import com.ozgurbaykal.ecored.model.CreditCard
import kotlinx.android.parcel.Parcelize

class AddressOrCardAdapter(
    private val type: String,
    private val onItemEdit: (AddressOrCardItem) -> Unit,
    private val onItemDelete: (AddressOrCardItem) -> Unit,
    private val onItemClick: (AddressOrCardItem) -> Unit
) : RecyclerView.Adapter<AddressOrCardAdapter.ViewHolder>() {

    private var items: List<AddressOrCardItem> = listOf()

    inner class ViewHolder(val binding: ItemAddressOrCardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAddressOrCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        when (item) {
            is AddressOrCardItem.AddressItem -> {
                holder.binding.titleText.text = item.addressTitle
                holder.binding.detailText.text = item.detail
                holder.binding.itemIcon.setImageResource(R.drawable.address_filled)

                // holder.binding.editButton.setOnClickListener { onItemEdit(item) }
                holder.binding.deleteButton.setOnClickListener { onItemDelete(item) }
                holder.binding.root.setOnClickListener { onItemClick(item) } // Item click listener eklendi
            }
            is AddressOrCardItem.CardItem -> {
                holder.binding.titleText.text = item.cardTitle
                holder.binding.detailText.text = item.detail
                holder.binding.itemIcon.setImageResource(R.drawable.card_filled)

                // holder.binding.editButton.setOnClickListener { onItemEdit(item) }
                holder.binding.deleteButton.setOnClickListener { onItemDelete(item) }
                holder.binding.root.setOnClickListener { onItemClick(item) } // Item click listener eklendi
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun setItems(newItems: List<AddressOrCardItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    fun getItem(position: Int): AddressOrCardItem {
        return items[position]
    }
}



@Parcelize
sealed class AddressOrCardItem : Parcelable {
    data class AddressItem(
        val addressTitle: String,
        val detail: String,
        val address: Address
    ) : AddressOrCardItem()

    data class CardItem(
        val cardTitle: String,
        val detail: String,
        val card: CreditCard
    ) : AddressOrCardItem()
}
