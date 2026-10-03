package com.onesecond.app

import android.app.Application
import com.onesecond.app.data.Graph
import com.onesecond.app.notify.Reminder

class OneSecondApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Graph.init(this)
        Reminder.ensureChannel(this)
        // Self-heal: alarms are cleared on force-stop/update, so re-arm whenever the app starts.
        val s = Graph.settings
        if (s.reminderEnabled) Reminder.schedule(this, s.reminderHour, s.reminderMinute)
    }
}
