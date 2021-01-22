package vn.fractal.camerax

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fragment_preview.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import vn.fractal.camerax.utils.Utils
import vn.fractal.camerax.utils.delay
import java.io.File

class PreviewFragment : Fragment() {
    private var path: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.onBackPressedDispatcher?.addCallback {
            File(path).delete()
            findNavController().navigate(R.id.cameraFragment)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_preview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args = arguments ?: return
        path = args.getString(CameraFragment.FILE_NAME_KEY)
        val resource = path?.let { File(it) }
        CoroutineScope(Main).launch {
            view_loading.visibility = View.VISIBLE
            resize(resource as File)
            Glide.with(view).load(resource).into(img_picture)
            view_loading.visibility = View.GONE
        }

        btn_cancel.setOnClickListener {
            it.delay()
            File(path).delete()
            findNavController().navigate(R.id.cameraFragment)
        }

        btn_save.setOnClickListener {
            it.delay()
            XCamera.instance.cameraListener?.onSuccess(path)
            XCamera.instance.cameraListener = null
            activity?.finish()
        }
    }

    private suspend fun resize(file: File) = withContext(IO) {
        Utils.resizeImage(file)
    }

}
