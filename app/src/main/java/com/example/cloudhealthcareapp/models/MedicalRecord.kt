package com.example.cloudhealthcareapp.models

import com.google.firebase.firestore.PropertyName

data class MedicalRecord(
    @PropertyName("recordId") var recordId: String? = null,
    @PropertyName("patientId") var patientId: String? = null,
    @PropertyName("doctorId") var doctorId: String? = null,
    @PropertyName("date") var date: String? = null,
    @PropertyName("diagnosis") var diagnosis: String? = null,
    @PropertyName("prescription") var prescription: String? = null,
    @PropertyName("notes") var notes: String? = null,
    @PropertyName("fileUrl") var fileUrl: String? = null // URL to uploaded file (e.g., image, PDF)
)