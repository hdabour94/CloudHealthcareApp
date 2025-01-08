package com.example.cloudhealthcareapp.ui.patient

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.cloudhealthcareapp.R
import com.example.cloudhealthcareapp.models.MedicalRecord
import com.example.cloudhealthcareapp.viewmodel.PatientViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*

class UploadMedicalRecordActivity : AppCompatActivity() {

    private val viewModel: PatientViewModel by viewModels()
    private lateinit var selectFileButton: Button
    private lateinit var uploadButton: Button
    private lateinit var diagnosisEditText: EditText
    private lateinit var prescriptionEditText: EditText
    private lateinit var notesEditText: EditText
    private var selectedFileUri: Uri? = null

    companion object {
        private const val PICK_FILE_REQUEST = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_medical_record)

        selectFileButton = findViewById(R.id.selectFileButton)
        uploadButton = findViewById(R.id.uploadButton)
        diagnosisEditText = findViewById(R.id.diagnosisEditText)
        prescriptionEditText = findViewById(R.id.prescriptionEditText)
        notesEditText = findViewById(R.id.notesEditText)

        selectFileButton.setOnClickListener {
            openFileSelector()
        }

        uploadButton.setOnClickListener {
            uploadFileAndRecord()
        }
    }

    private fun openFileSelector() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*" // Allow all file types
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        startActivityForResult(intent, PICK_FILE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_FILE_REQUEST && resultCode == Activity.RESULT_OK) {
            selectedFileUri = data?.data
            Toast.makeText(this, "File selected: ${selectedFileUri?.path}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadFileAndRecord() {
        val patientId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val fileUri = selectedFileUri ?: return
        val storageRef = FirebaseStorage.getInstance().reference
        val fileRef = storageRef.child("medical_records/$patientId/${UUID.randomUUID()}")

        val uploadTask = fileRef.putFile(fileUri)
        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            fileRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result.toString()
                saveMedicalRecord(patientId, downloadUri)
            } else {
                Toast.makeText(this, "Upload failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveMedicalRecord(patientId: String, fileUrl: String) {
        val record = MedicalRecord(
            recordId = UUID.randomUUID().toString(),
            patientId = patientId,
            doctorId = null, // Doctor can be assigned later
            date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
            diagnosis = diagnosisEditText.text.toString(),
            prescription = prescriptionEditText.text.toString(),
            notes = notesEditText.text.toString(),
            fileUrl = fileUrl
        )

        viewModel.uploadMedicalRecord(record)
    }
}