package com.ozgurbaykal.ecored.view.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ozgurbaykal.ecored.databinding.ItemSearchBinding
import com.ozgurbaykal.ecored.model.SearchHistoryItem

class SearchHistoryAdapter(
    private var items: List<SearchHistoryItem>,
    private val onItemClick: (SearchHistoryItem) -> Unit,
    private val onRemoveClick: (SearchHistoryItem) -> Unit,
    private var isSearchResults: Boolean = false
) : RecyclerView.Adapter<SearchHistoryAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemSearchBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSearchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        Log.i("SearchHistoryAdapter", "Binding item: $item at position $position")
        holder.binding.searchText.text = item.query

        holder.binding.removeFromHistory.visibility = if (isSearchResults) View.GONE else View.VISIBLE

        holder.binding.removeFromHistory.setOnClickListener {
            onRemoveClick(item)
        }

        holder.binding.root.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount() = items.size

    fun updateItems(newItems: List<SearchHistoryItem>, isSearchResults: Boolean = false) {
        items = newItems
        this.isSearchResults = isSearchResults
        notifyDataSetChanged()
    }
}
