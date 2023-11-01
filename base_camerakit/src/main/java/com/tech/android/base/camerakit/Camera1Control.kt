package com.tech.android.base.camerakit

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.SurfaceTexture
import android.graphics.YuvImage
import android.hardware.Camera
import android.hardware.Camera.*
import android.view.TextureView
import android.view.TextureView.SurfaceTextureListener
import android.view.View
import android.widget.FrameLayout
import androidx.core.app.ActivityCompat
import com.tech.android.base.camerakit.CameraThreadPool.cancelAutoFocusTimer
import com.tech.android.base.camerakit.CameraThreadPool.createAutoFocusTimerTask
import com.tech.android.base.camerakit.CameraThreadPool.execute
import com.tech.android.base.camerakit.ICameraControl.Companion.FLASH_MODE_OFF
import com.tech.android.base.camerakit.ICameraControl.Companion.FLASH_MODE_TORCH
import com.tech.android.base.camerakit.ICameraControl.FlashMode
import com.tech.android.base.camerakit.ICameraControl.OnDetectPictureCallback
import com.tech.android.base.camerakit.view.CameraView
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean


/**
 * @auther: xuan
 * @date  : 2023/10/19 .
 * <P>
 * Description:
 * <P>
 */
class Camera1Control(val context: Context) : ICameraControl {

    private var displayOrientation = 0
    private var cameraId = 0
    private var flashMode = 0
    private val takingPicture = AtomicBoolean(false)
    private var camera: Camera? = null

    private var parameters: Parameters? = null
    private var permissionCallback: PermissionCallback? = null
    private val previewFrame = Rect()

    private var previewView = PreviewView(context)
    private var displayView: View? = null
    private var rotation = 0
    private var detectCallback: OnDetectPictureCallback? = null
    private var previewFrameCount = 0
    private var optSize: Size? = null

    /*
     * 非扫描模式
     */
    private val MODEL_NOSCAN = 0

    /*
     * 本地质量控制扫描模式
     */
    private val MODEL_SCAN = 1

    private var detectType = MODEL_NOSCAN

    private var surfaceCache: SurfaceTexture? = null

    private var buffer: ByteArray? = null

    private val surfaceTextureListener: SurfaceTextureListener = object : SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
            surfaceCache = surface
            initCamera()
        }

        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
            opPreviewSize(previewView.width, previewView.height)
            startPreview(false)
            setPreviewCallbackImpl()
        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
            return false
        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
            setPreviewCallbackImpl()
        }
    }

    init {
        openCamera()
    }

    private fun openCamera() {
        setupDisplayView()
    }

    private fun setupDisplayView() {
        val textureView = TextureView(context)
        textureView.surfaceTextureListener = surfaceTextureListener
        previewView.setTextureView(textureView)
        displayView = previewView
    }

    private fun getCameraRotation(): Int {
        return rotation
    }

    override fun setDetectCallback(callback: OnDetectPictureCallback?) {
        detectType = MODEL_SCAN
        detectCallback = callback
    }

    private fun onRequestDetect(data: ByteArray?) {
        // 相机已经关闭
        if (camera == null
            || data == null
            || optSize == null
        ) {
            return
        }
        val img = YuvImage(data, ImageFormat.NV21, optSize!!.width, optSize!!.height, null)
        var os: ByteArrayOutputStream? = null
        try {
            os = ByteArrayOutputStream(data.size)
            img.compressToJpeg(Rect(0, 0, optSize!!.width, optSize!!.height), 80, os)
            val jpeg = os.toByteArray()
            val status = detectCallback!!.onDetect(jpeg, getCameraRotation())
            if (status == 0) {
                clearPreviewCallback()
            }
        } catch (e: OutOfMemoryError) {
            // 内存溢出则取消当次操作
        } finally {
            try {
                os!!.close()
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    override fun setDisplayOrientation(@CameraView.Orientation displayOrientation: Int) {
        this.displayOrientation = displayOrientation
        rotation = when (displayOrientation) {
            CameraView.ORIENTATION_PORTRAIT -> 90
            CameraView.ORIENTATION_HORIZONTAL -> 0
            CameraView.ORIENTATION_INVERT -> 180
            else -> 0
        }
        previewView.requestLayout()
    }

    /**
     * {@inheritDoc}
     */
    override fun refreshPermission() {
        startPreview(true)
    }

    /**
     * {@inheritDoc}
     */
    override fun setFlashMode(@FlashMode flashMode: Int) {
        if (this.flashMode == flashMode) {
            return
        }
        this.flashMode = flashMode
        updateFlashMode(flashMode)
    }

    override fun getFlashMode(): Int {
        return flashMode
    }

    override fun start() {
        startPreview(false)
    }

    override fun stop() {
        if (camera != null) {
            camera!!.setPreviewCallback(null)
            stopPreview()
            // 避免同步代码，为了先设置null后release
            val tempC = camera
            camera = null
            tempC?.release()
            camera = null
            buffer = null
        }
    }

    private fun stopPreview() {
        if (camera != null) {
            camera!!.stopPreview()
        }
    }

    override fun pause() {
        if (camera != null) {
            stopPreview()
        }
        setFlashMode(FLASH_MODE_OFF)
    }

    override fun resume() {
        takingPicture.set(false)
        if (camera == null) {
            openCamera()
        } else {
            previewView.getTextureView()!!.surfaceTextureListener = surfaceTextureListener
            if (previewView.getTextureView()!!.isAvailable) {
                startPreview(false)
            }
        }
    }

    override fun getDisplayView(): View? {
        return displayView
    }

    override fun takePicture(onTakePictureCallback: ICameraControl.OnTakePictureCallback) {
        if (takingPicture.get()) {
            return
        }
        when (displayOrientation) {
            CameraView.ORIENTATION_PORTRAIT -> parameters?.setRotation(90)
            CameraView.ORIENTATION_HORIZONTAL -> parameters?.setRotation(0)
            CameraView.ORIENTATION_INVERT -> parameters?.setRotation(180)
        }
        try {
            val picSize = getOptimalSize(camera!!.parameters.supportedPictureSizes)
            parameters!!.setPictureSize(picSize.width, picSize.height)
            camera!!.parameters = parameters
            takingPicture.set(true)
            cancelAutoFocus()
            execute {
                camera!!.takePicture(
                    null, null
                ) { data, camera ->
                    startPreview(false)
                    takingPicture.set(false)
                    onTakePictureCallback.onPictureTaken(data)
                }
            }
        } catch (e: RuntimeException) {
            e.printStackTrace()
            startPreview(false)
            takingPicture.set(false)
        }
    }

    override fun setPermissionCallback(callback: PermissionCallback?) {
        permissionCallback = callback
    }

    private fun setPreviewCallbackImpl() {
        if (buffer == null) {
            buffer = ByteArray(
                (displayView!!.width
                        * displayView!!.height * ImageFormat.getBitsPerPixel(ImageFormat.NV21)) / 8
            )
        }
        if (camera != null && detectType == MODEL_SCAN) {
            camera!!.addCallbackBuffer(buffer)
            camera!!.setPreviewCallback(previewCallback)
        }
    }

    private fun clearPreviewCallback() {
        if (camera != null && detectType == MODEL_SCAN) {
            camera!!.setPreviewCallback(null)
            stopPreview()
        }
    }

    var previewCallback = PreviewCallback { data, camera -> // 扫描成功阻止打开新线程处理
        // 节流
        if (previewFrameCount++ % 5 != 0) {
            return@PreviewCallback
        }

        // 在某些机型和某项项目中，某些帧的data的数据不符合nv21的格式，需要过滤，否则后续处理会导致crash
        if (data.size.toDouble() != parameters!!.previewSize.width * parameters!!.previewSize.height * 1.5) {
            return@PreviewCallback
        }
        camera.addCallbackBuffer(buffer)
        execute { onRequestDetect(data) }
    }


    private fun initCamera() {
        try {
            if (camera == null) {
                val cameraInfo = CameraInfo()
                for (i in 0 until getNumberOfCameras()) {
                    getCameraInfo(i, cameraInfo)
                    if (cameraInfo.facing == CameraInfo.CAMERA_FACING_BACK) {
                        cameraId = i
                    }
                }
                camera = try {
                    open(cameraId)
                } catch (e: Throwable) {
                    e.printStackTrace()
                    startPreview(true)
                    return
                }
            }
            if (parameters == null) {
                parameters = camera!!.parameters
                parameters?.setPreviewFormat(ImageFormat.NV21)
            }
            opPreviewSize(previewView!!.width, previewView!!.height)
            camera!!.setPreviewTexture(surfaceCache)
            setPreviewCallbackImpl()
            startPreview(false)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    // 开启预览
    private fun startPreview(checkPermission: Boolean) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (checkPermission && permissionCallback != null) {
                permissionCallback?.onRequestPermission()
            }
            return
        }
        if (camera == null) {
            initCamera()
        } else {
            camera?.startPreview()
            startAutoFocus()
        }
    }

    private fun cancelAutoFocus() {
        camera!!.cancelAutoFocus()
        cancelAutoFocusTimer()
    }

    private fun startAutoFocus() {
        createAutoFocusTimerTask {
            synchronized(this@Camera1Control) {
                if (camera != null && !takingPicture.get()) {
                    try {
                        camera!!.autoFocus { success, camera -> }
                    } catch (e: Throwable) {
                        // startPreview是异步实现，可能在某些机器上前几次调用会autofocus failß
                    }
                }
            }
        }
    }

    private fun opPreviewSize(width: Int, height: Int) {
        if (parameters != null
            && camera != null
            && width > 0
        ) {
            optSize = getOptimalSize(camera!!.parameters.supportedPreviewSizes)
            parameters!!.setPreviewSize(optSize!!.width, optSize!!.height)
            previewView!!.setRatio(1.0f * optSize!!.width / optSize!!.height)
            camera!!.setDisplayOrientation(getSurfaceOrientation())
            stopPreview()
            try {
                camera!!.parameters = parameters
            } catch (e: RuntimeException) {
                e.printStackTrace()
            }
        }
    }

    private fun getOptimalSize(sizes: List<Size>): Size {
        val width = previewView!!.getTextureView()!!.width
        val height = previewView!!.getTextureView()!!.height
        val pictureSize = sizes[0]
        val candidates: MutableList<Size> = ArrayList()
        for (size in sizes) {
            if (size.width >= width
                && size.height >= height && size.width * height == size.height * width
            ) {
                // 比例相同
                candidates.add(size)
            } else if (size.height >= width
                && size.width >= height && size.width * width == size.height * height
            ) {
                // 反比例
                candidates.add(size)
            }
        }
        if (!candidates.isEmpty()) {
            return Collections.min(candidates, sizeComparator)
        }
        for (size in sizes) {
            if (size.width > width && size.height > height) {
                return size
            }
        }
        return pictureSize
    }

    private val sizeComparator: Comparator<Size> = object : Comparator<Size> {
        override fun compare(lhs: Size, rhs: Size): Int {
            val result = (lhs.width.toLong() * lhs.height) - (rhs.width.toLong() * rhs.height)
            return if (result > 0) 1
            else if (result < 0) -1
            else 0
        }
    }

    private fun updateFlashMode(flashMode: Int) {
        when (flashMode) {
            FLASH_MODE_TORCH -> parameters!!.flashMode = Parameters.FLASH_MODE_TORCH
            FLASH_MODE_OFF -> parameters!!.flashMode = Parameters.FLASH_MODE_OFF
            ICameraControl.FLASH_MODE_AUTO -> parameters!!.flashMode = Parameters.FLASH_MODE_AUTO
            else -> parameters!!.flashMode = Parameters.FLASH_MODE_AUTO
        }
        camera!!.parameters = parameters
    }

    private fun getSurfaceOrientation(): Int {
        return when (displayOrientation) {
            CameraView.ORIENTATION_PORTRAIT -> 90
            CameraView.ORIENTATION_HORIZONTAL -> 0
            CameraView.ORIENTATION_INVERT -> 180
            else -> 90
        }
    }

    /**
     * 有些相机匹配不到完美的比例。比如。我们的layout是4:3的。预览只有16:9
     * 的，如果直接显示图片会拉伸，变形。缩放的话，又有黑边。所以我们采取的策略
     * 是，等比例放大。这样预览的有一部分会超出屏幕。拍照后再进行裁剪处理。
     */
    inner class PreviewView(context: Context?) : FrameLayout(context!!) {
        private var textureView: TextureView? = null
        private var ratio = 0.75f
        fun setTextureView(textureView: TextureView?) {
            this.textureView = textureView
            removeAllViews()
            addView(textureView)
        }

        fun getTextureView(): TextureView? {
            return this.textureView
        }

        fun setRatio(ratio: Float) {
            this.ratio = ratio
            requestLayout()
            relayout(width, height)
        }

        override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
            super.onSizeChanged(w, h, oldw, oldh)
            relayout(w, h)
        }

        private fun relayout(w: Int, h: Int) {
            var width = w
            var height = h
            if (w < h) {
                // 垂直模式，高度固定。
                height = (width * ratio).toInt()
            } else {
                // 水平模式，宽度固定。
                width = (height * ratio).toInt()
            }
            val l = (getWidth() - width) / 2
            val t = (getHeight() - height) / 2
            previewFrame.left = l
            previewFrame.top = t
            previewFrame.right = l + width
            previewFrame.bottom = t + height
        }

        override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
            super.onLayout(changed, left, top, right, bottom)
            textureView?.layout(
                previewFrame.left,
                previewFrame.top,
                previewFrame.right,
                previewFrame.bottom
            )
        }
    }

    override fun getPreviewFrame(): Rect? {
        return previewFrame
    }
}