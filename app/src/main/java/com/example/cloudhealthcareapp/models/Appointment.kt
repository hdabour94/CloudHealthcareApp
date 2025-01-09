package com.example.cloudhealthcareapp.models

import com.google.firebase.firestore.PropertyName

data class Appointment(
    @PropertyName("appointmentId") var appointmentId: String? = null,
    @PropertyName("patientId") var patientId: String? = null,
    @PropertyName("doctorId") var doctorId: String? = null,
    @PropertyName("patientName") var patientName: String? = null, // Add patientName
    @PropertyName("doctorName") var doctorName: String? = null, // Add doctorName
    @PropertyName("appointmentDateTime") var appointmentDateTime: String? = null,
    @PropertyName("reason") var reason: String? = null,
    @PropertyName("status") var status: String? = null
)