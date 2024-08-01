package com.ozgurbaykal.ecored.view.customs

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.ozgurbaykal.ecored.databinding.AddAddressDialogBinding
import com.ozgurbaykal.ecored.model.Address
import com.ozgurbaykal.ecored.viewmodel.UserViewModel

class AddAddressDialog(context: Context, private val userViewModel: UserViewModel) : Dialog(context) {

    private val dialogBinding: AddAddressDialogBinding = AddAddressDialogBinding.inflate(LayoutInflater.from(context))

    init {
        setContentView(dialogBinding.root)
        window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        setCancelable(true)

        dialogBinding.addToAddress.setOnClickListener {
            val name = dialogBinding.nameInput.text.toString()
            val lastName = dialogBinding.lastNameInput.text.toString()
            val phoneNumber = dialogBinding.phoneInput.text.toString()
            val address = dialogBinding.addressInput.text.toString()
            val city = dialogBinding.cityInput.text.toString()
            val state = dialogBinding.stateInput.text.toString()
            val addressTitle = dialogBinding.addressSavedTitle.text.toString()

            val addressModel = Address(name, lastName, phoneNumber, address, city, state, addressTitle)
            userViewModel.addAddress(addressModel)
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
