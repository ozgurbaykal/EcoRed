package com.ozgurbaykal.ecored.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.ozgurbaykal.ecored.databinding.ActivityMainBinding
import com.ozgurbaykal.ecored.databinding.ActivityOrderHistoryBinding
import com.ozgurbaykal.ecored.view.adapter.OrderHistoryAdapter
import com.ozgurbaykal.ecored.view.customs.CustomDialogFragment
import com.ozgurbaykal.ecored.viewmodel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OrderHistoryActivity : BaseActivity() {

    private lateinit var binding: ActivityOrderHistoryBinding
    private val userViewModel: UserViewModel by viewModels()
    private lateinit var orderHistoryAdapter: OrderHistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityOrderHistoryBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setupRecyclerView()
        fetchOrderHistory()

        binding.backButton.setOnClickListener {
            finish()
        }

        userViewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        userViewModel.orderHistory.observe(this) { orders ->
            orderHistoryAdapter.updateItems(orders)
        }
    }

    private fun setupRecyclerView() {
        orderHistoryAdapter = OrderHistoryAdapter(
            fetchProductDetails = { productId, callback ->
                userViewModel.getProductById(productId, callback)
            },
            onItemClicked = { order ->
                val intent = Intent(this, OrderDetailActivity::class.java)
                intent.putExtra("order", order)
                startActivity(intent)
            }
        )
        binding.orderHistoryRecycler.layoutManager = LinearLayoutManager(this)
        binding.orderHistoryRecycler.adapter = orderHistoryAdapter
    }

    private fun fetchOrderHistory() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        userId?.let {
            userViewModel.fetchOrderHistory(it)
        }
    }
}
