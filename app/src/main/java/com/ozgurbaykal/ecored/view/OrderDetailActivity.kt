package com.ozgurbaykal.ecored.view

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.ozgurbaykal.ecored.databinding.ActivityMainBinding
import com.ozgurbaykal.ecored.databinding.ActivityOrderDetailBinding
import com.ozgurbaykal.ecored.databinding.ActivityOrderHistoryBinding
import com.ozgurbaykal.ecored.model.CartItem
import com.ozgurbaykal.ecored.model.Order
import com.ozgurbaykal.ecored.view.adapter.OrderHistoryAdapter
import com.ozgurbaykal.ecored.view.adapter.ProductAdapter
import com.ozgurbaykal.ecored.view.customs.CustomDialogFragment
import com.ozgurbaykal.ecored.viewmodel.CommonViewModel
import com.ozgurbaykal.ecored.viewmodel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OrderDetailActivity : BaseActivity() {

    private lateinit var binding: ActivityOrderDetailBinding
    private val userViewModel: UserViewModel by viewModels()
    private val commonViewModel: CommonViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityOrderDetailBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val order = intent.getParcelableExtra<Order>("order")

        order?.let { setupOrderDetails(it) }

        binding.backButton.setOnClickListener {
            finish()
        }
    }

    private fun setupOrderDetails(order: Order) {
        binding.addressTitle.text = order.address.addressTitle
        binding.fullAddressText.text = order.address.address
        binding.phoneNumber.text = order.address.phoneNumber

        binding.nameAndLastName.text =  "${order.address.name?.replaceFirstChar(Char::titlecase)}" + " " + "${order.address.lastName?.replaceFirstChar(Char::titlecase)}"

        binding.cardTitle.text = order.card.cardTitle
        binding.cardNumberText.text = "**** **** **** ${order.card.cardNumber.takeLast(4)}"
        binding.totalPrice.text = String.format("$%.2f", order.totalPrice)

        setupProductsRecyclerView(order.items)
    }

    private fun setupProductsRecyclerView(items: List<CartItem>) {
        val productIds = items.map { it.productId }
        commonViewModel.fetchProductsByIds(productIds) { products ->
            val productAdapter = ProductAdapter(products)
            binding.productsInOrder.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            binding.productsInOrder.adapter = productAdapter
        }
    }
}
