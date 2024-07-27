package com.ozgurbaykal.ecored.view.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.ozgurbaykal.ecored.databinding.FragmentHomeBinding
import com.ozgurbaykal.ecored.view.BaseFragment
import com.ozgurbaykal.ecored.view.adapter.BannerAdapter
import com.ozgurbaykal.ecored.view.adapter.CatalogAdapter
import com.ozgurbaykal.ecored.view.adapter.ProductAdapter
import com.ozgurbaykal.ecored.viewmodel.CommonViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class HomeFragment : BaseFragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var bannerAdapter: BannerAdapter

    private val commonViewModel: CommonViewModel by viewModels()


    private val bannerUrls = listOf(
        "https://i.hizliresim.com/ltb63q3.jpeg",
        "https://i.hizliresim.com/i95axw6.jpeg",
        "https://i.hizliresim.com/fo0fhme.jpeg"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root

        setupViewPager()

        val layoutManagerHiglight = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerViewHiglights.layoutManager = layoutManagerHiglight

        commonViewModel.discountedProducts.observe(viewLifecycleOwner) { products ->
            binding.recyclerViewHiglights.adapter = ProductAdapter(products)
        }

        commonViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
           // binding.recyclerViewHiglights.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        commonViewModel.fetchDiscountedProducts()



        val layoutManagerCatalog = GridLayoutManager(context, 3)
        binding.recyclerViewCatalog.layoutManager = layoutManagerCatalog

        commonViewModel.catalogs.observe(viewLifecycleOwner) { catalogs ->
            Log.i("HomeFragment", catalogs.toString())

            binding.recyclerViewCatalog.adapter = CatalogAdapter(catalogs)
        }

        commonViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            //binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        commonViewModel.fetchCatalogs()

        return view
    }


    private fun setupViewPager() {
        bannerAdapter = BannerAdapter(bannerUrls)
        binding.viewPagerBanner.adapter = bannerAdapter

        binding.viewPagerBanner.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.textViewBannerIndicator.text = "${position + 1}/${bannerUrls.size}"
            }
        })
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}