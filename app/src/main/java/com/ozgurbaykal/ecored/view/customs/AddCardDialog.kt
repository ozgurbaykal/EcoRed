package com.ozgurbaykal.ecored.view.customs

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.ozgurbaykal.ecored.R
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

        val editTextArray = listOf(dialogBinding.cardNumberInput, dialogBinding.monthExpDateInput, dialogBinding.yearExpDateInput, dialogBinding.ccvNumberInput, dialogBinding.cardTitle)

        dialogBinding.addToCart.setOnClickListener {
            if (emptyAndValidateInputControl(editTextArray)) {
                val cardNumber = dialogBinding.cardNumberInput.text.toString()
                val monthExp = dialogBinding.monthExpDateInput.text.toString()
                val yearExp = dialogBinding.yearExpDateInput.text.toString()
                val ccv = dialogBinding.ccvNumberInput.text.toString()
                val cardTitle = dialogBinding.cardTitle.text.toString()

                userViewModel.currentUser.value?.let { user ->
                    if (user.creditCards.any { it.cardTitle == cardTitle }) {
                        Toast.makeText(context, "Card title already used. Change the title!", Toast.LENGTH_SHORT).show()
                    } else {
                        val creditCard = CreditCard(cardNumber, monthExp, yearExp, ccv, cardTitle)
                        userViewModel.addCreditCard(creditCard)
                    }
                }
            }
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

    private fun emptyAndValidateInputControl(editTextArray: List<EditText>): Boolean {
        val cardNumberPattern = Regex("^[0-9]{16}\$")
        val monthPattern = Regex("^(0[1-9]|1[0-2])\$")
        val yearPattern = Regex("^[0-9]{4}\$")
        val ccvPattern = Regex("^[0-9]{3}\$")

        for (editText in editTextArray) {
            if (editText.text?.isEmpty() == true) {
                editText.postInvalidate()
                Toast.makeText(context, context.getString(R.string.please_fill_all_fields), Toast.LENGTH_SHORT).show()
                return false
            } else {
                editText.setBackgroundResource(R.drawable.edittext_background)
            }
        }

        if (!cardNumberPattern.matches(dialogBinding.cardNumberInput.text.toString())) {
            Toast.makeText(context, context.getString(R.string.invalid_card_number), Toast.LENGTH_SHORT).show()
            dialogBinding.cardNumberInput.error = context.getString(R.string.invalid_card_number)
            return false
        }

        if (!monthPattern.matches(dialogBinding.monthExpDateInput.text.toString())) {
            Toast.makeText(context, context.getString(R.string.invalid_month), Toast.LENGTH_SHORT).show()
            dialogBinding.monthExpDateInput.error = context.getString(R.string.invalid_month)
            return false
        }

        if (!yearPattern.matches(dialogBinding.yearExpDateInput.text.toString())) {
            Toast.makeText(context, context.getString(R.string.invalid_year), Toast.LENGTH_SHORT).show()
            dialogBinding.yearExpDateInput.error = context.getString(R.string.invalid_year)
            return false
        }

        if (!ccvPattern.matches(dialogBinding.ccvNumberInput.text.toString())) {
            Toast.makeText(context, context.getString(R.string.invalid_ccv), Toast.LENGTH_SHORT).show()
            dialogBinding.ccvNumberInput.error = context.getString(R.string.invalid_ccv)
            return false
        }

        return true
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
