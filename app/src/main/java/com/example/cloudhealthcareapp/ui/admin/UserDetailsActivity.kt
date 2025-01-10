package com.example.cloudhealthcareapp.ui.admin

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.cloudhealthcareapp.R
import com.example.cloudhealthcareapp.models.Doctor
import com.example.cloudhealthcareapp.models.Patient
import com.example.cloudhealthcareapp.viewmodel.AdminViewModel

class UserDetailsActivity : AppCompatActivity() {

    private val viewModel: AdminViewModel by viewModels()

    private lateinit var fullNameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var addressEditText: EditText
    private lateinit var specialtyEditText: EditText
    private lateinit var profileImageView: ImageView
    private lateinit var idCardImageView: ImageView
    private lateinit var updateButton: Button
    private lateinit var userType: String
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_details)

        fullNameEditText = findViewById(R.id.fullNameEditText)
        emailEditText = findViewById(R.id.emailEditText)
        phoneEditText = findViewById(R.id.phoneEditText)
        addressEditText = findViewById(R.id.addressEditText)
        specialtyEditText = findViewById(R.id.specialtyEditText)
        profileImageView = findViewById(R.id.profileImageButton)
        idCardImageView = findViewById(R.id.idCardImageButton)
        updateButton = findViewById(R.id.updateButton)

        userId = intent.getStringExtra("userId") ?: ""
        userType = intent.getStringExtra("userType") ?: ""

        viewModel.getUserDetails(userId, userType)

        viewModel.userDetails.observe(this) { user ->
            when (user) {
                is Patient -> {
                    fullNameEditText.setText(user.fullName)
                    emailEditText.setText(user.email)
                    phoneEditText.setText(user.phone)
                    addressEditText.setText(user.address)
                    specialtyEditText.visibility = EditText.GONE

                    Glide.with(this).load(user.profileImageUrl).into(profileImageView)
                    Glide.with(this).load(user.idCardImageUrl).into(idCardImageView)
                }
                is Doctor -> {
                    fullNameEditText.setText(user.fullName)
                    emailEditText.setText(user.email)
                    phoneEditText.setText(user.phone)
                    addressEditText.visibility = EditText.GONE
                    specialtyEditText.setText(user.specialty)

                    Glide.with(this).load(user.profileImageUrl).into(profileImageView)
                    Glide.with(this).load(user.idCardImageUrl).into(idCardImageView)
                }
                // Handle Administrator if needed
            }
        }

        updateButton.setOnClickListener {
            val updatedUser = when (val user = viewModel.userDetails.value) {
                is Patient -> Patient(
                    userId = userId,
                    fullName = fullNameEditText.text.toString(),
                    email = emailEditText.text.toString(),
                    phone = phoneEditText.text.toString(),
                    address = addressEditText.text.toString(),
                    profileImageUrl = (user as Patient).profileImageUrl,
                    idCardImageUrl = (user as Patient).idCardImageUrl,
                    isVerified = user.isVerified
                )
                is Doctor -> Doctor(
                    userId = userId,
                    fullName = fullNameEditText.text.toString(),
                    email = emailEditText.text.toString(),
                    phone = phoneEditText.text.toString(),
                    specialty = specialtyEditText.text.toString(),
                    profileImageUrl = (user as Doctor).profileImageUrl,
                    idCardImageUrl = (user as Doctor).idCardImageUrl,
                    isVerified = user.isVerified
                )
                // Handle Administrator if needed
                else -> null
            }

            if (updatedUser != null) {
                viewModel.updateUser(updatedUser, userId, userType)
            }
        }

        viewModel.updateResult.observe(this) { success ->
            if (success) {
                Toast.makeText(this, "User updated successfully", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Failed to update user", Toast.LENGTH_SHORT).show()
            }
        }
    }
}