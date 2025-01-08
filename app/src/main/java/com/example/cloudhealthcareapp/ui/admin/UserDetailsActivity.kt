package com.example.cloudhealthcareapp.ui.admin

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.cloudhealthcareapp.R
import com.example.cloudhealthcareapp.models.Administrator
import com.example.cloudhealthcareapp.models.Doctor
import com.example.cloudhealthcareapp.models.Patient
import com.example.cloudhealthcareapp.viewmodel.AdminViewModel
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class UserDetailsActivity : AppCompatActivity() {

    private val viewModel: AdminViewModel by viewModels()

    private lateinit var fullNameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var addressEditText: EditText
    private lateinit var specialtyEditText: EditText
    private lateinit var profileImageButton: ImageButton
    private lateinit var idCardImageButton: ImageButton
    private lateinit var updateButton: Button

    private lateinit var userType: String
    private lateinit var userId: String

    private var profileImageUri: Uri? = null
    private var idCardImageUri: Uri? = null
    private var originalProfileImageUrl: String? = null
    private var originalIdCardImageUrl: String? = null
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    // ActivityResultLauncher for selecting profile image
    private val selectProfileImage =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                profileImageUri = result.data?.data
                profileImageButton.setImageURI(profileImageUri)
            }
        }

    // ActivityResultLauncher for selecting ID card image
    private val selectIdCardImage =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                idCardImageUri = result.data?.data
                idCardImageButton.setImageURI(idCardImageUri)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_details)

        fullNameEditText = findViewById(R.id.fullNameEditText)
        emailEditText = findViewById(R.id.emailEditText)
        phoneEditText = findViewById(R.id.phoneEditText)
        addressEditText = findViewById(R.id.addressEditText)
        specialtyEditText = findViewById(R.id.specialtyEditText)
        profileImageButton = findViewById(R.id.profileImageButton)
        idCardImageButton = findViewById(R.id.idCardImageButton)
        updateButton = findViewById(R.id.updateButton)

        userId = intent.getStringExtra("userId") ?: ""
        userType = intent.getStringExtra("userType") ?: ""

        profileImageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "image/*"
            }
            selectProfileImage.launch(intent)
        }

        idCardImageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "image/*"
            }
            selectIdCardImage.launch(intent)
        }

        viewModel.getUserDetails(userId, userType)

        viewModel.userDetails.observe(this) { user ->
            when (user) {
                is Patient -> {
                    fullNameEditText.setText(user.fullName)
                    emailEditText.setText(user.email)
                    phoneEditText.setText(user.phone)
                    addressEditText.setText(user.address)
                    originalProfileImageUrl = user.profileImageUrl
                    originalIdCardImageUrl = user.idCardImageUrl

                    Glide.with(this).load(user.profileImageUrl).into(profileImageButton)
                    Glide.with(this).load(user.idCardImageUrl).into(idCardImageButton)
                }
                is Doctor -> {
                    fullNameEditText.setText(user.fullName)
                    emailEditText.setText(user.email)
                    phoneEditText.setText(user.phone)
                    specialtyEditText.setText(user.specialty)
                    originalProfileImageUrl = user.profileImageUrl
                    originalIdCardImageUrl = user.idCardImageUrl

                    Glide.with(this).load(user.profileImageUrl).into(profileImageButton)
                    Glide.with(this).load(user.idCardImageUrl).into(idCardImageButton)
                }
                // Handle Administrator if needed
            }
        }

        updateButton.setOnClickListener {
            uploadImagesAndUpdateUser()
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

    private fun uploadImagesAndUpdateUser() {
        // Upload new images if selected, otherwise use original URLs
        val profileImageRef = storage.reference.child("profileImages/$userId/${UUID.randomUUID()}")
        val idCardImageRef = storage.reference.child("idCards/$userId/${UUID.randomUUID()}")

        val uploadProfileTask = profileImageUri?.let { profileImageRef.putFile(it) }
        val uploadIdCardTask = idCardImageUri?.let { idCardImageRef.putFile(it) }

        val profileImageUrl = if (uploadProfileTask != null) {
            profileImageRef.downloadUrl.toString()
        } else {
            originalProfileImageUrl
        }

        val idCardImageUrl = if (uploadIdCardTask != null) {
            idCardImageRef.downloadUrl.toString()
        } else {
            originalIdCardImageUrl
        }

        // Update user data after handling image uploads
        val uploadTasks = listOfNotNull(uploadProfileTask, uploadIdCardTask)
        if (uploadTasks.isNotEmpty()) {
            uploadTasks.forEach { task ->
                task.addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to upload image: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            // Wait for all upload tasks to complete
            com.google.android.gms.tasks.Tasks.whenAll(uploadTasks).addOnSuccessListener {
                // Get download URLs after uploads are complete
                val profileTask = if (uploadProfileTask != null) profileImageRef.downloadUrl else com.google.android.gms.tasks.Tasks.forResult(Uri.parse(originalProfileImageUrl))
                val idCardTask = if (uploadIdCardTask != null) idCardImageRef.downloadUrl else com.google.android.gms.tasks.Tasks.forResult(Uri.parse(originalIdCardImageUrl))

                com.google.android.gms.tasks.Tasks.whenAllSuccess<Uri>(profileTask, idCardTask).addOnSuccessListener { results ->
                    val updatedProfileImageUrl = results[0].toString()
                    val updatedIdCardImageUrl = results[1].toString()

                    // Update user with new image URLs
                    updateUser(updatedProfileImageUrl, updatedIdCardImageUrl)
                }
            }
        } else {
            // No new images to upload, update user directly
            updateUser(profileImageUrl, idCardImageUrl)
        }
    }

    private fun updateUser(profileImageUrl: String?, idCardImageUrl: String?) {
        val updatedUser = when (val user = viewModel.userDetails.value) {
            is Patient -> Patient(
                userId = userId,
                fullName = fullNameEditText.text.toString(),
                email = emailEditText.text.toString(),
                phone = phoneEditText.text.toString(),
                address = addressEditText.text.toString(),
                profileImageUrl = profileImageUrl,
                idCardImageUrl = idCardImageUrl,
                isVerified = user.isVerified
            )
            is Doctor -> Doctor(
                userId = userId,
                fullName = fullNameEditText.text.toString(),
                email = emailEditText.text.toString(),
                phone = phoneEditText.text.toString(),
                specialty = specialtyEditText.text.toString(),
                profileImageUrl = profileImageUrl,
                idCardImageUrl = idCardImageUrl,
                isVerified = user.isVerified
            )
            // Handle Administrator if needed

            is Administrator -> Administrator(
                adminId = userId,
                fullName = fullNameEditText.text.toString(),
                email = emailEditText.text.toString(),
                phone = phoneEditText.text.toString(),
                profileImageUrl = profileImageUrl,
                idCardImageUrl = idCardImageUrl,
                isVerified = user.isVerified
            )
            else -> null
        }

        if (updatedUser != null) {
            viewModel.updateUser(updatedUser, userId, userType)
        }
    }
}