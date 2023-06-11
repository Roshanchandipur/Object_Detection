package com.example.objectdetection

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.view.TextureView
import android.widget.Button
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.objectdetection.ml.SsdMobilenetV11Metadata1
//import com.example.objectdetection.ml.SsdMobilenetV11Metadata1
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp

class HomePage : AppCompatActivity() {


    lateinit var imageView: ImageView
    lateinit var bitmap: Bitmap
    lateinit var model: SsdMobilenetV11Metadata1
    lateinit var imageProcessor: ImageProcessor
    lateinit var labels: List<String>
    lateinit var button: Button
    val paint = Paint()
    var uri: Uri?=null

    var colors = listOf<Int>(
        Color.BLUE, Color.GREEN, Color.RED, Color.CYAN, Color.GRAY, Color.BLACK,
        Color.DKGRAY, Color.MAGENTA, Color.YELLOW, Color.RED
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)
        imageView = findViewById(R.id.imageView)
        button = findViewById(R.id.b1)
        model = SsdMobilenetV11Metadata1.newInstance(this)
        val intent = Intent()
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT)
        labels = FileUtil.loadLabels(this, "labels.txt")
        imageProcessor = ImageProcessor.Builder().add(ResizeOp(300,300, ResizeOp.ResizeMethod.BILINEAR)).build()
        button.setOnClickListener{
            startActivityForResult(intent, 1001)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==1001){
            uri = if (data != null) data.data else null
            bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
            pridict()
        }
    }

    fun pridict(){
        val newBitmap = rotateBitmap(bitmap, 90)
        var image = TensorImage.fromBitmap(newBitmap)
        image = imageProcessor.process(image)

        val outputs = model.process(image)
        val locations = outputs.locationsAsTensorBuffer.floatArray
        val classes = outputs.classesAsTensorBuffer.floatArray
        val scores = outputs.scoresAsTensorBuffer.floatArray
        val numberOfDetections = outputs.numberOfDetectionsAsTensorBuffer.floatArray

        var mutable = newBitmap.copy(Bitmap.Config.ARGB_8888,true)
        val canvas = Canvas(mutable)

        val h = mutable.height
        val w = mutable.width

        paint.textSize = h/20f
        paint.strokeWidth = 15f
        var x = 0
        scores.forEachIndexed{index, f->
            x = index
            x*=4
            if(f>0.5){
                paint.setColor(colors.get(index))
                paint.style = Paint.Style.STROKE
                canvas.drawRect(RectF(locations.get(x+1)*w, locations.get(x)*h, locations.get(x+3)*w, locations.get(x+2)*h), paint)
                paint.style = Paint.Style.FILL
                var name = "..."
                if(index>=0 && index<classes.size && classes.get(index).toInt()<labels.size){
                    name = labels.get(classes.get(index).toInt())
                }
                canvas.drawText(name+" "+((f*100).toInt()/100f).toString(), locations.get(x+1)*w, locations.get(x)*h,paint)
            }
        }
        imageView.setImageBitmap(mutable)
    }
    private fun rotateBitmap(bitmap: Bitmap, rotation: Int): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(rotation.toFloat())
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
    override fun onDestroy() {
        super.onDestroy()
        model.close()
    }
}