package com.aniper.util

import android.content.Context
import android.content.SharedPreferences

/**
 * Helper class for managing user preferences, including Y-axis movement ranges.
 */
object PreferencesHelper {

    private const val PREFS_NAME = "aniper_prefs"
    private const val KEY_Y_MIN_PERCENT = "y_min_percent"
    private const val KEY_Y_MAX_PERCENT = "y_max_percent"

    // Default values: pet can move from 30% (top) to 90% (bottom) of screen
    private const val DEFAULT_Y_MIN_PERCENT = 0.3f
    private const val DEFAULT_Y_MAX_PERCENT = 0.9f

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Get the minimum Y position as a percentage of screen height (0.0 to 1.0).
     * This represents the top boundary where pets can move to.
     */
    fun getYMinPercent(context: Context): Float {
        return getPreferences(context).getFloat(KEY_Y_MIN_PERCENT, DEFAULT_Y_MIN_PERCENT)
    }

    /**
     * Get the maximum Y position as a percentage of screen height (0.0 to 1.0).
     * This represents the bottom boundary where pets rest/move to.
     */
    fun getYMaxPercent(context: Context): Float {
        return getPreferences(context).getFloat(KEY_Y_MAX_PERCENT, DEFAULT_Y_MAX_PERCENT)
    }

    /**
     * Set the Y-axis movement range.
     * @param minPercent The top boundary (0.0 to 1.0, typically 0.2 to 0.5)
     * @param maxPercent The bottom boundary (0.0 to 1.0, typically 0.7 to 0.95)
     */
    fun setYRange(context: Context, minPercent: Float, maxPercent: Float) {
        val prefs = getPreferences(context)
        prefs.edit().apply {
            putFloat(KEY_Y_MIN_PERCENT, minPercent.coerceIn(0f, 1f))
            putFloat(KEY_Y_MAX_PERCENT, maxPercent.coerceIn(0f, 1f))
            apply()
        }
    }

    /**
     * Reset Y-axis range to default values.
     */
    fun resetYRange(context: Context) {
        val prefs = getPreferences(context)
        prefs.edit().apply {
            remove(KEY_Y_MIN_PERCENT)
            remove(KEY_Y_MAX_PERCENT)
            apply()
        }
    }
}
