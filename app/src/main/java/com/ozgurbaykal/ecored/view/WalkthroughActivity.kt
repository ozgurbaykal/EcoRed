package com.ozgurbaykal.ecored.view

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.ozgurbaykal.ecored.R
import com.ozgurbaykal.ecored.databinding.ActivityLoginBinding
import com.ozgurbaykal.ecored.databinding.ActivityMainBinding
import com.ozgurbaykal.ecored.databinding.ActivityWalkthroughBinding
import com.ozgurbaykal.ecored.model.WalkthroughItem
import com.ozgurbaykal.ecored.util.SharedPreferencesHelper
import com.ozgurbaykal.ecored.view.adapter.WalkthroughAdapter
import dagger.hilt.android.AndroidEntryPoint
import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts

@AndroidEntryPoint
class WalkthroughActivity : BaseActivity() {

    private lateinit var binding: ActivityWalkthroughBinding
    private lateinit var adapter: WalkthroughAdapter
    private lateinit var walkthroughItems: List<WalkthroughItem>

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            finish()
        } else {
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWalkthroughBinding.inflate(layoutInflater)
        setContentView(binding.root)

        walkthroughItems = listOf(
            WalkthroughItem(R.drawable.shop_background, getString(R.string.walkthrough_title_1), getString(R.string.walkthrough_desc_1)),
            WalkthroughItem(R.drawable.backround_catalog, getString(R.string.walkthrough_title_2), getString(R.string.walkthrough_desc_2)),
            WalkthroughItem(R.drawable.notification_background, getString(R.string.walkthrough_title_3), getString(R.string.walkthrough_desc_3))
        )

        setupViewPager()
        setupIndicators()
        setCurrentIndicator(0)

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                setCurrentIndicator(position)
            }
        })

        binding.btnNext.setOnClickListener {
            val currentItem = binding.viewPager.currentItem
            if (currentItem + 1 < walkthroughItems.size) {
                binding.viewPager.currentItem = currentItem + 1
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    requestNotificationPermission()
                } else {
                    finish()
                }
            }
        }
    }

    private fun setupViewPager() {
        adapter = WalkthroughAdapter(walkthroughItems)
        binding.viewPager.adapter = adapter
    }

    private fun setupIndicators() {
        val indicators = arrayOfNulls<ImageView>(walkthroughItems.size)
        val layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(8, 0, 8, 0)
        for (i in indicators.indices) {
            indicators[i] = ImageView(applicationContext)
            indicators[i]?.apply {
                setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.indicator_inactive
                    )
                )
                this.layoutParams = layoutParams
                binding.indicatorLayout.addView(this)
            }
        }
    }

    private fun setCurrentIndicator(index: Int) {
        val childCount = binding.indicatorLayout.childCount
        for (i in 0 until childCount) {
            val imageView = binding.indicatorLayout.getChildAt(i) as ImageView
            if (i == index) {
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.indicator_active
                    )
                )
            } else {
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.indicator_inactive
                    )
                )
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestNotificationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED -> {
                finish()
            }
            else -> {

                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}