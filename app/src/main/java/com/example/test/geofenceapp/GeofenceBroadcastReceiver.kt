package com.example.test.geofenceapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.google.android.gms.location.sample.geofencing.GeofenceErrorMessages

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    companion object {
        val TAG = "GeofenceBroadcast"
        fun getGeofenceTransitionDetails(
            receiver: GeofenceBroadcastReceiver,
            geofenceTransition: Int,
            triggeringGeofences: List<Geofence>
        ): String? {
            val geofenceTransitionName = when (geofenceTransition) {
                Geofence.GEOFENCE_TRANSITION_DWELL -> "DWELL"
                Geofence.GEOFENCE_TRANSITION_ENTER -> "ENTER"
                Geofence.GEOFENCE_TRANSITION_EXIT -> "EXIT"
                else -> "Transition undefined: $geofenceTransition"
            }
            return "$receiver $geofenceTransitionName; triggeringGeofences: $triggeringGeofences"
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent.hasError()) {
            val errorMessage = GeofenceErrorMessages.getErrorString(
                context,
                geofencingEvent.errorCode
            )
            Log.e(GeofenceTransitionsIntentService.TAG, errorMessage)
            return
        }

        // Get the transition type.
        val geofenceTransition = geofencingEvent.geofenceTransition

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
            geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT ||
            geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL
        ) {

            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            val triggeringGeofences = geofencingEvent.triggeringGeofences

            // Get the transition details as a String.
            val geofenceTransitionDetails = getGeofenceTransitionDetails(
                this,
                geofenceTransition,
                triggeringGeofences
            )

            // Send notification and log the transition details.
            sendNotification(geofenceTransitionDetails)
            Log.i(GeofenceTransitionsIntentService.TAG, geofenceTransitionDetails)
        } else {
            // Log the error.
            Log.e(GeofenceTransitionsIntentService.TAG, "geofence_transition_invalid_type $geofenceTransition")
        }

    }

    private fun sendNotification(geofenceTransitionDetails: String?) {
        // Geofences triggered
        Log.e(TAG, "Geofences triggered: $geofenceTransitionDetails")
    }
}