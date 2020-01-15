package com.zhanglihow.sipnewtest.ui

import android.Manifest
import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import net.gotev.sipservice.BroadcastEventReceiver
import net.gotev.sipservice.SipAccountData
import net.gotev.sipservice.SipServiceCommand
import org.greenrobot.eventbus.ThreadMode
import org.greenrobot.eventbus.Subscribe
import androidx.appcompat.app.AlertDialog
import com.tbruyelle.rxpermissions2.RxPermissions
import com.zhanglihow.sipnewtest.Constants
import com.zhanglihow.sipnewtest.R
import com.zhanglihow.sipnewtest.sip_control.scAudioManager
import com.zhanglihow.sipnewtest.utils.ToastManage
import net.gotev.sipservice.event.CallComingEvent
import net.gotev.sipservice.event.CallOutEvent
import org.greenrobot.eventbus.EventBus


class MainActivity : AppCompatActivity(), View.OnClickListener {

    private val mBroadcastEventReceiver by lazy {
        BroadcastEventReceiver()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        EventBus.getDefault().register(this)

        getPermissions()

        initView()
        initSip()
    }

    @SuppressLint("CheckResult")
    private fun getPermissions() {
        RxPermissions(this)
            .request(Manifest.permission.RECORD_AUDIO)
            .subscribe {
                if(!it){
                    ToastManage.s(this,"need permissions")
                }else{
                    initAudio()
                }
            }
    }

    private fun initAudio(){
        val scAudio = scAudioManager.getInstance()
        scAudio.initialise(this)
    }

    private fun initSip() {
        mBroadcastEventReceiver.register(this)
        SipServiceCommand.setEncryption(this, true, "tived")
        SipServiceCommand.start(this)

    }

    private fun initView() {
        btn_call.setOnClickListener(this)
        btn_register.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_register -> {
                val user = SipAccountData()
                user.realm = "myvoipapp.com"
                user.host = Constants.SIP_DOMAIN
                user.username = edit_mine.text.toString()
                user.password = "100"
                user.contactUriParams = ""
                user.port = Constants.SIP_PORT
                Constants.SipAccountID = SipServiceCommand.setAccount(applicationContext, user)
            }
            R.id.btn_call -> {
                CallOutActivity.start(this, edit_call.text.toString())
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun callComing(event: CallComingEvent) {
        scAudioManager.getInstance().startRingtone()
        CallInActivity.start(application, event)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        SipServiceCommand.removeAccount(this, Constants.SipAccountID)
        SipServiceCommand.stop(this)
        mBroadcastEventReceiver.unregister(this)

    }

}
