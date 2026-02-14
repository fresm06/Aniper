package com.aniper.ui.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.aniper.R
import com.aniper.util.PreferencesHelper
import com.google.android.material.button.MaterialButton
import com.google.android.material.slider.Slider

class YRangeSettingsDialog(private val context: Context, private val onSaved: () -> Unit = {}) {

    private var topPercent = PreferencesHelper.getYMinPercent(context) * 100
    private var bottomPercent = PreferencesHelper.getYMaxPercent(context) * 100

    fun show() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_y_range_settings, null)

        val sliderTop = dialogView.findViewById<Slider>(R.id.slider_top_boundary)
        val sliderBottom = dialogView.findViewById<Slider>(R.id.slider_bottom_boundary)
        val tvTopValue = dialogView.findViewById<TextView>(R.id.tv_top_value)
        val tvBottomValue = dialogView.findViewById<TextView>(R.id.tv_bottom_value)
        val previewRange = dialogView.findViewById<View>(R.id.preview_range)
        val btnReset = dialogView.findViewById<MaterialButton>(R.id.btn_reset)
        val btnSave = dialogView.findViewById<MaterialButton>(R.id.btn_save)

        // Initialize sliders with current values
        sliderTop.value = topPercent
        sliderBottom.value = bottomPercent

        updatePreview(dialogView, topPercent, bottomPercent)

        sliderTop.addOnChangeListener { _, value, _ ->
            topPercent = value
            tvTopValue.text = "${value.toInt()}% from top"
            updatePreview(dialogView, topPercent, bottomPercent)
        }

        sliderBottom.addOnChangeListener { _, value, _ ->
            bottomPercent = value
            tvBottomValue.text = "${value.toInt()}% from top"
            updatePreview(dialogView, topPercent, bottomPercent)
        }

        btnReset.setOnClickListener {
            topPercent = 30f
            bottomPercent = 90f
            sliderTop.value = topPercent
            sliderBottom.value = bottomPercent
            tvTopValue.text = "30% from top"
            tvBottomValue.text = "90% from top"
            updatePreview(dialogView, topPercent, bottomPercent)
        }

        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        btnSave.setOnClickListener {
            // Save with button click animation
            btnSave.animate().scaleX(0.95f).scaleY(0.95f).duration = 100
            btnSave.animate().scaleX(1f).scaleY(1f).duration = 100

            PreferencesHelper.setYRange(context, topPercent / 100f, bottomPercent / 100f)
            onSaved()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun updatePreview(dialogView: View, topPercent: Float, bottomPercent: Float) {
        val previewScreen = dialogView.findViewById<View>(R.id.preview_screen)
        val previewTopLine = dialogView.findViewById<View>(R.id.preview_top_line)
        val previewBottomLine = dialogView.findViewById<View>(R.id.preview_bottom_line)
        val previewRange = dialogView.findViewById<View>(R.id.preview_range)

        previewScreen.post {
            val screenHeight = previewScreen.height.takeIf { height -> height > 0 } ?: 200
            val topPx = (screenHeight * topPercent / 100).toInt()
            val bottomPx = (screenHeight * bottomPercent / 100).toInt()

            // Update line positions using translationY
            previewTopLine.translationY = topPx.toFloat()
            previewBottomLine.translationY = (bottomPx - previewBottomLine.height).toFloat()

            // Update range highlight
            val rangePx = (bottomPx - topPx).coerceAtLeast(0)
            val params = previewRange.layoutParams
            if (params is FrameLayout.LayoutParams) {
                params.height = rangePx
                previewRange.layoutParams = params
            }
            previewRange.translationY = topPx.toFloat()
        }
    }
}
