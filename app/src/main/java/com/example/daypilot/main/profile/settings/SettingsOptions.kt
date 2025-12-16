package com.example.daypilot.main.profile.settings

data class RegionOption(
    val id: String,
    val label: String
)

val RegionOptions = listOf(
    RegionOption("Europe/Madrid", "Europe/Madrid (UTC+01:00)"),
    RegionOption("Atlantic/Canary", "Atlantic/Canary (UTC+00:00)"),
    RegionOption("Europe/London", "Europe/London (UTC+00:00)"),
    RegionOption("Europe/Paris", "Europe/Paris (UTC+01:00)"),
    RegionOption("America/New_York", "America/New_York (UTC-05:00)"),
    RegionOption("America/Los_Angeles", "America/Los_Angeles (UTC-08:00)"),
    RegionOption("America/Mexico_City", "America/Mexico_City (UTC-06:00)"),
    RegionOption("America/Sao_Paulo", "America/Sao_Paulo (UTC-03:00)"),
    RegionOption("Asia/Tokyo", "Asia/Tokyo (UTC+09:00)"),
    RegionOption("Asia/Shanghai", "Asia/Shanghai (UTC+08:00)"),
    RegionOption("Asia/Dubai", "Asia/Dubai (UTC+04:00)"),
    RegionOption("Australia/Sydney", "Australia/Sydney (UTC+10:00)")
)

val LanguageOptions = listOf(
    "es" to "Español",
    "en" to "English"
)

fun regionLabel(regionId: String?): String {
    if (regionId.isNullOrBlank()) return ""
    return RegionOptions.firstOrNull { it.id == regionId }?.label ?: regionId
}

fun languageLabel(code: String): String {
    return LanguageOptions.firstOrNull { it.first == code }?.second ?: "Desconocido"
}
