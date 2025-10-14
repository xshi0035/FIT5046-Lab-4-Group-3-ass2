package com.example.fit5046_lab4_group3_ass2.data

import android.content.Context
import androidx.core.content.edit

object UserPrefs {
    private const val FILE = "user_prefs"
    private const val KEY_ONBOARDED = "onboarded"

    fun isOnboarded(ctx: Context): Boolean =
        ctx.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .getBoolean(KEY_ONBOARDED, false)

    fun setOnboarded(ctx: Context, value: Boolean = true) {
        ctx.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .edit { putBoolean(KEY_ONBOARDED, value) }
    }
}
