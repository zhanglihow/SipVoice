package com.zhanglihow.mysip.sip_utils


/**
 *
 * @author zhangli
 * @email zhanglihow@gmail.com
 * @time
 */
interface CallbackContext {

    fun success(msg:String)
    fun error(msg:String)
}