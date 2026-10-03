package com.onesecond.app.data

import java.time.LocalDate

enum class MomentType { PHOTO, VIDEO, NOTE }

enum class Mood(val label: String) {
    CALM("Calm"),
    JOYFUL("Joyful"),
    GRATEFUL("Grateful"),
    TIRED("Tired"),
    RESTLESS("Restless"),
    HEAVY("Heavy")
}

/** One kept moment. There is at most one per calendar day. */
data class Moment(
    val id: String,
    val epochDay: Long,
    val type: MomentType,
    val mediaFile: String?,
    val thumbFile: String?,
    val caption: String,
    val mood: Mood?,
    val createdAt: Long,
    val durationMs: Long
) {
    val date: LocalDate get() = LocalDate.ofEpochDay(epochDay)
}
