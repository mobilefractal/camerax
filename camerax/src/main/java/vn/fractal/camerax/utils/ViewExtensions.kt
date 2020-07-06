package vn.fractal.camerax.utils

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