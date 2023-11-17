package com.example.selfieassignment

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.getSystemService
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.selfieassignment.databinding.FragmentFullScreenBinding
import com.example.selfieassignment.databinding.FragmentMainBinding


class FullScreenFragment : Fragment(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var _binding: FragmentFullScreenBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        _binding = FragmentFullScreenBinding.inflate(inflater, container, false)


        val args: FullScreenFragmentArgs by navArgs()
        val imageUrl = args.imageUrl

        Glide.with(this)
            .load(imageUrl)
            .into(binding.fullscreenImageView)

        return binding.root
    }
    private var lastUpdate: Long = 0
    private var last_x: Float = 0.0f
    private var last_y: Float = 0.0f
    private var last_z: Float = 0.0f
    private val SHAKE_THRESHOLD = 5

    override fun onSensorChanged(event: SensorEvent?) {
        val curTime = System.currentTimeMillis()

        // only allow one update every 100ms.
        if ((curTime - lastUpdate) > 100) {
            val diffTime = (curTime - lastUpdate)
            lastUpdate = curTime

            val x = event!!.values[0]
            val y = event.values[1]
            val z = event.values[2]

            val speed = Math.abs(x + y + z - last_x - last_y - last_z) / diffTime * 10000

            if (speed > SHAKE_THRESHOLD) {
                // navigate to CameraActivity if phone is shaken
                if (findNavController().currentDestination?.id == R.id.fullScreenFragment) {
                    findNavController().navigate(R.id.action_fullScreenFragment_to_cameraFragment)

                }
            }

            last_x = x
            last_y = y
            last_z = z
        }
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.d(ContentValues.TAG, "TGEST")
    }


}