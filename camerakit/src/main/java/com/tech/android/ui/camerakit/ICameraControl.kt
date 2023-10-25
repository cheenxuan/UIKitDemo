package com.tech.android.ui.camerakit

import android.graphics.Rect
import android.view.View
import androidx.annotation.IntDef
import com.tech.android.ui.camerakit.view.CameraView
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @auther: QinjianXuan
 * @date  : 2023/10/19 .
 * <P>
 * Description: Android 5.0 相机的API发生很大的变化。些类屏蔽掉了 api的变化。相机的操作和功能，抽象剥离出来。
 * <P>
 */
interface ICameraControl {

    companion object {
        /**
         * 闪光灯关 [.setFlashMode]
         */
        const val FLASH_MODE_OFF = 0

        /**
         * 闪光灯开 [.setFlashMode]
         */
        const val FLASH_MODE_TORCH = 1

        /**
         * 闪光灯自动 [.setFlashMode]
         */
        const val FLASH_MODE_AUTO = 2
    }

    @IntDef(FLASH_MODE_TORCH, FLASH_MODE_OFF, FLASH_MODE_AUTO)
    annotation class FlashMode

    /**
     * 照相回调。
     */
    interface OnTakePictureCallback {
        fun onPictureTaken(data: ByteArray?)
    }

    /**
     * 设置本地质量控制回调，如果不设置则视为不扫描调用本地质量控制代码。
     */
    fun setDetectCallback(callback: OnDetectPictureCallback?)

    /**
     * 预览回调
     */
    interface OnDetectPictureCallback {
        fun onDetect(data: ByteArray, rotation: Int): Int
    }

    /**
     * 打开相机。
     */
    fun start()

    /**
     * 关闭相机
     */
    fun stop()

    fun pause()

    fun resume()

    /**
     * 相机对应的预览视图。
     * @return 预览视图
     */
    fun getDisplayView(): View?

    /**
     * 看到的预览可能不是照片的全部。返回预览视图的全貌。
     * @return 预览视图frame;
     */
    fun getPreviewFrame(): Rect?

    /**
     * 拍照。结果在回调中获取。
     * @param callback 拍照结果回调
     */
    fun takePicture(callback: OnTakePictureCallback)

    /**
     * 设置权限回调，当手机没有拍照权限时，可在回调中获取。
     * @param callback 权限回调
     */
    fun setPermissionCallback(callback: PermissionCallback?)

    /**
     * 设置水平方向
     * @param displayOrientation 参数值见 [com.baidu.ocr.ui.camera.CameraView.Orientation]
     */
    fun setDisplayOrientation(@CameraView.Orientation displayOrientation: Int)

    /**
     * 获取到拍照权限时，调用些函数以继续。
     */
    fun refreshPermission()

    /**
     * 设置闪光灯状态。
     * @param flashMode [.FLASH_MODE_TORCH]
     */
    fun setFlashMode(@FlashMode flashMode: Int)

    /**
     * 获取当前闪光灯状态
     * @return 当前闪光灯状态 参见 [.setFlashMode]
     */
    @FlashMode
    fun getFlashMode(): Int
}