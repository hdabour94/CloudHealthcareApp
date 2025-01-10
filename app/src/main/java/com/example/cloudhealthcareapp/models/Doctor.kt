package com.example.cloudhealthcareapp.models

import com.google.firebase.firestore.PropertyName

data class Doctor(
    @PropertyName("userId") var userId: String? = null,
    @PropertyName("fullName") var fullName: String? = null,
    @PropertyName("email") var email: String? = null,
    @PropertyName("phone") var phone: String? = null,
    @PropertyName("specialty") var specialty: String? = null,
    @PropertyName("qualifications") var qualifications: String? = null,
    @PropertyName("licenseNumber") var licenseNumber: String? = null,
    @PropertyName("hospital") var hospital: String? = null,
    @PropertyName("idCardImageUrl") var idCardImageUrl: String? = null,
    @PropertyName("profileImageUrl") var profileImageUrl: String? = null,
    @PropertyName("isVerified") var isVerified: Boolean? = false,
    @PropertyName("startTime") var startTime: String? = null, // e.g., "09:00"
    @PropertyName("endTime") var endTime: String? = null,     // e.g., "17:00"
    @PropertyName("vacationDays") var vacationDays: List<String>? = null, // e.g., ["2024-03-21", "2024-03-22"]
    @PropertyName("weeklyDaysOff") var weeklyDaysOff: List<Int>? = null, // e.g., [Calendar.SATURDAY, Calendar.SUNDAY]
    @PropertyName("fcmToken") var fcmToken: String? = null
)