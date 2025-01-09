package com.example.cloudhealthcareapp.ui.patient

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.cloudhealthcareapp.R
import com.example.cloudhealthcareapp.models.MedicalRecord
import com.example.cloudhealthcareapp.viewmodel.PatientViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import java.text.SimpleDateFormat
import java.util.*

class UploadMedicalRecordActivity : AppCompatActivity() {

    private val viewModel: PatientViewModel by viewModels()
    private lateinit var selectFileButton: Button
    private lateinit var uploadButton: Button
    private lateinit var diagnosisEditText: EditText
    private lateinit var prescriptionEditText: EditText
    private lateinit var notesEditText: EditText
    private lateinit var uploadProgressBar: ProgressBar
    private lateinit var uploadStatusTextView: TextView
    private var selectedFileUri: Uri? = null

    companion object {
        private const val MAX_FILE_SIZE_MB = 50
    }

    private val pickFileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedFileUri = result.data?.data
            selectedFileUri?.let {
                val fileSize = contentResolver.openFileDescriptor(it, "r")?.statSize ?: 0
                val fileSizeMB = fileSize.toDouble() / (1024 * 1024)
                if (fileSizeMB > MAX_FILE_SIZE_MB) {
                    Toast.makeText(this, "File size too large. Max size is 50MB.", Toast.LENGTH_SHORT).show()
                    selectedFileUri = null
                } else {
                    uploadStatusTextView.visibility = TextView.VISIBLE
                    uploadStatusTextView.text = "File selected: ${it.path}"
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_medical_record)

        selectFileButton = findViewById(R.id.selectFileButton)
        uploadButton = findViewById(R.id.uploadButton)
        diagnosisEditText = findViewById(R.id.diagnosisEditText)
        prescriptionEditText = findViewById(R.id.prescriptionEditText)
        notesEditText = findViewById(R.id.notesEditText)
        uploadProgressBar = findViewById(R.id.uploadProgressBar)
        uploadStatusTextView = findViewById(R.id.uploadStatusTextView)

        selectFileButton.setOnClickListener {
            openFileSelector()
        }

        uploadButton.setOnClickListener {
            uploadFileAndRecord()
        }
    }

    private fun openFileSelector() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        pickFileLauncher.launch(intent)
    }

    private fun uploadFileAndRecord() {
        val patientId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val fileUri = selectedFileUri ?: return

        uploadProgressBar.visibility = ProgressBar.VISIBLE
        uploadStatusTextView.visibility = TextView.VISIBLE
        uploadStatusTextView.text = "Uploading..."

        val storageRef = FirebaseStorage.getInstance().reference
        val fileRef = storageRef.child("medical_records/$patientId/${UUID.randomUUID()}")

        val uploadTask = fileRef.putFile(fileUri)

        uploadTask.addOnProgressListener { snapshot ->
            val progress = (100.0 * snapshot.bytesTransferred / snapshot.totalByteCount).toInt()
            uploadProgressBar.progress = progress
            uploadStatusTextView.text = "Uploading... $progress%"
        }.addOnSuccessListener {
            uploadProgressBar.visibility = ProgressBar.GONE
            uploadStatusTextView.text = "Upload successful!"
            fileRef.downloadUrl.addOnSuccessListener { downloadUri ->
                saveMedicalRecord(patientId, downloadUri.toString())
            }
        }.addOnFailureListener { e ->
            uploadProgressBar.visibility = ProgressBar.GONE
            uploadStatusTextView.text = "Upload failed: ${e.message}"
            Toast.makeText(this, "Upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
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