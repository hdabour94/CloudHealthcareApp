package com.example.cloudhealthcareapp.ui.doctor

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.MediaController
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

//import com.example.cloudhealthcareapp.BuildConfig // Import BuildConfig
import com.example.cloudhealthcareapp.R
import com.example.cloudhealthcareapp.models.MedicalRecord
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import firebase.com.protolitewrapper.BuildConfig
import java.io.File
import java.io.IOException

class MedicalRecordsAdapter(
    private val context: Context,
    private var medicalRecords: List<MedicalRecord>
) : RecyclerView.Adapter<MedicalRecordsAdapter.MedicalRecordViewHolder>() {

    private val storage = Firebase.storage

    class MedicalRecordViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val fileTypeImageView: ImageView = view.findViewById(R.id.fileTypeImageView)
        val fileNameTextView: TextView = view.findViewById(R.id.fileNameTextView)
        val fileImageView: ImageView = view.findViewById(R.id.fileImageView)
        val fileVideoView: VideoView = view.findViewById(R.id.fileVideoView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicalRecordViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_medical_record, parent, false)
        return MedicalRecordViewHolder(view)
    }

    override fun onBindViewHolder(holder: MedicalRecordViewHolder, position: Int) {
        val medicalRecord = medicalRecords[position]

        // Determine the file type and set the appropriate icon
        when {
            medicalRecord.fileUrl?.endsWith(".jpg", true) == true ||
                    medicalRecord.fileUrl?.endsWith(".jpeg", true) == true ||
                    medicalRecord.fileUrl?.endsWith(".png", true) == true -> {
                holder.fileTypeImageView.setImageResource(R.drawable.ic_image)
                holder.fileImageView.visibility = View.VISIBLE
                holder.fileVideoView.visibility = View.GONE
                Glide.with(context)
                    .load(medicalRecord.fileUrl)
                    .into(holder.fileImageView)
            }
            medicalRecord.fileUrl?.endsWith(".mp4", true) == true -> {
                holder.fileTypeImageView.setImageResource(R.drawable.ic_video)
                holder.fileImageView.visibility = View.GONE
                holder.fileVideoView.visibility = View.VISIBLE
                holder.fileVideoView.setVideoURI(Uri.parse(medicalRecord.fileUrl))
                val mediaController = MediaController(context)
                mediaController.setAnchorView(holder.fileVideoView)
                holder.fileVideoView.setMediaController(mediaController)
            }
            else -> {
                holder.fileTypeImageView.setImageResource(R.drawable.ic_file)
                holder.fileImageView.visibility = View.GONE
                holder.fileVideoView.visibility = View.GONE
            }
        }

        holder.fileNameTextView.text = medicalRecord.fileUrl?.let {
            val lastSlashIndex = it.lastIndexOf('/')
            val lastDotIndex = it.lastIndexOf('.')

            // Check if both '/' and '.' are found and if '.' comes after '/'
            if (lastSlashIndex != -1 && lastDotIndex != -1 && lastDotIndex > lastSlashIndex) {
                it.substring(lastSlashIndex + 1, lastDotIndex)
            } else {
                // If '/' or '.' is not found or '.' is before '/', return the whole URL or a default name
                it.substringAfterLast('/', "Unknown")
            }
        } ?: "Unknown"

        // Set the click listener for the entire item view
        holder.itemView.setOnClickListener {
            medicalRecord.fileUrl?.let { fileUrl ->
                downloadAndOpenFile(context, fileUrl)
            }
        }
    }

    override fun getItemCount() = medicalRecords.size

    fun updateMedicalRecords(newRecords: List<MedicalRecord>) {
        medicalRecords = newRecords
        notifyDataSetChanged()
    }

    private fun downloadAndOpenFile(context: Context, fileUrl: String) {
        val storageRef = storage.getReferenceFromUrl(fileUrl)
        val localFile = createTempFile(fileUrl)

        storageRef.getFile(localFile).addOnSuccessListener {
            // Local temp file has been created
            val fileUri: Uri = FileProvider.getUriForFile(
                context,
                "${BuildConfig.APPLICATION_ID}.fileprovider",
                localFile
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(fileUri, getMimeType(fileUrl))
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
            }

            try {
                context.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(context, "No app found to open this file type.", Toast.LENGTH_SHORT).show()
                Log.e("MedicalRecordsAdapter", "No activity found to handle file: $fileUrl", e)
            }
        }.addOnFailureListener { exception ->
            // Handle any errors
            Log.e("MedicalRecordsAdapter", "Error downloading file: $fileUrl", exception)
            Toast.makeText(context, "Failed to download file.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createTempFile(fileUrl: String): File {
        val extension = fileUrl.substringAfterLast('.', "")
        return File.createTempFile("tempFile", ".$extension", context.cacheDir)
    }

    private fun getMimeType(url: String): String {
        return when {
            url.endsWith(".jpg", true) || url.endsWith(".jpeg", true) -> "image/jpeg"
            url.endsWith(".png", true) -> "image/png"
            url.endsWith(".mp4", true) -> "video/mp4"
            // Add more mime types as needed
            else -> "*/*" // Default for unknown types
        }
    }
}