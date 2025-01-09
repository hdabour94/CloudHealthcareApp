package com.example.cloudhealthcareapp.repository

import android.util.Log
import com.example.cloudhealthcareapp.models.Appointment
import com.example.cloudhealthcareapp.models.Doctor
import com.example.cloudhealthcareapp.models.MedicalRecord
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

import com.example.cloudhealthcareapp.models.*

import com.google.firebase.firestore.QuerySnapshot

import kotlinx.coroutines.tasks.await
import com.example.cloudhealthcareapp.models.*
import kotlinx.coroutines.tasks.await

import com.example.cloudhealthcareapp.models.*
import kotlinx.coroutines.tasks.await

class FirebaseRepository {

    private val db = FirebaseFirestore.getInstance()


    suspend fun getDoctors(): List<Doctor> {
        return try {
            val snapshot = db.collection("doctors").get().await()
            snapshot.toObjects(Doctor::class.java)
        } catch (e: Exception) {
            throw e // Re-throw the exception to be handled in the ViewModel
        }
    }

    suspend fun bookAppointment(appointment: Appointment): Boolean {
        return try {
            // Check for conflicts
            val conflictingAppointments = db.collection("appointments")
                .whereEqualTo("doctorId", appointment.doctorId)
                .whereEqualTo("appointmentDateTime", appointment.appointmentDateTime)
                .get()
                .await()

            if (conflictingAppointments.isEmpty) {
                db.collection("appointments").document(appointment.appointmentId!!).set(appointment).await()
                true
            } else {
                false // Conflict found
            }
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun getDoctorAppointments(doctorId: String): List<Appointment> {
        return try {
            val snapshot = db.collection("appointments")
                .whereEqualTo("doctorId", doctorId)
                .get()
                .await()
            snapshot.toObjects(Appointment::class.java)
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun addMedicalRecord(record: MedicalRecord) {
        try {
            db.collection("medicalRecords").document(record.recordId!!).set(record).await()
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun getAllUsers(): List<User> {
        val users = mutableListOf<User>()
        try {
            val patients = db.collection("patients").get().await()
            users.addAll(mapQuerySnapshotToUsers(patients, "Patient"))
            val doctors = db.collection("doctors").get().await()
            users.addAll(mapQuerySnapshotToUsers(doctors, "Doctor"))
            // Add administrators to the list
            val administrators = db.collection("administrators").get().await()
            users.addAll(mapQuerySnapshotToUsers(administrators, "Administrator"))
        } catch (e: Exception) {
            throw e
        }
        return users
    }

    private fun mapQuerySnapshotToUsers(querySnapshot: QuerySnapshot, userType: String): List<User> {
        return querySnapshot.documents.mapNotNull { document ->
            val userId = document.id
            val fullName = document.getString("fullName")
            val email = document.getString("email")
            // Handle isVerified for administrators (default to true if not present)
            val isVerified = if (userType == "Administrator") {
                document.getBoolean("isVerified") ?: true
            } else {
                document.getBoolean("isVerified") ?: false
            }

            if (userId != null && fullName != null && email != null) {
                User(userId, fullName, email, userType, isVerified)
            } else {
                null
            }
        }
    }
    suspend fun getUserDetails(userId: String, userType: String): Any? {
        return try {
            val collectionName = when (userType) {
                "Patient" -> "patients"
                "Doctor" -> "doctors"
                "Administrator" -> "administrators"
                else -> throw IllegalArgumentException("Invalid user type")
            }
            val document = db.collection(collectionName).document(userId).get().await()
            when (userType) {
                "Patient" -> document.toObject(Patient::class.java)
                "Doctor" -> document.toObject(Doctor::class.java)
                "Administrator" -> document.toObject(Administrator::class.java)
                else -> null
            }
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun updateUser(user: Any, userId: String, userType: String) {
        try {
            val collectionName = when (userType) {
                "Patient" -> "patients"
                "Doctor" -> "doctors"
                "Administrator" -> "administrators"
                else -> throw IllegalArgumentException("Invalid user type")
            }
            db.collection(collectionName).document(userId).set(user).await()
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun activateUser(userId: String, userType: String) {
        try {
            val collectionName = when (userType) {
                "Patient" -> "patients"
                "Doctor" -> "doctors"
                "Administrator" -> "administrators" // Add administrator case
                else -> throw IllegalArgumentException("Invalid user type")
            }
            db.collection(collectionName).document(userId).update("isVerified", true).await()
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun deactivateUser(userId: String, userType: String) {
        try {
            val collectionName = when (userType) {
                "Patient" -> "patients"
                "Doctor" -> "doctors"
                "Administrator" -> "administrators" // Add administrator case
                else -> throw IllegalArgumentException("Invalid user type")
            }
            db.collection(collectionName).document(userId).update("isVerified", false).await()
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun deleteUser(userId: String, userType: String) {
        try {
            val collectionName = when (userType) {
                "Patient" -> "patients"
                "Doctor" -> "doctors"
                "Administrator" -> "administrators" // Add administrator case
                else -> throw IllegalArgumentException("Invalid user type")
            }
            db.collection(collectionName).document(userId).delete().await()
        } catch (e: Exception) {
            throw e
        }
    }
    suspend fun checkAppointmentConflict(doctorId: String, appointmentDateTime: String): Boolean {
        return try {
            val conflictingAppointments = db.collection("appointments")
                .whereEqualTo("doctorId", doctorId)
                .whereEqualTo("appointmentDateTime", appointmentDateTime)
                .get()
                .await()

            !conflictingAppointments.isEmpty
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error checking appointment conflict: ${e.message}")
            true // Assume conflict in case of error
        }
    }

    // في FirebaseRepository.kt
    suspend fun getBookedTimes(doctorId: String, date: String): List<String> {
        return try {
            val appointments = db.collection("appointments")
                .whereEqualTo("doctorId", doctorId)
                .whereGreaterThanOrEqualTo("appointmentDateTime", "$date ")
                .whereLessThan("appointmentDateTime", "$date\uf8ff")
                .get()
                .await()

            appointments.documents.mapNotNull {
                // Extract the time part (HH:mm) from appointmentDateTime
                it.getString("appointmentDateTime")?.substringAfter("$date ")
            }
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error getting booked times: ${e.message}")
            emptyList()
        }
    }


}