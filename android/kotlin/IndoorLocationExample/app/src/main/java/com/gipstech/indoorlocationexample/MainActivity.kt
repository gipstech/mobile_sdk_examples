package com.gipstech.indoorlocationexample

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

import com.gipstech.*

class MainActivity : AppCompatActivity() {
    val DEV_KEY = "" // PUT YOUR DEV KEY HERE
    val BUILDING_ID = "" // PUT THE ID OF THE BUILDING HERE

    var textView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        textView = findViewById(R.id.mainTextView)
        textView?.movementMethod = ScrollingMovementMethod()

        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.BLUETOOTH)

        GiPStech.init(this, DEV_KEY)

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            checkSelfPermission(Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(permissions, 0)
        } else {
            startLocation()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (!allGranted(grantResults)) {
            textView?.append("Please grant all permissions to start location")
            return
        }

        startLocation()
    }

    fun allGranted(grantResults: IntArray): Boolean {
        if (grantResults.isEmpty()) {
            return false
        }
        for (res in grantResults) {
            if (res != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    fun performFastCalibration() {
        GiPStech.getCalibrationManager().beginCalibration(CalibrationManager.CALIBRATION_TYPE_FAST, object: ProgressCallback<Void?> {
            override fun onResult(result: Void?) {
                textView?.append("Calibration completed\n")
                GiPStech.getIndoorLocalizer().registerListener(listener)
            }

            override fun onProgress(progress: Int) {
                textView?.append("Calibration progress: " + progress + "%\n")

                if (progress >= 25) {
                    GiPStech.getCalibrationManager().endCalibration()
                }
            }

            override fun onError(error: GiPStechError) {
                textView?.append(error.message + "\n")
            }
        })
    }

    fun startLocation() {
        GiPStech.getIndoorLocalizer().selectBuilding(BUILDING_ID, object: ProgressCallback<Void?> {
            override fun onResult(result: Void?) {
                textView?.append("Selection completed\n")
                GiPStech.getIndoorLocalizer().registerListener(listener)
            }

            override fun onProgress(progress: Int) {
                textView?.append("Selection progress: " + progress + "%\n")
            }

            override fun onError(error: GiPStechError) {
                textView?.append(error.message + "\n")
            }
        })
    }

    val listener = object: IndoorLocalizer.Listener {
        override fun onRegistrationComplete() {
            textView?.append("Registration completed\n")
        }

        override fun onRegistrationUpdate(progress: Int) {
            textView?.append("Registration progress: " + progress + "%\n")
        }

        override fun onLocationUpdate(location: IndoorLocation) {
            textView?.append("Location: " + location.longitude + " " + location.latitude + "\n")
        }

        override fun onFloorChange(floor: Floor): Boolean {
            textView?.append("Floor: " + floor.name + "\n")
            return true
        }

        override fun onAttitudeUpdate(attitude: Attitude) {
            textView?.append("Heading: " + attitude.heading + "\n")
        }

        override fun onError(error: GiPStechError) {
            textView?.append(error.message + "\n")

            if (GiPStechError.FAST_CALIBRATION_REQUIRED == error.code) {
                performFastCalibration()
            }
        }
    }
}