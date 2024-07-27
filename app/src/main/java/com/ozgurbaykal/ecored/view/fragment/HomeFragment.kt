package com.ozgurbaykal.ecored.view.fragment

import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
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

    private val commonViewModel: CommonViewModel by viewModels()

    private lateinit var bannerAdapter: BannerAdapter

    private var bannerUrls: List<String> = emptyList()

    private var countDownTimer: CountDownTimer? = null


    private val handler = Handler(Looper.getMainLooper())
    private val runnable = object : Runnable {
        override fun run() {
            val currentItem = binding.viewPagerBanner.currentItem
            val nextItem = (currentItem + 1) % bannerUrls.size
            binding.viewPagerBanner.setCurrentItem(nextItem, true)
            handler.postDelayed(this, 3000)
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root



        val layoutManagerHiglight = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerViewHiglights.layoutManager = layoutManagerHiglight

        commonViewModel.discountedProducts.observe(viewLifecycleOwner) { products ->
            binding.recyclerViewHiglights.adapter = ProductAdapter(products)
        }

        commonViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            Log.i("HomeFragment", "isLoading: $isLoading")
            manageProgressBar(isLoading)
           // binding.recyclerViewHiglights.visibility = if (isLoading) View.VISIBLE else View.GONE
        }


        val layoutManagerCatalog = GridLayoutManager(context, 3)
        binding.recyclerViewCatalog.layoutManager = layoutManagerCatalog

        commonViewModel.catalogs.observe(viewLifecycleOwner) { catalogs ->
            Log.i("HomeFragment", catalogs.toString())

            binding.recyclerViewCatalog.adapter = CatalogAdapter(catalogs)
        }

        commonViewModel.banners.observe(viewLifecycleOwner) { banners ->
            bannerUrls = banners.map { it.image.toString() }
            setupViewPager()
            startAutoScroll()
        }
        commonViewModel.fetchDiscountedProducts()
        commonViewModel.fetchCatalogs()
        commonViewModel.fetchBanners()

        startCountdownTimer()
        return view
    }

    private fun startCountdownTimer() {
        //FAKE COUNTDOWN FOR DEAL OF THE DAY
        val totalMillis = 21 * 60 * 60 * 1000L + 12 * 60 * 1000L

        countDownTimer = object : CountDownTimer(totalMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val hours = (millisUntilFinished / (1000 * 60 * 60)).toInt()
                val minutes = ((millisUntilFinished / (1000 * 60)) % 60).toInt()
                val seconds = ((millisUntilFinished / 1000) % 60).toInt()
                _binding?.dealOfDayTimeText?.text = String.format("%02dh %02dm %02ds left", hours, minutes, seconds)
            }

            override fun onFinish() {
                _binding?.dealOfDayTimeText?.text = "0h 0m 0s"
            }
        }.start()
    }

    private fun setupViewPager() {
        if (bannerUrls.isNotEmpty()) {
            bannerAdapter = BannerAdapter(bannerUrls)
            binding.viewPagerBanner.adapter = bannerAdapter

            binding.viewPagerBanner.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    binding.textViewBannerIndicator.text = "${position + 1}/${bannerUrls.size}"
                    handler.removeCallbacks(runnable)
                    handler.postDelayed(runnable, 3000)
                }
            })
        }
    }

    private fun stopCountdownTimer() {
        countDownTimer?.cancel()
    }

    private fun startAutoScroll() {
        handler.postDelayed(runnable, 3000)
    }

    private fun stopAutoScroll() {
        handler.removeCallbacks(runnable)
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        stopAutoScroll()
        stopCountdownTimer()
    }

}