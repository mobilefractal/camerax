package vn.fractal.camerax.utils

import android.content.res.Resources
import android.content.res.Resources.getSystem
import android.os.Handler
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import vn.fractal.camerax.XCamera

fun View.delay() {
    isEnabled = false
    Handler().postDelayed({ isEnabled = true }, 300)
}

fun View.delay(delayMillis: Long) {
    isEnabled = false
    Handler().postDelayed({ isEnabled = true }, delayMillis)
}

val Int.toPx: Int get() = (this * Resources.getSystem().displayMetrics.density).toInt()

val Int.toDp: Int get() = (this / getSystem().displayMetrics.density).toInt()

fun Fragment.findNavControllerSafely(): NavController? {
    return try {
        findNavController()
    } catch (e: Exception) {
        XCamera.instance.cameraListener?.onFailure(e.message)
        activity?.finish()
        null
    }
}