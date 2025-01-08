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

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Check user type and start the appropriate activity
                            checkUserRoleAndRedirect()
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

    private fun checkUserRoleAndRedirect() {
        val user = auth.currentUser
        if (user != null) {
            val userId = user.uid
            // Check Patient collection
            firestore.collection("patients").document(userId).get()
                .addOnSuccessListener { patientDocument ->
                    if (patientDocument.exists()) {
                        startActivity(Intent(this, PatientHomeActivity::class.java))
                        finish()
                        return@addOnSuccessListener
                    }
                    // Check Doctor collection
                    firestore.collection("doctors").document(userId).get()
                        .addOnSuccessListener { doctorDocument ->
                            if (doctorDocument.exists()) {
                                startActivity(Intent(this, DoctorHomeActivity::class.java))
                                finish()
                                return@addOnSuccessListener
                            }
                            // Check Admin collection
                            firestore.collection("administrators").document(userId).get()
                                .addOnSuccessListener { adminDocument ->
                                    if (adminDocument.exists()) {
                                        startActivity(Intent(this, AdminHomeActivity::class.java))
                                        finish()
                                    } else {
                                        // User not found in any collection
                                        Toast.makeText(this, "User role not found.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        }
                }
        }
    }
}