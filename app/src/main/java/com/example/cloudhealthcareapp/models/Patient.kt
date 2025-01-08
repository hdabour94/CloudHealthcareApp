// Patient.kt
package com.example.cloudhealthcareapp.models

import com.google.firebase.firestore.PropertyName

data class Patient(
    @PropertyName("userId") var userId: String? = null,
    @PropertyName("fullName") var fullName: String? = null,
    @PropertyName("email") var email: String? = null,
    @PropertyName("phone") var phone: String? = null,
    @PropertyName("dateOfBirth") var dateOfBirth: String? = null,
    @PropertyName("gender") var gender: String? = null,
    @PropertyName("address") var address: String? = null,
    @PropertyName("medicalHistory") var medicalHistory: String? = null,
    @PropertyName("idCardImageUrl") var idCardImageUrl: String? = null, // Add ID card image URL
    @PropertyName("profileImageUrl") var profileImageUrl: String? = null // Add profile image URL
)