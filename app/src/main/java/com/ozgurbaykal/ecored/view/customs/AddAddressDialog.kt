package com.ozgurbaykal.ecored.view.customs

import android.app.Dialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat.getString
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.ozgurbaykal.ecored.R
import com.ozgurbaykal.ecored.databinding.AddAddressDialogBinding
import com.ozgurbaykal.ecored.model.Address
import com.ozgurbaykal.ecored.viewmodel.UserViewModel

class AddAddressDialog(context: Context, private val userViewModel: UserViewModel) :
    Dialog(context) {

    private val dialogBinding: AddAddressDialogBinding =
        AddAddressDialogBinding.inflate(LayoutInflater.from(context))

    init {
        setContentView(dialogBinding.root)
        window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        setCancelable(true)

        val editTextArray = listOf(
            dialogBinding.nameInput,
            dialogBinding.lastNameInput,
            dialogBinding.phoneInput,
            dialogBinding.addressInput,
            dialogBinding.cityInput,
            dialogBinding.stateInput,
            dialogBinding.addressSavedTitle
        )

        dialogBinding.addToAddress.setOnClickListener {
            if (emptyAndValidateInputControl(editTextArray)) {
                val name = dialogBinding.nameInput.text.toString()
                val lastName = dialogBinding.lastNameInput.text.toString()
                val phoneNumber = dialogBinding.phoneInput.text.toString()
                val address = dialogBinding.addressInput.text.toString()
                val city = dialogBinding.cityInput.text.toString()
                val state = dialogBinding.stateInput.text.toString()
                val addressTitle = dialogBinding.addressSavedTitle.text.toString()

                userViewModel.currentUser.value?.let { user ->
                    if (user.addresses.any { it.addressTitle == addressTitle }) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.address_already_used),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        val addressModel =
                            Address(name, lastName, phoneNumber, address, city, state, addressTitle)
                        userViewModel.addAddress(addressModel)
                    }
                }
            }
        }

        userViewModel.isAddressAdded.observeForever { isAdded ->
            if (isAdded) {
                FirebaseAuth.getInstance().currentUser?.let { userViewModel.fetchUser(it.uid) }
                dismiss()
            }
        }

        userViewModel.isLoading.observeForever { isLoading ->
            dialogBinding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun emptyAndValidateInputControl(editTextArray: List<EditText>): Boolean {
        for (editText in editTextArray) {
            if (editText.text?.isEmpty() == true) {
                editText.postInvalidate()
                Toast.makeText(
                    context,
                    context.getString(R.string.please_fill_all_fields),
                    Toast.LENGTH_SHORT
                ).show()
                return false
            } else {
                editText.setBackgroundResource(R.drawable.edittext_background)
            }
        }
        return true
    }

    fun showDialog(address: Address? = null) {
        address?.let {
            dialogBinding.nameInput.setText(it.name)
            dialogBinding.lastNameInput.setText(it.lastName)
            dialogBinding.phoneInput.setText(it.phoneNumber)
            dialogBinding.addressInput.setText(it.address)
            dialogBinding.cityInput.setText(it.city)
            dialogBinding.stateInput.setText(it.state)
            dialogBinding.addressSavedTitle.setText(it.addressTitle)
        }
        show()
    }
}

