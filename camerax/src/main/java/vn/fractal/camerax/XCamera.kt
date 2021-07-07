package vn.fractal.camerax

import android.content.Context
import android.graphics.Bitmap


class XCamera private constructor() {
    private object Holder {
        val INSTANCE = XCamera()
    }

    var captions: List<Caption> = emptyList()
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
        captions: List<Caption> = emptyList(),
        compressFormat: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG,
        isEnableSound: Boolean? = false
    ) {
        CameraActivity.startCamera(context, isBack)
        this.cameraListener = listener
        this.imagePath = imagePath
        this.captions = captions
        this.compressFormat = compressFormat
        this.isEnableSound = isEnableSound
    }

    interface CameraListener {
        fun onSuccess(path: String?)
        fun onFailure(msg: String?)
    }

}