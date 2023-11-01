package com.tech.android.base.camerakit

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import com.tech.android.base.camerakit.utils.ImageUtil
import com.tech.android.base.camerakit.view.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * @auther: xuan
 * @date  : 2023/10/19 .
 * <P>
 * Description:
 * <P>
 */
open class CameraActivity : FragmentActivity() {

    companion object {
        const val KEY_CONTENT_TYPE = "contentType"
        const val KEY_OUTPUT_FILE_PATH = "outputFilePath"

        const val CONTENT_TYPE_GENERAL = "TYPE_GENERAL"
        const val ID_CARD_RS = "ID_CARD_RS"
        const val ID_CARD_WS = "ID_CARD_WS"
        const val BANK_CARD = "BANK_CARD"
        const val TYPE_ALBUM = "TYPE_ALBUM"


        //result key
        const val KEY_REC_RESULT_MAP = "mapResult"
        const val LOG_ID = "log_id"
        const val ERROR_CODE = "error_code"
        const val DIRECTION = "direction"
        const val RISK_TYPE = "risk_type"
        const val IMAGE_STATUS = "image_status"
        const val ID_CARD_NAME = "name"
        const val ID_CARD_ADDRESS = "address"
        const val ID_CARD_NO = "idcardNo"
        const val ID_CARD_VAILD_START = "vaildStart"
        const val ID_CARD_VAILD_END = "vaildEnd"
        const val IMAGE_PATH = "iamgePath"
        const val IS_CLEAR = "IsClear"
        const val IS_COMPLETE = "IsComplete"
        const val IS_NO_COVER = "IsNoCover"

        const val BANK_CARD_NO = "bankCardNo"
        const val BANK_NAME = "bankName"
        const val BANK_CARD_TYPE = "bankCardType"


        const val REQUEST_CODE_PICK_IMAGE = 100
        const val PERMISSIONS_REQUEST_CAMERA = 800
        const val PERMISSIONS_EXTERNAL_STORAGE = 801

        const val FAST_CLICK_DELAY_TIME = 1000

        const val IMAGE_MAX_WIDTH = 2560
        const val IMAGE_MAX_HEIGHT = 2560
    }

    private var outputFile: File? = null
    private var contentType: String? = null
    private val handler = Handler(Looper.getMainLooper())
    private var takePictureContainer: CameraLayout? = null
    private var cropContainer: CameraLayout? = null
    private var confirmResultContainer: CameraLayout? = null
    private var lightButton: ImageView? = null
    private var cameraView: CameraView? = null
    private var idCardExamView: View? = null
    private var idCardBackExamView: View? = null
    private var bankCardExamView: View? = null
    private var displayImageView: ImageView? = null
    private var cropView: CropView? = null
    private var overlayView: FrameOverlayView? = null
    private var cropMaskView: MaskView? = null
    private var takePhotoBtn: ImageView? = null
    private val permissionCallback: PermissionCallback = object : PermissionCallback {
        override fun onRequestPermission(): Boolean {
            ActivityCompat.requestPermissions(
                this@CameraActivity, arrayOf(Manifest.permission.CAMERA),
                PERMISSIONS_REQUEST_CAMERA
            )
            return false
        }
    }

    private val albumButtonOnClickListener = View.OnClickListener {
        openAlbum()
    }

    private val lightButtonOnClickListener = View.OnClickListener {
        if (cameraView?.getCameraControl()?.getFlashMode() == ICameraControl.FLASH_MODE_OFF) {
            cameraView?.getCameraControl()?.setFlashMode(ICameraControl.FLASH_MODE_TORCH)
        } else {
            cameraView?.getCameraControl()?.setFlashMode(ICameraControl.FLASH_MODE_OFF)
        }
        updateFlashMode()
    }

    private val takeButtonOnClickListener =
        View.OnClickListener { cameraView?.takePicture(outputFile, takePictureCallback) }

//    private val autoTakePictureCallback: CameraView.OnTakePictureCallback =
//        object : CameraView.OnTakePictureCallback {
//            override fun onPictureTaken(bitmap: Bitmap?) {
//                CameraThreadPool.execute {
//                    try {
//                        val fileOutputStream: FileOutputStream =
//                            FileOutputStream(outputFile)
//                        bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
//                        bitmap.recycle()
//                        fileOutputStream.close()
//                    } catch (e: IOException) {
//                        e.printStackTrace()
//                    }
//                    val intent = Intent()
//                    intent.putExtra(KEY_CONTENT_TYPE, contentType)
//                    setResult(RESULT_OK, intent)
//                    finish()
//                }
//            }
//        }

    private val takePictureCallback: CameraView.OnTakePictureCallback =
        object : CameraView.OnTakePictureCallback {
            override fun onPictureTaken(bitmap: Bitmap?) {
                handler.post {
                    takePictureContainer?.visibility = View.INVISIBLE
//                    if (cropMaskView?.getMaskType() == MaskView.MASK_TYPE_NONE) {
//                        cropView?.setFilePath(outputFile?.getAbsolutePath())
//                        showCrop()
//                    } else if (cropMaskView?.getMaskType() == MaskView.MASK_TYPE_BANK_CARD) {
//                        cropView?.setFilePath(outputFile?.getAbsolutePath())
//                        cropMaskView?.setVisibility(View.INVISIBLE)
//                        overlayView?.setVisibility(View.VISIBLE)
//                        overlayView?.setTypeWide()
//                        showCrop()
//                    } else {
                    displayImageView?.setImageBitmap(bitmap)
                    showResultConfirm()
//                    }
                }
            }
        }

    private val cropCancelButtonListener = View.OnClickListener { // 释放 cropView中的bitmap;
        cropView?.setFilePath(null)
        showTakePicture()
    }

    private val cropConfirmButtonListener = View.OnClickListener {
        val rect: Rect? = cropMaskView?.getOriginFrameRect()
        val cropped: Bitmap? = cropView?.crop(rect!!)
        displayImageView?.setImageBitmap(cropped)
        cropAndConfirm()
    }

    private val confirmButtonOnClickListener = View.OnClickListener { doConfirmResult() }

    private val confirmCancelButtonOnClickListener = View.OnClickListener {
        if (TYPE_ALBUM != contentType) {
            //释放cropView中的bitmap;
            cropView?.setFilePath(null)
            showTakePicture()
        } else {
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    private val rotateButtonOnClickListener = View.OnClickListener { cropView?.rotate(90) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ck_activity_camera)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        takePictureContainer = findViewById(R.id.take_picture_container)
        idCardExamView = takePictureContainer?.findViewById<View>(R.id.id_card_exam_container)
        idCardBackExamView =
            takePictureContainer?.findViewById<View>(R.id.id_card_back_exam_container)
        bankCardExamView = takePictureContainer?.findViewById<View>(R.id.bank_card_exam_container)

        confirmResultContainer = findViewById(R.id.confirm_result_container)
        cameraView = findViewById(R.id.camera_view)
        cameraView?.getCameraControl()?.setPermissionCallback(permissionCallback)
        lightButton = findViewById(R.id.light_button)
        lightButton?.setOnClickListener(lightButtonOnClickListener)
        takePhotoBtn = findViewById(R.id.take_photo_button)
        takePhotoBtn?.setOnClickListener(takeButtonOnClickListener)

        findViewById<ImageView>(R.id.album_button).setOnClickListener(albumButtonOnClickListener)

        // confirm result;
        displayImageView = findViewById(R.id.display_image_view)
        confirmResultContainer!!.findViewById<ImageView>(R.id.confirm_button)
            .setOnClickListener(confirmButtonOnClickListener)
        confirmResultContainer!!.findViewById<ImageView>(R.id.cancel_button)
            .setOnClickListener(confirmCancelButtonOnClickListener)
        findViewById<ImageView>(R.id.rotate_button).setOnClickListener(rotateButtonOnClickListener)


        cropView = findViewById(R.id.crop_view)
        cropContainer = findViewById(R.id.crop_container)
        overlayView = findViewById(R.id.overlay_view)
        cropContainer!!.findViewById<ImageView>(R.id.confirm_button)
            .setOnClickListener(cropConfirmButtonListener)
        cropMaskView = cropContainer!!.findViewById(R.id.crop_mask_view)
        cropContainer!!.findViewById<ImageView>(R.id.cancel_button)
            .setOnClickListener(cropCancelButtonListener)

        setOrientation()
//        setOrientation(resources.configuration)
        initParams()
//        cameraView?.setAutoPictureCallback(autoTakePictureCallback)
    }

    private fun setOrientation() {
        takePictureContainer?.setOrientation(CameraLayout.ORIENTATION_HORIZONTAL)
        cameraView?.setOrientation(CameraView.ORIENTATION_PORTRAIT)
        cropContainer?.setOrientation(CameraLayout.ORIENTATION_HORIZONTAL)
        confirmResultContainer?.setOrientation(CameraLayout.ORIENTATION_HORIZONTAL)
    }
//    private  fun setOrientation(newConfig: Configuration) {
//        val rotation = windowManager.defaultDisplay.rotation
//        val orientation: Int
//        var cameraViewOrientation = CameraView.ORIENTATION_PORTRAIT
//        when (newConfig.orientation) {
//            Configuration.ORIENTATION_PORTRAIT -> {
//                cameraViewOrientation = CameraView.ORIENTATION_PORTRAIT
//                orientation = CameraLayout.ORIENTATION_PORTRAIT
//            }
//            Configuration.ORIENTATION_LANDSCAPE -> {
//                orientation = CameraLayout.ORIENTATION_HORIZONTAL
//                cameraViewOrientation =
//                    if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_90) {
//                        CameraView.ORIENTATION_HORIZONTAL
//                    } else {
//                        CameraView.ORIENTATION_INVERT
//                    }
//            }
//            else -> {
//                orientation = CameraLayout.ORIENTATION_PORTRAIT
//                cameraView!!.setOrientation(CameraView.ORIENTATION_PORTRAIT)
//            }
//        }
//        takePictureContainer!!.setOrientation(orientation)
//        cameraView!!.setOrientation(cameraViewOrientation)
//        cropContainer!!.setOrientation(orientation)
//        confirmResultContainer!!.setOrientation(orientation)
//    }

    private fun initParams() {
        val outputPath = intent.getStringExtra(KEY_OUTPUT_FILE_PATH)
        if (outputPath != null) {
            outputFile = File(outputPath)
        }
        contentType = intent.getStringExtra(KEY_CONTENT_TYPE)
        if (contentType == null) {
            contentType = CONTENT_TYPE_GENERAL
        }
        val maskType: Int
        when (contentType) {
            ID_CARD_RS -> {
                maskType = MaskView.MASK_TYPE_ID_CARD_FRONT
                overlayView?.visibility = View.INVISIBLE
                idCardExamView?.visibility = View.VISIBLE
            }
            ID_CARD_WS -> {
                maskType = MaskView.MASK_TYPE_ID_CARD_BACK
                overlayView?.visibility = View.INVISIBLE
                idCardBackExamView?.visibility = View.VISIBLE
            }
            BANK_CARD -> {
                maskType = MaskView.MASK_TYPE_BANK_CARD
                overlayView?.visibility = View.INVISIBLE
                bankCardExamView?.visibility = View.VISIBLE
            }
            TYPE_ALBUM, CONTENT_TYPE_GENERAL -> {
                maskType = MaskView.MASK_TYPE_NONE
                cropMaskView?.visibility = View.INVISIBLE
            }
            else -> {
                maskType = MaskView.MASK_TYPE_NONE
                cropMaskView?.visibility = View.INVISIBLE
            }
        }

        cameraView?.setMaskType(maskType, this)
        cropMaskView?.setMaskType(maskType)

        if (TYPE_ALBUM == contentType) {
            openAlbum()
        }
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onPause() {
        super.onPause()
        cameraView?.stop()
    }

    override fun onResume() {
        super.onResume()
        cameraView?.start()
    }

    private fun openAlbum() {
        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this@CameraActivity, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                PERMISSIONS_EXTERNAL_STORAGE
            )
            return
        }
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE)
    }

    private fun showTakePicture() {
        cameraView?.getCameraControl()?.resume()
        updateFlashMode()
        takePictureContainer?.visibility = View.VISIBLE
        confirmResultContainer?.visibility = View.INVISIBLE
        cropContainer?.visibility = View.INVISIBLE
    }

    private fun showCrop() {
        cameraView?.getCameraControl()?.pause()
        updateFlashMode()
        takePictureContainer?.visibility = View.INVISIBLE
        confirmResultContainer?.visibility = View.INVISIBLE
        cropContainer?.visibility = View.VISIBLE
    }

    private fun showResultConfirm() {
        cameraView?.getCameraControl()?.pause()
        updateFlashMode()
        takePictureContainer?.visibility = View.INVISIBLE
        confirmResultContainer?.visibility = View.VISIBLE
        cropContainer?.visibility = View.INVISIBLE
    }

    // take photo;
    private fun updateFlashMode() {
        val flashMode: Int? = cameraView?.getCameraControl()?.getFlashMode()
        if (flashMode == ICameraControl.FLASH_MODE_TORCH) {
            lightButton?.setImageResource(R.drawable.camera_light_on)
        } else {
            lightButton?.setImageResource(R.drawable.camera_light_off)
        }
    }

    private fun cropAndConfirm() {
        cameraView?.getCameraControl()?.pause()
        updateFlashMode()
        doConfirmResult()
    }

    private fun doConfirmResult() {
        showLoading()
        CameraThreadPool.execute {
            try {
                val fileOutputStream: FileOutputStream = FileOutputStream(outputFile)
                val bitmap = (displayImageView?.drawable as BitmapDrawable).bitmap
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
                fileOutputStream.close()

                makeFileSucc(contentType, outputFile?.absolutePath)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    open fun makeFileSucc(contentType: String?, filePath: String?) {

    }

    open fun showLoading() {

    }

    open fun hideLoading() {

    }

    open fun getOutputFile() = outputFile

    open fun setRecResult(resultArr: HashMap<String, String?>) {
        hideLoading()
        val intent = Intent()
        intent.putExtra(KEY_REC_RESULT_MAP, resultArr)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PICK_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    if (data != null) {
                        val uri: Uri = data.data ?: return
                        val bitmap = setFilePath(getRealPathFromURI(uri))
                        if (bitmap == null) {
                            handlePickImageFailed()
                        } else {
                            displayImageView?.setImageBitmap(bitmap)
                            showResultConfirm()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    handlePickImageFailed()
                }
            } else {
                handlePickImageFailed()
            }
        }
    }

    private fun handlePickImageFailed() {
        if (contentType != TYPE_ALBUM) {
            cameraView?.getCameraControl()?.resume()
        } else {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

    private fun getRealPathFromURI(contentURI: Uri?): String? {
        val result: String?
        var cursor: Cursor? = null
        try {
            cursor = contentResolver.query(contentURI!!, null, null, null, null)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        if (cursor == null) {
            result = contentURI!!.path
        } else {
            cursor.moveToFirst()
            val idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
            result = cursor.getString(idx)
            cursor.close()
        }
        return result
    }

    private fun setFilePath(path: String?): Bitmap? {
        if (path == null) {
            return null
        }
        var bitmap: Bitmap? = null
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        val original = BitmapFactory.decodeFile(path, options)
        try {
            val exif = ExifInterface(path)
            val rotation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            val matrix = Matrix()
            val rotationInDegrees: Int = ImageUtil.exifToDegrees(rotation)
            if (rotation.toFloat() != 0f) {
                matrix.preRotate(rotationInDegrees.toFloat())
            }

            // 图片太大会导致内存泄露，所以在显示前对图片进行裁剪。
            val maxPreviewImageSize = 2560
            var min = Math.min(options.outWidth, options.outHeight)
            min = Math.min(min, maxPreviewImageSize)
            val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val screenSize = Point()
            windowManager.defaultDisplay.getSize(screenSize)
            min = Math.min(min, screenSize.x * 2 / 3)
            options.inSampleSize = ImageUtil.calculateInSampleSize(options, min, min)
            options.inScaled = true
            options.inDensity = options.outWidth
            options.inTargetDensity = min * options.inSampleSize
            options.inPreferredConfig = Bitmap.Config.RGB_565
            options.inJustDecodeBounds = false
            bitmap = BitmapFactory.decodeFile(path, options)
            return bitmap
        } catch (e: IOException) {
            e.printStackTrace()
            return original
        } catch (e: NullPointerException) {
            e.printStackTrace()
            return null
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSIONS_REQUEST_CAMERA -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    cameraView?.getCameraControl()?.refreshPermission()
                } else {
                    showToastMessage(getString(R.string.camera_permission_required))
                }
            }
            PERMISSIONS_EXTERNAL_STORAGE -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openAlbum()
                } else {
                    showToastMessage(getString(R.string.read_storage_permission_required))
                }
            }
            else -> {}
        }
    }

    open fun showToastMessage(message: String) {

    }

    open fun restart() {
        hideLoading()
        handler.postDelayed({
            showTakePicture()
        }, 500)
    }

    /**
     * 做一些收尾工作
     */
    open fun doClear() {
        CameraThreadPool.cancelAutoFocusTimer()
    }

    override fun onDestroy() {
        super.onDestroy()
        this.doClear()
    }

}