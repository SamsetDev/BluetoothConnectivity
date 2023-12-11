package com.tech.bluetooth.utils

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.view.ContextThemeWrapper
import com.tech.bluetooth.R

object ProgressBarUtil {

    private var alertDialog: Dialog? = null


    /***
     * To dismiss the dialog
     */
    fun dismissLoadingDialog() {
        try {
            if (alertDialog != null && alertDialog!!.isShowing) {
                alertDialog?.dismiss()
                alertDialog?.cancel()
            }
        } catch (e: IllegalArgumentException) {
           e.printStackTrace()
        } finally {
            alertDialog = null
        }
    }

    fun showLoadingDialog(context: Context,type:PROGRESSBAR_TYPE) {
        val themeSmall = ContextThemeWrapper(context, android.R.style.Widget_ProgressBar_Small)
        val themeLarge = ContextThemeWrapper(context, android.R.style.Widget_ProgressBar_Large)

        try {
            if (context is Activity) {
                val activity: Activity = context
                if (activity.isFinishing) {
                    return
                }
            }
            if (alertDialog != null && alertDialog!!.isShowing) {
                return
            }
            val result = if (type == PROGRESSBAR_TYPE.LARGE) themeLarge else themeSmall
            alertDialog = Dialog(result)
            alertDialog.let {
                it?.requestWindowFeature(Window.FEATURE_NO_TITLE)
                it?.requestWindowFeature(Window.FEATURE_NO_TITLE)
                it?.setContentView(R.layout.progress_view)
                (it?.findViewById(R.id.progress_bar) as ProgressBar).visibility = View.VISIBLE
                it?.setCanceledOnTouchOutside(false)
                it?.setCancelable(false)
                it?.window?.setGravity(Gravity.CENTER)
                it?.window?.setBackgroundDrawable(ColorDrawable(0))
                it?.show()
            }


        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
    }

    fun showLoadingDialogWithMessage(context: Context,msg:String,type:PROGRESSBAR_TYPE) {
        val themeSmall = ContextThemeWrapper(context, android.R.style.Widget_ProgressBar_Small)
        val themeLarge = ContextThemeWrapper(context, android.R.style.Widget_ProgressBar_Large)

        try {
            if (context is Activity) {
                val activity: Activity = context
                if (activity.isFinishing) {
                    return
                }
            }
            if (alertDialog != null && alertDialog!!.isShowing) {
                return
            }

            val result = if (type == PROGRESSBAR_TYPE.LARGE) themeLarge else themeSmall
            alertDialog = Dialog(result)
            alertDialog?.let {
                it.requestWindowFeature(Window.FEATURE_NO_TITLE)
                it.setContentView(R.layout.progress_view)
                (it.findViewById(R.id.progress_bar) as ProgressBar).visibility = View.VISIBLE
                val progressMsg = it.findViewById(R.id.progressbarMsg) as TextView
                progressMsg.text = msg
                it.setCanceledOnTouchOutside(false)
                it.setCancelable(false)
                it.window?.setBackgroundDrawable(ColorDrawable(0))
                it.show()
            }
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
    }
    enum class PROGRESSBAR_TYPE{
        LARGE,SMALL
    }

}