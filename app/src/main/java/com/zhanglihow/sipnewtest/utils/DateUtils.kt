package com.zhanglihow.sipnewtest.utils

import android.annotation.SuppressLint
import java.text.SimpleDateFormat


/**
 *
 * @author zhangli
 * @email zhanglihow@gmail.com
 * @time
 */
class DateUtils {
    companion object{
        fun timeParse(duration: Long): String {
            var time = ""
            if (duration > 1000) {
                time = timeParseMinute(duration)
            } else {
                val minute = duration / 60000
                val seconds = duration % 60000
                val second = Math.round(seconds.toFloat() / 1000).toLong()
                if (minute < 10) {
                    time += "0"
                }
                time += "$minute:"
                if (second < 10) {
                    time += "0"
                }
                time += second
            }
            return time
        }

        @SuppressLint("SimpleDateFormat")
        private fun timeParseMinute(duration: Long): String {
            return try {
                SimpleDateFormat("mm:ss").format(duration)
            } catch (e: Exception) {
                e.printStackTrace()
                "0:00"
            }

        }
    }

}