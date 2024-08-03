package com.ozgurbaykal.ecored.view

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.ozgurbaykal.ecored.model.Order
import com.ozgurbaykal.ecored.R
import com.ozgurbaykal.ecored.databinding.ActivityCheckoutBinding
import com.ozgurbaykal.ecored.model.Address
import com.ozgurbaykal.ecored.model.CreditCard
import com.ozgurbaykal.ecored.model.User
import com.ozgurbaykal.ecored.view.adapter.CartAdapter
import com.ozgurbaykal.ecored.view.customs.AddAddressDialog
import com.ozgurbaykal.ecored.view.customs.AddCardDialog
import com.ozgurbaykal.ecored.viewmodel.CommonViewModel
import com.ozgurbaykal.ecored.viewmodel.UserViewModel
import com.ozgurbaykal.paymentsdk.PaymentCallback
import com.ozgurbaykal.paymentsdk.PaymentSDK


class CheckoutActivity : BaseActivity() {

    private lateinit var binding: ActivityCheckoutBinding
    private val userViewModel: UserViewModel by viewModels()
    private val commonViewModel: CommonViewModel by viewModels()

    private val TAG = "CheckoutActivityTAG"
    private val REQUEST_CODE_SELECT_ADDRESS = 1
    private val REQUEST_CODE_SELECT_CARD = 2

    private var selectedAddress: Address? = null
    private var selectedCard: CreditCard? = null
    private var totalDiscountedPrice = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCheckoutBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        FirebaseAuth.getInstance().currentUser?.let { userViewModel.fetchUser(it.uid) }

        setupCartRecyclerView()

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

        binding.backButton.setOnClickListener {
            finish()
        }

        binding.pricesLayout.setOnClickListener {
            if (binding.totalDiscount.visibility == View.GONE) {
                binding.totalDiscount.visibility = View.VISIBLE
                binding.totalPriceBeforeDiscount.visibility = View.VISIBLE
                binding.arrowIcon.setImageResource(R.drawable.arrow_down)
            } else {
                binding.totalDiscount.visibility = View.GONE
                binding.totalPriceBeforeDiscount.visibility = View.GONE
                binding.arrowIcon.setImageResource(R.drawable.arrow_up)
            }
        }

        val totalPrice = intent.getDoubleExtra("totalPrice", 0.0)
        totalDiscountedPrice = intent.getDoubleExtra("totalDiscountedPrice", 0.0)
        val totalDiscount = intent.getDoubleExtra("totalDiscount", 0.0)

        binding.calculatedTotalPrice.text = String.format("$%.2f", totalDiscountedPrice)
        binding.totalPriceBeforeDiscount.text = String.format("$%.2f", totalPrice)
        binding.totalDiscount.text = String.format("-$%.2f", totalDiscount)

        binding.commpletePayment.setOnClickListener {
            selectedCard?.let { card ->
                PaymentSDK.startPayment(card.cardNumber, card.expirationYear, card.ccv, totalPrice, object : PaymentCallback {
                    override fun onSuccess() {
                        runOnUiThread {
                            showOtpDialog()
                        }
                    }

                    override fun onFailure(error: String) {
                        Toast.makeText(this@CheckoutActivity, error, Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }

        observeUserChanges()
    }

    private fun showOtpDialog() {
        val dialog = Dialog(this@CheckoutActivity)
        dialog.setContentView(R.layout.otp_verification_dialog)
        dialog.window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(true)

        val otpInput = dialog.findViewById<EditText>(R.id.cardNumberInput)
        val verifyButton = dialog.findViewById<Button>(R.id.addToCart)
        val sendOtpText = dialog.findViewById<TextView>(R.id.sendOtpNumber)
        val progressBar = dialog.findViewById<View>(R.id.progressBar)

        sendOtpText.text = this@CheckoutActivity.getString(R.string.verify_otp, selectedAddress?.phoneNumber)

        verifyButton.setOnClickListener {
            val otp = otpInput.text.toString()
            progressBar.visibility = View.VISIBLE
            PaymentSDK.confirmPayment(otp, object : PaymentCallback {
                override fun onSuccess() {
                    runOnUiThread {
                        progressBar.visibility = View.GONE
                        dialog.dismiss()
                        completeOrder()
                    }

                }

                override fun onFailure(error: String) {
                    runOnUiThread {
                        progressBar.visibility = View.GONE
                        Toast.makeText(this@CheckoutActivity, error, Toast.LENGTH_SHORT).show()
                    }

                }
            })
        }

        dialog.show()
    }

    private fun completeOrder() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null && selectedAddress != null && selectedCard != null) {
            val order = Order(
                address = selectedAddress!!,
                card = selectedCard!!,
                totalPrice = totalDiscountedPrice,
                items = userViewModel.currentUser.value?.cart ?: emptyList()
            )

            userViewModel.completeOrder(order)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("SHOW_HOME_FRAGMENT", true)
        }
        startActivity(intent)
        finish()
    }


    private fun setupCartRecyclerView() {
        val cartAdapter = CartAdapter(
            this,
            emptyList(),
            onIncreaseClick = { },
            onDecreaseClick = { },
            fetchProductDetails = { productId, callback ->
                commonViewModel.getProductById(productId, callback)
            },
            isCheckoutActivity = true
        )
        binding.cartRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.cartRecyclerView.adapter = cartAdapter

        userViewModel.currentUser.observe(this) { user ->
            user?.let {
                cartAdapter.updateItems(it.cart)
            }
        }
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

        userViewModel.isOrderCompleted.observe(this) { isCompleted ->
            if (isCompleted == true) {
                Toast.makeText(this, "Order completed successfully!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Failed to complete order", Toast.LENGTH_SHORT).show()
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
            binding.addressTitleName.text = getString(R.string.no_address)
            binding.addressDetail.text = getString(R.string.please_add_address)
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
            binding.cardTitleName.text = getString(R.string.no_card)
            binding.cardNumber.text = getString(R.string.please_add_card)
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
