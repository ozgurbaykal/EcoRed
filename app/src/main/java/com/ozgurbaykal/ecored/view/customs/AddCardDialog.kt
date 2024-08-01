package com.ozgurbaykal.ecored.view.customs

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.ozgurbaykal.ecored.databinding.AddCardDialogBinding
import com.ozgurbaykal.ecored.model.CreditCard
import com.ozgurbaykal.ecored.viewmodel.UserViewModel

class AddCardDialog(context: Context, private val userViewModel: UserViewModel) : Dialog(context) {

    private val dialogBinding: AddCardDialogBinding = AddCardDialogBinding.inflate(LayoutInflater.from(context))

    init {
        setContentView(dialogBinding.root)
        window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        setCancelable(true)

        dialogBinding.addToCart.setOnClickListener {
            val cardNumber = dialogBinding.cardNumberInput.text.toString()
            val monthExp = dialogBinding.monthExpDateInput.text.toString()
            val yearExp = dialogBinding.yearExpDateInput.text.toString()
            val ccv = dialogBinding.ccvNumberInput.text.toString()
            val cardTitle = dialogBinding.cardTitle.text.toString()

            val creditCard = CreditCard(cardNumber, monthExp, yearExp, ccv, cardTitle)
            userViewModel.addCreditCard(creditCard)
        }

        userViewModel.isCardAdded.observeForever { isAdded ->
            if (isAdded) {
                FirebaseAuth.getInstance().currentUser?.let { userViewModel.fetchUser(it.uid) }
                dismiss()
            }
        }

        userViewModel.isLoading.observeForever { isLoading ->
            dialogBinding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    fun showDialog(card: CreditCard? = null) {
        card?.let {
            dialogBinding.cardNumberInput.setText(it.cardNumber)
            dialogBinding.monthExpDateInput.setText(it.expirationMonth)
            dialogBinding.yearExpDateInput.setText(it.expirationYear)
            dialogBinding.ccvNumberInput.setText(it.ccv)
            dialogBinding.cardTitle.setText(it.cardTitle)
        }
        show()
    }
}
