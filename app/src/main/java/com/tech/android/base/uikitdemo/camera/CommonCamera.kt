package com.tech.android.base.uikitdemo.camera

import android.util.Log
import com.tech.android.base.camerakit.CameraActivity
import com.tech.android.base.camerakit.utils.ImageUtil


/**
 * @auther: xuan
 * @date  : 2023/11/1 .
 * <P>
 * Description:
 * <P>
 */
open class CommonCamera : CameraActivity() {

    override fun makeFileSucc(contentType: String?) {
        super.makeFileSucc(contentType)
        Log.d("CAMETA","contentType = $contentType, filePath = ${getOutputFile()?.absolutePath}")

        recognitionCardInfo(contentType)
    }

    private fun recognitionCardInfo(contentType: String?) {

        if (getOutputFile() == null) {
            showToast("图片保存失败，请重试")
            return
        }

        //压缩
        ImageUtil.resize(
            getOutputFile()?.absolutePath,
            getOutputFile()?.absolutePath,
            IMAGE_MAX_WIDTH,
            IMAGE_MAX_HEIGHT
        )

        when (contentType) {
            ID_CARD_RS, ID_CARD_WS, BANK_CARD -> {
                
            }
            else -> {
                //完成
                val map = HashMap<String, String?>()
                map.put(IMAGE_PATH, getOutputFile()?.absolutePath)

                Log.d("CAMETA","ocr recognition result =  $map")
                setRecResult(map)
            }
        }
    }

    

    fun handleException(throwable: Throwable) {
        hideLoading()
    }

//    fun handleData(contentType: String?, temp: CardInfo?) {
//        if (temp == null) return
//
//        val status = checkCardQuality(temp.image_status)
//
//        if ((ID_CARD_RS == contentType || ID_CARD_WS == contentType) && "normal" != status) {
//            showToast(status)
//            restart()
//            return
//        }
//
//        //先剪切
//        if (temp.card_location != null) {
//            cropIdCardImg(temp.card_location)
//        }
//        //再旋转
//        ImageUtil.changeFileRotate(temp.direction, getOutputFile())
//
//        val map = HashMap<String, String?>()
//        map.put(KEY_CONTENT_TYPE, contentType)
//        map.put(IMAGE_PATH, getOutputFile()?.absolutePath)
//        map.put(LOG_ID, temp.log_id)
//        map.put(ERROR_CODE, temp.error_code)
//        map.put(RISK_TYPE, temp.risk_type)
//        map.put(IMAGE_STATUS, temp.image_status)
//
//        if (temp.card_quality != null) {
//            getCardQuality(map, temp.card_quality)
//        }
//
//        if (temp.words_result != null) {
//            //人像面
//            if (ID_CARD_RS == contentType) {
//                map.put(ID_CARD_ADDRESS, temp.words_result.住址?.words)
//                map.put(ID_CARD_NAME, temp.words_result.姓名?.words)
//                map.put(ID_CARD_NO, temp.words_result.公民身份号码?.words)
//            } else if (ID_CARD_WS == contentType) {
//                map.put(ID_CARD_VAILD_START, temp.words_result.签发日期?.words)
//                map.put(ID_CARD_VAILD_END, temp.words_result.失效日期?.words)
//            }
//        }
//
//        if (temp.result != null) {
//            map.put(BANK_CARD_NO,
//                if (temp.result.bank_card_number.isNotBlank()) temp.result.bank_card_number.replace(
//                    " ",
//                    ""
//                ) else ""
//            )
//            map.put(BANK_NAME, temp.result.bank_name)
//            map.put(BANK_CARD_TYPE, getCardType(temp.result.bank_card_type))
//        }
//
//        Log.d("CAMETA","ocr recognition result =  $map")
//        setRecResult(map)
//    }

    private fun checkCardQuality(imageStatus: String?): String? {
        return when (imageStatus) {
            "normal" -> "normal"
            "reversed_side" -> "身份证正反面颠倒"
            "non_idcard" -> "上传的图片中不包含身份证"
            "blurred" -> "身份证模糊"
            "other_type_card" -> "其他类型证照"
            "over_exposure" -> "身份证关键字段反光或过曝"
            "over_dark" -> "身份证欠曝（亮度过低)"
            else -> "不能识别"
        }
    }

//    private fun cropIdCardImg(cardLocation: CardLocation?) {
//        if (cardLocation == null) return
//        ImageUtil.cropImg(
//            getOutputFile(),
//            cardLocation.top,
//            cardLocation.left,
//            cardLocation.width,
//            cardLocation.height
//        )
//    }

//    private fun getCardQuality(map: HashMap<String, String?>, cardQuality: CardQuality) {
//        map.put(IS_CLEAR, cardQuality.IsClear)//是否清晰
//        map.put(IS_COMPLETE, cardQuality.IsComplete)//是否边框/四角完整
//        map.put(IS_NO_COVER, cardQuality.IsNoCover)//是否头像、关键字段无遮挡/马赛克
//    }

    private fun getCardType(type: String): String {
        return when (type) {
            "1" -> "Debit"
            "2" -> "Credit"
            else -> "Unknown"
        }
    }

    override fun showLoading() {
        super.showLoading()
    }

    override fun hideLoading() {
        super.hideLoading()
    }

    override fun showToastMessage(message: String) {
        super.showToastMessage(message)
        showToast(message)
    }

    private fun showToast(msg: String?) {
        if (msg?.isNullOrBlank() == true) return
        Log.d("CAMETA",msg)
    }


}