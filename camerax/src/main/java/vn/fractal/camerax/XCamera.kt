package vn.fractal.camerax

import android.content.Context


class XCamera private constructor() {
    private object Holder {
        val INSTANCE = XCamera()
    }

    var imageCaption: String? = null
    var cameraListener: CameraListener? = null
    var imagePath: String? = "CameraX"

    companion object {
        val instance: XCamera by lazy { Holder.INSTANCE }
    }

    fun startCamera(
        context: Context,
        listener: CameraListener,
        isBack: Boolean? = true,
        imagePath: String? = "CameraX",
        imageCaption: String? = null
    ) {
        CameraActivity.startCamera(context, isBack)
        this.cameraListener = listener
        this.imagePath = imagePath
        this.imageCaption = imageCaption
    }

    interface CameraListener {
        fun onSuccess(path: String?)
        fun onFailure(msg: String?)
    }

}