package com.tech.android.base.camerakit.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.annotation.IntDef
import com.tech.android.base.camerakit.Camera1Control
import com.tech.android.base.camerakit.CameraThreadPool
import com.tech.android.base.camerakit.ICameraControl
import com.tech.android.base.camerakit.R
import com.tech.android.base.camerakit.utils.DimensUtil
import com.tech.android.base.camerakit.utils.ImageUtil
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * @auther: xuan
 * @date  : 2023/10/19 .
 * <P>
 * Description: 负责，相机的管理。同时提供，裁剪遮罩功能。
 * <P>
 */
class CameraView : FrameLayout {

    companion object {
        /**
         * 垂直方向 [.setOrientation]
         */
        const val ORIENTATION_PORTRAIT = 0

        /**
         * 水平方向 [.setOrientation]
         */
        const val ORIENTATION_HORIZONTAL = 90

        /**
         * 水平翻转方向 [.setOrientation]
         */
        const val ORIENTATION_INVERT = 270
    }


    private var maskType = 0

    @IntDef(ORIENTATION_PORTRAIT, ORIENTATION_HORIZONTAL, ORIENTATION_INVERT)
    annotation class Orientation

    private val cameraViewTakePictureCallback = CameraViewTakePictureCallback()

    private var cameraControl: ICameraControl? = null

    /**
     * 相机预览View
     */
    private var displayView: View? = null

    /**
     * 身份证，银行卡，等裁剪用的遮罩
     */
    private var maskView: MaskView? = null

    /**
     * 用于显示提示证 "请对齐身份证正面" 之类的背景
     */
    private var hintView: ImageView? = null

    /**
     * 用于显示提示证 "请对齐身份证正面" 之类的文字
     */
//    private var hintViewText: TextView? = null

    /**
     * 提示文案容器
     */
//    private var hintViewTextWrapper: LinearLayout? = null

//    private var autoPictureCallback: OnTakePictureCallback? = null

    /**
     * UI线程的handler
     */
//    private var uiHandler = Handler(Looper.getMainLooper())

    constructor(context: Context) : this(context, null) {}

    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0) {}

    constructor(context: Context, attributeSet: AttributeSet?, defStyle: Int) : super(
        context,
        attributeSet,
        defStyle
    ) {
        init()
    }

    private fun init() {
        cameraControl = Camera1Control(context)
        displayView = cameraControl?.getDisplayView()
        addView(displayView)

        maskView = MaskView(context)
        addView(maskView)

        hintView = ImageView(context)
        addView(hintView)
//
//        hintViewTextWrapper = LinearLayout(context)
//        hintViewTextWrapper!!.orientation = LinearLayout.VERTICAL
//        val lp = LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, DimensUtil.dpToPx(25))
//        lp.gravity = Gravity.CENTER
//        hintViewText = TextView(context)
//        hintViewText?.setBackgroundResource(R.drawable.camera_round_corner)
//        hintViewText?.alpha = 0.5f
//        hintViewText?.setPadding(DimensUtil.dpToPx(10), 0, DimensUtil.dpToPx(10), 0)
//        hintViewTextWrapper?.addView(hintViewText, lp)
//        hintViewText?.gravity = Gravity.CENTER
//        hintViewText?.setTextColor(Color.WHITE)
//        hintViewText?.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
//        hintViewText?.text = getScanMessage(-1)
//        addView(hintViewTextWrapper, lp)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        displayView?.layout(left, 0, right, bottom - top)
        maskView?.layout(left, 0, right, bottom - top)
        val hintViewWidth: Int = DimensUtil.dpToPx(250)
        val hintViewHeight: Int = DimensUtil.dpToPx(25)
        val hintViewLeft = (width - hintViewWidth) / 2
        val hintViewTop: Int = (maskView?.getFrameRect()?.bottom ?: 0) + DimensUtil.dpToPx(16)
//        hintViewTextWrapper?.layout(
//            hintViewLeft, hintViewTop,
//            hintViewLeft + hintViewWidth, hintViewTop + hintViewHeight
//        )
        hintView?.layout(
            hintViewLeft, hintViewTop,
            hintViewLeft + hintViewWidth, hintViewTop + hintViewHeight
        )
    }

    fun start() {
        cameraControl?.start()
        keepScreenOn = true
    }

    fun stop() {
        cameraControl?.stop()
        keepScreenOn = false
    }

    fun takePicture(file: File?, callback: OnTakePictureCallback) {
        cameraViewTakePictureCallback.file = file
        cameraViewTakePictureCallback.callback = callback
        cameraControl?.takePicture(cameraViewTakePictureCallback)
    }

    fun getCameraControl(): ICameraControl? {
        return cameraControl
    }

    fun setOrientation(@Orientation orientation: Int) {
        cameraControl?.setDisplayOrientation(orientation)
    }

//    fun setAutoPictureCallback(callback: OnTakePictureCallback?) {
//        autoPictureCallback = callback
//    }

    fun setMaskType(@MaskView.MaskType maskType: Int, ctx: Context?) {
        maskView?.setMaskType(maskType)
        maskView?.visibility = VISIBLE
        hintView?.visibility = VISIBLE
        var hintResourceId = R.drawable.camera_hint_align_id_card
        this.maskType = maskType
        var isNeedSetImage = false
        when (maskType) {
            MaskView.MASK_TYPE_ID_CARD_FRONT -> {
                hintResourceId = R.drawable.camera_hint_align_id_card
                isNeedSetImage = true
            }
            MaskView.MASK_TYPE_ID_CARD_BACK -> {
                hintResourceId = R.drawable.camera_hint_align_id_card_back
                isNeedSetImage = true
            }
            MaskView.MASK_TYPE_BANK_CARD -> {
                hintResourceId = R.drawable.camera_hint_align_bank_card
                isNeedSetImage = true
            }
            MaskView.MASK_TYPE_NONE -> {
                maskView?.visibility = INVISIBLE
                hintView?.visibility = INVISIBLE
            }
            else -> {
                maskView?.visibility = INVISIBLE
                hintView?.visibility = INVISIBLE
            }
        }
        if (isNeedSetImage) {
            hintView?.setImageResource(hintResourceId)
//            hintViewTextWrapper?.visibility = INVISIBLE
        }
//        if (maskType == MaskView.MASK_TYPE_ID_CARD_FRONT) {
//            cameraControl?.setDetectCallback(object : ICameraControl.OnDetectPictureCallback {
//                override fun onDetect(data: ByteArray, rotation: Int): Int {
//                    return detect(data, rotation)
//                }
//            })
//        }
//        if (maskType == MaskView.MASK_TYPE_ID_CARD_BACK) {
//            cameraControl?.setDetectCallback(object : ICameraControl.OnDetectPictureCallback {
//                override fun onDetect(data: ByteArray, rotation: Int): Int {
//                    return detect(data, rotation)
//                }
//            })
//        }
    }

//    private fun detect(data: ByteArray, rotation: Int): Int {
//        // 扫描成功阻止多余的操作
//        val previewFrame = cameraControl!!.getPreviewFrame()
//        if (maskView!!.width == 0 || maskView!!.height == 0 || previewFrame!!.width() == 0 || previewFrame.height() == 0) {
//            return 0
//        }
//
//        // BitmapRegionDecoder不会将整个图片加载到内存。
//        var decoder: BitmapRegionDecoder? = null
//        try {
//            decoder = BitmapRegionDecoder.newInstance(data, 0, data.size, true)
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
//        val width = if (rotation % 180 == 0) decoder!!.width else decoder!!.height
//        val height = if (rotation % 180 == 0) decoder.height else decoder.width
//        val frameRect = maskView!!.getFrameRectExtend()
//        var left = width * frameRect!!.left / maskView!!.width
//        var top = height * frameRect!!.top / maskView!!.height
//        var right = width * frameRect!!.right / maskView!!.width
//        var bottom = height * frameRect!!.bottom / maskView!!.height
//
//        // 高度大于图片
//        if (previewFrame.top < 0) {
//            // 宽度对齐。
//            val adjustedPreviewHeight = previewFrame.height() * getWidth() / previewFrame.width()
//            val topInFrame =
//                (adjustedPreviewHeight - frameRect!!.height()) / 2 * getWidth() / previewFrame.width()
//            val bottomInFrame =
//                ((adjustedPreviewHeight + frameRect!!.height()) / 2 * getWidth() / previewFrame.width())
//
//            // 等比例投射到照片当中。
//            top = topInFrame * height / previewFrame.height()
//            bottom = bottomInFrame * height / previewFrame.height()
//        } else {
//            // 宽度大于图片
//            if (previewFrame.left < 0) {
//                // 高度对齐
//                val adjustedPreviewWidth =
//                    previewFrame.width() * getHeight() / previewFrame.height()
//                val leftInFrame =
//                    ((adjustedPreviewWidth - maskView!!.getFrameRect()!!.width()) / 2 * getHeight() / previewFrame.height())
//                val rightInFrame =
//                    ((adjustedPreviewWidth + maskView!!.getFrameRect()!!.width()) / 2 * getHeight() / previewFrame.height())
//
//                // 等比例投射到照片当中。
//                left = leftInFrame * width / previewFrame.width()
//                right = rightInFrame * width / previewFrame.width()
//            }
//        }
//        val region = Rect()
//        region.left = left
//        region.top = top
//        region.right = right
//        region.bottom = bottom
//
//        // 90度或者270度旋转
//        if (rotation % 180 == 90) {
//            val x = decoder.width / 2
//            val y = decoder.height / 2
//            val rotatedWidth = region.height()
//            val rotated = region.width()
//
//            // 计算，裁剪框旋转后的坐标
//            region.left = x - rotatedWidth / 2
//            region.top = y - rotated / 2
//            region.right = x + rotatedWidth / 2
//            region.bottom = y + rotated / 2
//            region.sort()
//        }
//        val options = BitmapFactory.Options()
//
//        // 最大图片大小。
//        val maxPreviewImageSize = 2560
//        var size = Math.min(decoder.width, decoder.height)
//        size = Math.min(size, maxPreviewImageSize)
//        options.inSampleSize = ImageUtil.calculateInSampleSize(options, size, size)
//        options.inScaled = true
//        options.inDensity = Math.max(options.outWidth, options.outHeight)
//        options.inTargetDensity = size
//        options.inPreferredConfig = Bitmap.Config.RGB_565
//        var bitmap = decoder.decodeRegion(region, options)
//        if (rotation != 0) {
//            // 只能是裁剪完之后再旋转了。有没有别的更好的方案呢？
//            val matrix = Matrix()
//            matrix.postRotate(rotation.toFloat())
//            val rotatedBitmap = Bitmap.createBitmap(
//                bitmap, 0, 0, bitmap.width, bitmap.height, matrix, false
//            )
//            if (bitmap != rotatedBitmap) {
//                // 有时候 createBitmap会复用对象
//                bitmap.recycle()
//            }
//            bitmap = rotatedBitmap
//        }
//        
//        autoPictureCallback!!.onPictureTaken(bitmap)
//        
//        return 1
//    }

//    private fun getScanMessage(status: Int): String? {
//        val message: String = when (status) {
//            0 -> ""
//            2 -> "身份证模糊，请重新尝试"
//            3 -> "身份证反光，请重新尝试"
//            4 -> "请将身份证前后反转再进行识别"
//            5 -> "请拿稳镜头和身份证"
//            6 -> "请将镜头靠近身份证"
//            7 -> "请将身份证完整置于取景框内"
//            1 -> "请将身份证置于取景框内"
//            else -> "请将身份证置于取景框内"
//        }
//        return message
//    }

    /**
     * 拍摄后的照片。需要进行裁剪。有些手机（比如三星）不会对照片数据进行旋转，而是将旋转角度写入EXIF信息当中，
     * 所以需要做旋转处理。
     *
     * @param outputFile 写入照片的文件。
     * @param data  原始照片数据。
     * @param rotation   照片exif中的旋转角度。
     *
     * @return 裁剪好的bitmap。
     */
    private fun crop(outputFile: File, data: ByteArray, rotation: Int): Bitmap? {
        try {
            val previewFrame = cameraControl?.getPreviewFrame()
            if (maskView == null
                || maskView?.width == 0
                || maskView?.height == 0
                || previewFrame == null
                || previewFrame.width() == 0
                || previewFrame.height() == 0
            ) {
                return null
            }

            // BitmapRegionDecoder不会将整个图片加载到内存。
            val decoder = BitmapRegionDecoder.newInstance(data, 0, data.size, true)
            val width = if (rotation % 180 == 0) decoder.width else decoder.height
            val height = if (rotation % 180 == 0) decoder.height else decoder.width
            val frameRect = maskView!!.getFrameRect()

            var left = width * frameRect.left / maskView!!.width
            var top = height * frameRect.top / maskView!!.height
            var right = width * frameRect.right / maskView!!.width
            var bottom = height * frameRect.bottom / maskView!!.height

            // 高度大于图片 
            if (previewFrame.top < 0) {
                val scaleWidth = width.toFloat() / previewFrame.width().toFloat()
                val scaleHeight = getWidth().toFloat() / previewFrame.width().toFloat()
                
                val adjustedPreviewWidth = previewFrame.width().toFloat() * scaleWidth
                val adjustedPreviewHeight = previewFrame.height() * scaleHeight
                
                val scale = getHeight().toFloat() / previewFrame.height().toFloat()
                
                // 等比例投射到照片当中。
                if (scaleWidth != 1f) {
                    left =
                        ((adjustedPreviewWidth - frameRect.width() * scale * scaleWidth) / 2f).toInt()
                    right =
                        ((adjustedPreviewWidth + frameRect.width() * scale * scaleWidth) / 2f).toInt()
                }

                val topInFrame =
                    (adjustedPreviewHeight - frameRect.height()) / 2 * getWidth() / previewFrame.width()
                val bottomInFrame =
                    ((adjustedPreviewHeight + frameRect.height()) / 2 * getWidth() / previewFrame.width())
                // 等比例投射到照片当中。
                top = (topInFrame * height / previewFrame.height()).toInt()
                bottom = (bottomInFrame * height / previewFrame.height()).toInt()
            } else if (previewFrame.left < 0) {
                val scaleHeight = height.toFloat() / previewFrame.height().toFloat()
                val adjustedPreviewHeight = previewFrame.height().toFloat() * scaleHeight

                val scaleWidth = getHeight().toFloat() / previewFrame.height().toFloat()
                val scale = getWidth().toFloat() / previewFrame.width().toFloat()
              
                // 等比例投射到照片当中。
                if (scaleHeight != 1f) {
                    top = ((adjustedPreviewHeight - frameRect.height() * scale * scaleHeight) / 2f).toInt()
                    bottom = ((adjustedPreviewHeight + frameRect.height() * scale * scaleHeight) / 2f).toInt()
                }

                // 高度对齐
                val adjustedPreviewWidth = previewFrame.width() * scaleWidth
                val leftInFrame = (adjustedPreviewWidth - maskView!!.getFrameRect().width()) / 2 * scaleWidth
                val rightInFrame = (adjustedPreviewWidth + maskView!!.getFrameRect().width()) / 2 * scaleWidth

                // 等比例投射到照片当中。
                left = (leftInFrame * width / previewFrame.width()).toInt()
                right = (rightInFrame * width / previewFrame.width()).toInt()
            }

            // 高度大于图片
//            if (previewFrame.top < 0) {
//                // 宽度对齐。
//                val adjustedPreviewHeight =
//                    previewFrame.height() * getWidth() / previewFrame.width()
//                val topInFrame = (adjustedPreviewHeight - frameRect.height()) / 2 * getWidth() / previewFrame.width()
//                val bottomInFrame = ((adjustedPreviewHeight + frameRect.height()) / 2 * getWidth()
//                        / previewFrame.width())
//
//                // 等比例投射到照片当中。
//                top = topInFrame * height / previewFrame.height()
//                bottom = bottomInFrame * height / previewFrame.height()
//            } else {
//                // 宽度大于图片
//                if (previewFrame.left < 0) {
//                    // 高度对齐
//                    val adjustedPreviewWidth =
//                        previewFrame.width() * getHeight() / previewFrame.height()
//                    val leftInFrame = ((adjustedPreviewWidth - maskView!!.getFrameRect()
//                        .width()) / 2 * getHeight()
//                            / previewFrame.height())
//                    val rightInFrame = ((adjustedPreviewWidth + maskView!!.getFrameRect()
//                        .width()) / 2 * getHeight()
//                            / previewFrame.height())
//
//                    // 等比例投射到照片当中。
//                    left = leftInFrame * width / previewFrame.width()
//                    right = rightInFrame * width / previewFrame.width()
//                }
//            }

            val region = Rect()
            region.left = left
            region.top = top
            region.right = right
            region.bottom = bottom

            // 90度或者270度旋转
            if (rotation % 180 == 90) {
                val x = decoder.width / 2
                val y = decoder.height / 2
                val rotatedWidth = region.height()
                val rotated = region.width()

                // 计算，裁剪框旋转后的坐标
                region.left = x - rotatedWidth / 2
                region.top = y - rotated / 2
                region.right = x + rotatedWidth / 2
                region.bottom = y + rotated / 2
                region.sort()
            }
            val options = BitmapFactory.Options()

            // 最大图片大小。
            val maxPreviewImageSize = 2560
            var size = Math.min(decoder.width, decoder.height)
            size = Math.min(size, maxPreviewImageSize)
            options.inSampleSize = ImageUtil.calculateInSampleSize(options, size, size)
            options.inScaled = true
            options.inDensity = Math.max(options.outWidth, options.outHeight)
            options.inTargetDensity = size
            options.inPreferredConfig = Bitmap.Config.ARGB_8888
            var bitmap = decoder.decodeRegion(region, options)
            if (rotation != 0) {
                // 只能是裁剪完之后再旋转了。有没有别的更好的方案呢？
                val matrix = Matrix()
                matrix.postRotate(rotation.toFloat())
                val rotatedBitmap = Bitmap.createBitmap(
                    bitmap, 0, 0, bitmap.width, bitmap.height, matrix, false
                )
                if (bitmap != rotatedBitmap) {
                    // 有时候 createBitmap会复用对象
                    bitmap.recycle()
                }
                bitmap = rotatedBitmap
            }
            try {
                if (!outputFile.exists()) {
                    outputFile.createNewFile()
                }
                val fileOutputStream = FileOutputStream(outputFile)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fileOutputStream)
                fileOutputStream.flush()
                fileOutputStream.close()
                return bitmap
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    inner class CameraViewTakePictureCallback : ICameraControl.OnTakePictureCallback {
        var file: File? = null
        var callback: OnTakePictureCallback? = null

        override fun onPictureTaken(data: ByteArray?) {
            CameraThreadPool.execute {
                try {
                    if (file != null && data != null) {
                        val rotation: Int = ImageUtil.getOrientation(data)
                        val bitmap: Bitmap? = crop(file!!, data, rotation)
                        callback?.onPictureTaken(bitmap)
                    } else {
                        callback?.onPictureTaken(null)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    callback?.onPictureTaken(null)
                }
                
            }
        }
    }

    /**
     * 照相回调
     */
    interface OnTakePictureCallback {
        fun onPictureTaken(bitmap: Bitmap?)
    }
}