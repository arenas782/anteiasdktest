package co.anteia.anteiasdk.ui.aidetection

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import co.anteia.anteiasdk.R
import co.anteia.anteiasdk.data.api.ApiHelper
import co.anteia.anteiasdk.data.api.DataProviderSingleton
import co.anteia.anteiasdk.data.api.RetrofitBuilder
import co.anteia.anteiasdk.data.dto.RefreshClientTokenRequest
import co.anteia.anteiasdk.databinding.FragmentDocumentBackDetectionBinding
import co.anteia.anteiasdk.utils.Status
import co.anteia.anteiasdk.utils.Utilities
import co.anteia.anteiasdk.viewModel.BaseViewModelFactory
import co.anteia.anteiasdk.viewModel.DetectionViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import retrofit2.HttpException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.roundToInt


class DocumentBackDetectionFragment : Fragment() {
    private lateinit var binding: FragmentDocumentBackDetectionBinding
    private lateinit var cameraExecutor: ExecutorService
    private val data = DataProviderSingleton.instance
    private var registrationID : String? = null
    private lateinit var analyzerUseCase: ImageAnalysis
    private var XOffset: Int = 0
    private var YOffset: Int = 0
    private var boxHeight: Int = 0
    private var boxWidth: Int = 0
    private lateinit var cameraProvider: ProcessCameraProvider
    private lateinit var viewModel: DetectionViewModel
    private lateinit var apiHelper : ApiHelper
    val handler = Handler(Looper.getMainLooper())
    private var runInterpreter: Boolean = true
    private val startedDetection = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this, BaseViewModelFactory(ApiHelper(RetrofitBuilder.apiService))).get(
            DetectionViewModel::class.java)

        cameraExecutor = Executors.newSingleThreadExecutor()
        if (allPermissionsGranted()) {
            startCamera()

        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }
        if (data.token == null){
            if (registrationID ==null){
                Utilities.closeActivity(requireActivity(), "Debes indicar el registration ID")
            }
            else{
                Log.d(TAG,"No hay token.. generando uno nuevo")
                CoroutineScope(Dispatchers.IO).launch{
                    var credentials = data.userName+":"+data.apiKey
                    credentials = credentials.toBase64()
                    try{
                        val newToken =  apiHelper.refreshClientToken(RefreshClientTokenRequest(registrationID),credentials)
                        withContext(Dispatchers.Main){
                            if (newToken.token !=null){
                                data.token = newToken.token
                                Log.d(TAG,"nuevotoken")
                                try{
                                    val matiToken = data.token?.let { apiHelper.matiInit(it) }
                                    if (matiToken  != null){
                                        runInterpreter = true
                                    }
                                }catch (e: HttpException){
                                    Utilities.closeActivity(
                                        requireActivity(),
                                        "Ha ocurrido un error refrescando el token ${e.code()}"
                                    )
                                }
                            }
                        }
                    }
                    catch (e: HttpException){
                        Utilities.closeActivity(
                            requireActivity(),
                            "Ha ocurrido un error refrescando el token ${e.code()}"
                        )
                    }
                }
            }
        }

    }

    private fun updateViewFinderSize() {
        binding.viewFinder.scaleType = PreviewView.ScaleType.FILL_CENTER
        val layoutParams = binding.rectangleLayout.layoutParams
        val displayMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
        var width = displayMetrics.widthPixels
        width = (width * 0.9).toInt()
        val newHeight = width*1.62
        layoutParams.height = newHeight.roundToInt()
        binding.rectangleLayout.layoutParams
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_document_back_detection, container, false
        )
        updateViewFinderSize()
        return binding.root
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireContext(), it
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()

            }
        }
    }
    private fun nextFragment() {
        handler.removeCallbacksAndMessages(null)
        cameraProvider.unbindAll()
        val transaction = parentFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_detection, FaceDetectionFragment())
        transaction.commit()

    }

    override fun onPause() {
        if (allPermissionsGranted())
            cameraProvider.unbindAll()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        startCamera()
    }

    private fun startCamera() {
        runInterpreter = true
        val analyzer = ImageAnalysis.Builder()
            .setTargetResolution(Size(1080, 1920))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
        analyzerUseCase = analyzer.apply {
            setAnalyzer(cameraExecutor, CustomAnalyzer())
        }

        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({

            cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, analyzerUseCase
                )
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }


    private inner class CustomAnalyzer() : ImageAnalysis.Analyzer {
        val bufferSize = 2 * java.lang.Float.SIZE / java.lang.Byte.SIZE

        val modelOutput = ByteBuffer.allocateDirect(bufferSize)
            .order(ByteOrder.nativeOrder())

        //instanciar modelo
        val tfliteModel = FileUtil.loadMappedFile(
            activity!!,
            "model_id.tflite"
        )

        val tflite : Interpreter = Interpreter(tfliteModel)


        @RequiresApi(Build.VERSION_CODES.R)
        @SuppressLint("UnsafeExperimentalUsageError", "UnsafeOptInUsageError")
        override fun analyze(image: ImageProxy) {
            val mediaImage = image.image
            if (runInterpreter) {
                if (mediaImage != null) {
                    handler.post {
                        val bitmap = binding.viewFinder.bitmap
                        if (bitmap != null) {
                            val croppedImage: Bitmap?
                            //recortar imagen
                            croppedImage = cropImage(bitmap)

                            //mostrar imagen recortada en la vista previa miniatura superior
                            binding.previewImageview.setImageBitmap(croppedImage)

                            //preparar imagen de input para el tensor
                            val scaledBitmap = Bitmap.createScaledBitmap(croppedImage, 224, 224, true)
                            var input = ByteBuffer.allocateDirect(224 * 224 * 3 * 4)
                                .order(ByteOrder.nativeOrder())
                            input = prepareInput(input,scaledBitmap)

                            //buffer de salida

                            tflite.run(input, modelOutput)

                            modelOutput.rewind()

                            val probabilities: FloatBuffer = modelOutput.asFloatBuffer()

                            if (probabilities.get(0)>0.6){
                                val date = Date()
                                val currentTimeMilli = date.time

                                val timeDiference = currentTimeMilli - globalTimeMilis
                                Log.e("Diference:", timeDiference.toString())
                                if (timeDiference >= TIME_TO_WAIT) {
                                    binding.rectangle.background =
                                        ContextCompat.getDrawable(
                                            requireContext(),
                                            R.drawable.rectangle_green
                                        )
                                    if (runInterpreter) {
                                        sendFrame(bitmap)
                                    }

                                } else {
                                    binding.rectangle.background = ContextCompat.getDrawable(
                                        requireContext(),
                                        R.drawable.rectangle_yellow
                                    )
                                }

                            }
                            else {
                                resetTimeCount()
                            }
                        }

                    }
                }

            } else {
                globalTimeDate = Date()
                globalTimeMilis = globalTimeDate.time
                try {
                    binding.rectangle.background =
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.rectangle_green
                        )
                } catch (e: Exception) {

                }

            }
            image.close()


        }
        /*
        *
        * calcular dimensiones del recuadro en la pantalla donde se debe colocar el objeto
        * */
        private fun calculateSize() {
            val height: Int = binding.rectangle.height
            val width: Int = binding.rectangle.width

            var diameter: Int
            diameter = width;
            if (height < width) {
                diameter = height;
            }
            val offset = (0.05 * diameter).toInt()
            diameter -= offset



            val rectf = Rect()
            binding.rectangle.getGlobalVisibleRect(rectf)


            XOffset = rectf.left
            YOffset = rectf.top

            boxHeight = rectf.height()
            boxWidth = rectf.width()

        }

        /*
        recortar imagen para reducir el area de deteccion antes de pasarla a ML Kit
        * */
        private fun cropImage(bitmap: Bitmap): Bitmap {
            calculateSize()
            val matrix = Matrix()
            matrix.postRotate(0f)
            return Bitmap.createBitmap(
                bitmap,
                0, 0, boxWidth, boxHeight, matrix, true
            )
        }
    }

    private fun resetTimeCount() {
        globalTimeDate = Date()
        globalTimeMilis = globalTimeDate.time
        binding.rectangle.background = ContextCompat.getDrawable(
            requireContext(),
            R.drawable.rectangle_red
        )
    }

    private fun sendFrame(croppedImage: Bitmap) {

        globalTimeDate = Date()
        globalTimeMilis = globalTimeDate.time
        runInterpreter = false
        binding.rectangle.background =
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.rectangle_green
            )

        viewModel.sendDocumentBack(croppedImage).observe(requireActivity(),{
            it?.let { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {
                        if (resource.data!!.success == true){
                            Log.e("response", resource.data.toString())
                            runInterpreter = false
                            nextFragment()
                        }
                        else{
                            runInterpreter = true
                            binding.instructionsRelativeLayout.visibility = View.VISIBLE
                            binding.animationView.visibility = View.INVISIBLE
                            Utilities.showSnackbar(requireActivity(),"Falló el reconocimiento")
                        }

                    }
                    Status.ERROR -> {
                        binding.instructionsRelativeLayout.visibility = View.VISIBLE
                        binding.animationView.visibility = View.INVISIBLE
                        Log.e(TAG,"error.. "+resource.message.toString())
                        runInterpreter = true
                        Utilities.showSnackbar(requireActivity(),resource.message.toString())
                    }
                    Status.LOADING -> {
                        binding.instructionsRelativeLayout.visibility = View.INVISIBLE
                        binding.animationView.visibility = View.VISIBLE
                        Log.e(TAG,"cargando..")
                        runInterpreter = false
                    }
                }
            }
        })
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        globalTimeDate = Date()
        globalTimeMilis = globalTimeDate.time

    }

    override fun onDetach() {
        super.onDetach()
        globalTimeDate = Date()
        globalTimeMilis = globalTimeDate.time
    }


    private fun prepareInput(input :ByteBuffer, scaledBitmap : Bitmap) : ByteBuffer{

        for (y in 0 until 224) {
            for (x in 0 until 224) {
                val px = scaledBitmap.getPixel(x, y)

                // Get channel values from the pixel value.
                val r = Color.red(px)
                val g = Color.green(px)
                val b = Color.blue(px)

                // Normalize channel values to [-1.0, 1.0]. This requirement depends on the model.
                // For example, some models might require values to be normalized to the range
                // [0.0, 1.0] instead.
                val rf = (r - 127) / 255f
                val gf = (g - 127) / 255f
                val bf = (b - 127) / 255f

                input.putFloat(rf)
                input.putFloat(gf)
                input.putFloat(bf)
            }
        }
        return input
    }

    companion object {
        private const val TAG = "CameraXFaceDetection"
        private var globalTimeDate = Date()
        private var globalTimeMilis = globalTimeDate.time
        private const val TIME_TO_WAIT = 3 * 1000 // 5 seconds

        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
    private fun String.toBase64(): String {
        return String(
            android.util.Base64.encode(this.toByteArray(), android.util.Base64.NO_WRAP),
            StandardCharsets.UTF_8
        )
    }

}
