package com.ozgurbaykal.ecored.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.ozgurbaykal.ecored.R
import com.ozgurbaykal.ecored.databinding.ActivityLoginBinding
import com.ozgurbaykal.ecored.databinding.ActivityMainBinding
import com.ozgurbaykal.ecored.util.SharedPreferencesHelper
import com.ozgurbaykal.ecored.view.fragment.CartFragment
import com.ozgurbaykal.ecored.view.fragment.HomeFragment
import com.ozgurbaykal.ecored.view.fragment.ProductListFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val isFirstLogin = SharedPreferencesHelper.getBoolean(SharedPreferencesHelper.KEY_IS_FIRST_RUN, true)

        if (isFirstLogin) {
            startActivity(Intent(this, WalkthroughActivity::class.java))
            SharedPreferencesHelper.setBoolean(SharedPreferencesHelper.KEY_IS_FIRST_RUN, false)
        }

        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.mainActivityFragmentView, HomeFragment(), "HomeFragmentTAG")
                        .commit()
                    true
                }
                R.id.navigation_favorites -> {
                    val bundle = Bundle().apply {
                        putString("caller", "favoritesButton")
                    }
                    changeFragmentWithBundle(ProductListFragment(), R.id.mainActivityFragmentView, "ProductListFragmentTAG", bundle)
                    true
                }
                R.id.navigation_cart -> {
                    changeFragment(CartFragment(), R.id.mainActivityFragmentView, "CartFragmentTAG")
                    true
                }
                R.id.navigation_account -> {
                    true
                }
                else -> false
            }
        }

        binding.bottomNavigationView.selectedItemId = R.id.navigation_home

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val fragment = supportFragmentManager.findFragmentById(R.id.mainActivityFragmentView)
                if (fragment is ProductListFragment && fragment.arguments?.getString("caller") == "favoritesButton") {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.mainActivityFragmentView, HomeFragment(), "HomeFragmentTAG")
                        .commit()
                    binding.bottomNavigationView.selectedItemId = R.id.navigation_home
                } else if (fragment is CartFragment) {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.mainActivityFragmentView, HomeFragment(), "HomeFragmentTAG")
                        .commit()
                    binding.bottomNavigationView.selectedItemId = R.id.navigation_home
                } else {
                    if (supportFragmentManager.backStackEntryCount > 0) {
                        supportFragmentManager.popBackStack()
                    } else {
                        finish()
                    }
                }
            }
        })
    }
}
