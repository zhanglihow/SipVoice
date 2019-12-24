package com.zhanglihow.sipnewtest.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.zhanglihow.sipnewtest.Constants
import com.zhanglihow.sipnewtest.R
import com.zhanglihow.sipnewtest.sip_control.scAudioManager
import com.zhanglihow.sipnewtest.utils.DateUtils
import com.zhanglihow.sipnewtest.utils.ToastManage
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_call_in_layout.*
import net.gotev.sipservice.SipServiceCommand
import net.gotev.sipservice.event.CallConfirmedEvent
import net.gotev.sipservice.event.CallDisconnectEvent
import net.gotev.sipservice.log.TyLog
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.concurrent.TimeUnit


/**
 *
 * @author zhangli
 * @email zhanglihow@gmail.com
 * @time 2019/11/27 14:58
 */
class CallOutActivity : AppCompatActivity() {
    private val name by lazy {
        intent.getStringExtra("name")
    }
    private val scAudio by lazy {
        scAudioManager.getInstance()
    }

    //是否静音
    private var isMicOff = false
    //是否扩音器
    private var isVolumeOpen = false
    //计时器
    private var timeDispose: Disposable? = null
    //计时器
    private var timeOutDispose: Disposable? = null
    private var updateDispose: Disposable? = null

    private var startVoiceTime: Long = 0
    //是否接通
    private var isConfirmedCall = false
    private var callId = -1

    private var sipUrl: String = ""

    companion object {
        fun start(context: Context, name: String) {
            val intent = Intent(context, CallOutActivity::class.java)
            intent.putExtra("name", name)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_call_out_layout)
        EventBus.getDefault().register(this)
        loadView()
        loadData()
    }

    private fun loadView() {
        tv_name.text = name
        sipUrl = "sip:${name}@${Constants.SIP_DOMAIN}:${Constants.SIP_PORT}"
        tv_time.text = "正在呼叫"
        tv_sip.text = sipUrl

        circleImageInputCall.setOnClickListener {
            EventBus.getDefault().unregister(this)
            SipServiceCommand.hangUpActiveCalls(applicationContext, Constants.SipAccountID)
            TyLog.i("sip endCall")
            ToastManage.s(this@CallOutActivity, "已挂断")
            finish()
        }
        imgControlMic.setOnClickListener {
            if (!isConfirmedCall) {
                return@setOnClickListener
            }
            isMicOff = !isMicOff
            imgControlMic.isSelected = isMicOff
            SipServiceCommand.setCallMute(
                applicationContext,
                Constants.SipAccountID,
                callId,
                isMicOff
            )
        }
        imgControlVolume.setOnClickListener {
            isVolumeOpen = !isVolumeOpen
            imgControlVolume.isSelected = isVolumeOpen
            scAudio.setSpeakerMode(isVolumeOpen)
        }
    }

    private fun loadData() {
        scAudio.initialise(this)

        scAudio.muteMicrophone(isMicOff)
        scAudio.setSpeakerMode(isVolumeOpen)

        SipServiceCommand.makeCall(applicationContext, Constants.SipAccountID, sipUrl)

        //超时未响应
        timeOutDispose = Observable.interval(0, 1000, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if (it > 15) {
                    EventBus.getDefault().unregister(this)

                    SipServiceCommand.hangUpActiveCalls(applicationContext, Constants.SipAccountID)

                    ToastManage.s(applicationContext, "对方未响应")
                    finish()
                }
            }
    }

    /**
     * 挂断电话
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun disconnectCall(event: CallDisconnectEvent) {
        ToastManage.s(applicationContext, "对方已挂断")
        finish()
    }


    /**
     * 对方接听电话
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun confirmedCall(event: CallConfirmedEvent) {
        isConfirmedCall = true
        callId = event.callId
        timeOutDispose?.dispose()
        startVoiceTime = System.currentTimeMillis()
        getCall()
    }

    /**
     * 接听电话后计时
     */
    private fun getCall() {
        timeDispose = Observable.interval(0, 1000, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                tv_time.text = DateUtils.timeParse(it * 1000)
            }
    }

    //屏蔽返回键
    override fun onBackPressed() {
        // super.onBackPressed();
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        timeDispose?.dispose()
        timeOutDispose?.dispose()
        updateDispose?.dispose()
    }
}
