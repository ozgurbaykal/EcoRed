package com.ozgurbaykal.ecored.view.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.ozgurbaykal.ecored.R
import com.ozgurbaykal.ecored.databinding.FragmentSearchBinding
import com.ozgurbaykal.ecored.databinding.FragmentWelcomeForLoginBinding
import com.ozgurbaykal.ecored.model.SearchHistoryItem
import com.ozgurbaykal.ecored.view.BaseFragment
import com.ozgurbaykal.ecored.view.ProductViewActivity
import com.ozgurbaykal.ecored.view.adapter.SearchHistoryAdapter
import com.ozgurbaykal.ecored.viewmodel.CommonViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class SearchFragment : BaseFragment() {

    private val TAG = "SearchFragmentTAG"

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private val commonViewModel: CommonViewModel by viewModels()
    private lateinit var searchHistoryAdapter: SearchHistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        val view = binding.root

        setupRecyclerView()
        setupListeners()
        setupObservers()

        FirebaseAuth.getInstance().currentUser?.let { commonViewModel.fetchSearchHistory(it.uid) }

        binding.searchEditText.requestFocus()
        binding.searchEditText.post {
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.searchEditText, InputMethodManager.SHOW_IMPLICIT)
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupRecyclerView() {
        searchHistoryAdapter = SearchHistoryAdapter(emptyList(), this::onSearchItemClick, this::onRemoveItemClick)
        binding.listView.layoutManager = LinearLayoutManager(context)
        binding.listView.adapter = searchHistoryAdapter
    }

    private fun setupListeners() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                s?.toString()?.let {
                    if (it.isNotEmpty()) {
                        Log.i(TAG, "afterTextChanged -> ${it.toString()}")
                        commonViewModel.searchProducts(it)
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.clearAllButton.setOnClickListener {
            FirebaseAuth.getInstance().currentUser?.let { commonViewModel.clearSearchHistory(it.uid) }
        }

        binding.backButton.setOnClickListener {
            activity?.onBackPressedDispatcher?.onBackPressed()
        }
    }

    private fun setupObservers() {
        commonViewModel.searchHistory.observe(viewLifecycleOwner) { history ->
            Log.i(TAG, "history -> ${history.toString()}")
            searchHistoryAdapter.updateItems(history, isSearchResults = false)
        }

        commonViewModel.searchResults.observe(viewLifecycleOwner) { results ->
            searchHistoryAdapter.updateItems(results.map { SearchHistoryItem(it.title, it.id) }, isSearchResults = true)
        }
    }


    private fun onSearchItemClick(item: SearchHistoryItem) {
        val productId = item.productId
        commonViewModel.getProductById(productId) { product ->
            product?.let {
                FirebaseAuth.getInstance().currentUser?.let { user ->
                    commonViewModel.addSearchHistory(item.query, productId, user.uid)
                }
                val intent = Intent(requireContext(), ProductViewActivity::class.java)
                intent.putExtra("product", it)
                startActivity(intent)
            }
        }
    }

    private fun onRemoveItemClick(item: SearchHistoryItem) {
        FirebaseAuth.getInstance().currentUser?.let { commonViewModel.removeSearchHistoryItem(item.query, it.uid) }
    }
}
