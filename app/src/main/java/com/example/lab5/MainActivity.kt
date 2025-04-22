package com.example.lab5

import android.content.pm.ActivityInfo
import android.hardware.*
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var magnetometer: Sensor? = null

    private lateinit var compassView: CompassView
    private lateinit var degreeText: TextView
    private lateinit var directionHistoryText: TextView
    private lateinit var warningText: TextView
    private lateinit var timeText: TextView

    private var gravity = FloatArray(3)
    private var geomagnetic = FloatArray(3)

    private val directionHistory = mutableListOf<String>()
    private var lastDirection: Float = 0f

    private var timeInDirection: Long = 0
    private var timerRunning: Boolean = false
    private var lastTimeChecked: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        degreeText = findViewById(R.id.degreeText)
        compassView = findViewById(R.id.compassView)
        directionHistoryText = findViewById(R.id.directionHistoryText)
        warningText = findViewById(R.id.warningText)
        timeText = findViewById(R.id.timeText)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        magnetometer?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> gravity = event.values.clone()
            Sensor.TYPE_MAGNETIC_FIELD -> geomagnetic = event.values.clone()
        }

        val R = FloatArray(9)
        val I = FloatArray(9)
        if (SensorManager.getRotationMatrix(R, I, gravity, geomagnetic)) {
            val orientation = FloatArray(3)
            SensorManager.getOrientation(R, orientation)
            val azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
            val degree = (azimuth + 360) % 360

            compassView.updateDirection(degree)
            degreeText.text = "${degree.toInt()}°"

            val directionLabel = getDirectionLabel(degree)

            if (directionHistory.isEmpty() || directionHistory.last() != directionLabel) {
                directionHistory.add(directionLabel)
                if (directionHistory.size > 5) {
                    directionHistory.removeAt(0)
                }
                directionHistoryText.text = "History: ${directionHistory.joinToString(" → ")}"
                compassView.updateDirectionHistory(directionHistory)
            }

            val currentTime = System.currentTimeMillis()

            if (directionLabel != getDirectionLabel(lastDirection)) {
                timeInDirection = 0
                lastTimeChecked = currentTime
                timerRunning = true
            }

            if (timerRunning) {
                timeInDirection = (currentTime - lastTimeChecked) / 1000
                timeText.text = "Time in the direction: $timeInDirection с"
            }

            lastDirection = degree
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun getDirectionLabel(degree: Float): String {
        return when (degree) {
            in 337.5..360.0, in 0.0..22.5 -> "N"
            in 22.5..67.5 -> "NE"
            in 67.5..112.5 -> "E"
            in 112.5..157.5 -> "SE"
            in 157.5..202.5 -> "S"
            in 202.5..247.5 -> "SW"
            in 247.5..292.5 -> "W"
            in 292.5..337.5 -> "NW"
            else -> "?"
        }
    }
}
