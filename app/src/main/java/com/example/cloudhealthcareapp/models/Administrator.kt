package com.example.cloudhealthcareapp.models

import com.google.firebase.firestore.PropertyName

data class Administrator(
    @PropertyName("adminId") var adminId: String? = null,
    @PropertyName("fullName") var fullName: String? = null,
    @PropertyName("email") var email: String? = null,
    @PropertyName("phone") var phone: String? = null,
    @PropertyName("idCardImageUrl") var idCardImageUrl: String? = null, // Add ID card image URL
    @PropertyName("profileImageUrl") var profileImageUrl: String? = null, // Add profile image URL
    @PropertyName("isVerified") var isVerified: Boolean? = true, // Add isVerified field for Administrators
    @PropertyName("fcmToken") var fcmToken: String? = null
)