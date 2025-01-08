package com.example.cloudhealthcareapp

import android.app.Application
import com.google.firebase.FirebaseApp

class CloudHealthcareApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}