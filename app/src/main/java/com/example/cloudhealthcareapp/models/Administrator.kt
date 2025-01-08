package com.example.cloudhealthcareapp.models

import com.google.firebase.firestore.PropertyName

data class Administrator(
    @PropertyName("adminId") var adminId: String? = null,
    @PropertyName("fullName") var fullName: String? = null,
    @PropertyName("email") var email: String? = null,
    @PropertyName("phone") var phone: String? = null
)