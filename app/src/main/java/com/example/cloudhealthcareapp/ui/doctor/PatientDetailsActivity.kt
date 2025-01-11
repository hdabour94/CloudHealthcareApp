package com.example.cloudhealthcareapp.ui.doctor

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cloudhealthcareapp.R
import com.example.cloudhealthcareapp.models.Doctor
import com.example.cloudhealthcareapp.models.Patient
import com.example.cloudhealthcareapp.ui.patient.PatientDiagnosisActivity
import com.example.cloudhealthcareapp.ui.patient.PatientPrescriptionsActivity
import com.example.cloudhealthcareapp.viewmodel.AdminViewModel
import com.example.cloudhealthcareapp.viewmodel.DoctorViewModel

class PatientDetailsActivity : AppCompatActivity() {

    private val viewModel: AdminViewModel by viewModels()
    private val doctorViewModel: DoctorViewModel by viewModels()

    private lateinit var fullNameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var addressEditText: EditText
    private lateinit var specialtyEditText: EditText
    private lateinit var profileImageView: ImageView
    private lateinit var idCardImageView: ImageView
    private lateinit var addDiagnosisButton: Button
    private lateinit var writePrescriptionButton: Button
    private lateinit var viewMedicalRecordsButton: Button
    private lateinit var viewDiagnosisButton: Button
    private lateinit var viewPrescriptionsButton: Button
    private lateinit var userType: String
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_details)

        fullNameEditText = findViewById(R.id.fullNameEditText)
        emailEditText = findViewById(R.id.emailEditText)
        phoneEditText = findViewById(R.id.phoneEditText)
        addressEditText = findViewById(R.id.addressEditText)
        specialtyEditText = findViewById(R.id.specialtyEditText)
        profileImageView = findViewById(R.id.profileImageButton)
        idCardImageView = findViewById(R.id.idCardImageButton)
        addDiagnosisButton = findViewById(R.id.addDiagnosisButton)
        writePrescriptionButton = findViewById(R.id.writePrescriptionButton)
        viewMedicalRecordsButton = findViewById(R.id.viewMedicalRecordsButton)
        viewDiagnosisButton = findViewById(R.id.viewDiagnosisButton)
        viewPrescriptionsButton = findViewById(R.id.viewPrescriptionsButton)

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

        addDiagnosisButton.setOnClickListener {
            val intent = Intent(this, AddDiagnosisActivity::class.java)
            intent.putExtra("patientId", userId)
            intent.putExtra("patientName", fullNameEditText.text.toString())
            startActivity(intent)
        }

        writePrescriptionButton.setOnClickListener {
            val intent = Intent(this, WritePrescriptionActivity::class.java)
            intent.putExtra("patientId", userId)
            startActivity(intent)
        }

        viewMedicalRecordsButton.setOnClickListener {
            val intent = Intent(this, MedicalRecordsActivity::class.java)
            intent.putExtra("patientId", userId)
            startActivity(intent)
        }

        viewDiagnosisButton.setOnClickListener {
            val intent = Intent(this, PatientDiagnosisActivity::class.java)
            intent.putExtra("patientId", userId)
            startActivity(intent)
        }

        viewPrescriptionsButton.setOnClickListener {
            val intent = Intent(this, PatientPrescriptionsActivity::class.java)
            intent.putExtra("patientId", userId)
            startActivity(intent)
        }

    }
}