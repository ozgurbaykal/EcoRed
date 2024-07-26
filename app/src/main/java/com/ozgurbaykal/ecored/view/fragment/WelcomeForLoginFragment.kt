package com.ozgurbaykal.ecored.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ozgurbaykal.ecored.R
import com.ozgurbaykal.ecored.databinding.FragmentWelcomeForLoginBinding
import com.ozgurbaykal.ecored.view.BaseFragment
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class WelcomeForLoginFragment : BaseFragment() {

    private var _binding: FragmentWelcomeForLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentWelcomeForLoginBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.signInButton.setOnClickListener {
            changeFragment(SignInFragment(), R.id.loginActivityFragmentView, "SignInFragmentTAG")
        }

        binding.signUpButton.setOnClickListener {
            changeFragment(SignUpFragment(), R.id.loginActivityFragmentView, "SignUpFragmentTAG")
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}