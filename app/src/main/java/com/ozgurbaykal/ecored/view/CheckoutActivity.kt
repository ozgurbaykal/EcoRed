package com.ozgurbaykal.ecored.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import com.google.firebase.auth.FirebaseAuth
import com.ozgurbaykal.ecored.databinding.ActivityCheckoutBinding
import com.ozgurbaykal.ecored.view.customs.AddAddressDialog
import com.ozgurbaykal.ecored.view.customs.AddCardDialog
import com.ozgurbaykal.ecored.viewmodel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CheckoutActivity : BaseActivity() {

    private lateinit var binding: ActivityCheckoutBinding

    private val userViewModel: UserViewModel by viewModels()

    private val TAG = "CheckoutActivityTAG"

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
            startActivity(intent)
        }

        binding.showAllCardButton.setOnClickListener {
            val intent = Intent(this, ListAddressOrCardActivity::class.java)
            intent.putExtra("type", "cards")
            startActivity(intent)
        }

        userViewModel.currentUser.observe(this) { user ->
            user?.let {
                Log.i(TAG, "user -> ${user.toString()}")
                if (it.addresses.isNotEmpty()) {
                    val firstAddress = it.addresses.first()
                    binding.addressTitleName.text = firstAddress.addressTitle
                    binding.addressDetail.text = firstAddress.address
                }
                if (it.creditCards.isNotEmpty()) {
                    val firstCard = it.creditCards.first()
                    binding.cardTitleName.text = firstCard.cardTitle
                    binding.cardNumber.text = "**** **** **** ${firstCard.cardNumber.takeLast(4)}"
                }
            }
        }

        userViewModel.isAddressAdded.observe(this) { isAdded ->
            if (isAdded) {
                FirebaseAuth.getInstance().currentUser?.let { userId ->
                    userViewModel.fetchUser(userId.toString())
                }
            }
        }

        userViewModel.isCardAdded.observe(this) { isAdded ->
            if (isAdded) {
                FirebaseAuth.getInstance().currentUser?.let { userId ->
                    userViewModel.fetchUser(userId.toString())
                }
            }
        }
    }


}
