package com.onesecond.app.data

import android.app.Application

/** Tiny service locator; initialised once in [com.onesecond.app.OneSecondApp]. */
object Graph {
    lateinit var store: MomentStore
        private set
    lateinit var settings: SettingsStore
        private set

    fun init(app: Application) {
        store = MomentStore(app)
        settings = SettingsStore(app)
    }
}
