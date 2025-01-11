package com.example.cloudhealthcareapp.ui.patient

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cloudhealthcareapp.R
import com.example.cloudhealthcareapp.models.Doctor
import com.example.cloudhealthcareapp.ui.LoginActivity
import com.example.cloudhealthcareapp.ui.doctor.MedicalRecordsActivity
import com.example.cloudhealthcareapp.viewmodel.PatientViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class PatientHomeActivity : AppCompatActivity() {

    private val viewModel: PatientViewModel by viewModels()
    private lateinit var doctorsRecyclerView: RecyclerView
    private lateinit var doctorsAdapter: DoctorsAdapter
    private lateinit var uploadMedicalRecordButton: Button
    private lateinit var viewAppointmentsButton: Button
    private lateinit var welcomeTextView: TextView
    private lateinit var signOutButton: Button
    private lateinit var viewMedicalRecordsButton: Button // Correct

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_home)

        doctorsRecyclerView = findViewById(R.id.doctorsRecyclerView)
        uploadMedicalRecordButton = findViewById(R.id.uploadMedicalRecordButton)
        viewAppointmentsButton = findViewById(R.id.viewAppointmentsButton)
        welcomeTextView = findViewById(R.id.welcomeTextView)
        signOutButton = findViewById(R.id.signOutButton)
        viewMedicalRecordsButton = findViewById(R.id.viewMedicalRecordsButton) // Correct

        doctorsAdapter = DoctorsAdapter(emptyList()) { doctor ->
            val intent = Intent(this, BookAppointmentActivity::class.java)
            intent.putExtra("doctorId", doctor.userId)
            intent.putExtra("doctorName", doctor.fullName)
            startActivity(intent)
        }

        doctorsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@PatientHomeActivity)
            adapter = doctorsAdapter
        }

        viewModel.doctors.observe(this) { doctors ->
            doctorsAdapter.updateDoctors(doctors)
        }

        viewModel.getDoctors()

        uploadMedicalRecordButton.setOnClickListener {
            startActivity(Intent(this, UploadMedicalRecordActivity::class.java))
        }

        viewAppointmentsButton.setOnClickListener {
            startActivity(Intent(this, PatientAppointmentsActivity::class.java))
        }

        signOutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        viewMedicalRecordsButton.setOnClickListener {
            val patientId = FirebaseAuth.getInstance().currentUser?.uid
            if (patientId != null) {
                val intent = Intent(this, MedicalRecordsActivity::class.java)
                intent.putExtra("patientId", patientId)
                startActivity(intent)
            } else {
                // Handle the case where patientId is null
                Toast.makeText(this, "Error: User not signed in.", Toast.LENGTH_SHORT).show()
            }
        }

        // Get patient's name and update welcome message
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId != null) {
            Firebase.firestore.collection("patients").document(currentUserId).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val patientName = document.getString("fullName") ?: "Patient"
                        welcomeTextView.text = "Welcome, $patientName"
                    } else {
                        welcomeTextView.text = "Welcome, Patient"
                    }
                }
                .addOnFailureListener {
                    welcomeTextView.text = "Welcome, Patient"
                }
        }
    }
}