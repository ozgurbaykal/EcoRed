package com.ozgurbaykal.ecored.view.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.CheckBox
import android.widget.LinearLayout
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.ozgurbaykal.ecored.R
import com.ozgurbaykal.ecored.databinding.FragmentSignUpBinding
import com.ozgurbaykal.ecored.model.User
import com.ozgurbaykal.ecored.view.BaseFragment
import com.ozgurbaykal.ecored.view.MainActivity
import com.ozgurbaykal.ecored.view.customs.CustomDialogFragment
import com.ozgurbaykal.ecored.view.customs.DialogTypes
import com.ozgurbaykal.ecored.viewmodel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SignUpFragment : BaseFragment() {

    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!

    var customToast: CustomDialogFragment? = null
    private lateinit var auth: FirebaseAuth
    private val userViewModel: UserViewModel by viewModels()

    private lateinit var mailInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var passwordAgainInput: TextInputEditText
    private lateinit var passwordLengthErrorLayout: LinearLayout
    private lateinit var passwordDigitErrorLayout: LinearLayout
    private lateinit var passwordLetterError: LinearLayout
    private lateinit var passwordMatchError: LinearLayout
    private lateinit var privacyErrorLayout: LinearLayout;
    private lateinit var privacyPolicyCheckBox: CheckBox
    private lateinit var name: TextInputEditText
    private lateinit var lastName: TextInputEditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        val view = binding.root

        mailInput = binding.mailEditText
        passwordInput = binding.passwordEditText
        passwordAgainInput = binding.passwordAgainEditText
        passwordLengthErrorLayout = binding.characterLengthError
        passwordDigitErrorLayout = binding.needDigitError
        passwordLetterError = binding.needLetterError
        passwordMatchError = binding.passwordMatchError
        privacyPolicyCheckBox = binding.privacyPolicyCheckBox
        privacyErrorLayout = binding.privacyCheckError
        name = binding.name
        lastName = binding.lastName

        customToast = CustomDialogFragment(requireActivity())
        var editTextArray = listOf(mailInput, passwordInput, passwordAgainInput, name, lastName)

        auth = Firebase.auth

        userViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            manageProgressBar(isLoading)
        }

        userViewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                customToast?.show(
                    getString(R.string.error),
                    it,
                    dialogType = DialogTypes.ERROR
                )
            }
        }

        userViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            user?.let {
                startActivity(Intent(requireActivity(), MainActivity::class.java))
                activity?.finish()
            }
        }


        binding.signUpButton.setOnClickListener {
            if (emptyAndValidateInputControl(editTextArray)) {
                signUpToFirebase(
                    mailInput.text.toString(),
                    passwordInput.text.toString(),
                    name.text.toString(),
                    lastName.text.toString()
                )
            }
        }

        binding.goToSignIn.setOnClickListener {
            changeFragment(SignInFragment(), R.id.loginActivityFragmentView, "SignInFragmentTAG")
        }

        passwordInput.doAfterTextChanged { it ->
            val password = it.toString()

            if (it.toString().length < 8 || it.toString().length > 30) {
                passwordLengthErrorLayout.visibility = View.VISIBLE
            } else {
                passwordLengthErrorLayout.visibility = View.GONE
            }

            val containsDigit = password.any { it.isDigit() }

            val containsLetter = password.any { it.isLetter() }

            if (!containsDigit) {
                passwordDigitErrorLayout.visibility = View.VISIBLE
            } else passwordDigitErrorLayout.visibility = View.GONE

            if (!containsLetter) {
                passwordLetterError.visibility = View.VISIBLE
            } else passwordLetterError.visibility = View.GONE

            if (passwordAgainInput.text?.isNotEmpty() == true) {
                if (password != passwordAgainInput.text.toString()) passwordMatchError.visibility =
                    View.VISIBLE
                else passwordMatchError.visibility = View.GONE
            }


        }
        passwordAgainInput.doAfterTextChanged { it ->
            val password = it.toString()

            if (password != passwordInput.text.toString()) passwordMatchError.visibility =
                View.VISIBLE
            else passwordMatchError.visibility = View.GONE

        }

        privacyPolicyCheckBox.setOnClickListener {
            privacyErrorLayout.visibility = View.GONE
        }

        return view
    }

    fun validateEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z](.*)([@]{1})(.{1,})(\\.)(.{1,})$"
        return email.matches(emailRegex.toRegex())
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

        if (!validateEmail(mailInput.text.toString())) {
            customToast!!.show(
                getString(R.string.error),
                getString(R.string.invalid_mail),
                dialogType = DialogTypes.ERROR
            )
            return false
        }

        if (passwordLengthErrorLayout.visibility == View.VISIBLE || passwordLetterError.visibility == View.VISIBLE || passwordDigitErrorLayout.visibility == View.VISIBLE) {

            customToast!!.show(
                getString(R.string.error),
                getString(R.string.invalid_password),
                dialogType = DialogTypes.WARNING
            )
            return false
        }

        if (passwordMatchError.visibility == View.VISIBLE) {
            customToast!!.show(
                getString(R.string.error),
                getString(R.string.password_match_error_2),
                dialogType = DialogTypes.WARNING
            )
            return false
        }

        if (!privacyPolicyCheckBox.isChecked) {
            val shakeCheckBox: Animation = AnimationUtils.loadAnimation(
                requireContext(), R.anim.shake_animation
            )
            privacyPolicyCheckBox.startAnimation(shakeCheckBox)

            privacyErrorLayout.visibility = View.VISIBLE

            customToast!!.show(
                getString(R.string.error),
                getString(R.string.privacy_not_checked_error),
                dialogType = DialogTypes.WARNING
            )

            return false
        }

        return true
    }

    private fun signUpToFirebase(email: String, password: String, name: String, lastName: String) {
        manageProgressBar(true, 7000)
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    val user = auth.currentUser
                    val uid = user?.uid

                    val profileUpdates = userProfileChangeRequest {
                        displayName = "$name $lastName"
                    }

                    user?.updateProfile(profileUpdates)?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {

                            val user = User(
                                userId = uid.toString(),
                                email = email,
                                name = name,
                                lastName = lastName,
                            )

                            userViewModel.addUser(user)
                        }
                    }

                } else {
                    manageProgressBar(false, 0)

                    activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    val errorCode =
                        if (task.exception is FirebaseAuthException) {
                            (task.exception as FirebaseAuthException).errorCode
                        } else {
                            ""
                        }

                    when (errorCode) {
                        "ERROR_EMAIL_ALREADY_IN_USE" -> {
                            customToast!!.show(
                                getString(R.string.error),
                                getString(R.string.mail_already_used),
                                dialogType = DialogTypes.ERROR
                            )
                        }

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
                                getString(R.string.sign_up_error),
                                dialogType = DialogTypes.ERROR
                            )
                        }
                    }

                }
            }
    }


}