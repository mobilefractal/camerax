package vn.fractal.camerax.utils

import android.graphics.*
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.os.Build
import androidx.exifinterface.media.ExifInterface
import vn.fractal.camerax.XCamera
import java.io.File
import java.io.IOException
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

                    XCamera.instance.imageCaption?.let {
                        drawCaptionWithTime(scaleBitmap!!, it)
                    }
                    file.outputStream().use {
                        scaleBitmap?.compress(XCamera.instance.compressFormat, 80, it)
                        scaleBitmap?.recycle()
                    }
                }
            } catch (ex: Exception) {
                XCamera.instance.cameraListener?.onFailure("Exception: ${ex.message}")
            }
        }

        private fun drawCaptionWithTime(scaleBitmap: Bitmap, caption: String) {
            val cv = Canvas(scaleBitmap)
            cv.drawBitmap(scaleBitmap, 0f, 0f, null)
            val paintText = Paint(ANTI_ALIAS_FLAG)
            paintText.color = Color.RED
            paintText.textSize = 17f
            paintText.style = Paint.Style.FILL
            val rectText = Rect()
            paintText.getTextBounds(caption, 0, caption.length, rectText)

            cv.drawText(
                caption,
                (cv.width / 2 - rectText.width() / 2) * 0.1.toFloat(),
                (cv.height - rectText.height() / 3).toFloat() - 30f,
                paintText
            )
            val paintTime = Paint(ANTI_ALIAS_FLAG)
            paintTime.color = Color.YELLOW
            paintTime.textSize = 15f
            paintTime.style = Paint.Style.FILL
            cv.drawText(
                getTimeCurrent(),
                (cv.width / 2 - rectText.width() / 2) * 0.1.toFloat(),
                (cv.height - rectText.height() / 3).toFloat() - 10,
                paintTime
            )
        }

        private fun getTimeCurrent(): String {
            var answer = ""
            answer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val current = LocalDateTime.now()
                val formatter = DateTimeFormatter.ofPattern("HH:mm:ss MM/dd/yyyy ")
                current.format(formatter)
            } else {
                var date = Date();
                val formatter = SimpleDateFormat("HH:mm:ss MM/dd/yyyy")
                formatter.format(date)
            }
            return "Time: $answer"
        }


        @Throws(IOException::class)
        private fun decodeSampledBitmapFromFile(
            imageFile: File,
            reqWidth: Float,
            reqHeight: Float
        ): Bitmap? {
            // First decode with inJustDecodeBounds=true to check dimensions
            var scaledBitmap: Bitmap? = null
            var bmp: Bitmap?
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            bmp = BitmapFactory.decodeFile(imageFile.absolutePath, options)
            var actualHeight = options.outHeight
            var actualWidth = options.outWidth
            var imgRatio = actualWidth.toFloat() / actualHeight.toFloat()
            val maxRatio = reqWidth / reqHeight
            if (actualHeight > reqHeight || actualWidth > reqWidth) {
                //If Height is greater
                if (imgRatio < maxRatio) {
                    imgRatio = reqHeight / actualHeight
                    actualWidth = (imgRatio * actualWidth).toInt()
                    actualHeight = reqHeight.toInt()
                } //If Width is greater
                else if (imgRatio > maxRatio) {
                    imgRatio = reqWidth / actualWidth
                    actualHeight = (imgRatio * actualHeight).toInt()
                    actualWidth = reqWidth.toInt()
                } else {
                    actualHeight = reqHeight.toInt()
                    actualWidth = reqWidth.toInt()
                }
            }

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight)
            options.inJustDecodeBounds = false
            options.inTempStorage = ByteArray(16 * 1024)
            try {
                bmp = BitmapFactory.decodeFile(imageFile.absolutePath, options)
            } catch (exception: OutOfMemoryError) {
                exception.printStackTrace()
                XCamera.instance.cameraListener?.onFailure("Exception: ${exception.message}")
            }
            try {
                scaledBitmap =
                    Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.RGB_565)
            } catch (exception: OutOfMemoryError) {
                exception.printStackTrace()
                XCamera.instance.cameraListener?.onFailure(exception.message)
            }
            val ratioX = actualWidth / options.outWidth.toFloat()
            val ratioY = actualHeight / options.outHeight.toFloat()
            val middleX = actualWidth / 2.0f
            val middleY = actualHeight / 2.0f
            val scaleMatrix = Matrix()
            scaleMatrix.setScale(ratioX, ratioY, middleX, middleY)
            val canvas = Canvas(scaledBitmap!!)
            canvas.setMatrix(scaleMatrix)
            canvas.drawBitmap(
                bmp!!, middleX - bmp.width / 2,
                middleY - bmp.height / 2, Paint(Paint.FILTER_BITMAP_FLAG)
            )
            bmp.recycle()
            val exif: ExifInterface
            try {
                exif = ExifInterface(imageFile.absolutePath)
                val orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    0
                )
                val matrix = Matrix()
                if (orientation == 6) {
                    matrix.postRotate(90F)
                } else if (orientation == 3) {
                    matrix.postRotate(180F)
                } else if (orientation == 8) {
                    matrix.postRotate(270F)
                }
                scaledBitmap = Bitmap.createBitmap(
                    scaledBitmap, 0, 0, scaledBitmap.width,
                    scaledBitmap.height, matrix, true
                )
            } catch (e: IOException) {
                e.printStackTrace()
                XCamera.instance.cameraListener?.onFailure("Exception: ${e.message}")
            }
            return scaledBitmap
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