package com.example.cloudhealthcareapp.services

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Handle the received message here
        // You can show a notification or update UI elements
    }

    override fun onNewToken(token: String) {
        // Called when a new token is generated
        // You can send this token to your server to update the user's token
    }
}