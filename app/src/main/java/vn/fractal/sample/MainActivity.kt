package vn.fractal.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import vn.fractal.camerax.Caption
import vn.fractal.camerax.XCamera

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        XCamera.instance.startCamera(
            this, object : XCamera.CameraListener {
                override fun onSuccess(path: String?) {
                }

                override fun onFailure(msg: String?) {
                }
            },
            isEnableSound = true,
            captions = listOf(
                Caption("Caption 1"),
                Caption("Caption 2"),
                Caption("Caption 3")
            )
        )
    }
}