package com.tech.android.base.uikitdemo.camera

import com.tech.android.base.camerakit.CameraActivity

class CoreCameraActivity : CameraActivity() {

    companion object {
        const val TAG = "Core_Camera_Activity"
    }

//    override fun makeFileSucc(contentType: String?, filePath: String?) {
//        super.makeFileSucc(contentType, filePath)
//        Log.d(TAG, "contentType = $contentType, filePath = $filePath")
//
//        recognitionCardInfo(contentType, filePath)
//    }

//    private fun recognitionCardInfo(contentType: String?, filePath: String?) {
//
//        if (filePath.isNullOrBlank()) {
//            Toast.makeText(this, "图片保存失败，请重试", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        //压缩
//        Utils.resize(
//            getOutputFile()?.absolutePath,
//            getOutputFile()?.absolutePath,
//            IMAGE_MAX_WIDTH,
//            IMAGE_MAX_HEIGHT
//        )
//
//        when (contentType) {
//            ID_CARD_RS, ID_CARD_WS, BANK_CARD -> {
//                
//            }
//            else -> {
//                //完成
//                val map = HashMap<String, String?>()
//                map.put(IMAGE_PATH, filePath)
//
//                Log.d(TAG, "ocr recognition result =  $map")
//                setRecResult(map)
//            }
//        }
//    }

    override fun makeFileSucc(contentType: String?, filePath: String?) {
        super.makeFileSucc(contentType, filePath)
        finish()
    }
}