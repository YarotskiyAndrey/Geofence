package com.example.test.geofenceapp

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {

    private val RECORD_REQUEST_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        geofencingClient = LocationServices.getGeofencingClient(this)
        fab.setOnClickListener { view ->
            val myLocation: Location? = getLastKnownLocation()
            val text = myLocation.let { myLocation.toString() }

            Snackbar.make(view, text, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()

            geofencingClient.removeGeofences(geofencePendingIntent)?.run {
                addOnSuccessListener {
                    // Geofences added
                    val toast = Toast.makeText(this@MainActivity, "Geofences Removed", Toast.LENGTH_LONG)
                    toast.show()
                }
                addOnFailureListener {
                    // Failed to add geofences
                    val toast = Toast.makeText(this@MainActivity, "Failed to Remove geofences\n $it", Toast.LENGTH_LONG)
                    toast.show()
                }
            }
        }


        geofenceList.add(simpleGeofence(location, 400f))
        geofenceList.add(simpleGeofence(location, 100f))
        setGeofences()
    }

    private var geofenceList: ArrayList<Geofence> = ArrayList()
    lateinit var geofencingClient: GeofencingClient

    private val location: Location by lazy {
        val location = Location("")
        location.latitude = 46.4688
        location.longitude = 30.7409
        location
    }

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(this, GeofenceTransitionsIntentService::class.java)
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun getGeofencingRequest(): GeofencingRequest {
        return GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofences(geofenceList)
        }.build()
    }

    private lateinit var mLocationManager: LocationManager

    @Throws(SecurityException::class)
    private fun getLastKnownLocation(): Location? {
        mLocationManager = applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val providers = mLocationManager.getProviders(true)
        var bestLocation: Location? = null
        for (provider in providers) {
            val l = mLocationManager.getLastKnownLocation(provider) ?: continue
            if (bestLocation == null || l.accuracy < bestLocation.accuracy) {
                // Found best last known location: %s", l);
                bestLocation = l
            }
        }
        return bestLocation
    }

    private fun setGeofences() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                RECORD_REQUEST_CODE
            )
        } else {
            geofencingClient.addGeofences(getGeofencingRequest(), geofencePendingIntent)?.run {
                addOnSuccessListener {
                    // Geofences added
                    val toast = Toast.makeText(this@MainActivity, "Geofences added", Toast.LENGTH_LONG)
                    toast.show()
                }
                addOnFailureListener {
                    // Failed to add geofences
                    val toast = Toast.makeText(this@MainActivity, "Failed to add geofences\n $it", Toast.LENGTH_LONG)
                    toast.show()
                }
            }
        }
    }

    private fun simpleGeofence(location: Location, radius: Float): Geofence {
        return Geofence.Builder()
            // Set the request ID of the geofence. This is a string to identify this
            // geofence.
            .setRequestId("RequestId.Rad_$radius")
            // Set the circular region of this geofence.
            .setCircularRegion(
                location.latitude,
                location.longitude,
                radius
            )
            // Set the expiration duration of the geofence. This geofence gets automatically
            // removed after this period of time.
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            // Set the transition types of interest. Alerts are only generated for these
            // transition. We track entry and exit transitions in this sample.
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT or Geofence.GEOFENCE_TRANSITION_DWELL)
            // Set the delay between GEOFENCE_TRANSITION_ENTER and GEOFENCE_TRANSITION_DWELLING in milliseconds
            .setLoiteringDelay(TimeUnit.SECONDS.toMillis(2).toInt())
            // Create the geofence.
            .build()

    }
}
