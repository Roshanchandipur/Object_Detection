package com.example.objectdetection

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.provider.MediaStore
import android.view.*
import android.widget.ImageView
import com.example.objectdetection.ml.SsdMobilenetV11Metadata1
//import com.example.objectdetection.ml.SsdMobilenetV11Metadata1
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

class DetectionVideo : AppCompatActivity(), SurfaceHolder.Callback,
    TextureView.SurfaceTextureListener {

    private lateinit var surfaceView: SurfaceView
    private lateinit var textureView: TextureView
    private lateinit var surfaceHolder: SurfaceHolder
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var videoUri: Uri
    private lateinit var mediaMetadataRetriever: MediaMetadataRetriever

    private lateinit var tflite: Interpreter

    var colors = listOf<Int>(
        Color.BLUE, Color.GREEN, Color.RED, Color.CYAN, Color.GRAY, Color.BLACK,
        Color.DKGRAY, Color.MAGENTA, Color.YELLOW, Color.RED
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detection_video)

        surfaceView = findViewById(R.id.surfaceView)
        textureView = findViewById(R.id.textureView)

        surfaceHolder = surfaceView.holder
        surfaceHolder.addCallback(this)

        textureView.surfaceTextureListener = this

        mediaPlayer = MediaPlayer()
        mediaMetadataRetriever = MediaMetadataRetriever()

        tflite = Interpreter(loadModelFile())

        // Launch the system file picker to choose a video file
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "video/*"
        }
        startActivityForResult(intent, REQUEST_VIDEO)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_VIDEO && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                videoUri = uri
                initializeMediaPlayer()
            }
        }
    }
    private fun initializeMediaPlayer() {
        try {
            val videoPath = getVideoPath(videoUri)
            mediaMetadataRetriever.setDataSource(videoPath)

            mediaPlayer.setDataSource(videoPath)
            mediaPlayer.setSurface(surfaceHolder.surface)
            mediaPlayer.prepare()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    override fun surfaceCreated(holder: SurfaceHolder) {}

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        mediaPlayer.start()
        processVideoFrames()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        mediaPlayer.release()
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {}

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        return true
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
        // Not used in this example
    }

    private fun processVideoFrames() {
        val frameRateString = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE)
        val frameRate = frameRateString?.toFloatOrNull() ?: 30f // Default to 30fps

        val durationString = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        val duration = durationString?.toLongOrNull() ?: 0L

        val frameInterval = (1000 / frameRate).toLong()

        var currentPosition = 0L
        while (currentPosition < duration) {
            mediaPlayer.seekTo(currentPosition.toInt())
            val frameBitmap = textureView.bitmap
            val detectedObjects = performObjectDetection(frameBitmap!!)
            displayDetectedObjects(detectedObjects)

            currentPosition += frameInterval
        }
    }

    private fun performObjectDetection(frameBitmap: Bitmap): List<Rect> {
        val inputBuffer = preprocessFrame(frameBitmap)
        val outputBuffer = ByteBuffer.allocateDirect(4 * NUM_CLASSES)
            .order(ByteOrder.nativeOrder())

        tflite.run(inputBuffer, outputBuffer)

        val detectedObjects = mutableListOf<Rect>()
        for (i in 0 until NUM_CLASSES) {
            val confidence = outputBuffer.getFloat(i * 4)
            if (confidence > CONFIDENCE_THRESHOLD) {
                val left = outputBuffer.getFloat(i * 4 + 1)
                val top = outputBuffer.getFloat(i * 4 + 2)
                val right = outputBuffer.getFloat(i * 4 + 3)
                val bottom = outputBuffer.getFloat(i * 4 + 4)
                detectedObjects.add(Rect(left.toInt(), top.toInt(), right.toInt(), bottom.toInt()))
            }
        }

        return detectedObjects
    }

    private fun displayDetectedObjects(detectedObjects: List<Rect>) {
        // Display the detected objects on the TextureView
        // You can draw rectangles or overlays on the TextureView to represent the detected objects
    }

    private fun preprocessFrame(frame: Bitmap): ByteBuffer {
        // Preprocess the frame bitmap before feeding it to the TensorFlow Lite model
        // Perform any necessary resizing, normalization, or other preprocessing steps
        // Return the preprocessed frame as a ByteBuffer
        // Example:
        val inputSize = 300 // Replace with your input size
        val resizedBitmap = Bitmap.createScaledBitmap(frame, inputSize, inputSize, false)
        val inputBuffer = ByteBuffer.allocateDirect(4 * inputSize * inputSize * 3)
            .order(ByteOrder.nativeOrder())

        val intValues = IntArray(resizedBitmap.width * resizedBitmap.height)

        resizedBitmap.getPixels(intValues, 0, resizedBitmap.width, 0, 0, resizedBitmap.width, resizedBitmap.height)
        var pixel = 0
        for (i in 0 until inputSize) {
            for (j in 0 until inputSize) {
                val value = intValues[pixel++]
                inputBuffer.putFloat(((value shr 16 and 0xFF) - IMAGE_MEAN).toFloat() / IMAGE_STD.toFloat())
                inputBuffer.putFloat(((value shr 8 and 0xFF) - IMAGE_MEAN).toFloat() / IMAGE_STD.toFloat())
                inputBuffer.putFloat(((value and 0xFF) - IMAGE_MEAN).toFloat() / IMAGE_STD.toFloat())
            }
        }

        return inputBuffer
    }

    private fun loadModelFile(): ByteBuffer {
        val modelFilePath = "ml/ssd_mobilenet_v1_1_metadata_1.tflite"
        val modelFile = File(applicationContext.filesDir, modelFilePath)
        val modelFileLength = modelFile.length().toInt()
        val modelBuffer = ByteBuffer.allocateDirect(modelFileLength)
        val fileChannel = FileInputStream(modelFile).channel
        fileChannel.read(modelBuffer)
        modelBuffer.rewind()

        return modelBuffer
    }

    private fun getVideoPath(uri: Uri): String {
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.moveToFirst()
        val columnIndex = cursor?.getColumnIndex(MediaStore.Images.Media.DATA)
        val videoPath = cursor?.getString(columnIndex ?: 0)
        cursor?.close()
        return videoPath ?: ""
    }

    companion object {
        private const val REQUEST_VIDEO = 1001
        private const val NUM_CLASSES = 10
        private const val CONFIDENCE_THRESHOLD = 0.5
        private const val IMAGE_MEAN = 0.0
        private const val IMAGE_STD = 255.0
    }
}