package com.belyaev.artem.agzs_diskont.controllers.fragment

import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.belyaev.artem.agzs_diskont.R
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.journeyapps.barcodescanner.BarcodeEncoder
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread

/**
 * Created by Artem on 26.03.2018.
 */
class BarcodeFragment: Fragment() {

    private val white: Int = 0xFFFF
    private val black: Int = 0x0000

    private var tvDate: TextView? = null
    private var progressBarQR: ProgressBar? = null
    private var progressBarEAN: ProgressBar? = null
    private var progressBarDate: ProgressBar? = null
    private var imageViewQR: ImageView? = null
    private var imageViewEAN: ImageView? = null

    private var cardNumber: String? = null
    private lateinit var mainView: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        mainView = inflater.inflate(R.layout.fragment_barcode, null)



        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity?.applicationContext)
        cardNumber = sharedPreferences?.getString("card", "card not found")

        tvDate = mainView.findViewById(R.id.tv_current_date)

        progressBarQR = mainView.findViewById(R.id.progress_bar_qr)
        progressBarEAN = mainView.findViewById(R.id.progress_bar_ean13)
        progressBarDate = mainView.findViewById(R.id.progress_bar_date)

        imageViewQR = mainView.findViewById(R.id.img_qr)
        imageViewEAN = mainView.findViewById(R.id.img_ean13)


        return mainView
    }

    override fun onStart() {
        super.onStart()

        thread (start = true) {

            val formatter: DateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm",
                    Locale.FRANCE)
            val date = formatter.format(Calendar.getInstance().time)
            activity?.runOnUiThread {
                tvDate?.text = date
                progressBarDate?.visibility = View.INVISIBLE
            }
        }

        if (cardNumber != "card not found"){
            mainView.post {
                thread (start = true) {
                    Log.d("getWidth", "Start ${Calendar.getInstance()}")
                    val vHeight = mainView.measuredHeight
                    val vWidth = mainView.measuredWidth
                    Log.d("getWidth", "End ${Calendar.getInstance()}")

                    createBitmap(cardNumber!!, BarcodeFormat.EAN_13, vWidth, vHeight/2-300, {
                        activity?.runOnUiThread {
                            imageViewEAN?.setImageBitmap(it)
                            progressBarEAN?.visibility = View.INVISIBLE

                        }
                    })

                    createBitmap(cardNumber!!,
                            BarcodeFormat.QR_CODE, (vHeight/1.2).toInt(), (vHeight/1.2).toInt()-40, {
                        activity?.runOnUiThread {
                            imageViewQR?.setImageBitmap(it)
                            progressBarQR?.visibility = View.INVISIBLE
                        }

                    })
                }
            }
        }
    }

    private fun createBitmap(contents: String, format: BarcodeFormat,
                             width: Int, height: Int, completion: (Bitmap?)->Unit){
        val resultBitmap: Bitmap?
        val writer = MultiFormatWriter()
        try {
            val bitMatrix = writer.encode(contents, format, width, height)
            val barcodeEncoder = BarcodeEncoder()
            resultBitmap = barcodeEncoder.createBitmap(bitMatrix)
            completion(resultBitmap)
        } catch (e: WriterException){
            e.printStackTrace()
        }
    }



    private fun getViewWidth(viewInflate: View,
                             completion: (width: Int?, height: Int?) -> Unit){
        Log.d("getWidth", "Start ${Calendar.getInstance()}")
        viewInflate.viewTreeObserver.addOnGlobalLayoutListener {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN){
                viewInflate.viewTreeObserver.removeOnGlobalLayoutListener{}
            } else {
                viewInflate.viewTreeObserver.removeGlobalOnLayoutListener {}
            }
            completion(view?.measuredWidth, view?.measuredHeight)
            Log.d("getWidth", "End ${Calendar.getInstance()}")
        }
    }

    private fun getViewWidth(completion: (width: Int?, height: Int?) -> Unit){
        Log.d("getWidth", "Start")
        val display = activity?.windowManager?.defaultDisplay


        completion(display?.width, display?.height)
        Log.d("getWidth", "End");

    }

    private fun encodeAsBitmap(contents: String, format: BarcodeFormat,
                               width: Int, height: Int): Bitmap?{
        val contentsToEncode: String? = contents
        if (contentsToEncode == null){
            return null
        }

        var hints: Map<EncodeHintType, String?>? = null
        val encoding = guessAppropriateEncoding(contentsToEncode)
        if (encoding != null){
            hints = EnumMap<EncodeHintType, String?>(EncodeHintType::class.java)
            hints.put(EncodeHintType.CHARACTER_SET, encoding)
        }
        val writer = MultiFormatWriter()
        var result: BitMatrix
        try {
            result = writer.encode(contentsToEncode, format, width, height)
        } catch (iae: IllegalArgumentException) {
            return null
        }

        val resultWidth = result.width
        val resultHeight = result.height
        val pixels = IntArray(resultHeight * resultWidth)

        for (y in 0..resultHeight){
            val offset = y * resultWidth
            for (x in 0..resultWidth){
                pixels[offset + x] = if (result.get(x, y) ) black else white
            }
        }

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return bitmap
    }

    private fun guessAppropriateEncoding(contents: CharSequence): String?{
        for (symbol in contents){
            if (symbol.toInt() > 0xFF){
                return "UTF-8"
            }
        }
        return null
    }

}