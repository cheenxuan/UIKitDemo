package com.tech.android.ui.camerakit

import java.util.*
import java.util.concurrent.Executors

/**
 * @auther: xuan
 * @date  : 2023/10/19 .
 * <P>
 * Description:
 * <P>
 */
object CameraThreadPool {
    var timerFocus: Timer? = null

    /*
     * 对焦频率
     */
    val cameraScanInterval: Long = 2000

    /*
     * 线程池大小
     */
    private val poolCount = Runtime.getRuntime().availableProcessors()

    private val fixedThreadPool = Executors.newFixedThreadPool(poolCount)

    /**
     * 给线程池添加任务
     * @param runnable 任务
     */
    fun execute(runnable: Runnable?) {
        fixedThreadPool.execute(runnable)
    }

    /**
     * 创建一个定时对焦的timer任务
     * @param runnable 对焦代码
     * @return Timer Timer对象，用来终止自动对焦
     */
    fun createAutoFocusTimerTask(runnable: Runnable): Timer? {
        if (timerFocus != null) {
            return timerFocus
        }
        timerFocus = Timer()
        val task: TimerTask = object : TimerTask() {
            override fun run() {
                runnable.run()
            }
        }
        timerFocus!!.scheduleAtFixedRate(task, 0, cameraScanInterval)
        return timerFocus
    }

    /**
     * 终止自动对焦任务，实际调用了cancel方法并且清空对象
     * 但是无法终止执行中的任务，需额外处理
     *
     */
    fun cancelAutoFocusTimer() {
        if (timerFocus != null) {
            timerFocus!!.cancel()
            timerFocus = null
        }
    }
}