package com.example.cloudhealthcareapp.repository

import com.example.cloudhealthcareapp.models.Appointment
import com.example.cloudhealthcareapp.models.Doctor
import com.example.cloudhealthcareapp.models.MedicalRecord
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class FirebaseRepository {

    private val db = Firebase.firestore

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

    // TODO: Add functions for Admin operations (activate, deactivate, delete users)
}