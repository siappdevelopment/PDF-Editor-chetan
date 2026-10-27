package com.reader.alldocviewer.pdfeditor.navigation

enum class StartupFlowStep(val key: String) {
    LANGUAGE("Language"),
    ONBOARDING("OnBoarding"),
    PERMISSION("Permission"),
    POLICY_SCREEN("PolicyScreen"),
    DEFAULT_HOME("DefaultHome"),
    HOME("Home");

    companion object {
        fun fromKey(raw: String?): StartupFlowStep? {
            if (raw.isNullOrBlank()) return null
            val normalized = raw.trim()
            return entries.firstOrNull { step ->
                step.key.equals(normalized, ignoreCase = true) ||
                    step.aliases.any { it.equals(normalized, ignoreCase = true) }
            }
        }
    }

    private val aliases: List<String>
        get() = when (this) {
            ONBOARDING -> listOf("Onboarding", "Intro", "IntroFlow")
            POLICY_SCREEN -> listOf("PrivacyPolicySetting", "PrivacyPolicy", "Policy")
            DEFAULT_HOME -> listOf("DefaultHomeApp", "DefaultApp", "DefaultHomeAppActivity")
            HOME -> listOf("Main", "HomeActivity")
            else -> emptyList()
        }
}
