package com.onesecond.app.data

import android.content.Context

class SettingsStore(context: Context) {
    private val sp = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    var onboarded: Boolean
        get() = sp.getBoolean("onboarded", false)
        set(v) { sp.edit().putBoolean("onboarded", v).apply() }

    /** null until the person chooses; then true = Dark (glass), false = Light. */
    var darkMode: Boolean?
        get() = if (sp.contains("dark_mode")) sp.getBoolean("dark_mode", true) else null
        set(v) {
            val e = sp.edit()
            if (v == null) e.remove("dark_mode") else e.putBoolean("dark_mode", v)
            e.apply()
        }

    var reminderEnabled: Boolean
        get() = sp.getBoolean("reminder_enabled", false)
        set(v) { sp.edit().putBoolean("reminder_enabled", v).apply() }

    var reminderHour: Int
        get() = sp.getInt("reminder_hour", 20)
        set(v) { sp.edit().putInt("reminder_hour", v).apply() }

    var reminderMinute: Int
        get() = sp.getInt("reminder_minute", 0)
        set(v) { sp.edit().putInt("reminder_minute", v).apply() }
}
