package com.ozgurbaykal.ecored.view.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.ozgurbaykal.ecored.R
import com.ozgurbaykal.ecored.databinding.FragmentSignInBinding
import com.ozgurbaykal.ecored.view.BaseFragment
import com.ozgurbaykal.ecored.view.MainActivity
import com.ozgurbaykal.ecored.view.customs.CustomDialogFragment
import com.ozgurbaykal.ecored.view.customs.DialogTypes
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignInFragment : BaseFragment() {

    private var _binding: FragmentSignInBinding? = null
    private val binding get() = _binding!!

    var customToast: CustomDialogFragment? = null
    private lateinit var auth: FirebaseAuth

    private lateinit var mailInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSignInBinding.inflate(inflater, container, false)
        val view = binding.root

        mailInput = binding.mailEditText
        passwordInput = binding.passwordEditText

        customToast = CustomDialogFragment(requireActivity())
        var editTextArray = listOf(mailInput, passwordInput)

        auth = Firebase.auth

        binding.signInButton.setOnClickListener {
            if (emptyAndValidateInputControl(editTextArray)) {
                signInWithFirebase(mailInput.text.toString(), passwordInput.text.toString())
            }
        }

        binding.goToSignUp.setOnClickListener {
            changeFragment(SignUpFragment(), R.id.loginActivityFragmentView, "SignUpFragmentTAG")
        }

        return view
    }

    private fun signInWithFirebase(mail: String, password: String) {
        manageProgressBar(true, 7000){
            customToast!!.show(
                getString(R.string.error),
                getString(R.string.sign_in_error),
                dialogType = DialogTypes.ERROR
            )
        }

        FirebaseAuth.getInstance()
            .signInWithEmailAndPassword(mail, password)
            .addOnCompleteListener { task ->
                activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                if (task.isSuccessful) {
                    val user = FirebaseAuth.getInstance().currentUser
                    //DIRECT TO MAINACTIVITY
                    manageProgressBar(false, 0)

                    startActivity(Intent(requireActivity(), MainActivity::class.java))

                } else {
                    manageProgressBar(false, 0)

                    val errorCode =
                        if (task.exception is FirebaseAuthException) {
                            (task.exception as FirebaseAuthException).errorCode
                        } else {
                            ""
                        }

                    when (errorCode) {


                        "ERROR_INVALID_EMAIL" -> {
                            customToast!!.show(
                                getString(R.string.error),
                                getString(R.string.invalid_mail),
                                dialogType = DialogTypes.ERROR
                            )
                        }

                        else -> {
                            customToast!!.show(
                                getString(R.string.error),
                                getString(R.string.sign_in_error),
                                dialogType = DialogTypes.ERROR
                            )
                        }
                    }

                }
            }
    }


    private fun emptyAndValidateInputControl(editTextArray: List<TextInputEditText>): Boolean {

        for (editText in editTextArray) {
            if (editText.text?.isEmpty() == true) {
                editText.postInvalidate()
                customToast!!.show(
                    getString(R.string.warning),
                    getString(R.string.please_fill_all_fields),
                    dialogType = DialogTypes.WARNING
                )
                return false
            } else {
                editText.setBackgroundResource(R.drawable.edittext_background)
            }
        }

        return true
    }

    override fun onPause() {
        super.onPause()
        customToast?.removeToast()
    }


}