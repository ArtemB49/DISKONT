package com.belyaev.artem.agzs_diskont.controllers

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.SparseArray
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.belyaev.artem.agzs_diskont.R
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import java.io.IOException

// Tutorial https://www.youtube.com/watch?v=o69UqAKi47I

const val REQUEST_CAMERA_PERMISSION = 1001

class EAN13ScanActivity : AppCompatActivity() {

    // View
    lateinit var cameraPreview: SurfaceView
    lateinit var cameraSource: CameraSource

    private var barcodeDetector: BarcodeDetector? = null

    var holder: SurfaceHolder? = null

    val codeNotSGP = 66

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ean13_scan)

        cameraPreview = findViewById(R.id.camera_view)

        cameraPreview.setZOrderMediaOverlay(true)
        holder = cameraPreview.holder

        barcodeDetector = BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.EAN_13)
                .build()

        if (barcodeDetector?.isOperational == false){
            setResult(codeNotSGP)
            this.finish()
        }
        cameraSource = CameraSource.Builder(this, barcodeDetector)
                //.setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedFps(24F)
                .setAutoFocusEnabled(true)
                //.setRequestedPreviewSize(640, 480)
                .build()

        cameraPreview.holder.addCallback(object : SurfaceHolder.Callback{

            override fun surfaceChanged(p0: SurfaceHolder?, p1: Int, p2: Int, p3: Int) {

            }

            override fun surfaceDestroyed(p0: SurfaceHolder?) {
                cameraSource.stop()
            }

            override fun surfaceCreated(p0: SurfaceHolder?) {

                if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){

                    ActivityCompat
                            .requestPermissions(this@EAN13ScanActivity,
                                    arrayOf(android.Manifest.permission.CAMERA),
                                    REQUEST_CAMERA_PERMISSION)
                    return
                }

                try {
                    cameraSource.start(cameraPreview.holder)


                } catch (ex: Exception){
                    ex.printStackTrace()
                }
            }
        })

        barcodeDetector?.setProcessor(object : Detector.Processor<Barcode>{
            override fun release() {

            }

            override fun receiveDetections(p0: Detector.Detections<Barcode>?) {
                if (p0 != null){
                    val barcodes: SparseArray<Barcode> = p0.detectedItems
                    if (barcodes.size() > 0){

                        //vibrate()
                        val intent = Intent()
                        intent.putExtra("barcode", barcodes.valueAt(0))
                        setResult(Activity.RESULT_OK, intent)
                        finish()
                    }
                }

            }
        })

    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode){
            REQUEST_CAMERA_PERMISSION -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                        return
                    }
                    try {
                        cameraSource.start(cameraPreview.holder)
                    } catch (e: IOException){
                        e.printStackTrace()
                    }

                }
            }
        }
    }

    private fun vibrate(){
        /*
        val vibrator = applicationContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)

        if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            vibrator.vibrate(VibrationEffect.DEFAULT_AMPLITUDE)
        } else {
            vibrator.vibrate(1000)
        }
        */
    }
}

