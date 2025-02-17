package com.ozgurbaykal.ecored.view.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.ozgurbaykal.ecored.R
import com.ozgurbaykal.ecored.databinding.FragmentProductListBinding
import com.ozgurbaykal.ecored.view.BaseFragment
import com.ozgurbaykal.ecored.view.adapter.MarginItemDecoration
import com.ozgurbaykal.ecored.view.adapter.ProductAdapter
import com.ozgurbaykal.ecored.view.adapter.ProductBigListAdapter
import com.ozgurbaykal.ecored.viewmodel.CommonViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProductListFragment : BaseFragment() {

    private var _binding: FragmentProductListBinding? = null
    private val binding get() = _binding!!

    private val commonViewModel: CommonViewModel by viewModels()
    private lateinit var productAdapter: ProductBigListAdapter
    private var isLoading = false
    private val limit: Long = 10

    private var catalogId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProductListBinding.inflate(inflater, container, false)
        val view = binding.root

        val caller = arguments?.getString("caller")
        catalogId = arguments?.getString("catalogId")
        val catalogTitle = arguments?.getString("catalogTitle")

        Log.i("ProductListFragment", "catalogId: $catalogId")

        if (caller == "viewAllButton") {
            binding.searchEditText.visibility = View.GONE
            binding.title.visibility = View.GONE
            binding.titleTopLayout.visibility = View.VISIBLE
            binding.titleTopLayout.text = getString(R.string.daily_deal_title)
            commonViewModel.fetchRandomDiscountedProducts(2)
        } else if (caller == "favoritesButton") {
            FirebaseAuth.getInstance().currentUser?.let { user ->
                commonViewModel.fetchFavoriteProducts(user.uid)
            }
            binding.searchEditText.visibility = View.GONE
            binding.title.visibility = View.GONE
            binding.titleTopLayout.visibility = View.VISIBLE
            binding.titleTopLayout.text =  getString(R.string.my_favorites)
        } else {
            if (catalogTitle != null)
                binding.title.text = catalogTitle.toString()

            commonViewModel.loadProducts(limit, catalogId)

            binding.productList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (!recyclerView.canScrollVertically(1) && !isLoading) {
                        commonViewModel.loadProducts(limit, catalogId)
                    }
                }
            })
        }

        setupRecyclerView()
        setupObservers()

        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                s?.toString()?.let {
                    if (it.isNotEmpty()) {
                        commonViewModel.searchProducts(it, catalogId)
                    } else {
                        commonViewModel.loadProducts(limit, catalogId)
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.backButton.setOnClickListener {
            activity?.onBackPressedDispatcher?.onBackPressed()
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        val caller = arguments?.getString("caller")
        if (caller == "favoritesButton") {
            FirebaseAuth.getInstance().currentUser?.let { user ->
                commonViewModel.fetchFavoriteProducts(user.uid)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupRecyclerView() {
        productAdapter = ProductBigListAdapter()
        binding.productList.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = productAdapter
            //addItemDecoration(MarginItemDecoration(16))
        }
    }

    private fun setupObservers() {
        commonViewModel.generalProducts.observe(viewLifecycleOwner) { products ->
            productAdapter.submitList(products)
            productAdapter.notifyDataSetChanged()
        }

        commonViewModel.favoriteProducts.observe(viewLifecycleOwner) { products ->
            productAdapter.submitList(products)
            productAdapter.notifyDataSetChanged()
        }

        commonViewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            isLoading = loading
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }
    }
}
