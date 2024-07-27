package com.ozgurbaykal.ecored.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.ozgurbaykal.ecored.R
import com.ozgurbaykal.ecored.databinding.ActivityLoginBinding
import com.ozgurbaykal.ecored.databinding.ActivityMainBinding
import com.ozgurbaykal.ecored.util.SharedPreferencesHelper
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

        if(isFirstLogin){
            startActivity(Intent(this, WalkthroughActivity::class.java))
            SharedPreferencesHelper.setBoolean(SharedPreferencesHelper.KEY_IS_FIRST_RUN, false)
        }


        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {

                    true
                }
                R.id.navigation_favorites -> {

                    true
                }
                R.id.navigation_cart -> {

                    true
                }
                R.id.navigation_account -> {

                    true
                }
                else -> false
            }
        }


        binding.bottomNavigationView.selectedItemId = R.id.navigation_home


    }
}