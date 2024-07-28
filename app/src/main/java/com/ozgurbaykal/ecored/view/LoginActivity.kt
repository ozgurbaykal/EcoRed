package com.ozgurbaykal.ecored.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.add
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import com.google.firebase.auth.FirebaseAuth
import com.ozgurbaykal.ecored.R
import com.ozgurbaykal.ecored.databinding.ActivityLoginBinding
import com.ozgurbaykal.ecored.view.customs.DialogTypes
import com.ozgurbaykal.ecored.view.fragment.WelcomeForLoginFragment
import com.ozgurbaykal.ecored.viewmodel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : BaseActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val userViewModel: UserViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        userViewModel.isLoading.observe(this) { isLoading ->
            //manageProgressBar(isLoading)
        }

        userViewModel.errorMessage.observe(this) { errorMessage ->
            errorMessage?.let {
                binding.fakeSplash.visibility = View.GONE
                if (savedInstanceState == null) {
                    supportFragmentManager.commit {
                        setReorderingAllowed(true)
                        add<WelcomeForLoginFragment>(
                            R.id.loginActivityFragmentView,
                            "WelcomeForLoginFragmentTAG"
                        )
                    }
                }
            }
        }

        userViewModel.currentUser.observe(this) { user ->
            user?.let {
                binding.fakeSplash.visibility = View.GONE
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }

        if(currentUser != null){
                userViewModel.fetchUser(currentUser.uid)
        }
        else{
            binding.fakeSplash.visibility = View.GONE
            if (savedInstanceState == null) {
                supportFragmentManager.commit {
                    setReorderingAllowed(true)
                    add<WelcomeForLoginFragment>(
                        R.id.loginActivityFragmentView,
                        "WelcomeForLoginFragmentTAG"
                    )
                }
            }
        }




    }

}