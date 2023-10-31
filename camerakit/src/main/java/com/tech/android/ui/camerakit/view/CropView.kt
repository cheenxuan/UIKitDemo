package com.tech.android.ui.camerakit.view

import android.content.Context
import android.graphics.*
import android.media.ExifInterface
import android.util.AttributeSet
import android.view.*
import android.view.ScaleGestureDetector.OnScaleGestureListener
import com.tech.android.ui.camerakit.utils.ImageUtil
import java.io.IOException

/**
 * @auther: xuan
 * @date  : 2023/10/19 .
 * <P>
 * Description:
 * <P>
 */
class CropView : View {

    private var setMinimumScale = 0.2f
    private var maximumScale = 4.0f

    private val matrixArray = FloatArray(9)
    private val mMatrix = Matrix()
    private var bitmap: Bitmap? = null

    private var gestureDetector: GestureDetector? = null

    private var scaleGestureDetector: ScaleGestureDetector? = null
    private val onScaleGestureListener: OnScaleGestureListener = object : OnScaleGestureListener {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            scale(detector)
            return true
        }

        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            return true
        }

        override fun onScaleEnd(detector: ScaleGestureDetector) {
            val scale = detector.scaleFactor
            mMatrix.postScale(scale, scale)
            invalidate()
        }
    }

    constructor(context: Context) : this(context, null) {}

    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0) {}

    constructor(context: Context, attributeSet: AttributeSet?, defStyle: Int) : super(
        context,
        attributeSet,
        defStyle
    ) {
        init(context)
    }

    fun setFilePath(path: String?) {
        if (bitmap != null && bitmap?.isRecycled == false) {
            bitmap?.recycle()
        }
        if (path == null) {
            return
        }
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
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
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
        } catch (e: IOException) {
            e.printStackTrace()
            bitmap = original
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
        setBitmap(bitmap)
    }

    private fun setBitmap(bitmap: Bitmap?) {
        this.bitmap = bitmap
        mMatrix.reset()
        centerImage(width, height)
        rotation = 0
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        centerImage(w, h)
        invalidate()
    }

    fun crop(frame: Rect): Bitmap? {
        
//        val rectCrop = Rect(this.left,this.top,this.right,this.bottom)
        
        val scale = getScale()
        val src = floatArrayOf(frame.left.toFloat(), frame.top.toFloat())
        val desc = floatArrayOf(0f, 0f)
        val invertedMatrix = Matrix()
        mMatrix.invert(invertedMatrix)
        invertedMatrix.mapPoints(desc, src)
        val matrix = Matrix()
        val width = (frame.width() / scale).toInt()
        val height = (frame.height() / scale).toInt()
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        val canvas = Canvas(bitmap)
        val originalBitmap = this.bitmap
        matrix.postTranslate(-desc[0], -desc[1])
        canvas.drawBitmap(originalBitmap!!, matrix, null)
        return bitmap
    }

    fun setMinimumScale(setMinimumScale: Float) {
        this.setMinimumScale = setMinimumScale
    }

    fun setMaximumScale(maximumScale: Float) {
        this.maximumScale = maximumScale
    }


    private fun init(context: Context) {
        scaleGestureDetector = ScaleGestureDetector(context, onScaleGestureListener)
        gestureDetector = GestureDetector(context, object : GestureDetector.OnGestureListener {
            override fun onDown(e: MotionEvent): Boolean {
                return true
            }

            override fun onShowPress(e: MotionEvent) {}
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                return false
            }

            override fun onScroll(
                e1: MotionEvent,
                e2: MotionEvent,
                distanceX: Float,
                distanceY: Float,
            ): Boolean {
                translate(distanceX, distanceY)
                return true
            }

            override fun onLongPress(e: MotionEvent) {}
            override fun onFling(
                e1: MotionEvent,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float,
            ): Boolean {
                return false
            }
        })
    }

    var rotation = 0

    fun rotate(degrees: Int) {
        if (bitmap == null) {
            return
        }
        val matrix = Matrix()
        val dx = bitmap!!.width / 2
        val dy = bitmap!!.height / 2
        matrix.postTranslate(-dx.toFloat(), -dy.toFloat())
        matrix.postRotate(degrees.toFloat())
        matrix.postTranslate(dy.toFloat(), dx.toFloat())
        val scaledBitmap = bitmap
        val rotatedBitmap = Bitmap.createBitmap(
            scaledBitmap!!.height, scaledBitmap.width,
            Bitmap.Config.RGB_565
        )
        val canvas = Canvas(rotatedBitmap)
        canvas.drawBitmap(bitmap!!, matrix, null)
        bitmap!!.recycle()
        bitmap = rotatedBitmap
        centerImage(width, height)
        invalidate()
    }

    private fun translate(distanceX: Float, distanceY: Float) {
        var distanceX = distanceX
        var distanceY = distanceY
        mMatrix.getValues(matrixArray)
        val left = matrixArray[Matrix.MTRANS_X]
        val top = matrixArray[Matrix.MTRANS_Y]
        val bound = getRestrictedBound()
        if (bound != null) {
            val scale = getScale()
            val right = left + (bitmap!!.width / scale).toInt()
            val bottom = top + (bitmap!!.height / scale).toInt()
            if (left - distanceX > bound.left) {
                distanceX = left - bound.left
            }
            if (top - distanceY > bound.top) {
                distanceY = top - bound.top
            }
            if (distanceX > 0) {
                if (right - distanceX < bound.right) {
                    distanceX = right - bound.right
                }
            }
            if (distanceY > 0) {
                if (bottom - distanceY < bound.bottom) {
                    distanceY = bottom - bound.bottom
                }
            }
        }
        mMatrix.postTranslate(-distanceX, -distanceY)
        invalidate()
    }

    private fun scale(detector: ScaleGestureDetector) {
        var scale = detector.scaleFactor
        val currentScale = getScale()
        if (currentScale * scale < setMinimumScale) {
            scale = setMinimumScale / currentScale
        }
        if (currentScale * scale > maximumScale) {
            scale = maximumScale / currentScale
        }
        mMatrix.postScale(scale, scale, detector.focusX, detector.focusY)
        invalidate()
    }

    private fun centerImage(width: Int, height: Int) {
        if (width <= 0 || height <= 0 || bitmap == null) {
            return
        }
        val widthRatio = 1.0f * height / bitmap!!.height
        val heightRatio = 1.0f * width / bitmap!!.width
        val ratio = Math.min(widthRatio, heightRatio)
        val dx = ((width - bitmap!!.width) / 2).toFloat()
        val dy = ((height - bitmap!!.height) / 2).toFloat()
        mMatrix.setTranslate(0f, 0f)
        mMatrix.setScale(
            ratio,
            ratio,
            (bitmap!!.width / 2).toFloat(),
            (bitmap!!.height / 2).toFloat()
        )
        mMatrix.postTranslate(dx, dy)
        invalidate()
    }

    private fun getScale(): Float {
        mMatrix.getValues(matrixArray)
        var scale = matrixArray[Matrix.MSCALE_X]
        if (Math.abs(scale) <= 0.1) {
            scale = matrixArray[Matrix.MSKEW_X]
        }
        return Math.abs(scale)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (bitmap != null) {
            canvas.drawBitmap(bitmap!!, mMatrix, null)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        var result = scaleGestureDetector!!.onTouchEvent(event)
        result = gestureDetector!!.onTouchEvent(event) || result
        return result || super.onTouchEvent(event)
//        return super.onTouchEvent(event)
    }

    private var restrictBound: Rect? = null

    private fun getRestrictedBound(): Rect? {
        return restrictBound
    }

    fun setRestrictBound(rect: Rect?) {
        restrictBound = rect
    }
}