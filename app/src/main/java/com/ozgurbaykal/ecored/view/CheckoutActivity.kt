package com.ozgurbaykal.ecored.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import com.google.firebase.auth.FirebaseAuth
import com.ozgurbaykal.ecored.databinding.ActivityCheckoutBinding
import com.ozgurbaykal.ecored.model.Address
import com.ozgurbaykal.ecored.model.CreditCard
import com.ozgurbaykal.ecored.model.User
import com.ozgurbaykal.ecored.view.customs.AddAddressDialog
import com.ozgurbaykal.ecored.view.customs.AddCardDialog
import com.ozgurbaykal.ecored.viewmodel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CheckoutActivity : BaseActivity() {

    private lateinit var binding: ActivityCheckoutBinding

    private val userViewModel: UserViewModel by viewModels()

    private val TAG = "CheckoutActivityTAG"
    private val REQUEST_CODE_SELECT_ADDRESS = 1
    private val REQUEST_CODE_SELECT_CARD = 2

    private var selectedAddress: Address? = null
    private var selectedCard: CreditCard? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCheckoutBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        FirebaseAuth.getInstance().currentUser?.let { userViewModel.fetchUser(it.uid) }

        binding.addNewCardButton.setOnClickListener {
            val dialog = AddCardDialog(this, userViewModel)
            dialog.showDialog()
        }

        binding.addNewAddress.setOnClickListener {
            val dialog = AddAddressDialog(this, userViewModel)
            dialog.showDialog()
        }

        binding.showAllAddressButton.setOnClickListener {
            val intent = Intent(this, ListAddressOrCardActivity::class.java)
            intent.putExtra("type", "addresses")
            startActivityForResult(intent, REQUEST_CODE_SELECT_ADDRESS)
        }

        binding.showAllCardButton.setOnClickListener {
            val intent = Intent(this, ListAddressOrCardActivity::class.java)
            intent.putExtra("type", "cards")
            startActivityForResult(intent, REQUEST_CODE_SELECT_CARD)
        }

        observeUserChanges()
    }

    private fun observeUserChanges() {
        userViewModel.currentUser.observe(this) { user ->
            user?.let {
                Log.i(TAG, "user -> ${user.toString()}")
                updateUIWithUserDetails(it)
            }
        }

        userViewModel.isAddressAdded.observe(this) { isAdded ->
            if (isAdded) {
                fetchUserDetails()
            }
        }

        userViewModel.isCardAdded.observe(this) { isAdded ->
            if (isAdded) {
                fetchUserDetails()
            }
        }
    }

    private fun fetchUserDetails() {
        Log.i(TAG, "fetchUserDetails() -> ")
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        Log.i(TAG, "fetchUserDetails() -> currentUser: $currentUser")

        if (currentUser != null) {
            userViewModel.fetchUser(currentUser.uid)
        }

    }

    private fun updateUIWithUserDetails(user: User) {
        Log.i(TAG, "updateUIWithUserDetails() -> 1")

        if (selectedAddress == null && user.addresses.isNotEmpty()) {
            val firstAddress = user.addresses.first()
            selectedAddress = firstAddress
            binding.addressTitleName.text = firstAddress.addressTitle
            binding.addressDetail.text = firstAddress.address
        } else if (selectedAddress != null) {
            binding.addressTitleName.text = selectedAddress?.addressTitle
            binding.addressDetail.text = selectedAddress?.address
        } else {
            binding.addressTitleName.text = "No Address"
            binding.addressDetail.text = "Please add an address"
        }

        if (selectedCard == null && user.creditCards.isNotEmpty()) {
            val firstCard = user.creditCards.first()
            selectedCard = firstCard
            binding.cardTitleName.text = firstCard.cardTitle
            binding.cardNumber.text = "**** **** **** ${firstCard.cardNumber.takeLast(4)}"
        } else if (selectedCard != null) {
            binding.cardTitleName.text = selectedCard?.cardTitle
            binding.cardNumber.text = "**** **** **** ${selectedCard?.cardNumber?.takeLast(4)}"
        } else {
            binding.cardTitleName.text = "No Card"
            binding.cardNumber.text = "Please add a card"
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_SELECT_ADDRESS -> {
                    val selectedAddress = data?.getParcelableExtra<Address>("selected_address")
                    selectedAddress?.let {
                        this.selectedAddress = it
                        binding.addressTitleName.text = it.addressTitle
                        binding.addressDetail.text = it.address
                    }
                }
                REQUEST_CODE_SELECT_CARD -> {
                    val selectedCard = data?.getParcelableExtra<CreditCard>("selected_card")
                    selectedCard?.let {
                        this.selectedCard = it
                        binding.cardTitleName.text = it.cardTitle
                        binding.cardNumber.text = "**** **** **** ${it.cardNumber.takeLast(4)}"
                    }
                }
            }
        } else {
            fetchUserDetails()
        }
    }

    override fun onResume() {
        super.onResume()
        fetchUserDetails()
    }
}
