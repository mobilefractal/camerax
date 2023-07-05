package vn.fractal.camerax.utils

import android.annotation.SuppressLint
import android.graphics.*
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.os.Build
import androidx.exifinterface.media.ExifInterface
import vn.fractal.camerax.Caption
import vn.fractal.camerax.XCamera
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


class Utils {
    companion object {
        fun resizeImage(file: File) {
            try {
                BitmapFactory.Options().run {
                    val scaleBitmap = decodeSampledBitmapFromFile(file, 1024F, 1024F)

                    if (XCamera.instance.captions.isNotEmpty()) {
                        drawCaptionWithTime(scaleBitmap!!, XCamera.instance.captions)
                    }
                    file.outputStream().use {
                        scaleBitmap?.compress(XCamera.instance.compressFormat, 80, it)
                        scaleBitmap?.recycle()
                    }
                }
            } catch (ex: Exception) {
                print("Resize Error: ${ex.message}")
//                XCamera.instance.cameraListener?.onFailure("Exception: ${ex.message}")
            }
        }

        private fun drawCaptionWithTime(scaleBitmap: Bitmap, captions: List<Caption>) {
            val cv = Canvas(scaleBitmap)
            cv.drawBitmap(scaleBitmap, 0f, 0f, null)
            for ((index, caption) in captions.withIndex()) {
                val paintText = Paint(ANTI_ALIAS_FLAG)
                paintText.color = caption.textColor
                paintText.textSize = caption.textSize
                paintText.style = Paint.Style.FILL
                val rectText = Rect()
                paintText.getTextBounds(caption.text, 0, caption.text.length, rectText)
                cv.drawText(
                    caption.text,
                    (cv.width - rectText.width()) / 2f,
                    (cv.height - rectText.height() / 3).toFloat() - (20f * (captions.size - index) + 10f),
                    paintText
                )
            }
        }

        @SuppressLint("SimpleDateFormat")
        private fun getTimeCurrent(): String {
            var answer = ""
            answer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val current = LocalDateTime.now()
                val formatter = DateTimeFormatter.ofPattern("HH:mm:ss MM/dd/yyyy ")
                current.format(formatter)
            } else {
                val date = Date();
                val formatter = SimpleDateFormat("HH:mm:ss MM/dd/yyyy")
                formatter.format(date)
            }
            return "Time: $answer"
        }


        @Throws(Exception::class)
        private fun decodeSampledBitmapFromFile(
            imageFile: File,
            reqWidth: Float,
            reqHeight: Float
        ): Bitmap? {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
                BitmapFactory.decodeFile(imageFile.absolutePath, this)
            }

            val actualHeight = options.outHeight
            val actualWidth = options.outWidth
            val imgRatio = actualWidth.toFloat() / actualHeight.toFloat()
            val maxRatio = reqWidth / reqHeight

            var targetWidth = actualWidth
            var targetHeight = actualHeight

            if (actualHeight > reqHeight || actualWidth > reqWidth) {
                if (imgRatio < maxRatio) {
                    targetWidth = (reqHeight / actualHeight * actualWidth).toInt()
                    targetHeight = reqHeight.toInt()
                } else if (imgRatio > maxRatio) {
                    targetWidth = reqWidth.toInt()
                    targetHeight = (reqWidth / actualWidth * actualHeight).toInt()
                } else {
                    targetWidth = reqWidth.toInt()
                    targetHeight = reqHeight.toInt()
                }
            }

            options.inSampleSize = calculateInSampleSize(options, targetWidth, targetHeight)
            options.inJustDecodeBounds = false
            options.inTempStorage = ByteArray(16 * 1024)

            return try {
                val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath, options)
                val scaledBitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
                bitmap.recycle()

                val exif = ExifInterface(imageFile.absolutePath)
                val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0)
                val matrix = Matrix()
                when (orientation) {
                    6 -> matrix.postRotate(90F)
                    3 -> matrix.postRotate(180F)
                    8 -> matrix.postRotate(270F)
                }
                Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.width, scaledBitmap.height, matrix, true)
            } catch (e: Exception) {
                e.printStackTrace()
                throw e
            }
        }

        private fun calculateInSampleSize(
            options: BitmapFactory.Options,
            reqWidth: Int,
            reqHeight: Int
        ): Int {
            // Raw height and width of image
            val (height: Int, width: Int) = options.run { outHeight to outWidth }
            var inSampleSize = 1

            if (height > reqHeight || width > reqWidth) {

                val halfHeight: Int = height / 2
                val halfWidth: Int = width / 2

                // Calculate the largest inSampleSize value that is a power of 2 and keeps both
                // height and width larger than the requested height and width.
                while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                    inSampleSize *= 2
                }
            }

            return inSampleSize
        }

    }
}