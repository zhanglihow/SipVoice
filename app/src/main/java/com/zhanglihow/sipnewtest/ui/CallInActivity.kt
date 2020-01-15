package com.zhanglihow.sipnewtest.ui

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.os.Bundle
import android.os.Handler
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MotionEventCompat
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
import net.gotev.sipservice.event.CallComingEvent
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
class CallInActivity : AppCompatActivity() {

    private val mCallComingEvent by lazy {
        intent.getParcelableExtra("callData") as CallComingEvent
    }
    private val scAudio by lazy {
        scAudioManager.getInstance()
    }

    companion object {
        private const val BUBBLES_PER_SIDE = 3

        fun start(context: Context, event: CallComingEvent) {
            val intent = Intent(context, CallInActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra("callData", event)
            context.startActivity(intent)
        }
    }

    private lateinit var inputCallAnimation: Animation
    private lateinit var inputCallBackgroundAnimation: Animation

    private lateinit var hangUpBackgroundRevealAnimation: Animation
    private lateinit var pickUpBackgroundRevealAnimation: Animation

    private var lastTouchX: Float = 0f
    private var lastTouchY: Float = 0f

    private var initInputCallX: Float = 0f
    private var initInputCallY: Float = 0f

    private var screenWidth: Int = 0

    private var leftBubbles = mutableListOf<ImageView>()
    private var rightBubbles = mutableListOf<ImageView>()

    //是否静音
    private var isMicOff = false
    //是否扩音器
    private var isVolumeOpen = false
    //计时器
    private var timeDispose: Disposable? = null
    //打进电话的用户名
    private var callName: String? = null
    private var startVoiceTime: Long = 0
    //是否是主动拒听
    private var isDecline: Boolean = false
    private var updateDispose: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_call_in_layout)
        EventBus.getDefault().register(this)
        loadView()
        scAudio.setSpeakerMode(isVolumeOpen)
    }

    @SuppressLint("SetTextI18n")
    fun loadView() {
        tv_sip.text = "sip:" + mCallComingEvent.displayName + ":" + Constants.SIP_PORT
        // displayName=test129@25.30.9.134
        callName = mCallComingEvent.displayName
                .replace("@" + Constants.SIP_DOMAIN, "")
        tv_name.text =callName

        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        screenWidth = size.x

        // Initialize animations and listeners
        inputCallAnimation = AnimationUtils.loadAnimation(this, R.anim.call_input_anim)
        imageInputCall.startAnimation(inputCallAnimation)

        inputCallBackgroundAnimation =
                AnimationUtils.loadAnimation(this, R.anim.call_input_background_anim)
        circleImageInputCall.startAnimation(inputCallBackgroundAnimation)

        hangUpBackgroundRevealAnimation =
                AnimationUtils.loadAnimation(this, R.anim.call_reveal_anim)
        hangUpBackgroundRevealAnimation.setAnimationListener(hangUpAnimListener)

        pickUpBackgroundRevealAnimation =
                AnimationUtils.loadAnimation(this, R.anim.call_reveal_anim)
        pickUpBackgroundRevealAnimation.setAnimationListener(pickUpAnimListener)

        createLeftBubbles()
        createRightBubbles()

        setupLeftBubblesAnimations()
        setupRightBubblesAnimations()

        circleImageInputCall.setOnTouchListener(touchListener)
    }

    /**
     * 右侧的动效小圆圈
     */
    private fun setupRightBubblesAnimations() {
        for (i in 1..BUBBLES_PER_SIDE) {
            val rightBubble = rightBubbles[i - 1]

            val rightTranslation = ObjectAnimator.ofFloat(rightBubble, "translationX", 85f).apply {
                duration = 1000
                addListener(object : Animator.AnimatorListener {
                    override fun onAnimationRepeat(animation: Animator?) {}
                    override fun onAnimationEnd(animation: Animator?) {}
                    override fun onAnimationCancel(animation: Animator?) {}
                    override fun onAnimationStart(animation: Animator?) {
                        rightBubble.visibility = View.VISIBLE
                    }
                })
            }

            val translationAlpha = ObjectAnimator.ofFloat(rightBubble, "alpha", 0.5f).apply {
                duration = 1000
            }

            val scaleAlpha = ObjectAnimator.ofFloat(rightBubble, "alpha", 0f).apply {
                duration = 1000
                startDelay = 250
            }

            val translationScaleX = ObjectAnimator.ofFloat(rightBubble, "scaleX", 1.5f).apply {
                duration = 1000
            }

            val alphaScaleX = ObjectAnimator.ofFloat(rightBubble, "scaleX", 3f).apply {
                duration = 1000
                startDelay = 1000
            }

            val translationScaleY = ObjectAnimator.ofFloat(rightBubble, "scaleY", 1.5f).apply {
                duration = 1000
            }

            val alphaScaleY = ObjectAnimator.ofFloat(rightBubble, "scaleY", 3f).apply {
                duration = 1000
                startDelay = 1000
            }

            val animatorSet = AnimatorSet().apply {
                play(rightTranslation).with(translationScaleX).with(translationScaleY)
                        .with(translationAlpha)
                        .before(scaleAlpha).with(alphaScaleX)
                        .with(alphaScaleY)
                addListener(object : Animator.AnimatorListener {
                    override fun onAnimationRepeat(animation: Animator?) {}
                    override fun onAnimationCancel(animation: Animator?) {}
                    override fun onAnimationStart(animation: Animator?) {}
                    override fun onAnimationEnd(animation: Animator?) {
                        start()
                    }
                })
            }
            Handler().postDelayed({
                animatorSet.start()
            }, 400L * (i + 1))
        }
    }

    private fun createRightBubbles() {
        for (i in 1..BUBBLES_PER_SIDE) {
            val bubble = ImageView(this)

            val layoutParams = RelativeLayout.LayoutParams(15, 15)
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT)
            layoutParams.addRule(RelativeLayout.CENTER_VERTICAL)

            bubble.layoutParams = layoutParams
            bubble.setImageResource(R.drawable.circle_view_green)
            bubble.visibility = View.GONE

            layoutRightBubbles.addView(bubble)

            rightBubbles.add(bubble)
        }
    }

    /**
     * 左侧的动效小圆圈
     */
    private fun setupLeftBubblesAnimations() {

        for (i in 1..BUBBLES_PER_SIDE) {

            val leftBubble = leftBubbles[i - 1]
            val leftTranslation = ObjectAnimator.ofFloat(leftBubble, "translationX", -85f).apply {
                duration = 1000
                addListener(object : Animator.AnimatorListener {
                    override fun onAnimationRepeat(animation: Animator?) {}
                    override fun onAnimationEnd(animation: Animator?) {}
                    override fun onAnimationCancel(animation: Animator?) {}
                    override fun onAnimationStart(animation: Animator?) {
                        leftBubble.visibility = View.VISIBLE
                    }
                })
            }

            val translationAlpha =
                    ObjectAnimator.ofFloat(leftBubble, "alpha", 0.5f).apply { duration = 1000 }

            val scaleAlpha = ObjectAnimator.ofFloat(leftBubble, "alpha", 0f).apply {
                duration = 1000
                startDelay = 250
            }

            val translationScaleX = ObjectAnimator.ofFloat(leftBubble, "scaleX", 1.5f).apply {
                duration = 1000
            }

            val alphaScaleX = ObjectAnimator.ofFloat(leftBubble, "scaleX", 3f).apply {
                duration = 1000
                startDelay = 1000
            }

            val translationScaleY = ObjectAnimator.ofFloat(leftBubble, "scaleY", 1.5f).apply {
                duration = 1000
            }

            val alphaScaleY = ObjectAnimator.ofFloat(leftBubble, "scaleY", 3f).apply {
                duration = 1000
                startDelay = 1000
            }

            val animatorSet = AnimatorSet().apply {
                play(leftTranslation).with(translationScaleX).with(translationScaleY)
                        .with(translationAlpha)
                        .before(scaleAlpha).with(alphaScaleX)
                        .with(alphaScaleY)
                addListener(object : Animator.AnimatorListener {
                    override fun onAnimationRepeat(animation: Animator?) {}
                    override fun onAnimationCancel(animation: Animator?) {}
                    override fun onAnimationStart(animation: Animator?) {}
                    override fun onAnimationEnd(animation: Animator?) {
                        start()
                    }
                })
            }
            // pan
            Handler().postDelayed({
                animatorSet.start()
            }, 400L * (i + 1))

        }
    }

    /**
     * 左侧确认圈
     */
    private fun createLeftBubbles() {
        for (i in 1..BUBBLES_PER_SIDE) {
            val bubble = ImageView(this)
            val layoutParams = RelativeLayout.LayoutParams(15, 15)
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
            layoutParams.addRule(RelativeLayout.CENTER_VERTICAL)

            bubble.layoutParams = layoutParams
            bubble.setImageResource(R.drawable.circle_view_red)
            bubble.visibility = View.GONE

            layoutLeftBubbles.addView(bubble)

            leftBubbles.add(bubble)
        }
    }

    private var hangUpAnimListener = object : Animation.AnimationListener {
        override fun onAnimationRepeat(animation: Animation?) {
        }

        override fun onAnimationEnd(animation: Animation?) {
        }

        override fun onAnimationStart(animation: Animation?) {
            civBgHangUp.visibility = View.VISIBLE
        }
    }

    private var pickUpAnimListener = object : Animation.AnimationListener {
        override fun onAnimationRepeat(animation: Animation?) {
        }

        override fun onAnimationEnd(animation: Animation?) {
        }

        override fun onAnimationStart(animation: Animation?) {
            civBgPickUp.visibility = View.VISIBLE
        }
    }

    private var touchListener = View.OnTouchListener { v, ev ->
        val action = MotionEventCompat.getActionMasked(ev)

        val posX = ev.rawX
        val posY = ev.rawY

        when (action) {

            MotionEvent.ACTION_DOWN -> {

                // Retrieve input call position
                initInputCallX = v.x
                initInputCallY = v.y

                lastTouchX = posX
                lastTouchY = posY

                circleImageInputCall.clearAnimation()
                imageInputCall.clearAnimation()
                imageInputCall.visibility = View.GONE

                imageHangUp.drawable.setTint(resources.getColor(android.R.color.white))
                imagePickUp.drawable.setTint(resources.getColor(android.R.color.white))

                civBgHangUp.startAnimation(hangUpBackgroundRevealAnimation)
                civBgPickUp.startAnimation(pickUpBackgroundRevealAnimation)

                layoutLeftBubbles.visibility = View.GONE
                layoutRightBubbles.visibility = View.GONE
            }

            MotionEvent.ACTION_MOVE -> {
                // Move only on x axis
                v.x += posX - lastTouchX

                when {
                    v.x < screenWidth / 2 - v.width -> (v as ImageView).setImageResource(R.drawable.circle_view_red)
                    v.x > screenWidth / 2 -> (v as ImageView).setImageResource(R.drawable.circle_view_green)
                    else -> (v as ImageView).setImageResource(R.drawable.circle_view)
                }

                lastTouchX = posX
                lastTouchY = posY
            }

            MotionEvent.ACTION_UP -> {
                when {
                    v.x > layoutRightBubbles.x + layoutRightBubbles.width / 2 -> {
                        TyLog.i("sip 接听")
                        scAudioManager.getInstance().stopRingtone()
                        callViewGone(v)

                        SipServiceCommand.acceptIncomingCall(
                                applicationContext,
                                mCallComingEvent.accountID,
                                mCallComingEvent.callID)
                        startVoiceTime = System.currentTimeMillis()

                        getInCall(v)
                    }
                    v.x < layoutLeftBubbles.x + layoutLeftBubbles.width / 2 -> {
                        scAudioManager.getInstance().stopRingtone()
                        callViewGone(v)
                        isDecline = true
                        TyLog.i("sip 拒听")

                        SipServiceCommand.declineIncomingCall(
                                applicationContext,
                                mCallComingEvent.accountID,
                                mCallComingEvent.callID)

//                        SipServiceCommand.sendBusyCall(
//                            applicationContext,
//                            mCallComingEvent.accountID,
//                            mCallComingEvent.callID)

                        ToastManage.s(this, "已拒绝")
                        finish()
                    }
                    else -> {
                        restoreCallIn(v)
                    }
                }
            }
            MotionEvent.ACTION_CANCEL -> {
                restoreCallIn(v)
            }
        }
        true
    }

    /**
     * 接听或拒绝
     */
    private fun callViewGone(v: View) {
        civBgHangUp.clearAnimation()
        civBgPickUp.clearAnimation()
        civBgHangUp.visibility = View.GONE
        civBgPickUp.visibility = View.GONE
        imageHangUp.visibility = View.GONE
        imagePickUp.visibility = View.GONE
        layoutLeftBubbles.visibility = View.GONE
        layoutRightBubbles.visibility = View.GONE

        v.scaleX = 1.3f
        v.scaleY = 1.3f
        v.x = initInputCallX
        v.y = initInputCallY
        (v as ImageView).setImageResource(R.drawable.circle_view_red)
        v.setOnTouchListener(null)

        imageInputCall.visibility = View.VISIBLE
        imageInputCall.setImageResource(R.drawable.ic_call_end_black_24dp)
        imageInputCall.drawable.setTint(resources.getColor(android.R.color.white))
        imageInputCall.startAnimation(inputCallAnimation)
        imageInputCall.clearAnimation()
    }

    /**
     * 还原来电状态
     */
    private fun restoreCallIn(v: View) {
        imageInputCall.visibility = View.VISIBLE

        circleImageInputCall.startAnimation(inputCallBackgroundAnimation)
        imageInputCall.startAnimation(inputCallAnimation)

        civBgHangUp.clearAnimation()
        civBgPickUp.clearAnimation()

        civBgHangUp.visibility = View.INVISIBLE
        civBgPickUp.visibility = View.INVISIBLE

        imageHangUp.drawable.setTint(resources.getColor(R.color.red))
        imagePickUp.drawable.setTint(resources.getColor(R.color.green_2))

        // Reset input call position
        v.x = initInputCallX
        v.y = initInputCallY

        (v as ImageView).setImageResource(R.drawable.circle_view)

        layoutLeftBubbles.visibility = View.VISIBLE
        layoutRightBubbles.visibility = View.VISIBLE
    }

    /**
     * 接听电话
     */
    private fun getInCall(v: View) {
        v.setOnClickListener {
            SipServiceCommand.hangUpActiveCalls(applicationContext, mCallComingEvent.accountID)
            TyLog.i("sip endCall")
            ToastManage.s(this@CallInActivity, "已挂断")
            finish()
        }
        imgControlMic.visibility = View.VISIBLE
        imgControlVolume.visibility = View.VISIBLE
        tvHangUp.visibility = View.VISIBLE
        tvMicOff.visibility = View.VISIBLE
        tvVolumeOpen.visibility = View.VISIBLE
        imgControlMic.setOnClickListener {
            isMicOff = !isMicOff
            imgControlMic.isSelected = isMicOff
            SipServiceCommand.setCallMute(applicationContext,
                    mCallComingEvent.accountID,
                    mCallComingEvent.callID,
                    isMicOff)
        }
        imgControlVolume.setOnClickListener {
            isVolumeOpen = !isVolumeOpen
            imgControlVolume.isSelected = isVolumeOpen
            scAudio.setSpeakerMode(isVolumeOpen)
        }
        //计时
        tv_time.visibility = View.VISIBLE
        timeDispose = Observable.interval(0, 1000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    tv_time.text = DateUtils.timeParse(it * 1000)
                }
        tv_state.visibility = View.GONE
    }

    /**
     * 挂断电话
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun disconnectCall(event: CallDisconnectEvent) {
        scAudioManager.getInstance().stopRingtone()
        if (isDecline) {
            return
        }
        runOnUiThread {
            TyLog.i("sip 断开")
            ToastManage.s(this@CallInActivity, "对方已挂断")
            finish()
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
        updateDispose?.dispose()
    }
}
