package com.zhanglihow.sipnewtest

import android.app.Application
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import com.orhanobut.logger.PrettyFormatStrategy
import net.gotev.sipservice.log.TyLog

/**
 *
 * @author zhangli
 * @email zhanglihow@gmail.com
 * @time
 */
open class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        initLog()
    }

    /**
     * 初始化log
     *
     * https://github.com/orhanobut/logger
     */
    private fun initLog() {
        val formatStrategy: PrettyFormatStrategy?
        formatStrategy = PrettyFormatStrategy.newBuilder()
                .showThreadInfo(false)  // 展示线程信息
                .methodCount(1)         // 展示调用的方法个数，默认是 2
                .methodOffset(1)        // 跳过堆栈中的方法个数， 默认是 0
                .tag("sip")   //  TAG 内容. 默认是 PRETTY_LOGGER
                .build()
        Logger.addLogAdapter(AndroidLogAdapter(formatStrategy))

        TyLog.opLog = BuildConfig.DEBUG
    }

}