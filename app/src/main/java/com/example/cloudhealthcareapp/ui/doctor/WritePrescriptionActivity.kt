package com.example.cloudhealthcareapp.ui.doctor

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.cloudhealthcareapp.R
import com.example.cloudhealthcareapp.viewmodel.DoctorViewModel

class WritePrescriptionActivity : AppCompatActivity() {

    private val viewModel: DoctorViewModel by viewModels()
    private lateinit var patientId: String
    private lateinit var prescriptionEditText: EditText
    private lateinit var saveButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_write_prescription)

        patientId = intent.getStringExtra("patientId") ?: ""
        if (patientId.isEmpty()) {
            Toast.makeText(this, "Patient ID is missing.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        prescriptionEditText = findViewById(R.id.prescriptionEditText)
        saveButton = findViewById(R.id.savePrescriptionButton)

        saveButton.setOnClickListener {
            val prescriptionText = prescriptionEditText.text.toString()
            if (prescriptionText.isNotEmpty()) {
                viewModel.savePrescription(patientId, prescriptionText)
            } else {
                Toast.makeText(this, "Please enter prescription text.", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.prescriptionSaveResult.observe(this) { success ->
            if (success) {
                Toast.makeText(this, "Prescription saved successfully!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Failed to save prescription.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}