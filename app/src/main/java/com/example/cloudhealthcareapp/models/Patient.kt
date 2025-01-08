package com.example.cloudhealthcareapp.models

import com.google.firebase.firestore.PropertyName

data class Patient(
    @PropertyName("userId") var userId: String? = null,
    @PropertyName("fullName") var fullName: String? = null,
    @PropertyName("email") var email: String? = null,
    @PropertyName("phone") var phone: String? = null,
    @PropertyName("dateOfBirth") var dateOfBirth: String? = null,
    @PropertyName("medicalHistory") var medicalHistory: String? = null
    // ... المزيد من الحقول
)