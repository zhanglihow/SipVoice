package com.zhanglihow.sipnewtest.utils

import android.content.Context
import android.widget.Toast


/**
 *
 * @author zhangli
 * @email zhanglihow@gmail.com
 * @time
 */
class ToastManage {
    companion object{
        fun s(mContext: Context, s: String) {
            Toast.makeText(mContext.applicationContext, s, Toast.LENGTH_SHORT)
                .show()
        }
    }
}