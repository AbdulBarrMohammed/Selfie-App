package com.example.selfieassignment

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.selfieassignment.databinding.FragmentCameraBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

class CameraFragment : Fragment() {
    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    private lateinit var imageViewModel: ImageViewModel
    private lateinit var cameraController: LifecycleCameraController

    private val imageCaptureDirectory: File by lazy {
        File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            "CameraX-Image"
        ).also {
            it.mkdirs()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imageViewModel = ViewModelProvider(this).get(ImageViewModel::class.java)

        if (!hasPermissions(requireContext())) {
            activityResultLauncher.launch(REQUIRED_PERMISSIONS)
        } else {
        startCamera()
       }

        binding.captureBtn.setOnClickListener {
            takePhoto()
        }
    }
    /**
     * starts camera with preview view and camera controller
     * @param none
     * @return none
     */
    private fun startCamera() {
        val previewView: PreviewView = binding.viewFinder
        cameraController = LifecycleCameraController(requireContext())
        cameraController.bindToLifecycle(this)
        cameraController.cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
        previewView.controller = cameraController
    }
    /**
     * Takes photo from cameraX
     * @param none
     * @return none
     */
    private fun takePhoto() {
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis())

        // Create a file to save the captured image in
        val imageFile = File(imageCaptureDirectory, "$name.jpg")

        val outputOptions = ImageCapture.OutputFileOptions.Builder(imageFile).build()

        cameraController.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val msg = "Photo capture success: ${output.savedUri}"
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, msg)

                    // Upload image to Firebase
                    output.savedUri?.let { savedUri ->
                        imageViewModel.saveImage(savedUri)
                    } ?: Log.e(TAG, "Saved URI is null")

                    // Navigate to MainFragment
                    val action = CameraFragmentDirections.actionCameraFragmentToMainFragment()
                    findNavController().navigate(action)
                }
            }
        )


    }
    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions())
        { permissions ->

            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in REQUIRED_PERMISSIONS && it.value == false)
                    permissionGranted = false
            }
            if (!permissionGranted) {
                Toast.makeText(requireContext(), "Permission request denied", Toast.LENGTH_LONG).show()
            }
            else {
                startCamera()
            }
        }


    /**
     * request permission to use camera
     * @param none
     * @return none
     */

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                startCamera()
            } else {
                Toast.makeText(requireContext(), "Permission request denied", Toast.LENGTH_LONG).show()
            }
        }
    }

    companion object {
        private const val TAG = "CameraFragment"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val PERMISSIONS_REQUEST_CODE = 1001
        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                android.Manifest.permission.CAMERA
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()

        fun hasPermissions(context: Context) = REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
