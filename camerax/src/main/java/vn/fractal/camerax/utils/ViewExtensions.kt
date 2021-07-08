package vn.fractal.camerax.utils

import android.content.res.Resources
import android.content.res.Resources.getSystem
import android.os.Handler
import android.view.View

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