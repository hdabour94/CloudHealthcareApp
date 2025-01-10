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
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale



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
            Log.e("FirebaseRepository", "Error adding medical record: ${e.message}")
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
    suspend fun getAppointmentsForPatient(patientId: String): List<Appointment> {
        return try {
            val snapshot = db.collection("appointments")
                .whereEqualTo("patientId", patientId)
                .get()
                .await()
            snapshot.toObjects(Appointment::class.java)
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error fetching appointments: ${e.message}")
            emptyList()
        }
    }

    suspend fun getAppointmentsForDoctor(doctorId: String, type: String = "all"): List<Appointment> {
        return try {
            val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Calendar.getInstance().time)
            val query = db.collection("appointments")
                .whereEqualTo("doctorId", doctorId)
                .orderBy("appointmentDateTime", Query.Direction.ASCENDING)

            val snapshot = when (type) {
                "upcoming" -> query
                    .whereGreaterThanOrEqualTo("appointmentDateTime", currentTime)
                    .whereIn("status", listOf("pending", "accepted"))
                "past" -> query
                    .whereLessThan("appointmentDateTime", currentTime)
                    .whereIn("status", listOf("completed", "rejected")) // Assuming you have a "rejected" status
                else -> query
                    .whereIn("status", listOf("pending", "accepted"))
            }.get().await()

            snapshot.toObjects(Appointment::class.java)
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error fetching appointments: ${e.message}")
            emptyList()
        }
    }

    suspend fun getDoctor(doctorId: String): Doctor? {
        return try {
            val document = db.collection("doctors").document(doctorId).get().await()
            document.toObject(Doctor::class.java)
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error fetching doctor: ${e.message}")
            null
        }
    }

    suspend fun getPatientsForDoctor(doctorId: String): List<Patient> {
        val patients = mutableListOf<Patient>()
        try {
            val appointments = db.collection("appointments")
                .whereEqualTo("doctorId", doctorId)
                .get()
                .await()

            val patientIds = appointments.documents.mapNotNull { it.getString("patientId") }.distinct()

            for (patientId in patientIds) {
                val patient = db.collection("patients").document(patientId).get().await()
                val patientData = patient.toObject(Patient::class.java)
                if (patientData != null) {
                    patientData.userId = patientId // Set the document ID as userId
                    patients.add(patientData)
                }
            }
            Log.d("FirebaseRepository", "Patients fetched for doctor $doctorId: ${patients.size}")
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error fetching patients for doctor: ${e.message}")
        }
        return patients
    }

    suspend fun getNewPatients(doctorId: String): List<Patient> {
        val newPatients = mutableListOf<Patient>()
        try {
            // Get the current date and the date one month ago
            val currentDate = Calendar.getInstance()
            val oneMonthAgo = Calendar.getInstance()
            oneMonthAgo.add(Calendar.MONTH, -1)

            // Format the dates to strings for comparison
            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val currentDateString = formatter.format(currentDate.time)
            val oneMonthAgoString = formatter.format(oneMonthAgo.time)

            // Fetch all appointments for the doctor within the last month
            val appointments = db.collection("appointments")
                .whereEqualTo("doctorId", doctorId)
                .whereGreaterThanOrEqualTo("appointmentDateTime", oneMonthAgoString)
                .whereLessThanOrEqualTo("appointmentDateTime", currentDateString)
                .get()
                .await()

            // Get a list of unique patient IDs from these appointments
            val patientIds = appointments.documents.mapNotNull { it.getString("patientId") }.distinct()

            // Fetch the patient data for each ID
            for (patientId in patientIds) {
                val patientSnapshot = db.collection("patients").document(patientId).get().await()
                val patient = patientSnapshot.toObject(Patient::class.java)

                // Check if the patient has only one appointment (new patient)
                val patientAppointments = appointments.documents.filter { it.getString("patientId") == patientId }
                if (patient != null && patientAppointments.size == 1) {
                    patient.userId = patientId // Set the document ID as userId
                    newPatients.add(patient)
                }
            }
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error fetching new patients: ${e.message}")
        }
        return newPatients
    }

    suspend fun getFollowUpPatients(doctorId: String): List<Patient> {
        val followUpPatients = mutableListOf<Patient>()
        try {
            // Get the current date and the date one month ago
            val currentDate = Calendar.getInstance()
            val oneMonthAgo = Calendar.getInstance()
            oneMonthAgo.add(Calendar.MONTH, -1)

            // Format the dates to strings for comparison
            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val currentDateString = formatter.format(currentDate.time)
            val oneMonthAgoString = formatter.format(oneMonthAgo.time)

            // Fetch all appointments for the doctor within the last month
            val appointments = db.collection("appointments")
                .whereEqualTo("doctorId", doctorId)
                .whereGreaterThanOrEqualTo("appointmentDateTime", oneMonthAgoString)
                .whereLessThanOrEqualTo("appointmentDateTime", currentDateString)
                .get()
                .await()

            // Get a list of unique patient IDs from these appointments
            val patientIds = appointments.documents.mapNotNull { it.getString("patientId") }.distinct()

            // Fetch the patient data for each ID
            for (patientId in patientIds) {
                val patientSnapshot = db.collection("patients").document(patientId).get().await()
                val patient = patientSnapshot.toObject(Patient::class.java)

                // Check if the patient has more than one appointment (follow-up patient)
                val patientAppointments = appointments.documents.filter { it.getString("patientId") == patientId }
                if (patient != null && patientAppointments.size > 1) {
                    patient.userId = patientId // Set the document ID as userId
                    followUpPatients.add(patient)
                }
            }
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error fetching follow-up patients: ${e.message}")
        }
        return followUpPatients
    }

    suspend fun getAppointmentRequests(doctorId: String): List<Appointment> {
        return try {
            val snapshot = db.collection("appointments")
                .whereEqualTo("doctorId", doctorId)
                .whereEqualTo("status", "pending")
                .get()
                .await()
            snapshot.toObjects(Appointment::class.java)
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error fetching appointment requests: ${e.message}")
            emptyList()
        }
    }

    suspend fun updateAppointmentStatus(appointmentId: String, status: String) {
        try {
            db.collection("appointments").document(appointmentId)
                .update("status", status)
                .await()
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error updating appointment status: ${e.message}")
            throw e
        }
    }

    suspend fun getExpiredAppointments(userId: String, userType: String, currentTime: String): List<Appointment> {
        return try {
            val query = db.collection("appointments")
                .whereLessThan("appointmentDateTime", currentTime)
                .whereEqualTo(userType, userId)
                .whereEqualTo("status", "pending")

            val userSpecificQuery = if (userType == "doctorId") {
                query.whereEqualTo("doctorId", userId)
            } else {
                query.whereEqualTo("patientId", userId)
            }

            val snapshot = userSpecificQuery.get().await()
            snapshot.toObjects(Appointment::class.java)
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error fetching expired appointments: ${e.message}")
            emptyList()
        }
    }

    suspend fun savePrescription(patientId: String, prescriptionText: String) {
        try {
            val prescription = hashMapOf(
                "prescriptionText" to prescriptionText,
                "date" to SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            )
            db.collection("patients").document(patientId)
                .collection("prescriptions").add(prescription).await()
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error saving prescription: ${e.message}")
            throw e
        }
    }


}