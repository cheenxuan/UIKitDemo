package com.tech.android.base.uikitdemo

import android.app.Application
import com.tech.android.base.log.ConsolePrinter
import com.tech.android.base.log.LogConfig
import com.tech.android.base.log.LogManager
import com.umpay.linkageguest.BuildConfig

/**
 * @auther: xuan
 * @date  : 2023/11/21 .
 * <P>
 * Description:
 * <P>
 */
class DemoApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initVLog()
    }

    private fun initVLog() {
        LogManager.init(object : LogConfig() {
            override fun getGlobalTag(): String = "APP_VLOG"
            override fun stackTraceDepth(): Int = 1
            override fun enable(): Boolean = BuildConfig.DEBUG
        }, ConsolePrinter())
    }

}