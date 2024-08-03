package com.ozgurbaykal.ecored.view.fragment

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.ozgurbaykal.ecored.R
import com.ozgurbaykal.ecored.databinding.FragmentProfileBinding
import com.ozgurbaykal.ecored.databinding.FragmentWelcomeForLoginBinding
import com.ozgurbaykal.ecored.view.BaseFragment
import com.ozgurbaykal.ecored.view.ListAddressOrCardActivity
import com.ozgurbaykal.ecored.view.LoginActivity
import com.ozgurbaykal.ecored.view.OrderHistoryActivity
import com.ozgurbaykal.ecored.view.customs.AddAddressDialog
import com.ozgurbaykal.ecored.view.customs.CustomDialogFragment
import com.ozgurbaykal.ecored.view.customs.DialogTypes
import com.ozgurbaykal.ecored.viewmodel.UserViewModel
import com.ozgurbaykal.paymentsdk.PaymentCallback
import com.ozgurbaykal.paymentsdk.PaymentSDK
import dagger.hilt.android.AndroidEntryPoint
import java.sql.Date
import java.text.SimpleDateFormat
import java.util.Locale


@AndroidEntryPoint
class ProfileFragment : BaseFragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    var currentUser: FirebaseUser? = null
    private val userViewModel: UserViewModel by viewModels()
    var customToast: CustomDialogFragment? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val view = binding.root
        customToast = CustomDialogFragment(requireActivity())

        fetchUserDetails()

        userViewModel.currentUser.observe(requireActivity()) { user ->
            if (user != null) {
                binding.nameAndLastName.text =
                    "${user.name?.replaceFirstChar(Char::titlecase)}" + " " + "${user.lastName?.replaceFirstChar(Char::titlecase)}"
                binding.nameAndLastNameFirstLetters.text =
                    user.name.first().uppercase() + user.lastName.first().uppercase()

                val creationDate = Date(currentUser?.metadata?.creationTimestamp ?: 0)
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val formattedDate = dateFormat.format(creationDate)
                binding.accountCreatedDate.text = getString(R.string.account_created, formattedDate)
            }
        }

        userViewModel.isLoading.observe(requireActivity()) { isLoading ->
            manageProgressBar(isLoading)
        }

        binding.myAddresses.setOnClickListener {
            val intent = Intent(requireContext(), ListAddressOrCardActivity::class.java)
            intent.putExtra("type", "addresses")
            intent.putExtra("isFromProfile", true)
            startActivity(intent)
        }

        binding.myCreditCards.setOnClickListener {
            val intent = Intent(requireContext(), ListAddressOrCardActivity::class.java)
            intent.putExtra("type", "cards")
            intent.putExtra("isFromProfile", true)
            startActivity(intent)
        }

        binding.changePassword.setOnClickListener {
            showChangePasswordDialog()
        }

        binding.orderHistory.setOnClickListener {
            val intent = Intent(requireContext(), OrderHistoryActivity::class.java)
            startActivity(intent)
        }

        binding.logOut.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }


        return view
    }

    private fun showChangePasswordDialog() {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.change_password_dialog)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(true)

        val oldPasswordInput = dialog.findViewById<TextInputEditText>(R.id.oldPasswordInput)
        val newPasswordInput = dialog.findViewById<TextInputEditText>(R.id.newPasswordInput)
        val newPasswordAgainInput = dialog.findViewById<TextInputEditText>(R.id.newPasswordAgainInput)
        val changePasswordButton = dialog.findViewById<Button>(R.id.changePassword)
        val passwordLengthErrorLayout = dialog.findViewById<LinearLayout>(R.id.characterLengthError)
        val passwordDigitErrorLayout = dialog.findViewById<LinearLayout>(R.id.needDigitError)
        val passwordLetterError = dialog.findViewById<LinearLayout>(R.id.needLetterError)
        val passwordMatchError = dialog.findViewById<LinearLayout>(R.id.passwordMatchError)
        val progressBar = dialog.findViewById<FrameLayout>(R.id.progressBar)

        val editTextArray = listOf(oldPasswordInput, newPasswordInput, newPasswordAgainInput)

        newPasswordInput.doAfterTextChanged { it ->
            val password = it.toString()

            passwordLengthErrorLayout.visibility = if (password.length < 8 || password.length > 30) View.VISIBLE else View.GONE
            passwordDigitErrorLayout.visibility = if (password.any { it.isDigit() }) View.GONE else View.VISIBLE
            passwordLetterError.visibility = if (password.any { it.isLetter() }) View.GONE else View.VISIBLE

            if (newPasswordAgainInput.text?.isNotEmpty() == true) {
                passwordMatchError.visibility = if (password != newPasswordAgainInput.text.toString()) View.VISIBLE else View.GONE
            }
        }

        newPasswordAgainInput.doAfterTextChanged { it ->
            passwordMatchError.visibility = if (it.toString() != newPasswordInput.text.toString()) View.VISIBLE else View.GONE
        }

        changePasswordButton.setOnClickListener {
            if (emptyAndValidateInputControl(
                    editTextArray,
                    passwordLengthErrorLayout,
                    passwordLetterError,
                    passwordDigitErrorLayout,
                    passwordMatchError
                )) {
                val oldPassword = oldPasswordInput.text.toString()
                val newPassword = newPasswordInput.text.toString()

                progressBar.visibility = View.VISIBLE

                val user = FirebaseAuth.getInstance().currentUser
                val credential = EmailAuthProvider.getCredential(user!!.email!!, oldPassword)

                user.reauthenticate(credential).addOnCompleteListener { reauthTask ->
                    if (reauthTask.isSuccessful) {
                        user.updatePassword(newPassword).addOnCompleteListener { updateTask ->
                            if (updateTask.isSuccessful) {
                                progressBar.visibility = View.GONE
                                customToast!!.show(
                                    getString(R.string.success),
                                    getString(R.string.pass_change_success),
                                    dialogType = DialogTypes.SUCCESS
                                )
                                dialog.dismiss()
                            } else {
                                progressBar.visibility = View.GONE
                                customToast!!.show(
                                    getString(R.string.error),
                                    updateTask.exception?.message ?: getString(R.string.pass_change_failed),
                                    dialogType = DialogTypes.ERROR
                                )
                            }
                        }
                    } else {
                        progressBar.visibility = View.GONE
                        customToast!!.show(
                            getString(R.string.error),
                            reauthTask.exception?.message ?: getString(R.string.pass_change_failed),
                            dialogType = DialogTypes.ERROR
                        )
                    }
                }
            }
        }

        dialog.show()
    }

    private fun emptyAndValidateInputControl(
        editTextArray: List<TextInputEditText>,
        passwordLengthErrorLayout: LinearLayout,
        passwordLetterError: LinearLayout,
        passwordDigitErrorLayout: LinearLayout,
        passwordMatchError: LinearLayout,
    ): Boolean {

        for (editText in editTextArray) {
            if (editText.text?.isEmpty() == true) {
                editText.postInvalidate()
                customToast!!.show(
                    getString(R.string.warning),
                    getString(R.string.please_fill_all_fields),
                    dialogType = DialogTypes.WARNING
                )

                Toast.makeText(requireContext(), getString(R.string.please_fill_all_fields), Toast.LENGTH_SHORT).show()

                return false
            } else {
                editText.setBackgroundResource(R.drawable.edittext_background)
            }
        }

        if (passwordLengthErrorLayout.visibility == View.VISIBLE || passwordLetterError.visibility == View.VISIBLE || passwordDigitErrorLayout.visibility == View.VISIBLE) {
            customToast!!.show(
                getString(R.string.error),
                getString(R.string.invalid_password),
                dialogType = DialogTypes.WARNING
            )

            Toast.makeText(requireContext(), getString(R.string.invalid_password), Toast.LENGTH_SHORT).show()

            return false
        }

        if (passwordMatchError.visibility == View.VISIBLE) {
            customToast!!.show(
                getString(R.string.error),
                getString(R.string.password_match_error_2),
                dialogType = DialogTypes.WARNING
            )

            Toast.makeText(requireContext(), getString(R.string.password_match_error_2), Toast.LENGTH_SHORT).show()

            return false
        }

        return true
    }

    private fun fetchUserDetails() {
        val auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser

        if (currentUser != null) {
            userViewModel.fetchUser(currentUser!!.uid)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
