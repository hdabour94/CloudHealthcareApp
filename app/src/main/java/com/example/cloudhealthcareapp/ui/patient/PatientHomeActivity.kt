package com.example.cloudhealthcareapp.ui.patient

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cloudhealthcareapp.R
import com.example.cloudhealthcareapp.models.Doctor
import com.example.cloudhealthcareapp.ui.LoginActivity
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
    private var isPermissionGranted = false

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_home)

        doctorsRecyclerView = findViewById(R.id.doctorsRecyclerView)
        uploadMedicalRecordButton = findViewById(R.id.uploadMedicalRecordButton)
        viewAppointmentsButton = findViewById(R.id.viewAppointmentsButton)
        welcomeTextView = findViewById(R.id.welcomeTextView)
        signOutButton = findViewById(R.id.signOutButton)

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

        // Check and request notification permission
        checkNotificationPermission()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun checkNotificationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            == PackageManager.PERMISSION_GRANTED) {
            // Permission is already granted
            isPermissionGranted = true
        } else {
            // Permission is not granted, request it
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private val requestNotificationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted
            isPermissionGranted = true
            Toast.makeText(this, "Notifications permission granted", Toast.LENGTH_SHORT).show()
        } else {
            // Permission denied
            isPermissionGranted = false
            Toast.makeText(this, "Notifications permission denied", Toast.LENGTH_SHORT).show()
            showPermissionRationaleDialog()
        }
    }

    private fun showPermissionRationaleDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permission Needed")
            .setMessage("This app requires notification permission to keep you informed. Please grant the permission in settings.")
            .setPositiveButton("Go to Settings") { _, _ ->
                openAppSettings()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }
}