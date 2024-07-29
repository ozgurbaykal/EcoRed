package com.ozgurbaykal.ecored.view.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProductListBinding.inflate(inflater, container, false)
        val view = binding.root

        val caller = arguments?.getString("caller")
        val catalogId = arguments?.getString("catalogId")
        val catalogTitle = arguments?.getString("catalogTitle")

        Log.i("ProductListFragment", "catalogId: ${arguments?.getString("catalogId")}")


        if (caller == "viewAllButton") {
            binding.searchEditText.visibility = View.GONE
            binding.title.visibility = View.GONE
            binding.titleTopLayout.visibility = View.VISIBLE
            binding.titleTopLayout.text = getString(R.string.daily_deal_title)
            commonViewModel.fetchRandomDiscountedProducts(2)
        } else {
            if(catalogTitle!=null)
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




        binding.backButton.setOnClickListener {
            activity?.onBackPressedDispatcher?.onBackPressed()
        }

        return view
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

        commonViewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            isLoading = loading
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }
    }
}