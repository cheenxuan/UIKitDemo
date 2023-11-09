package com.tech.android.base.uikitdemo.update

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.permissionx.guolindev.PermissionX
import com.tech.android.base.update.AppUpdate
import com.tech.android.base.update.interfaces.DownloadCallback
import com.tech.android.base.update.model.UpdateConfig
import com.umpay.linkageguest.R

class UpdateActivity : AppCompatActivity() {

    private val mDefaultExplainContent = "您必须同意 '应用内安装其他应用' 权限才能完成升级"
    private val mDefaultPositiveText = "确认"
    private val mDefaultNegativeText = "取消"

    //安装apk权限时版本判断条件
    private val installApkJudgeRule: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && Build.VERSION.SDK_INT < Build.VERSION_CODES.R

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update)

        val storeBtn = findViewById<Button>(R.id.btn_store_update)
        storeBtn.setOnClickListener {

            //url 
            //version
            //packageName
            //app升级 
            val arrayListOf = arrayListOf<String>("HUAWEI", "XIAOMI", "OPPO", "VIVO")
            AppUpdate.setUpdateConfig(
                UpdateConfig(
                    isUpdateFromStore = false,
                    targetBrandList = "",
                    apkUrl = "https://ldyscdn.unnipay.com/apk/liandongplus_release.apk",
                    filePath = applicationContext.cacheDir.absolutePath + "/pic/aaa.jpg",
                    notificationIconRes = R.mipmap.aabbcc
                )
            ).setUpdateCallback(object :DownloadCallback{
                override fun onProgressUpdate(progress: Int) {
                    storeBtn.setText("已下载 $progress")
                }

                override fun onDownloadCompleted(context: Context, filePath: String) {
                    storeBtn.setText("下载完成")
                }

                override fun onDownloadFailed(error: String?) {
                }

            })
            AppUpdate.update(this)
            //商店更新
//            appUpdate.launchAppDetail("com.umpay.linkageguest","com.sec.android.app.samsungapps")
            //apk更新
//            appUpdate.apkUpdate()
        }

        findViewById<Button>(R.id.btn_apk_update).setOnClickListener {
            if (installApkJudgeRule) {
                val isHasPermission = this.packageManager?.canRequestPackageInstalls() ?: false
                if (!isHasPermission) {
                    PermissionX.init(this)
                        .permissions(Manifest.permission.REQUEST_INSTALL_PACKAGES)
                        .onExplainRequestReason { scope, deniedlist ->
                            scope.showRequestReasonDialog(
                                deniedlist,
                                mDefaultExplainContent,
                                mDefaultPositiveText,
                                mDefaultNegativeText
                            )
                        }.request { allGranted, _, _ ->
                            if (!allGranted) {
                                return@request
                            }
                            //如同意权限了，再次调用此方法，达到继续刚才方法执行的目的
                            println("------------- agree install apk-----------------")
                        }
                }


            }

        }
    }
    
}