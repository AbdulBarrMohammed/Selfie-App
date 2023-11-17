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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.selfieassignment.databinding.FragmentMainBinding
import com.google.firebase.auth.FirebaseAuth

class MainFragment : Fragment(), SensorEventListener {
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private lateinit var imageViewModel: ImageViewModel
    private lateinit var imageAdapter: ImageAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {


        _binding = FragmentMainBinding.inflate(inflater, container, false)
        imageViewModel = ViewModelProvider(this).get(ImageViewModel::class.java)
        imageAdapter = ImageAdapter { imageUrl ->
            // navigates to full screen when image is clicked
            navigateToFullScreenImage(imageUrl)
        }
        setupRecyclerView()
        observeImages()

        sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        binding.logout.setOnClickListener {
            signOut()
        }
        return binding.root
    }

    private fun navigateToFullScreenImage(imageUrl: String) {
        // Define the navigation action with the imageUrl as argument
        val action = MainFragmentDirections.actionMainFragmentToFullScreenFragment(imageUrl)
        findNavController().navigate(action)

    }

    /**
     * signs out user from firebase
     * @param none
     * @return none
     */
    private fun signOut() {
        // Sign out logic
        FirebaseAuth.getInstance().signOut()
        if (findNavController().currentDestination?.id == R.id.mainFragment) {
            findNavController().navigate(R.id.action_mainFragment_to_signInFragment2)

        }
    }

    /**
     * sets up recycler view
     * @param none
     * @return none
     */
    private fun setupRecyclerView() {
        //val spanCount = 2 // Adjust the number as per your design needs

        // Set the RecyclerView to use the StaggeredGridLayoutManager with vertical orientation
        //val layoutManager = StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL)

        // Optionally, you can define the gap strategy for the StaggeredGridLayoutManager

        binding.rvImage.layoutManager = LinearLayoutManager(context)
        //binding.rvImage.layoutManager = layoutManager
        binding.rvImage.adapter = imageAdapter
    }

    /**
     * display imageview model
     * @param none
     * @return none
     */
    private fun observeImages() {
        imageViewModel.images.observe(viewLifecycleOwner, Observer { images ->
            imageAdapter.submitList(images.toList())
        })
        imageViewModel.fetchImages()
    }

    private var lastUpdate: Long = 0
    private var last_x: Float = 0.0f
    private var last_y: Float = 0.0f
    private var last_z: Float = 0.0f
    private val SHAKE_THRESHOLD = 2
    // Sensor event handling
    override fun onSensorChanged(event: SensorEvent?) {
        val curTime = System.currentTimeMillis()


        if ((curTime - lastUpdate) > 100) {
            val diffTime = (curTime - lastUpdate)
            lastUpdate = curTime

            val x = event!!.values[0]
            val y = event.values[1]
            val z = event.values[2]

            val speed = Math.abs(x + y + z - last_x - last_y - last_z) / diffTime * 10000

            if (speed > SHAKE_THRESHOLD) {
                // navigate to camera fragment
                if (findNavController().currentDestination?.id == R.id.mainFragment) {
                    findNavController().navigate(R.id.action_mainFragment_to_cameraFragment)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.d(ContentValues.TAG, "TEST")
    }
}
