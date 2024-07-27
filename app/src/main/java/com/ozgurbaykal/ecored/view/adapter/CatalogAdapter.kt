package com.ozgurbaykal.ecored.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ozgurbaykal.ecored.databinding.CatalogItemBinding
import com.ozgurbaykal.ecored.model.Catalog

class CatalogAdapter(private val catalogList: List<Catalog>) : RecyclerView.Adapter<CatalogAdapter.CatalogViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CatalogViewHolder {
        val binding = CatalogItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CatalogViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CatalogViewHolder, position: Int) {
        val catalog = catalogList[position]
        holder.bind(catalog)
    }

    override fun getItemCount(): Int = catalogList.size

    class CatalogViewHolder(private val binding: CatalogItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(catalog: Catalog) {
            with(binding) {
                Glide.with(root.context).load(catalog.image).into(catalogImage)
                catalogName.text = catalog.name
            }
        }
    }
}