package vn.fractal.camerax

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import java.io.File

private const val INTENT_IS_BACK = "intent_is_back"

class CameraActivity : AppCompatActivity() {
    private var isBack: Boolean? = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isBack = intent?.getBooleanExtra(INTENT_IS_BACK, true)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_camera)
    }

    companion object {
        fun startCamera(context: Context, isBack: Boolean? = true) {
            val intent = Intent(context, CameraActivity::class.java)
            intent.putExtra(INTENT_IS_BACK, isBack)
            context.startActivity(intent)
        }

        /** Use external media if it is available, our app's file directory otherwise */
        fun getOutputDirectory(context: Context): File {
            val appContext = context.applicationContext
            val mediaDir = context.externalMediaDirs.firstOrNull()?.let {
                File(it, XCamera.instance.imagePath).apply { mkdirs() }
            }
            return if (mediaDir != null && mediaDir.exists())
                mediaDir else appContext.filesDir
        }
    }


    override fun onBackPressed() {
        if (isBack == true) {
            super.onBackPressed()
            finish()
        }
    }

}
