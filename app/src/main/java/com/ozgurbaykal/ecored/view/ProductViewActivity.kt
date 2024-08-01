package com.ozgurbaykal.ecored.view

import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.ozgurbaykal.ecored.R
import com.ozgurbaykal.ecored.databinding.ActivityLoginBinding
import com.ozgurbaykal.ecored.databinding.ActivityMainBinding
import com.ozgurbaykal.ecored.databinding.ActivityProductViewBinding
import com.ozgurbaykal.ecored.model.Product
import com.ozgurbaykal.ecored.util.SharedPreferencesHelper
import com.ozgurbaykal.ecored.view.adapter.GalleryImageAdapter
import com.ozgurbaykal.ecored.view.adapter.ProductAdapter
import com.ozgurbaykal.ecored.view.adapter.ProductImageAdapter
import com.ozgurbaykal.ecored.view.fragment.HomeFragment
import com.ozgurbaykal.ecored.viewmodel.CommonViewModel
import dagger.hilt.android.AndroidEntryPoint
@AndroidEntryPoint
class ProductViewActivity : BaseActivity() {

    private lateinit var binding: ActivityProductViewBinding
    private val commonViewModel: CommonViewModel by viewModels()

    private val maxDescriptionLength = 100
    private lateinit var product: Product
    private var isFavorite: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProductViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        product = intent.getParcelableExtra<Product>("product")!!

        product?.let {
            setProductDetails(it)
            getSimilarProducts(it)
        }

        commonViewModel.favorites.observe(this) { favorites ->
            isFavorite = favorites.contains(product.id)
            updateFavoriteIcon()
        }

        FirebaseAuth.getInstance().currentUser?.let { user ->
            commonViewModel.fetchFavorites(user.uid)
            commonViewModel.fetchCart(user.uid)
        }

        setupListeners()

        commonViewModel.cartItems.observe(this) { cartItems ->
            val cartItem = cartItems.find { it.productId == product.id }
            if (cartItem != null) {
                showAddAndRemoveLayout(cartItem.quantity)
            } else {
                showAddToCartButton()
            }
        }

        val layoutManagerHiglight = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerViewSimilarProducts.layoutManager = layoutManagerHiglight

        commonViewModel.generalProducts.observe(this) { products ->
            binding.recyclerViewSimilarProducts.adapter = ProductAdapter(products)
        }

        commonViewModel.isLoading.observe(this) { isLoading ->
            Log.i("ProductViewActivity", "isLoading -> $isLoading")
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun setupListeners() {
        binding.backButton.setOnClickListener {
            finish()
        }

        binding.addToCart.setOnClickListener {
            FirebaseAuth.getInstance().currentUser?.let { user ->
                commonViewModel.addToCart(user.uid, product.id)
                showAddAndRemoveLayout(1)
            }
        }

        binding.addButton.setOnClickListener {
            FirebaseAuth.getInstance().currentUser?.let { user ->
                commonViewModel.addToCart(user.uid, product.id)
            }
        }

        binding.removeButton.setOnClickListener {
            FirebaseAuth.getInstance().currentUser?.let { user ->
                commonViewModel.removeFromCart(user.uid, product.id)
            }
        }

        binding.favoriteButton.setOnClickListener {
            FirebaseAuth.getInstance().currentUser?.let { user ->
                if (isFavorite) {
                    commonViewModel.removeFavorite(user.uid, product.id)
                } else {
                    commonViewModel.addFavorite(user.uid, product.id)
                }
            }
        }
    }

    private fun showAddToCartButton() {
        binding.addToCart.visibility = View.VISIBLE
        binding.addAndRemoveLayout.visibility = View.GONE
    }

    private fun showAddAndRemoveLayout(quantity: Int) {
        binding.addToCart.visibility = View.GONE
        binding.addAndRemoveLayout.visibility = View.VISIBLE
        binding.productAmount.text = quantity.toString()
    }

    private fun updateFavoriteIcon() {
        if (isFavorite) {
            binding.favoriteIcon.setImageResource(R.drawable.heaart_filled)
        } else {
            binding.favoriteIcon.setImageResource(R.drawable.heart_lined)
        }
    }

    private fun getSimilarProducts(product: Product) {
        val categoryId = product.categoryId
        commonViewModel.getProductsWithCategory(categoryId, product.id)
    }

    private fun setProductDetails(product: Product) {
        binding.productTitle.text = product.title
        binding.productPrice.text = "$${product.price}"
        binding.categoryText.text = "${product.categoryId}"
        binding.brandText.text = "${product.brand}"

        if (product.discountPercentage > 0) {
            binding.productDiscountPercentageLayout.visibility = View.VISIBLE
            binding.productDiscountPercentage.text = "${product.discountPercentage}% OFF"
            binding.productDiscountedPrice.visibility = View.VISIBLE
            binding.productDiscountedPrice.text = "$${product.discountedPrice}"

            binding.productPrice.apply {
                paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                textSize = 11F
                setTextColor(getColor(R.color.main_red))
            }
            binding.productDiscountedPrice.apply {
                textSize = 15F
            }
        } else {
            binding.productDiscountPercentageLayout.visibility = View.GONE
            binding.productDiscountedPrice.visibility = View.GONE
        }

        val imageAdapter = ProductImageAdapter(product.images)
        binding.productImagesViewPager.adapter = imageAdapter

        setupDescription(product.description)

        val galleryAdapter = GalleryImageAdapter(product.images) { position ->
            binding.productImagesViewPager.currentItem = position
        }
        binding.viewPageGallery.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.viewPageGallery.adapter = galleryAdapter
    }

    private fun setupDescription(description: String) {
        if (description.length > maxDescriptionLength) {
            binding.productDesc.text = description.substring(0, maxDescriptionLength) + "..."
            binding.readMore.visibility = View.VISIBLE
            binding.readMore.text = getString(R.string.read_more)
            var isExpanded = false

            binding.readMore.setOnClickListener {
                if (isExpanded) {
                    binding.productDesc.text = description.substring(0, maxDescriptionLength) + "..."
                    binding.readMore.text = getString(R.string.read_more)
                } else {
                    binding.productDesc.text = description
                    binding.readMore.text = getString(R.string.read_more)
                }
                isExpanded = !isExpanded
            }
        } else {
            binding.productDesc.text = description
            binding.readMore.visibility = View.GONE
        }
    }
}
