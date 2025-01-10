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
import com.example.cloudhealthcareapp.models.MedicalRecord
import com.example.cloudhealthcareapp.models.Patient
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
    private lateinit var userType: String
    private lateinit var userId: String
    private lateinit var medicalRecordsRecyclerView: RecyclerView
    private lateinit var medicalRecordsAdapter: MedicalRecordsAdapter
    private lateinit var diagnosisRecyclerView: RecyclerView
    private lateinit var diagnosisAdapter: DiagnosisAdapter
    private lateinit var prescriptionsRecyclerView: RecyclerView
    private lateinit var prescriptionsAdapter: PrescriptionsAdapter

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
        medicalRecordsRecyclerView = findViewById(R.id.medicalRecordsRecyclerView)
        diagnosisRecyclerView = findViewById(R.id.diagnosisRecyclerView)
        prescriptionsRecyclerView = findViewById(R.id.prescriptionsRecyclerView)

        userId = intent.getStringExtra("userId") ?: ""
        userType = intent.getStringExtra("userType") ?: ""

        // Setup RecyclerView for medical records
        medicalRecordsAdapter = MedicalRecordsAdapter(this, emptyList())
        medicalRecordsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@PatientDetailsActivity)
            adapter = medicalRecordsAdapter
        }

        // Setup RecyclerView for diagnosis records
        diagnosisAdapter = DiagnosisAdapter(emptyList())
        diagnosisRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@PatientDetailsActivity)
            adapter = diagnosisAdapter
        }

        // Setup RecyclerView for prescriptions
        prescriptionsAdapter = PrescriptionsAdapter(emptyList())
        prescriptionsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@PatientDetailsActivity)
            adapter = prescriptionsAdapter
        }

        // Observe the medical records LiveData
        doctorViewModel.medicalRecords.observe(this) { medicalRecords ->
            medicalRecordsAdapter.updateMedicalRecords(medicalRecords)
        }

        // Observe the diagnosis LiveData
        doctorViewModel.diagnosis.observe(this) { diagnosis ->
            diagnosisAdapter.updateDiagnosis(diagnosis)
        }

        // Observe the prescriptions LiveData
        doctorViewModel.prescriptions.observe(this) { prescriptions ->
            prescriptionsAdapter.updatePrescriptions(prescriptions)
        }

        // Fetch data based on user type
        if (userType == "Patient") {
            doctorViewModel.fetchMedicalRecords(userId)
            doctorViewModel.fetchDiagnosis(userId)
            doctorViewModel.fetchPrescriptions(userId)
        }

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
    }
}