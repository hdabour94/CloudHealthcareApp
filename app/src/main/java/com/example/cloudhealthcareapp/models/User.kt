package com.example.cloudhealthcareapp.models

data class User(
    val userId: String,
    val fullName: String,
    val email: String,
    val userType: String,
    val isVerified: Boolean
)