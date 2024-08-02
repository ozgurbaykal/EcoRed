package com.ozgurbaykal.ecored.view.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.ozgurbaykal.ecored.R
import com.ozgurbaykal.ecored.databinding.FragmentCartBinding
import com.ozgurbaykal.ecored.databinding.FragmentWelcomeForLoginBinding
import com.ozgurbaykal.ecored.model.CartItem
import com.ozgurbaykal.ecored.model.Product
import com.ozgurbaykal.ecored.view.BaseFragment
import com.ozgurbaykal.ecored.view.CheckoutActivity
import com.ozgurbaykal.ecored.view.MainActivity
import com.ozgurbaykal.ecored.view.ProductViewActivity
import com.ozgurbaykal.ecored.view.adapter.CartAdapter
import com.ozgurbaykal.ecored.viewmodel.CommonViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CartFragment : BaseFragment() {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!

    private val commonViewModel: CommonViewModel by viewModels()
    private lateinit var cartAdapter: CartAdapter

    var totalPrice = 0.0
    var totalDiscountedPrice = 0.0
    var totalDiscount = 0.0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        val view = binding.root

        setupRecyclerView()
        setupObservers()
        setupListeners()

        FirebaseAuth.getInstance().currentUser?.let { commonViewModel.fetchCart(it.uid) }

        binding.completeShopping.setOnClickListener {
            val intent = Intent(requireContext(), CheckoutActivity::class.java)
            intent.putExtra("totalPrice", totalPrice)
            intent.putExtra("totalDiscountedPrice", totalDiscountedPrice)
            intent.putExtra("totalDiscount", totalDiscount)
            startActivity(intent)
        }

        binding.pricesLayout.setOnClickListener {
            if (binding.totalDiscount.visibility == View.GONE) {
                binding.totalDiscount.visibility = View.VISIBLE
                binding.totalPriceBeforeDiscount.visibility = View.VISIBLE
                binding.arrowIcon.setImageResource(R.drawable.arrow_down)
            } else {
                binding.totalDiscount.visibility = View.GONE
                binding.totalPriceBeforeDiscount.visibility = View.GONE
                binding.arrowIcon.setImageResource(R.drawable.arrow_up)
            }
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupRecyclerView() {
        cartAdapter = CartAdapter(requireContext(), emptyList(), this::onIncreaseClick, this::onDecreaseClick, this::fetchProductDetails)
        binding.cartRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.cartRecyclerView.adapter = cartAdapter
    }

    private fun setupObservers() {
        commonViewModel.cartItems.observe(viewLifecycleOwner) { cartItems ->
            if(cartItems.isEmpty())
                binding.emptyList.visibility = View.VISIBLE

            cartAdapter.updateItems(cartItems)
            calculatePrices(cartItems)
        }

        commonViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            manageProgressBar(isLoading)
        }
    }

    private fun setupListeners() {
        binding.backButton.setOnClickListener {
            activity?.onBackPressedDispatcher?.onBackPressed()
        }

        binding.clearCartButton.setOnClickListener {
            FirebaseAuth.getInstance().currentUser?.let { commonViewModel.clearCart(it.uid) }
        }
    }

    private fun onIncreaseClick(productId: String) {
        FirebaseAuth.getInstance().currentUser?.let { commonViewModel.addToCart(it.uid, productId) }
    }

    private fun onDecreaseClick(productId: String) {
        FirebaseAuth.getInstance().currentUser?.let { commonViewModel.removeFromCart(it.uid, productId) }
    }

    private fun fetchProductDetails(productId: String, callback: (Product?) -> Unit) {
        commonViewModel.getProductById(productId) { product ->
            callback(product)
        }
    }

    private fun calculatePrices(cartItems: List<CartItem>) {
        totalPrice = 0.0
        totalDiscountedPrice = 0.0
        totalDiscount = 0.0

        val productsToFetch = cartItems.map { it.productId }

        commonViewModel.fetchProductsByIds(productsToFetch) { products ->
            products.forEach { product ->
                val item = cartItems.find { it.productId == product.id }
                item?.let {
                    val quantity = it.quantity
                    val price = product.price * quantity
                    val discountedPrice = if (product.discountPercentage > 0) product.discountedPrice * quantity else price
                    val discount = price - discountedPrice

                    totalPrice += price
                    totalDiscountedPrice += discountedPrice
                    totalDiscount += discount
                }
            }

            binding.calculatedTotalPrice.text = String.format("$%.2f", totalDiscountedPrice)
            binding.totalPriceBeforeDiscount.text = String.format("$%.2f", totalPrice)
            binding.totalDiscount.text = String.format("-$%.2f", totalDiscount)
        }
    }
}
