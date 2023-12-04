package com.tech.samsetdownloader.utils

import android.content.Context
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar

object ProgressBarUtil {
    private var progressBar: ProgressBar? = null

    fun showProgressBar(context: Context) {
        if (progressBar == null) {
            progressBar = createProgressBar(context)
        }

        progressBar?.visibility = ProgressBar.VISIBLE
    }

    fun hideProgressBar() {
        progressBar?.visibility = ProgressBar.GONE
    }

    private fun createProgressBar(context: Context): ProgressBar {
        val progressBar = ProgressBar(context)
        progressBar.visibility = ProgressBar.GONE

        val frameLayout = FrameLayout(context)
        val layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        layoutParams.gravity = Gravity.CENTER
        frameLayout.layoutParams = layoutParams
        frameLayout.addView(progressBar)

        return progressBar
    }
}