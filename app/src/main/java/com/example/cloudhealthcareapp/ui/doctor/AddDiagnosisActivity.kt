package com.example.cloudhealthcareapp.ui.doctor

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.cloudhealthcareapp.R
import com.example.cloudhealthcareapp.models.MedicalRecord
import com.example.cloudhealthcareapp.viewmodel.DoctorViewModel
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

class AddDiagnosisActivity : AppCompatActivity() {

    private val viewModel: DoctorViewModel by viewModels()
    private lateinit var patientNameTextView: TextView
    private lateinit var diagnosisEditText: EditText
    private lateinit var prescriptionEditText: EditText
    private lateinit var notesEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var progressBar: ProgressBar

    private lateinit var patientId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_diagnosis)

        patientNameTextView = findViewById(R.id.patientNameTextView)
        diagnosisEditText = findViewById(R.id.diagnosisEditText)
        prescriptionEditText = findViewById(R.id.prescriptionEditText)
        notesEditText = findViewById(R.id.notesEditText)
        saveButton = findViewById(R.id.saveButton)
        progressBar = findViewById(R.id.progressBar)

        // Get patientId from Intent
        patientId = intent.getStringExtra("patientId") ?: ""
        val patientName = intent.getStringExtra("patientName") ?: ""

        if (patientId.isEmpty() || patientName.isEmpty()) {
            Toast.makeText(this, "Patient ID or Name is missing.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        patientNameTextView.text = "Patient: $patientName"

        saveButton.setOnClickListener {
            val diagnosis = diagnosisEditText.text.toString()
            val prescription = prescriptionEditText.text.toString()
            val notes = notesEditText.text.toString()
            val doctorId = FirebaseAuth.getInstance().currentUser?.uid

            if (diagnosis.isNotEmpty() && doctorId != null) {
                progressBar.visibility = View.VISIBLE
                saveButton.isEnabled = false

                val record = MedicalRecord(
                    recordId = UUID.randomUUID().toString(),
                    patientId = patientId,
                    doctorId = doctorId,
                    date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                    diagnosis = diagnosis,
                    prescription = prescription,
                    notes = notes,
                    fileUrl = null // Add file URL logic if needed
                )

                viewModel.addDiagnosis(record)
            } else {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.addDiagnosisResult.observe(this) { success ->
            progressBar.visibility = View.GONE
            saveButton.isEnabled = true

            if (success) {
                Toast.makeText(this, "Diagnosis added successfully!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Failed to add diagnosis.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}