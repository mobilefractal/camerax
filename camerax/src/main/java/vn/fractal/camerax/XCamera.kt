package vn.fractal.camerax

import android.content.Context
import android.graphics.Bitmap


class XCamera private constructor() {
    private object Holder {
        val INSTANCE = XCamera()
    }

    var imageCaption: String? = null
    var cameraListener: CameraListener? = null
    var imagePath: String? = "CameraX"
    var compressFormat: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG
    var isEnableSound: Boolean? = false

    companion object {
        val instance: XCamera by lazy { Holder.INSTANCE }
    }

    fun startCamera(
        context: Context,
        listener: CameraListener,
        isBack: Boolean? = true,
        imagePath: String? = "CameraX",
        imageCaption: String? = null,
        compressFormat: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG,
        isEnableSound: Boolean? = false
    ) {
        CameraActivity.startCamera(context, isBack)
        this.cameraListener = listener
        this.imagePath = imagePath
        this.imageCaption = imageCaption
        this.compressFormat = compressFormat
        this.isEnableSound = isEnableSound
    }

    interface CameraListener {
        fun onSuccess(path: String?)
        fun onFailure(msg: String?)
    }

}