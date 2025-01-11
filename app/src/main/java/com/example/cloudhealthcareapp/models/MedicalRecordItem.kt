package com.example.cloudhealthcareapp.models

data class MedicalRecordItem(
    val recordId: String,
    val date: String,
    val diagnosis: String,
    val prescription: String? = null, // Allow null for cases without prescription
    val notes: String? = null // Add notes if needed
)