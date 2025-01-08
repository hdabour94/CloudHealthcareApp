package com.example.cloudhealthcareapp.ui.doctor

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
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
    private lateinit var patientIdEditText: EditText
    private lateinit var diagnosisEditText: EditText
    private lateinit var prescriptionEditText: EditText
    private lateinit var notesEditText: EditText
    private lateinit var saveButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_diagnosis)

        patientIdEditText = findViewById(R.id.patientIdEditText)
        diagnosisEditText = findViewById(R.id.diagnosisEditText)
        prescriptionEditText = findViewById(R.id.prescriptionEditText)
        notesEditText = findViewById(R.id.notesEditText)
        saveButton = findViewById(R.id.saveButton)

        saveButton.setOnClickListener {
            val patientId = patientIdEditText.text.toString()
            val diagnosis = diagnosisEditText.text.toString()
            val prescription = prescriptionEditText.text.toString()
            val notes = notesEditText.text.toString()
            val doctorId = FirebaseAuth.getInstance().currentUser?.uid

            if (patientId.isNotEmpty() && diagnosis.isNotEmpty() && doctorId != null) {
                val record = MedicalRecord(
                    recordId = UUID.randomUUID().toString(),
                    patientId = patientId,
                    doctorId = doctorId,
                    date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                    diagnosis = diagnosis,
                    prescription = prescription,
                    notes = notes,
                    fileUrl = null // File URL can be added later if needed
                )

                viewModel.addDiagnosis(record)
            } else {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.addDiagnosisResult.observe(this) { success ->
            if (success) {
                Toast.makeText(this, "Diagnosis added successfully!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Failed to add diagnosis.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}