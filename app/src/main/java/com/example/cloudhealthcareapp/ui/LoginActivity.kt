package com.example.cloudhealthcareapp.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.cloudhealthcareapp.R
import com.example.cloudhealthcareapp.ui.admin.AdminHomeActivity
import com.example.cloudhealthcareapp.ui.doctor.DoctorHomeActivity
import com.example.cloudhealthcareapp.ui.patient.PatientHomeActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var registerButton: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
        registerButton = findViewById(R.id.registerButton)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Check if user is already logged in
        if (auth.currentUser != null) {
            checkUserRoleAndRedirect()
        }

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Check user type and verification status
                            checkUserRoleAndVerification()
                        } else {
                            Toast.makeText(baseContext, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(baseContext, "Please enter email and password.", Toast.LENGTH_SHORT).show()
            }
        }

        registerButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun checkUserRoleAndVerification() {
        val user = auth.currentUser
        if (user != null) {
            val userId = user.uid
            // Check Patient collection
            firestore.collection("patients").document(userId).get()
                .addOnSuccessListener { patientDocument ->
                    if (patientDocument.exists()) {
                        if (patientDocument.getBoolean("isVerified") == true) {
                            startActivity(Intent(this, PatientHomeActivity::class.java))
                            finish()
                        } else {
                            auth.signOut() // Sign out the user
                            Toast.makeText(this, "Account not verified yet.", Toast.LENGTH_SHORT).show()
                        }
                        return@addOnSuccessListener
                    }
                    // Check Doctor collection
                    firestore.collection("doctors").document(userId).get()
                        .addOnSuccessListener { doctorDocument ->
                            if (doctorDocument.exists()) {
                                if (doctorDocument.getBoolean("isVerified") == true) {
                                    startActivity(Intent(this, DoctorHomeActivity::class.java))
                                    finish()
                                } else {
                                    auth.signOut() // Sign out the user
                                    Toast.makeText(this, "Account not verified yet.", Toast.LENGTH_SHORT).show()
                                }
                                return@addOnSuccessListener
                            }
                            // Check Admin collection
                            firestore.collection("administrators").document(userId).get()
                                .addOnSuccessListener { adminDocument ->
                                    if (adminDocument.exists()) {
                                        // Admins are considered verified by default
                                        startActivity(Intent(this, AdminHomeActivity::class.java))
                                        finish()
                                    } else {
                                        // User not found in any collection
                                        Toast.makeText(this, "User role not found.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error checking user role: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun checkUserRoleAndRedirect() {
        val user = auth.currentUser
        if (user != null) {
            val userId = user.uid
            firestore.collection("patients").document(userId).get()
                .addOnSuccessListener { patientDocument ->
                    if (patientDocument.exists()) {
                        startActivity(Intent(this, PatientHomeActivity::class.java))
                        finish()
                        return@addOnSuccessListener
                    }
                    firestore.collection("doctors").document(userId).get()
                        .addOnSuccessListener { doctorDocument ->
                            if (doctorDocument.exists()) {
                                startActivity(Intent(this, DoctorHomeActivity::class.java))
                                finish()
                                return@addOnSuccessListener
                            }
                            firestore.collection("administrators").document(userId).get()
                                .addOnSuccessListener { adminDocument ->
                                    if (adminDocument.exists()) {
                                        startActivity(Intent(this, AdminHomeActivity::class.java))
                                        finish()
                                    } else {
                                        Toast.makeText(this, "User role not found.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error checking user role: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}