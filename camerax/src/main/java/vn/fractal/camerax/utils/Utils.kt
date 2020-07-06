package vn.fractal.camerax.utils

import android.graphics.*
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.os.Build
import androidx.exifinterface.media.ExifInterface
import vn.fractal.camerax.XCamera
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.roundToInt


class Utils {
    companion object {
        fun resizeImage(file: File) {
            BitmapFactory.Options().run {
                inJustDecodeBounds = false
                val bitmap = BitmapFactory.decodeFile(file.absolutePath, this)
                // Calculate inSampleSize
                inPreferredConfig = Bitmap.Config.RGB_565

                val rotateBitmap = rotateImage(file, bitmap) ?: return
                val scaleBitmap = scaleBitmap(rotateBitmap, 800) ?: return

                XCamera.instance.imageCaption?.let {
                    drawCaptionWithTime(scaleBitmap, it)
                }
                file.outputStream().use {
                    scaleBitmap.compress(Bitmap.CompressFormat.PNG, 80, it)
                    scaleBitmap.recycle()
                }
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


        private fun rotateImage(file: File, bitmap: Bitmap): Bitmap? {
            var rotate = 0
            val exif = ExifInterface(file.absolutePath)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_270 -> rotate = 270
                ExifInterface.ORIENTATION_ROTATE_180 -> rotate = 180
                ExifInterface.ORIENTATION_ROTATE_90 -> rotate = 90
            }
            val matrix = Matrix()
            matrix.postRotate(rotate.toFloat())
            return Bitmap.createBitmap(
                bitmap, 0, 0, bitmap.width,
                bitmap.height, matrix, true
            )
        }

        private fun scaleBitmap(
            imageBitmap: Bitmap,
            targetLength: Int
        ): Bitmap? {
            // Get the dimensions of the original bitmap
            val originalWidth = imageBitmap.width
            val originalHeight = imageBitmap.height
            var aspectRatio = originalWidth.toFloat() / originalHeight

            // Calculate the target dimensions
            val targetWidth: Int
            val targetHeight: Int
            if (originalWidth > originalHeight) {
                targetWidth = targetLength
                targetHeight = (targetWidth / aspectRatio).roundToInt()
            } else {
                aspectRatio = 1 / aspectRatio
                targetHeight = targetLength
                targetWidth = (targetHeight / aspectRatio).roundToInt()
            }
            return Bitmap.createScaledBitmap(imageBitmap, targetWidth, targetHeight, true)
        }

    }
}