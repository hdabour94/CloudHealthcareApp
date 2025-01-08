package com.example.cloudhealthcareapp.ui

import android.content.Intent
import android.os.Bundle
import android.widget.*
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

class RegisterActivity : AppCompatActivity() {

    private lateinit var fullNameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var userTypeSpinner: Spinner
    private lateinit var registerButton: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        fullNameEditText = findViewById(R.id.fullNameEditText)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        userTypeSpinner = findViewById(R.id.userTypeSpinner)
        registerButton = findViewById(R.id.registerButton)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Setup Spinner
        ArrayAdapter.createFromResource(
            this,
            R.array.user_types,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            userTypeSpinner.adapter = adapter
        }

        registerButton.setOnClickListener {
            val fullName = fullNameEditText.text.toString()
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val userType = userTypeSpinner.selectedItem.toString()

            if (fullName.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val userId = auth.currentUser?.uid ?: ""
                            when (userType) {
                                "Patient" -> {
                                    val patient = Patient(userId = userId, fullName = fullName, email = email)
                                    firestore.collection("patients").document(userId).set(patient)
                                        .addOnSuccessListener {
                                            Toast.makeText(this, "Patient registered successfully!", Toast.LENGTH_SHORT).show()
                                            startActivity(Intent(this, PatientHomeActivity::class.java))
                                            finish()
                                        }
                                        .addOnFailureListener { e ->
                                            Toast.makeText(baseContext, "Registration failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                }
                                "Doctor" -> {
                                    val doctor = Doctor(userId = userId, fullName = fullName, email = email)
                                    firestore.collection("doctors").document(userId).set(doctor)
                                        .addOnSuccessListener {
                                            Toast.makeText(this, "Doctor registered successfully!", Toast.LENGTH_SHORT).show()
                                            startActivity(Intent(this, DoctorHomeActivity::class.java))
                                            finish()
                                        }
                                        .addOnFailureListener { e ->
                                            Toast.makeText(baseContext, "Registration failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                }
                                "Administrator" -> {
                                    val administrator = Administrator(adminId = userId, fullName = fullName, email = email)
                                    firestore.collection("administrators").document(userId).set(administrator)
                                        .addOnSuccessListener {
                                            Toast.makeText(this, "Administrator registered successfully!", Toast.LENGTH_SHORT).show()
                                            startActivity(Intent(this, AdminHomeActivity::class.java))
                                            finish()
                                        }
                                        .addOnFailureListener { e ->
                                            Toast.makeText(baseContext, "Registration failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                }
                            }
                        } else {
                            Toast.makeText(baseContext, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(baseContext, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }
}