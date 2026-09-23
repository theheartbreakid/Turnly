package com.crescentapps.turnly.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class ScheduleType(val displayName: String) {
    GENERAL("General"),
    MONEY("Money"),
    TASK("Task"),
    RESPONSIBILITY("Responsibility"),
    CUSTOM("Custom")
}

@Serializable
enum class FrequencyType(val displayName: String) {
    DAILY("Every Day"),
    EVERY_X_DAYS("Every X Days"),
    WEEKLY("Weekly"),
    EVERY_X_WEEKS("Every X Weeks"),
    MONTHLY("Monthly"),
    CUSTOM_WEEKDAYS("Specific Weekdays")
}

@Serializable
enum class OccurrenceStatus(val displayName: String) {
    PENDING("Pending"),
    COMPLETED("Completed"),
    SKIPPED("Skipped"),
    MISSED("Missed"),
    CANCELLED("Cancelled")
}

@Serializable
enum class OverrideStrategy(val displayName: String, val description: String) {
    SINGLE_DATE_ONLY(
        "Single date only",
        "Only overrides this specific date. Future dates continue with the normal rotation."
    ),
    CONTINUE_FROM_OVERRIDE(
        "Continue rotation from here",
        "The rotation index shifts so the next occurrence follows after this participant."
    )
}

@Serializable
enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

@Serializable
enum class FirstDayOfWeek {
    SUNDAY,
    MONDAY
}

@Serializable
enum class UiMode(val displayName: String, val badge: String) {
    MATERIAL_3("Material 3", "Stable · Recommended"),
    LIQUID("Liquid UI", "Experimental")
}
