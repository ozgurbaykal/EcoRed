package com.ozgurbaykal.ecored.view.customs

import android.app.Activity
import android.content.Context
import android.graphics.PixelFormat
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.ozgurbaykal.ecored.R

class CustomDialogFragment(private val activity: Activity) {
    private var toastView: View? = null
    private val lifecycleObserver = object : LifecycleObserver {

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun onDestroy() {
            removeToast()
        }
    }

    companion object {
        private var isShowing: Boolean = false
    }

    init {
        (activity as? LifecycleOwner)?.lifecycle?.addObserver(lifecycleObserver)
    }

    fun show(title: String, description: String, duration: Long = 3000L, dialogType: DialogTypes) {
        if (isShowing) return

        isShowing = true

        toastView =
            LayoutInflater.from(activity).inflate(R.layout.custom_dialog_layout, null, false)

        toastView?.findViewById<TextView>(R.id.toastTitle)?.text = title
        toastView?.findViewById<TextView>(R.id.toastDescription)?.text = description

        if (dialogType == DialogTypes.SUCCESS) {

            toastView?.findViewById<LinearLayout>(R.id.customToastContainer)
                ?.setBackgroundColor(activity.getColor(R.color.custom_green))
            toastView?.findViewById<ImageView>(R.id.toastIcon)
                ?.setImageResource(R.drawable.check_circle)

        } else if (dialogType == DialogTypes.ERROR) {

            toastView?.findViewById<LinearLayout>(R.id.customToastContainer)
                ?.setBackgroundColor(activity.getColor(R.color.main_red))
            toastView?.findViewById<ImageView>(R.id.toastIcon)
                ?.setImageResource(R.drawable.error_circle)

        } else if (dialogType == DialogTypes.WARNING) {

            toastView?.findViewById<LinearLayout>(R.id.customToastContainer)
                ?.setBackgroundColor(activity.getColor(R.color.custom_yellow))
            toastView?.findViewById<ImageView>(R.id.toastIcon)
                ?.setImageResource(R.drawable.warning_circle)

        } else if (dialogType == DialogTypes.INFO) {

            toastView?.findViewById<LinearLayout>(R.id.customToastContainer)
                ?.setBackgroundColor(activity.getColor(R.color.custom_yellow_green))
            toastView?.findViewById<ImageView>(R.id.toastIcon)
                ?.setImageResource(R.drawable.info_circle)

        }

        toastView?.findViewById<ImageView>(R.id.closeDialog)?.setOnClickListener {
            removeToast()
        }

        val params = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        toastView?.layoutParams = params

        val rootView = activity.findViewById<ViewGroup>(android.R.id.content)
        rootView.addView(toastView)

        toastView?.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.slide_in_top))

        toastView?.postDelayed({
            toastView?.startAnimation(
                AnimationUtils.loadAnimation(activity, R.anim.slide_out_top).apply {
                    setAnimationListener(object : Animation.AnimationListener {
                        override fun onAnimationStart(animation: Animation?) {}

                        override fun onAnimationEnd(animation: Animation?) {
                            removeToast()
                            isShowing = false
                        }

                        override fun onAnimationRepeat(animation: Animation?) {}
                    })
                })
        }, duration)
    }

    fun removeToast() {
        toastView?.let { view ->
            (view.parent as? ViewGroup)?.removeView(view)
            isShowing = false
            toastView = null
        }
    }

}

enum class DialogTypes {
    WARNING,
    ERROR,
    SUCCESS,
    INFO
}