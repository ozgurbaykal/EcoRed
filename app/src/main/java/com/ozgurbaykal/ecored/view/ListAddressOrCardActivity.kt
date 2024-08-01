package com.ozgurbaykal.ecored.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.ozgurbaykal.ecored.databinding.ActivityListAddressOrCardBinding
import com.ozgurbaykal.ecored.model.User
import com.ozgurbaykal.ecored.view.adapter.AddressOrCardAdapter
import com.ozgurbaykal.ecored.view.adapter.AddressOrCardItem
import com.ozgurbaykal.ecored.view.customs.AddAddressDialog
import com.ozgurbaykal.ecored.view.customs.AddCardDialog
import com.ozgurbaykal.ecored.viewmodel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ListAddressOrCardActivity : BaseActivity() {

    private lateinit var binding: ActivityListAddressOrCardBinding
    private val userViewModel: UserViewModel by viewModels()
    private lateinit var adapter: AddressOrCardAdapter
    private val type: String by lazy { intent.getStringExtra("type") ?: "addresses" }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityListAddressOrCardBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setupRecyclerView(type)

        userViewModel.currentUser.observe(this) { user ->
            user?.let {
                updateUIWithUserDetails(it)
            }
        }

        userViewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        userViewModel.errorMessage.observe(this) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }

        binding.backButton.setOnClickListener {
            finish()
        }

        binding.addButton.setOnClickListener {
            if (type == "addresses") {
                val dialog = AddAddressDialog(this, userViewModel)
                dialog.showDialog()
            } else {
                val dialog = AddCardDialog(this, userViewModel)
                dialog.showDialog()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        FirebaseAuth.getInstance().currentUser?.let { userViewModel.fetchUser(it.uid) }
    }

    private fun setupRecyclerView(type: String) {
        adapter = AddressOrCardAdapter(type, this::onItemEdit, this::onItemDelete, this::onItemClick)
        binding.itemRecycler.layoutManager = LinearLayoutManager(this)
        binding.itemRecycler.adapter = adapter
    }

    private fun updateUIWithUserDetails(user: User) {
        if (type == "addresses") {
            adapter.setItems(user.addresses.map { address -> AddressOrCardItem.AddressItem(addressTitle = address.addressTitle, detail = address.address, address = address) })
            binding.title.text = "Addresses"
        } else {
            adapter.setItems(user.creditCards.map { card -> AddressOrCardItem.CardItem(cardTitle = card.cardTitle, detail = "**** **** **** ${card.cardNumber.takeLast(4)}", card = card) })
            binding.title.text = "Credit Cards"
        }
    }

    private fun onItemEdit(item: AddressOrCardItem) {
        when (item) {
            is AddressOrCardItem.AddressItem -> {
                val dialog = AddAddressDialog(this, userViewModel)
                dialog.showDialog(item.address)
            }
            is AddressOrCardItem.CardItem -> {
                val dialog = AddCardDialog(this, userViewModel)
                dialog.showDialog(item.card)
            }
        }
    }

    private fun onItemDelete(item: AddressOrCardItem) {
        when (item) {
            is AddressOrCardItem.AddressItem -> {
                userViewModel.deleteAddress(item.address)
            }
            is AddressOrCardItem.CardItem -> {
                userViewModel.deleteCreditCard(item.card)
            }
        }
    }

    private fun onItemClick(item: AddressOrCardItem) {
        val resultIntent = Intent()
        when (item) {
            is AddressOrCardItem.AddressItem -> {
                resultIntent.putExtra("selected_address", item.address)
            }
            is AddressOrCardItem.CardItem -> {
                resultIntent.putExtra("selected_card", item.card)
            }
        }
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }
}
