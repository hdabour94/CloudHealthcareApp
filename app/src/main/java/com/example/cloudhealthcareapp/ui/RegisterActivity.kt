package com.example.cloudhealthcareapp.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.cloudhealthcareapp.R
import com.example.cloudhealthcareapp.models.Administrator
import com.example.cloudhealthcareapp.models.Doctor
import com.example.cloudhealthcareapp.models.Patient
import com.example.cloudhealthcareapp.ui.admin.AdminHomeActivity
import com.example.cloudhealthcareapp.ui.doctor.DoctorHomeActivity
import com.example.cloudhealthcareapp.ui.patient.PatientHomeActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class RegisterActivity : AppCompatActivity() {

    private lateinit var fullNameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var userTypeSpinner: Spinner
    private lateinit var registerButton: Button
    private lateinit var idCardImageButton: ImageButton
    private lateinit var profileImageButton: ImageButton
    private var idCardImageUri: Uri? = null
    private var profileImageUri: Uri? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    // ActivityResultLauncher for selecting ID card image
    private val selectIdCardImage =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                idCardImageUri = result.data?.data
                idCardImageButton.setImageURI(idCardImageUri)
            }
        }

    // ActivityResultLauncher for selecting profile image
    private val selectProfileImage =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                profileImageUri = result.data?.data
                profileImageButton.setImageURI(profileImageUri)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        fullNameEditText = findViewById(R.id.fullNameEditText)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        userTypeSpinner = findViewById(R.id.userTypeSpinner)
        registerButton = findViewById(R.id.registerButton)
        idCardImageButton = findViewById(R.id.idCardImageButton)
        profileImageButton = findViewById(R.id.profileImageButton)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        // Setup Spinner
        ArrayAdapter.createFromResource(
            this,
            R.array.user_types_without_admin,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            userTypeSpinner.adapter = adapter
        }

        idCardImageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "image/*"
            }
            selectIdCardImage.launch(intent)
        }

        profileImageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "image/*"
            }
            selectProfileImage.launch(intent)
        }

        registerButton.setOnClickListener {
            val fullName = fullNameEditText.text.toString()
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val userType = userTypeSpinner.selectedItem.toString()

            if (fullName.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() &&
                idCardImageUri != null && profileImageUri != null
            ) {
                // Check if the user is being created by an admin
                val isAdmin = intent.getBooleanExtra("isAdmin", false)
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val userId = auth.currentUser?.uid ?: ""
                            // Get the FCM token and then upload images and register user
                            FirebaseMessaging.getInstance().token.addOnCompleteListener { tokenTask ->
                                if (tokenTask.isSuccessful) {
                                    val fcmToken = tokenTask.result
                                    uploadImagesAndRegisterUser(userId, fullName, email, userType, isAdmin, fcmToken)
                                } else {
                                    Log.e("RegisterActivity", "Failed to get FCM token: ${tokenTask.exception?.message}")
                                    Toast.makeText(baseContext, "Failed to get FCM token.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            Toast.makeText(
                                baseContext,
                                "Registration failed: ${task.exception?.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            } else {
                Toast.makeText(baseContext, "Please fill all fields and select images", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun uploadImagesAndRegisterUser(
        userId: String,
        fullName: String,
        email: String,
        userType: String,
        isAdmin: Boolean,
        fcmToken: String?
    ) {
        val idCardRef = storage.reference.child("idCards/$userId/${UUID.randomUUID()}")
        val profileImageRef = storage.reference.child("profileImages/$userId/${UUID.randomUUID()}")

        // Upload ID card image
        idCardRef.putFile(idCardImageUri!!)
            .addOnSuccessListener {
                idCardRef.downloadUrl.addOnSuccessListener { idCardDownloadUri ->
                    // Upload profile image
                    profileImageRef.putFile(profileImageUri!!)
                        .addOnSuccessListener {
                            profileImageRef.downloadUrl.addOnSuccessListener { profileImageDownloadUri ->
                                // Register user with image URLs
                                registerUserWithImages(
                                    userId,
                                    fullName,
                                    email,
                                    userType,
                                    idCardDownloadUri.toString(),
                                    profileImageDownloadUri.toString(),
                                    isAdmin,
                                    fcmToken
                                )
                            }
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                baseContext,
                                "Failed to upload profile image: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(baseContext, "Failed to upload ID card image: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun registerUserWithImages(
        userId: String,
        fullName: String,
        email: String,
        userType: String,
        idCardImageUrl: String,
        profileImageUrl: String,
        isAdmin: Boolean,
        fcmToken: String?
    ) {
        when (userType) {
            "Patient" -> {
                val patient = Patient(
                    userId = userId,
                    fullName = fullName,
                    email = email,
                    idCardImageUrl = idCardImageUrl,
                    profileImageUrl = profileImageUrl,
                    isVerified = isAdmin, // Set isVerified based on admin creation
                    fcmToken = fcmToken
                )
                firestore.collection("patients").document(userId).set(patient)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Patient registered successfully!", Toast.LENGTH_SHORT).show()
                        if (!isAdmin) {
                            // Don't navigate to PatientHomeActivity
                            // Instead, show a message and/or finish the activity
                            Toast.makeText(this, "Your account will be activated by the admin.", Toast.LENGTH_LONG).show()
                            finish() // Close RegisterActivity
                        } else {
                            finish()
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(baseContext, "Registration failed: ${e.message}", Toast.LENGTH_SHORT)
                            .show()
                    }
            }

            "Doctor" -> {
                val doctor = Doctor(
                    userId = userId,
                    fullName = fullName,
                    email = email,
                    idCardImageUrl = idCardImageUrl,
                    profileImageUrl = profileImageUrl,
                    isVerified = isAdmin, // Set isVerified based on admin creation
                    fcmToken = fcmToken
                )
                firestore.collection("doctors").document(userId).set(doctor)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Doctor registered successfully!", Toast.LENGTH_SHORT).show()
                        if (!isAdmin) {
                            // Don't navigate to DoctorHomeActivity
                            // Instead, show a message and/or finish the activity
                            Toast.makeText(this, "Your account will be activated by the admin.", Toast.LENGTH_LONG).show()
                            finish() // Close RegisterActivity
                        } else {
                            finish()
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(baseContext, "Registration failed: ${e.message}", Toast.LENGTH_SHORT)
                            .show()
                    }
            }

            "Administrator" -> {
                val administrator = Administrator(
                    adminId = userId,
                    fullName = fullName,
                    email = email,
                    idCardImageUrl = idCardImageUrl,
                    profileImageUrl = profileImageUrl,
                    fcmToken = fcmToken
                )
                firestore.collection("administrators").document(userId).set(administrator)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Administrator registered successfully!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(baseContext, "Registration failed: ${e.message}", Toast.LENGTH_SHORT)
                            .show()
                    }
            }
        }
    }
}