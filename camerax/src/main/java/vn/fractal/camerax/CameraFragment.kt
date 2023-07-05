package vn.fractal.camerax

import android.content.res.Configuration
import android.media.MediaActionSound
import android.media.MediaActionSound.SHUTTER_CLICK
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.net.toFile
import androidx.fragment.app.Fragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import vn.fractal.camerax.utils.delay
import vn.fractal.camerax.utils.findNavControllerSafely
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class CameraFragment : Fragment() {

    private var isTurnOnFlash = false

    /** Blocking camera operations are performed using this executor */
    private val cameraExecutor = Executors.newSingleThreadExecutor()

    private lateinit var outputDirectory: File
    private lateinit var viewFinder: PreviewView
    private lateinit var container: ConstraintLayout
    private var displayId: Int = -1
    private var imageCapture: ImageCapture? = null
    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK
    private var preview: Preview? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null


    private val mDx = MediaActionSound()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_camera, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mDx.load(SHUTTER_CLICK)
        container = view as ConstraintLayout
        viewFinder = container.findViewById(R.id.view_finder)

        // Determine the output directory
        outputDirectory = CameraActivity.getOutputDirectory(requireContext())
        // Wait for the views to be properly laid out
        viewFinder.post {
            // Keep track of the display in which this view is attached
            displayId = viewFinder.display.displayId

            // Build UI controls
            updateCameraUi()

            // Set up the camera and its use cases
            setUpCamera()
        }

    }

    private fun setUpCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener(Runnable {

            // CameraProvider
            cameraProvider = cameraProviderFuture.get()

            // Select lensFacing depending on the available cameras
            when {
                hasBackCamera() -> {
                    lensFacing = CameraSelector.LENS_FACING_BACK
                }
                hasFrontCamera() -> {
                    lensFacing = CameraSelector.LENS_FACING_FRONT
                }
                else -> {
                    XCamera.instance.cameraListener?.onFailure("Back and front camera are unavailable")
                    activity?.finish()
                }
            }

            // Build and bind the camera use cases
            bindCameraUseCases()
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    /** Method used to re-draw the camera UI controls, called every time configuration changes. */
    private fun updateCameraUi() {
        // Remove previous UI if any
        container.findViewById<ConstraintLayout>(R.id.camera_ui_container)?.let {
            container.removeView(it)
        }
        // Inflate a new view containing all UI for controlling the camera
        val controls = View.inflate(requireContext(), R.layout.camera_ui_container, container)
        // Listener for flash button
        val flashImageView = controls.findViewById<ImageView>(R.id.img_flash)
        flashImageView.setOnClickListener {
            val hasFlash = camera?.cameraInfo?.hasFlashUnit()
            if (hasFlash == true) {
                if (isTurnOnFlash) {
                    flashImageView.setImageResource(R.drawable.ic_flash_on_white)
                    isTurnOnFlash = false
                    camera?.cameraControl?.enableTorch(false)
                } else {
                    flashImageView.setImageResource(R.drawable.ic_flash_off_white)
                    isTurnOnFlash = true
                    camera?.cameraControl?.enableTorch(true)
                }
            }
        }
        // Listener for button used to capture photo
        controls.findViewById<ImageView>(R.id.capture_button).setOnClickListener {
            it.delay(3000)
            if (XCamera.instance.isEnableSound == true) {
                mDx.play(SHUTTER_CLICK)
            }
            // Create output file to hold the image
            // Get a stable reference of the modifiable image capture use case
            imageCapture?.let { imageCapture ->

                // Create output file to hold the image
                val photoFile = createFile(outputDirectory, FILENAME, PHOTO_EXTENSION)
                // Setup image capture metadata
                val metadata = ImageCapture.Metadata().apply {

                    // Mirror image when using the front camera
                    isReversedHorizontal = lensFacing == CameraSelector.LENS_FACING_FRONT
                }

                // Create output options object which contains file + metadata
                val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile)
                    .setMetadata(metadata)
                    .build()
                // Setup image capture listener which is triggered after photo has been taken
                imageCapture.takePicture(
                    outputOptions, cameraExecutor, object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                            val savedUri = outputFileResults.savedUri ?: Uri.fromFile(photoFile)
                            val bundle = Bundle().apply {
                                putString(
                                    FILE_NAME_KEY,
                                    savedUri.toFile().absolutePath
                                )
                            }

                            CoroutineScope(Dispatchers.Main).launch {
                                findNavControllerSafely()?.navigate(R.id.previewFragment, bundle)
                            }
                        }

                        override fun onError(exception: ImageCaptureException) {
                            CoroutineScope(Dispatchers.Main).launch {
                                XCamera.instance.cameraListener?.onFailure(exception.message)
                                activity?.finish()
                            }
                        }
                    })

            }

        }
    }

    private fun bindCameraUseCases() {
        // Get screen metrics used to setup camera for full screen resolution
        val metrics = DisplayMetrics().also { viewFinder.display.getRealMetrics(it) }

        val screenAspectRatio = aspectRatio(metrics.widthPixels, metrics.heightPixels)

        val rotation = viewFinder.display.rotation

        // CameraProvider
        if (cameraProvider == null) {
            XCamera.instance.cameraListener?.onFailure("Camera initialization failed.")
            activity?.finish()
        }

        // CameraSelector
        val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

        // Preview
        preview = Preview.Builder()
            // We request aspect ratio but no resolution
            .setTargetAspectRatio(screenAspectRatio)
            // Set initial target rotation
            .setTargetRotation(rotation)
            .build()

        // ImageCapture
        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            // We request aspect ratio but no resolution to match preview config, but letting
            // CameraX optimize for whatever specific resolution best fits our use cases
            .setTargetAspectRatio(screenAspectRatio)
            // Set initial target rotation, we will have to call this again if rotation changes
            // during the lifecycle of this use case
            .setTargetRotation(rotation)
            .build()

        // Must unbind the use-cases before rebinding them
        cameraProvider?.unbindAll()

        try {
            // A variable number of use-cases can be passed here -
            // camera provides access to CameraControl & CameraInfo
            camera = cameraProvider?.bindToLifecycle(
                this, cameraSelector, preview, imageCapture
            )

            // Attach the viewfinder's surface provider to preview use case
            preview?.setSurfaceProvider(viewFinder.surfaceProvider)
        } catch (exc: Exception) {
            val msg = "Use case binding failed"
            Log.e(TAG, msg, exc)
            XCamera.instance.cameraListener?.onFailure(exc.message)
            activity?.finish()
        }
    }

    /**
     * Inflate camera controls and update the UI manually upon config changes to avoid removing
     * and re-adding the view finder from the view hierarchy; this provides a seamless rotation
     * transition on devices that support it.
     *
     * NOTE: The flag is supported starting in Android 8 but there still is a small flash on the
     * screen for devices that run Android 9 or below.
     */
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateCameraUi()
    }

    override fun onResume() {
        super.onResume()
        // Make sure that all permissions are still present, since the
        // user could have removed them while the app was in paused state.
        if (!PermissionsFragment.hasPermissions(requireContext())) {
            //findNavController().navigate(CameraFragmentDirections.actionCameraFragmentToPermissionsFragment())
            findNavControllerSafely()?.navigate(R.id.permissionsFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Shut down our background executor
        cameraExecutor.shutdown()
    }

    /**
     *  [androidx.camera.core.ImageAnalysisConfig] requires enum value of
     *  [androidx.camera.core.AspectRatio]. Currently it has values of 4:3 & 16:9.
     *
     *  Detecting the most suitable ratio for dimensions provided in @params by counting absolute
     *  of preview ratio to one of the provided values.
     *
     *  @param width - preview width
     *  @param height - preview height
     *  @return suitable aspect ratio
     */
    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = max(width, height).toDouble() / min(width, height)
        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }

    /** Returns true if the device has an available back camera. False otherwise */
    private fun hasBackCamera(): Boolean {
        return cameraProvider?.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) ?: false
    }

    /** Returns true if the device has an available front camera. False otherwise */
    private fun hasFrontCamera(): Boolean {
        return cameraProvider?.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) ?: false
    }

    companion object {
        private const val TAG = "CameraX"

        private const val FILENAME = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val PHOTO_EXTENSION = ".png"
        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0
        const val FILE_NAME_KEY = "file_name"


        /** Helper function used to create a timestamped file */
        private fun createFile(baseFolder: File, format: String, extension: String) =
            File(
                baseFolder, SimpleDateFormat(format, Locale.US)
                    .format(System.currentTimeMillis()) + extension
            )
    }
}
