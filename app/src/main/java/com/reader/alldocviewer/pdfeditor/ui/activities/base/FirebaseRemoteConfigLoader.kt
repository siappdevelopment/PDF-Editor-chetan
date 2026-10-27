package com.reader.alldocviewer.pdfeditor.ui.activities.base

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSMainClass
import com.reader.alldocviewer.pdfeditor.Advertisement.AdsLocationGate
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSUtilitis
import com.reader.alldocviewer.pdfeditor.Advertisement.ManegeUtilsView
import com.reader.alldocviewer.pdfeditor.R
import com.reader.alldocviewer.pdfeditor.launcher.LauncherFirebaseConfigRefresh
import com.reader.alldocviewer.pdfeditor.navigation.StartupFlowManager
import com.reader.alldocviewer.pdfeditor.ui.activities.comingSoon.CommingActivity
import org.json.JSONArray
import org.json.JSONObject

object FirebaseRemoteConfigLoader {

    enum class Source {
        SPLASH,
        LAUNCHER_HOME,
    }

    @JvmStatic
    fun showingToAllData(
        activity: Activity,
        source: Source,
        onNoNetwork: () -> Unit = {},
        onUpdateComingSoon: () -> Unit = {},
        onComplete: () -> Unit = {},
        onHideSplashBottomAdPlaceholders: (() -> Unit)? = null,
    ) {
        if (!ADSUtilitis.IsNetworkConnected(activity)) {
            onNoNetwork()
            return
        }

        val remoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings =
            FirebaseRemoteConfigSettings.Builder().setMinimumFetchIntervalInSeconds(0).build()
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(R.xml.default_config)

        remoteConfig.fetchAndActivate()
            .addOnCompleteListener(activity) { task ->
                if (task.isSuccessful) {
                    try {
                        val updateComingSoon = applyRemoteConfig(
                            context = activity,
                            remoteConfig = remoteConfig,
                            onHideSplashBottomAdPlaceholders = onHideSplashBottomAdPlaceholders,
                        )
                        LauncherFirebaseConfigRefresh.markConfigFetched(activity)

                        if (updateComingSoon) {
                            ADSMainClass.setComingSoon(true)
                            when (source) {
                                Source.SPLASH -> {
                                    activity.startActivity(
                                        Intent(
                                            activity,
                                            CommingActivity::class.java
                                        )
                                    )
                                    activity.finish()
                                }

                                Source.LAUNCHER_HOME -> {
                                    onUpdateComingSoon()
                                    activity.startActivity(
                                        Intent(
                                            activity,
                                            CommingActivity::class.java
                                        )
                                    )
                                }
                            }
                            return@addOnCompleteListener
                        }
                        onComplete()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        onComplete()
                    }
                } else {
                    onComplete()
                }
            }
    }

    private fun resolveVersionKey(context: Context): String {
        ManegeUtilsView.isUtilsManege(context)
        val versionName = context.packageManager.getPackageInfo(context.packageName, 0).versionName
        val result = versionName?.replace(".", "_")
        return ManegeUtilsView.ssfsfsfsf + result
    }

    private fun applyRemoteConfig(

        context: Context,
        remoteConfig: FirebaseRemoteConfig,
        onHideSplashBottomAdPlaceholders: (() -> Unit)?,
    ): Boolean {
        val versionKey = resolveVersionKey(context)
        val remoteConfigValue = remoteConfig.getString(versionKey)
        val rootConfigJson: JSONObject = when {
            remoteConfigValue.trim().startsWith("[") ->
                JSONArray(remoteConfigValue).getJSONObject(0)

            else ->
                JSONObject(remoteConfigValue)
        }

        val referrerUrl = ADSMainClass.getReferrerUrl().orEmpty()


        val configJson: JSONObject =
            if (isPaidMarketingReferrer(context, referrerUrl)) {
                rootConfigJson.getJSONObject(GlobalParameterManage.PAID_USER)
            } else {
                rootConfigJson.getJSONObject(GlobalParameterManage.PAID_USER)
            }
        val privacy_policy = configJson.getString(GlobalParameterManage.APP_PRIVACY_POLICY)
        val splash_ads_type = configJson.getString(GlobalParameterManage.SPLASH_AD_LOAD_TYPE)
        val ads_click = configJson.getLong(GlobalParameterManage.ADS_CLICK)
        val back_click = configJson.getLong(GlobalParameterManage.BACKS_CLICK)
        val native_by_page = configJson.getLong(GlobalParameterManage.NATIVE_BY_PAGE)
        val ads_blocker = configJson.getBoolean(GlobalParameterManage.ADS_BLOCKS)
        val coming_soon = configJson.getBoolean(GlobalParameterManage.COMING_SOON)
        val exit_ads = configJson.getBoolean(GlobalParameterManage.EXIT_DIALOG)
        val firebaseanalytics = configJson.getBoolean(GlobalParameterManage.FIREBASE_ANALYTICS)
        val update_coming_soon = configJson.getBoolean(GlobalParameterManage.UPDATE_COMING_SOON)
        val consent_form_show_screen =
            configJson.getString(GlobalParameterManage.consent_form_show_screen)

        ADSMainClass.setConsentScreenShow(consent_form_show_screen);
        val screen = configJson.getJSONObject(GlobalParameterManage.SCREEN)

        val adsLayout = screen.getJSONObject(GlobalParameterManage.ADS_LAYOUT)
        val lightMode = adsLayout.getJSONObject(GlobalParameterManage.LIGHT_MODE)
        val button_margin = adsLayout.getString(GlobalParameterManage.BUTTON_MARGIN)

        val lightNativeButtonColor =
            lightMode.getString(GlobalParameterManage.NATIVE_ADS_BUTTON_COLOR)

        val lightNativeBackgroundColor =
            lightMode.getString(GlobalParameterManage.NATIVE_AD_BACKGROUND_COLOR)

        val lightNativeAllTextColor =
            lightMode.getString(GlobalParameterManage.NATIVE_AD_ALL_TEXT_COLOR)

        val lightNativeButtonTextColor =
            lightMode.getString(GlobalParameterManage.NATIVE_AD_BUTTON_TEXT_COLOR)

        val darkMode = adsLayout.getJSONObject(GlobalParameterManage.DARK_MODE)

        val darkNativeButtonColor =
            darkMode.getString(GlobalParameterManage.NATIVE_ADS_BUTTON_COLOR)

        val darkNativeBackgroundColor =
            darkMode.getString(GlobalParameterManage.NATIVE_AD_BACKGROUND_COLOR)

        val darkNativeAllTextColor =
            darkMode.getString(GlobalParameterManage.NATIVE_AD_ALL_TEXT_COLOR)

        val darkNativeButtonTextColor =
            darkMode.getString(GlobalParameterManage.NATIVE_AD_BUTTON_TEXT_COLOR)

        ADSMainClass.setButtonMargins(button_margin);
        ADSMainClass.setLightNativeButtonColor(lightNativeButtonColor)
        ADSMainClass.setLightNativeBackgroundColor(lightNativeBackgroundColor)
        ADSMainClass.setLightNativeAllTextColor(lightNativeAllTextColor)
        ADSMainClass.setLightNativeButtonTextColor(lightNativeButtonTextColor)

        ADSMainClass.setDarkNativeButtonColor(darkNativeButtonColor)
        ADSMainClass.setDarkNativeBackgroundColor(darkNativeBackgroundColor)
        ADSMainClass.setDarkNativeAllTextColor(darkNativeAllTextColor)
        ADSMainClass.setDarkNativeButtonTextColor(darkNativeButtonTextColor)

        // Splash Screen
        val splash_screen = screen.getJSONObject(GlobalParameterManage.SPLASH_SCREEN)
        val splah_app_open_ads_show =
            splash_screen.getBoolean(GlobalParameterManage.SPLASH_APP_OPEN_ADS_SHOW)
        val splash_app_open_after_count =
            splash_screen.getInt(GlobalParameterManage.SPLASH_APP_OPEN_AFTER_COUNT)
        val app_open_show_in_background =
            splash_screen.getBoolean(GlobalParameterManage.APP_OPEN_SHOW_IN_BACKGROUND)
        val app_open_daily_show_count =
            splash_screen.getInt(GlobalParameterManage.APP_OPEN_DAILY_SHOW_COUNT)
        val app_open_id = splash_screen.getString(GlobalParameterManage.APP_OPEN_ID)
        val splash_banner_id =
            splash_screen.getString(GlobalParameterManage.SPLASH_BANNER_ID)
        val splash_native_id = splash_screen.getString(GlobalParameterManage.SPLASH_NATIVE_ID)
        val splash_ads_types = splash_screen.getString(GlobalParameterManage.SPLASH_ADS_TYPE)
        val splash_screen_bottom_ad_show =
            splash_screen.getBoolean(GlobalParameterManage.SPLASH_SCREEN_BOTTOM_AD_SHOW)
        ADSMainClass.setSplashAdsType(splash_ads_types)
        ADSMainClass.setSplashAppOpenAfterCount(splash_app_open_after_count)
        ADSMainClass.setAppOpenBackgroundShow(app_open_show_in_background)
        ADSMainClass.setAppOpenAdDailyLimit(app_open_daily_show_count)
        ADSMainClass.setBottomSplashShow(splash_screen_bottom_ad_show)
        if (!splash_screen_bottom_ad_show) {
            onHideSplashBottomAdPlaceholders?.invoke()
        }
        ADSMainClass.setSplashAppOpenShow(splah_app_open_ads_show)

        // Language Screen
        val language_screen = screen.getJSONObject(GlobalParameterManage.LANGUAGE_SCREEN)
        val splash_to_language =
            language_screen.getBoolean(GlobalParameterManage.SPLASH_TO_LANGUAGE)
        val language_screen_bottom_ad_show =
            language_screen.getBoolean(GlobalParameterManage.LANGUAGE_SCREEN_BOTTOM_AD_SHOW)
        val language_ads_type = language_screen.getString(GlobalParameterManage.LANGUAGE_ADS_TYPE)
        val language_inter_ads_show =
            language_screen.getBoolean(GlobalParameterManage.LANGUAGE_INTER_ADS_SHOW)
        val language_banner_id = language_screen.getString(GlobalParameterManage.LANGUAGE_BANNER_ID)
        val language_native_id = language_screen.getString(GlobalParameterManage.LANGUAGE_NATIVE_ID)

        ADSMainClass.setSplashToLanguage(splash_to_language)
        ADSMainClass.setLanguageScreenBottomAdShow(language_screen_bottom_ad_show)
        ADSMainClass.setLanguageAdsType(language_ads_type)
        ADSMainClass.setLanguageInterAdsShow(language_inter_ads_show)

        // Onboarding Intro Screen
        val onboarding_intro = screen.getJSONObject(GlobalParameterManage.ONBOARDING_INTRO)
        val onboarding_count_show =
            onboarding_intro.getInt(GlobalParameterManage.ONBOARDING_COUNT_SHOW)
        val onboarding_ads_type =
            onboarding_intro.getString(GlobalParameterManage.ONBOARDING_ADS_TYPE)
        val onboarding_inter_ads_show =
            onboarding_intro.getBoolean(GlobalParameterManage.ONBOARDING_INTER_ADS_SHOW)
        val onboarding_screen_bottom_ad_show =
            onboarding_intro.getBoolean(GlobalParameterManage.ONBOARDING_SCREEN_BOTTOM_AD_SHOW)

        val onboarding_native_id =
            onboarding_intro.getString(GlobalParameterManage.ONBOARDING_NATIVE_ID)
        val onboarding_banner_id =
            onboarding_intro.getString(GlobalParameterManage.ONBOARDING_BANNER_ID)
        ADSMainClass.setOnboardingAdsType(onboarding_ads_type)
        ADSMainClass.setOnboardingInterAdsShow(onboarding_inter_ads_show)
        ADSMainClass.setOnboardingScreenBottomAdShow(onboarding_screen_bottom_ad_show)
        ADSMainClass.setOnboardingCountShow(onboarding_count_show)

        // Permission Screen
        val permission_screen = screen.getJSONObject(GlobalParameterManage.PERMISSION_SCREEN)
        val permission_small_ads_show =
            permission_screen.optBoolean(GlobalParameterManage.PERMISSION_SMALL_ADS_SHOW, false)
        val permission_ads_type =
            permission_screen.getString(GlobalParameterManage.PERMISSION_ADS_TYPE)
        val permission_native_id =
            permission_screen.getString(GlobalParameterManage.PERMISSION_NATIVE_ID)
        val permission_banner_id =
            permission_screen.getString(GlobalParameterManage.PERMISSION_BANNER_ID)
        val default_permission_button_ads_show =
            permission_screen.optBoolean(
                GlobalParameterManage.DEFAULT_PERMISSION_BUTTON_ADS_SHOW,
                false
            )
        val default_permission_button_ads_type =
            permission_screen.optString(
                GlobalParameterManage.DEFAULT_PERMISSION_BUTTON_ADS_TYPE,
                "Appopen"
            )

        ADSMainClass.setPermissionSmallAdsShow(permission_small_ads_show)
        ADSMainClass.setPermissionAdsType(permission_ads_type)
        ADSMainClass.setDefaultPermissionButtonAdsShow(default_permission_button_ads_show)
        ADSMainClass.setDefaultPermissionButtonAdsType(default_permission_button_ads_type)

        // Default Permission Screen (Set as Default bottom ads)
        val default_permission_screem =
            screen.optJSONObject(GlobalParameterManage.DEFAULT_PERMISSION_SCREEM)
        val default_permission_bottom_ads_show =
            default_permission_screem?.optBoolean(
                GlobalParameterManage.DEFAULT_PERMISSION_BOTTOM_ADS_SHOW,
                true
            ) ?: true
        val default_permission_ads_type =
            default_permission_screem?.optString(
                GlobalParameterManage.DEFAULT_PERMISSION_ADS_TYPE,
                "banner"
            ) ?: "banner"
        val default_permission_banner_id =
            default_permission_screem?.optString(
                GlobalParameterManage.DEFAULT_PERMISSION_BANNER_ID,
                ""
            ) ?: ""
        val default_permission_native_id =
            default_permission_screem?.optString(
                GlobalParameterManage.DEFAULT_PERMISSION_NATIVE_ID,
                ""
            ) ?: ""
        ADSMainClass.setDefaultPermissionBottomAdsShow(default_permission_bottom_ads_show)
        ADSMainClass.setDefaultPermissionAdsType(default_permission_ads_type)

        // Home Screen
        val home_screen = screen.getJSONObject(GlobalParameterManage.HOME_SCREEN)
        val home_screen_bottom_show =
            home_screen.getBoolean(GlobalParameterManage.HOME_SCREEN_BOTTOM_SHOW)
        val home_screen_ads_type = home_screen.getString(GlobalParameterManage.HOME_SCREEN_ADS_TYPE)
        val home_banner_id = home_screen.getString(GlobalParameterManage.HOME_BANNER_ID)
        val home_native_id = home_screen.getString(GlobalParameterManage.HOME_NATIVE_ID)
        val first_native_position1 =
            home_screen.optBoolean(GlobalParameterManage.FIRST_NATIVE_POSITION1, false)
        val first_native_position3 =
            home_screen.optBoolean(GlobalParameterManage.FIRST_NATIVE_POSITION3, false)
        val home_native_list_id =
            home_screen.optString(GlobalParameterManage.HOME_NATIVE_LIST_ID, "")
        val home_native_list_id1 = home_screen.optString(
            GlobalParameterManage.HOME_NATIVE_LIST_ID1,
            home_native_list_id.ifEmpty { home_native_id },
        )
        val home_native_list_id2 = home_screen.optString(
            GlobalParameterManage.HOME_NATIVE_LIST_ID2,
            home_native_list_id.ifEmpty { home_native_id },
        )
        val home_bottom_nav_inter_show =
            home_screen.optBoolean(GlobalParameterManage.HOME_BOTTOM_NAV_INTER_SHOW, false)
        val home_bottom_count = home_screen.optInt(GlobalParameterManage.HOME_BOTTOM_COUNT, 2)
        val custom_dialog_show =
            home_screen.optBoolean(GlobalParameterManage.CUSTOM_DIALOG_SHOW, false)
        val custom_after_hour_show =
            home_screen.optString(GlobalParameterManage.CUSTOM_AFTER_HOUR_SHOW, "24")
                .toIntOrNull()
                ?: home_screen.optInt(GlobalParameterManage.CUSTOM_AFTER_HOUR_SHOW, 24)
        val custom_hour_count =
            home_screen.optString(GlobalParameterManage.CUSTOM_HOUR_COUNT, "2")
                .toIntOrNull()
                ?: home_screen.optInt(GlobalParameterManage.CUSTOM_HOUR_COUNT, 2)

        ADSMainClass.setHomeScreenBottomShow(home_screen_bottom_show)
        ADSMainClass.setHomeScreenAdsType(home_screen_ads_type)
        ADSMainClass.setFirstNativePosition1(first_native_position1)
        ADSMainClass.setFirstNativePosition3(first_native_position3)
        ADSMainClass.setHomeBottomNavInterShow(home_bottom_nav_inter_show)
        ADSMainClass.setHomeBottomCount(home_bottom_count)
        ADSMainClass.setCustomDialogShow(custom_dialog_show)
        ADSMainClass.setCustomAfterHourShow(custom_after_hour_show)
        ADSMainClass.setCustomHourCount(custom_hour_count)

        // Launcher Home Screen
        val launcher_home_screen = screen.getJSONObject(GlobalParameterManage.LAUNCHER_HOME_SCREEN)
        val swipe_inter_show =
            launcher_home_screen.getBoolean(GlobalParameterManage.SWIPE_INTER_SHOW)
        val swipe_inter_count =
            launcher_home_screen.getInt(GlobalParameterManage.SWIPE_INTER_COUNT)
        val launcher_bottom_ads_show =
            launcher_home_screen.getBoolean(GlobalParameterManage.LAUNCHER_BOTTOM_ADS_SHOW)
        val launcher_ads_type =
            launcher_home_screen.getString(GlobalParameterManage.LAUNCHER_ADS_TYPE)
        val launcher_banner_id =
            launcher_home_screen.getString(GlobalParameterManage.LAUNCHER_BANNER_ID)
        val launcher_native_id =
            launcher_home_screen.getString(GlobalParameterManage.LAUNCHER_NATIVE_ID)
        val bottom_drawer_ads_show_raw =
            launcher_home_screen.optString(GlobalParameterManage.BOTTOM_DRAWER_ADS_SHOW, "")
        val bottom_drawer_ads_show = when {
            bottom_drawer_ads_show_raw.isEmpty() -> launcher_bottom_ads_show
            else -> bottom_drawer_ads_show_raw.equals("true", ignoreCase = true)
        }
        val daily_ads_limit =
            launcher_home_screen.optString(GlobalParameterManage.DAILY_ADS_LIMIT, "2")
                .toIntOrNull()
                ?: launcher_home_screen.optInt(GlobalParameterManage.DAILY_ADS_LIMIT, 2)
        val daily_hour_quota_after_ads =
            launcher_home_screen.optString(GlobalParameterManage.DAILY_HOUR_QUOTA_AFTER_ADS, "24")
                .toIntOrNull()
                ?: launcher_home_screen.optInt(GlobalParameterManage.DAILY_HOUR_QUOTA_AFTER_ADS, 24)
        ADSMainClass.setLauncherHomeSwipeInterShow(swipe_inter_show)
        ADSMainClass.setLauncherHomeSwipeInterCount(swipe_inter_count)
        ADSMainClass.setLauncherHomeBottomAdsShow(launcher_bottom_ads_show)
        ADSMainClass.setLauncherHomeAdsType(launcher_ads_type)
        ADSMainClass.setLauncherBottomDrawerAdsShow(bottom_drawer_ads_show)
        ADSMainClass.setLauncherDailyAdsLimit(daily_ads_limit)
        ADSMainClass.setLauncherDailyHourQuotaAfterAds(daily_hour_quota_after_ads)

        // Recent Launcher Screen
        val recent_launcher_screen =
            screen.optJSONObject(GlobalParameterManage.RECENT_LAUNCHER_SCREEN)
        val recent_bottom_ads_show = recent_launcher_screen?.optBoolean(
            GlobalParameterManage.RECENT_BOTTOM_ADS_SHOW,
            false
        ) ?: false
        val recent_ads_type = recent_launcher_screen?.optString(
            GlobalParameterManage.RECENT_ADS_TYPE,
            "banner"
        ) ?: "banner"
        val recent_banner_id = recent_launcher_screen?.optString(
            GlobalParameterManage.RECENT_BANNER_ID,
            ""
        ) ?: ""
        val recent_native_id = recent_launcher_screen?.optString(
            GlobalParameterManage.RECENT_NATIVE_ID,
            ""
        ) ?: ""
        ADSMainClass.setRecentLauncherBottomAdsShow(recent_bottom_ads_show)
        ADSMainClass.setRecentLauncherAdsType(recent_ads_type)


        // Setting Screen
        val setting_screen = screen.getJSONObject(GlobalParameterManage.SETTING_SCREEN)
        val setting_bottom_ads_show =
            setting_screen.getBoolean(GlobalParameterManage.SETTING_BOTTOM_ADS_SHOW)
        val setting_ads_type = setting_screen.getString(GlobalParameterManage.SETTING_ADS_TYPE)
        val setting_banner_id = setting_screen.getString(GlobalParameterManage.SETTING_BANNER_ID)
        val setting_native_id = setting_screen.getString(GlobalParameterManage.SETTING_NATIVE_ID)
        ADSMainClass.setSettingBottomAdsShow(setting_bottom_ads_show)
        ADSMainClass.setSettingAdsType(setting_ads_type)

        // PDF Viewer Screen
        val pdf_viewer_screen = screen.getJSONObject(GlobalParameterManage.PDF_VIEWER_SCREEN)
        val pdf_viewer_screen_bottom_ads_show =
            pdf_viewer_screen.getBoolean(GlobalParameterManage.PDF_VIEWER_SCREEN_BOTTOM_ADS_SHOW)
        val pdf_viewer_screen_ads_type =
            pdf_viewer_screen.getString(GlobalParameterManage.PDF_VIEWER_SCREEN_ADS_TYPE)
        val pdf_viewer_screen_banner_id =
            pdf_viewer_screen.getString(GlobalParameterManage.PDF_VIEWER_SCREEN_BANNER_ID)
        val pdf_viewer_screen_native_id =
            pdf_viewer_screen.getString(GlobalParameterManage.PDF_VIEWER_SCREEN_NATIVE_ID)
        ADSMainClass.setPdfViewerSmallAdsShow(pdf_viewer_screen_bottom_ads_show)
        ADSMainClass.setPdfViewerAdsType(pdf_viewer_screen_ads_type)

        // Call End Screen
        val callend_screen = screen.getJSONObject(GlobalParameterManage.CALLEND_SCREEN)

        val is_callend_show = callend_screen.getBoolean(GlobalParameterManage.IS_CALLEND_SHOW)
        val is_callend_bottom_ad_show =
            callend_screen.getBoolean(GlobalParameterManage.IS_CALLEND_BOTTOM_AD_SHOW)
        val callend_bottom_ads_type =
            callend_screen.getString(GlobalParameterManage.CALLEND_BOTTOM_ADS_TYPE)
        val callend_banner_ad_id =
            callend_screen.getString(GlobalParameterManage.CALLEND_BANNER_AD_ID)
        val callend_native_ad_id =
            callend_screen.getString(GlobalParameterManage.CALLEND_NATIVE_AD_ID)
        val notification_install_days =
            callend_screen.getInt(GlobalParameterManage.NOTIFICATION_INSTALL_DAYS)
        val notification_call_install_days =
            callend_screen.getInt(GlobalParameterManage.NOTIFICATION_CALL_INSTALL_DAYS)
        val notification_call_overlay_install_days =
            callend_screen.getInt(GlobalParameterManage.NOTIFICATION_CALL_OVERLAY_INSTALL_DAYS)
        val country_get_with_ip =
            callend_screen.getBoolean(GlobalParameterManage.COUNTRY_GET_WITH_IP)

        ADSMainClass.setIsShowCallEnd(is_callend_show)
        ADSMainClass.setCallEndBottomAdsShow(is_callend_bottom_ad_show)
        ADSMainClass.setCallEndBottomAdsType(callend_bottom_ads_type)
        ADSMainClass.setNotificationInstallDays(notification_install_days)
        ADSMainClass.setNotificationCallInstallDays(notification_call_install_days)
        ADSMainClass.setNotificationCallOverlayInstallDays(
            notification_call_overlay_install_days
        )
        val all_allow_notif =
            callend_screen.optBoolean(GlobalParameterManage.ALL_ALLOW_PERMISSION_SHOW_FB_NOTIFICATION)
        ADSMainClass.setAllAllowPermissionShowFbNotification(all_allow_notif)
        val close_button_show_on_full_native_ads =
            callend_screen.getBoolean(GlobalParameterManage.CLOSE_BUTTON_SHOW_ON_FULL_NATIVE_ADS)
        ADSMainClass.setCloseButtonShowOnFullNativeAds(
            close_button_show_on_full_native_ads
        )
        ADSMainClass.setCountryGetWithIp(country_get_with_ip)

        val notification_country_list = ArrayList<String>()
        val notification_country_array =
            callend_screen.getJSONArray(GlobalParameterManage.NOTIFICATION_COUNTRY)
        for (i in 0 until notification_country_array.length()) {
            notification_country_list.add(notification_country_array.getString(i))
        }
        ADSMainClass.setNotificationCountries(notification_country_list)

        val notification_call_country_list = ArrayList<String>()
        val notification_call_country_array =
            callend_screen.getJSONArray(GlobalParameterManage.NOTIFICATION_CALL_COUNTRY)
        for (i in 0 until notification_call_country_array.length()) {
            notification_call_country_list.add(
                notification_call_country_array.getString(
                    i
                )
            )
        }
        ADSMainClass.setNotificationCallCountries(notification_call_country_list)

        val notification_call_overlay_country_list = ArrayList<String>()
        val notification_call_overlay_country_array =
            callend_screen.getJSONArray(GlobalParameterManage.NOTIFICATION_CALL_OVERLAY_COUNTRY)
        for (i in 0 until notification_call_overlay_country_array.length()) {
            notification_call_overlay_country_list.add(
                notification_call_overlay_country_array.getString(i)
            )
        }
        ADSMainClass.setNotificationCallOverlayCountries(
            notification_call_overlay_country_list
        )


        val call_end_bacK_inter =
            callend_screen.getJSONObject(GlobalParameterManage.CALL_END_BACK_INTER)
        val call_end_inter_ads_show =
            call_end_bacK_inter.getBoolean(GlobalParameterManage.CALL_END_INTER_ADS_SHOW)
        val notification_screen_back_ads_show =
            call_end_bacK_inter.getBoolean(GlobalParameterManage.NOTIFICATION_SCREEN_BACK_ADS_SHOW)
        val call_end_inter_ads_type =
            call_end_bacK_inter.getString(GlobalParameterManage.CALL_END_INTER_ADS_TYPE)
        val call_end_inter_day_count =
            call_end_bacK_inter.getLong(GlobalParameterManage.CALL_END_INTER_DAY_COUNT)
        val callend_again_open_count =
            call_end_bacK_inter.getLong(GlobalParameterManage.CALLEND_AGAIN_OPEN_COUNT)
        val call_end_inter_active_total_show_count =
            call_end_bacK_inter.getLong(GlobalParameterManage.CALL_END_INTER_ACTIVE_TOTAL_SHOW_COUNT)
        val callend_inter_ad_id =
            call_end_bacK_inter.getString(GlobalParameterManage.CALLEND_INTER_AD_ID)


        val call_end_inter_ads_show_country = ArrayList<String>()
        try {
            val countryArray =
                call_end_bacK_inter.getJSONArray(GlobalParameterManage.CALL_END_INTER_ADS_SHOW_COUNTRY)
            for (i in 0 until countryArray.length()) {
                call_end_inter_ads_show_country.add(countryArray.getString(i))
            }
        } catch (e: Exception) {
        }

        ADSMainClass.setCallEndAdCountries(call_end_inter_ads_show_country)
        ADSMainClass.setCallEndInterAdsShow(call_end_inter_ads_show)
        ADSMainClass.setNotificationScreenBackAdsShow(
            notification_screen_back_ads_show
        )
        ADSMainClass.setCallEndInterAdsType(call_end_inter_ads_type)
        ADSMainClass.setCallEndInterDayCount(
            Math.toIntExact(
                call_end_inter_day_count
            )
        )
        ADSMainClass.setCallEndAgainOpenCount(
            Math.toIntExact(
                callend_again_open_count
            )
        )

        ADSMainClass.setCallEndInterShowCount(
            Math.toIntExact(
                call_end_inter_active_total_show_count
            )
        )

        val other_screen = screen.getJSONObject(GlobalParameterManage.OTHER_SCREEN)
        val inter_first_time = other_screen.getString(GlobalParameterManage.INTER_FIRST_TIME)
        val inter_second_time = other_screen.getString(GlobalParameterManage.INTER_SECOND_TIME)
        val other_native_id = other_screen.getString(GlobalParameterManage.OTHER_NATIVE_ID)
        val other_banner_id = other_screen.getString(GlobalParameterManage.OTHER_BANNER_ID)
        val splash_inter_ads_load_type =
            configJson.optString(GlobalParameterManage.INTER_ADS_LOAD_TYPE, "PreLoad")
        val app_inter_ads_load_type =
            other_screen.optString(GlobalParameterManage.OTHER_INTER_ADS_LOAD_TYPE, "PreLoad")
        val native_ads_load_type =
            configJson.optString(GlobalParameterManage.NATIVE_ADS_LOAD_TYPE, "PreLoad")
        val banner_ads_load_type =
            configJson.optString(GlobalParameterManage.BANNER_ADS_LOAD_TYPE, "PreLoad")
        val appopen_ads_load_type =
            configJson.optString(GlobalParameterManage.APPOPEN_ADS_LOAD_TYPE, "PreLoad")
        val other_bottom_ads_type =
            other_screen.getString(GlobalParameterManage.OTHER_BOTTOM_ADS_TYPE)
        val other_bottom_ads_show =
            other_screen.getBoolean(GlobalParameterManage.OTHER_BOTTOM_ADS_SHOW)
        val inter_ads_show = other_screen.getBoolean(GlobalParameterManage.INTER_ADS_SHOW)
        val inter_ads_show_on_back =
            other_screen.getBoolean(GlobalParameterManage.INTER_ADS_SHOW_ON_BACK)
        val exit_native_id = other_screen.getString(GlobalParameterManage.EXIT_NATIVE_ID)
        val watermark_dialog_show = other_screen.optBoolean(
            GlobalParameterManage.WATERMARK_DIALOG_SHOW,
            configJson.optBoolean(
                GlobalParameterManage.WATERMARK_DIALOG_SHOW,
                remoteConfig.getBoolean(GlobalParameterManage.WATERMARK_DIALOG_SHOW),
            ),
        )
        val watermark_reward_ads_unit = other_screen.optString(
            GlobalParameterManage.WATERMARK_REWARD_ADS_UNIT,
            configJson.optString(
                GlobalParameterManage.WATERMARK_REWARD_ADS_UNIT,
                remoteConfig.getString(GlobalParameterManage.WATERMARK_REWARD_ADS_UNIT),
            ),
        )
        ADSMainClass.setWatermarkDialogShow(watermark_dialog_show)
        ADSMainClass.setStringValue(
            ADSMainClass.WATERMARK_REWARD_ADS_UNIT,
            watermark_reward_ads_unit,
        )
        ADSMainClass.setInterAdsShow(inter_ads_show)
        ADSMainClass.setSplashInterAdsLoadType(splash_inter_ads_load_type)
        ADSMainClass.setInterAdsLoadType(app_inter_ads_load_type)
        ADSMainClass.setNativeAdsLoadType(native_ads_load_type)
        ADSMainClass.setBannerAdsLoadType(banner_ads_load_type)
        ADSMainClass.setAppOpenAdsLoadType(appopen_ads_load_type)
        ADSMainClass.setInterAdsOnBackShow(inter_ads_show_on_back)
        ADSMainClass.setOtherAdsType(other_bottom_ads_type)
        ADSMainClass.setOtherAdsShow(other_bottom_ads_show)

        parseAdditionalScreenBottomAds(
            context,
            screen,
            other_bottom_ads_show,
            other_bottom_ads_type,
            other_banner_id,
            other_native_id
        )

        val update_app = screen.getJSONObject(GlobalParameterManage.UPDATE_APP)
        val in_app_update_show = update_app.getBoolean(GlobalParameterManage.IN_APP_UPDATE_SHOW)
        val in_app_update_type = update_app.getString(GlobalParameterManage.IN_APP_UPDATE_TYPE)
        val in_appp_dailog_daily_show_count =
            update_app.getInt(GlobalParameterManage.IN_APP_DIALOG_DAILY_SHOW_COUNT)

        ADSMainClass.setInAppUpdateShow(in_app_update_show)
        ADSMainClass.setInAppUpdateType(in_app_update_type)
        ADSMainClass.setInApppDialogDailyShowCount(in_appp_dailog_daily_show_count)


        val overlay_notification_show =
            screen.optBoolean(
                GlobalParameterManage.OVERLAY_PERMISSION_NOTIFICATION_SHOW,
                configJson.optBoolean(
                    GlobalParameterManage.OVERLAY_PERMISSION_NOTIFICATION_SHOW,
                    false
                )
            )
        ADSMainClass.setOverlayPermissionNotificationShow(overlay_notification_show)

        val overlay_notification_days =
            screen.optInt(
                GlobalParameterManage.OVERLAY_PERMISSION_NOTIFICATION_DAYS_SHOW_COUNT,
                configJson.optInt(
                    GlobalParameterManage.OVERLAY_PERMISSION_NOTIFICATION_DAYS_SHOW_COUNT,
                    0
                )
            )
        ADSMainClass.setOverlayPermissionNotificationDaysShowCount(
            overlay_notification_days
        )

        parseInterAdsShower(context, screen, configJson)

        ADSMainClass.setStringValue(ADSMainClass.PERMISSION_SCREEN_NATIVE, permission_native_id)
        ADSMainClass.setStringValue(ADSMainClass.PERMISSION_SCREEN_BANNER, permission_banner_id)
        ADSMainClass.setStringValue(
            ADSMainClass.DEFAULT_PERMISSION_SCREEN_BANNER,
            default_permission_banner_id
        )
        ADSMainClass.setStringValue(
            ADSMainClass.DEFAULT_PERMISSION_SCREEN_NATIVE,
            default_permission_native_id
        )

        ADSMainClass.setStringValue(ADSMainClass.splash_banner_id, splash_banner_id)
        ADSMainClass.setStringValue(ADSMainClass.splash_native_id, splash_native_id)

        ADSMainClass.setStringValue(ADSMainClass.onboarding_banner_id, onboarding_banner_id)
        ADSMainClass.setStringValue(ADSMainClass.onboarding_native_id, onboarding_native_id)

        ADSMainClass.setStringValue(ADSMainClass.LANGUAGE_SCREEN_NATIVE, language_native_id)
        ADSMainClass.setStringValue(ADSMainClass.LANGUAGE_SCREEN_BANNER, language_banner_id)

        ADSMainClass.setStringValue(ADSMainClass.HOME_SCREEN_NATIVE, home_native_id)
        ADSMainClass.setStringValue(ADSMainClass.HOME_SCREEN_BANNER, home_banner_id)
        ADSMainClass.setStringValue(ADSMainClass.HOME_SCREEN_NATIVE1, home_native_list_id1)
        ADSMainClass.setStringValue(ADSMainClass.HOME_SCREEN_NATIVE2, home_native_list_id2)
        if (home_native_list_id.isNotEmpty()) {
            ADSMainClass.setStringValue(
                ADSMainClass.ALARM_LIST_NATIVE_AD,
                home_native_list_id,
            )
        }

        ADSMainClass.setStringValue(ADSMainClass.LAUNCHER_HOME_SCREEN_NATIVE, launcher_native_id)
        ADSMainClass.setStringValue(ADSMainClass.LAUNCHER_HOME_SCREEN_BANNER, launcher_banner_id)

        ADSMainClass.setStringValue(ADSMainClass.RECENT_LAUNCHER_SCREEN_NATIVE, recent_native_id)
        ADSMainClass.setStringValue(ADSMainClass.RECENT_LAUNCHER_SCREEN_BANNER, recent_banner_id)


        ADSMainClass.setStringValue(ADSMainClass.SETTING_SCREEN_NATIVE, setting_native_id)
        ADSMainClass.setStringValue(ADSMainClass.SETTING_SCREEN_BANNER, setting_banner_id)

        ADSMainClass.setStringValue(ADSMainClass.PDF_VIEWER_NATIVE, pdf_viewer_screen_native_id)
        ADSMainClass.setStringValue(ADSMainClass.PDF_VIEWER_BANNER, pdf_viewer_screen_banner_id)

        ADSMainClass.setStringValue(ADSMainClass.CALL_END_Native, callend_native_ad_id)
        ADSMainClass.setStringValue(ADSMainClass.CALL_END_BANNER, callend_banner_ad_id)
        ADSMainClass.setStringValue(ADSMainClass.CALL_END_Inter, callend_inter_ad_id)


        ADSMainClass.setStringValue(ADSMainClass.APP_OPEN_ID, app_open_id)
        ADSMainClass.setStringValue(ADSMainClass.INTER_FIRST_TIME, inter_first_time)
        ADSMainClass.setStringValue(ADSMainClass.INTER_SECOND_TIME, inter_second_time)
        ADSMainClass.setStringValue(ADSMainClass.OTHER_SCREEN_NATIVE, other_native_id)
        ADSMainClass.setStringValue(ADSMainClass.OTHER_SCREEN_BANNER, other_banner_id)
        ADSMainClass.setStringValue(ADSMainClass.EXIT_SCREEN_NATIVE, exit_native_id)




        ADSMainClass.setExitAds(exit_ads)
        ADSMainClass.setComingSoon(coming_soon)
        ADSMainClass.setPrivacyPolicy(privacy_policy)
        FirebaseAnalytics.getInstance(context)
            .setAnalyticsCollectionEnabled(firebaseanalytics)
        ADSMainClass.setSplashADType(splash_ads_type)
        ADSMainClass.setAds_Free(ads_blocker)

        ADSMainClass.setAdsClick(Math.toIntExact(ads_click))
        ADSMainClass.setAdsBackClick(Math.toIntExact(back_click))
        ADSMainClass.setNativeByPage(Math.toIntExact(native_by_page))

        parseStartupFlowConfig(context, configJson)
        parseQuizAdsConfig(context, configJson)
        parseAdsLocationConfig(context, configJson)
        return update_coming_soon
    }

    private fun stringMarketingConverter(str: String): String? {
        if (str.isBlank()) return ""
        val arr = str.split("=".toRegex(), limit = 2).toTypedArray()
        return arr[0]
    }

    /**
     * Returns first utm key=value pair.
     * Example: utm_source=marketing&utm_medium=cpc -> utm_source=marketing
     */
    private fun managemarketingConverter(str: String): String {
        if (str.isBlank()) return ""
        val firstParam = str.substringBefore('&')
        return firstParam
    }

    /**
     * Returns first query param (before '&').
     * If there is no '&', returns the full string so plain utm_source=... still matches.
     */
    private fun facebookMarketingConverter(input: String): String {
        if (input.isBlank()) return ""
        val index = input.indexOf('&')
        return if (index != -1) {
            input.substring(0, index)
        } else {
            input
        }
    }

    private fun isPaidMarketingReferrer(context: Context, referrerUrl: String): Boolean {
        if (referrerUrl.isBlank()) return false

        val marketingUrl = managemarketingConverter(referrerUrl)
        val facebookUser = facebookMarketingConverter(referrerUrl)
        val newString = stringMarketingConverter(referrerUrl)

        // gclid can appear anywhere in the referrer query string
        val hasGclid = referrerUrl.contains("gclid=", ignoreCase = true) ||
                newString.equals(
                    context.getString(R.string.markrting_converter1),
                    ignoreCase = true
                )

        return hasGclid ||
                facebookUser.equals(
                    context.getString(R.string.utm_source_apps_facebook_com),
                    ignoreCase = true
                ) ||
                facebookUser.equals(
                    context.getString(R.string.utm_source_apps_instagram_com),
                    ignoreCase = true
                ) ||
                marketingUrl.equals(
                    context.getString(R.string.utm_source_marketing),
                    ignoreCase = true
                ) ||
                referrerUrl.contains(
                    context.getString(R.string.utm_source_marketing),
                    ignoreCase = true
                )
    }

    private data class ScreenBottomAdConfig(
        val show: Boolean,
        val type: String,
        val bannerId: String,
        val nativeId: String,
    )

    private fun parseScreenBottomAdConfig(
        screen: JSONObject,
        screenKey: String,
        showKey: String,
        typeKey: String,
        bannerKey: String,
        nativeKey: String,
        fallbackShow: Boolean,
        fallbackType: String,
        fallbackBannerId: String,
        fallbackNativeId: String,
    ): ScreenBottomAdConfig {
        val screenObject = screen.optJSONObject(screenKey)
        return ScreenBottomAdConfig(
            show = screenObject?.optBoolean(showKey, fallbackShow) ?: fallbackShow,
            type = screenObject?.optString(typeKey, fallbackType) ?: fallbackType,
            bannerId = screenObject?.optString(bannerKey, fallbackBannerId) ?: fallbackBannerId,
            nativeId = screenObject?.optString(nativeKey, fallbackNativeId) ?: fallbackNativeId,
        )
    }

    private fun applyScreenBottomAdConfig(
        config: ScreenBottomAdConfig,
        setShow: (Boolean) -> Unit,
        setType: (String) -> Unit,
        bannerPrefKey: String,
        nativePrefKey: String,
    ) {
        setShow(config.show)
        setType(config.type)
        ADSMainClass.setStringValue(bannerPrefKey, config.bannerId)
        ADSMainClass.setStringValue(nativePrefKey, config.nativeId)
    }

    private fun parseAdditionalScreenBottomAds(
        context: Context,
        screen: JSONObject,
        fallbackShow: Boolean,
        fallbackType: String,
        fallbackBannerId: String,
        fallbackNativeId: String,
    ) {
        applyScreenBottomAdConfig(
            parseScreenBottomAdConfig(
                screen,
                GlobalParameterManage.RECYCLE_BIN_SCREEN,
                GlobalParameterManage.RECYCLE_BIN_BOTTOM_ADS_SHOW,
                GlobalParameterManage.RECYCLE_BIN_ADS_TYPE,
                GlobalParameterManage.RECYCLE_BIN_BANNER_ID,
                GlobalParameterManage.RECYCLE_BIN_NATIVE_ID,
                fallbackShow,
                fallbackType,
                fallbackBannerId,
                fallbackNativeId,
            ),
            ADSMainClass::setRecycleBinBottomAdsShow,
            ADSMainClass::setRecycleBinAdsType,
            ADSMainClass.RECYCLE_BIN_SCREEN_BANNER,
            ADSMainClass.RECYCLE_BIN_SCREEN_NATIVE,
        )

        applyScreenBottomAdConfig(
            parseScreenBottomAdConfig(
                screen,
                GlobalParameterManage.SEARCH_ALL_FILES_SCREEN,
                GlobalParameterManage.SEARCH_ALL_FILES_BOTTOM_ADS_SHOW,
                GlobalParameterManage.SEARCH_ALL_FILES_ADS_TYPE,
                GlobalParameterManage.SEARCH_ALL_FILES_BANNER_ID,
                GlobalParameterManage.SEARCH_ALL_FILES_NATIVE_ID,
                fallbackShow,
                fallbackType,
                fallbackBannerId,
                fallbackNativeId,
            ),
            ADSMainClass::setSearchAllFilesBottomAdsShow,
            ADSMainClass::setSearchAllFilesAdsType,
            ADSMainClass.SEARCH_ALL_FILES_SCREEN_BANNER,
            ADSMainClass.SEARCH_ALL_FILES_SCREEN_NATIVE,
        )

        applyScreenBottomAdConfig(
            parseScreenBottomAdConfig(
                screen,
                GlobalParameterManage.FAQ_SCREEN,
                GlobalParameterManage.FAQ_BOTTOM_ADS_SHOW,
                GlobalParameterManage.FAQ_ADS_TYPE,
                GlobalParameterManage.FAQ_BANNER_ID,
                GlobalParameterManage.FAQ_NATIVE_ID,
                fallbackShow,
                fallbackType,
                fallbackBannerId,
                fallbackNativeId,
            ),
            ADSMainClass::setFaqBottomAdsShow,
            ADSMainClass::setFaqAdsType,
            ADSMainClass.FAQ_SCREEN_BANNER,
            ADSMainClass.FAQ_SCREEN_NATIVE,
        )

        applyScreenBottomAdConfig(
            parseScreenBottomAdConfig(
                screen,
                GlobalParameterManage.PDF_EDIT_SCREEN,
                GlobalParameterManage.PDF_EDIT_BOTTOM_ADS_SHOW,
                GlobalParameterManage.PDF_EDIT_ADS_TYPE,
                GlobalParameterManage.PDF_EDIT_BANNER_ID,
                GlobalParameterManage.PDF_EDIT_NATIVE_ID,
                fallbackShow,
                fallbackType,
                fallbackBannerId,
                fallbackNativeId,
            ),
            ADSMainClass::setPdfEditBottomAdsShow,
            ADSMainClass::setPdfEditAdsType,
            ADSMainClass.PDF_EDIT_SCREEN_BANNER,
            ADSMainClass.PDF_EDIT_SCREEN_NATIVE,
        )

        applyScreenBottomAdConfig(
            parseScreenBottomAdConfig(
                screen,
                GlobalParameterManage.PDF_MANAGE_PAGES_SCREEN,
                GlobalParameterManage.PDF_MANAGE_PAGES_BOTTOM_ADS_SHOW,
                GlobalParameterManage.PDF_MANAGE_PAGES_ADS_TYPE,
                GlobalParameterManage.PDF_MANAGE_PAGES_BANNER_ID,
                GlobalParameterManage.PDF_MANAGE_PAGES_NATIVE_ID,
                fallbackShow,
                fallbackType,
                fallbackBannerId,
                fallbackNativeId,
            ),
            ADSMainClass::setPdfManagePagesBottomAdsShow,
            ADSMainClass::setPdfManagePagesAdsType,
            ADSMainClass.PDF_MANAGE_PAGES_SCREEN_BANNER,
            ADSMainClass.PDF_MANAGE_PAGES_SCREEN_NATIVE,
        )

        applyScreenBottomAdConfig(
            parseScreenBottomAdConfig(
                screen,
                GlobalParameterManage.PAGE_SETUP_SCREEN,
                GlobalParameterManage.PAGE_SETUP_BOTTOM_ADS_SHOW,
                GlobalParameterManage.PAGE_SETUP_ADS_TYPE,
                GlobalParameterManage.PAGE_SETUP_BANNER_ID,
                GlobalParameterManage.PAGE_SETUP_NATIVE_ID,
                fallbackShow,
                fallbackType,
                fallbackBannerId,
                fallbackNativeId,
            ),
            ADSMainClass::setPageSetupBottomAdsShow,
            ADSMainClass::setPageSetupAdsType,
            ADSMainClass.PAGE_SETUP_SCREEN_BANNER,
            ADSMainClass.PAGE_SETUP_SCREEN_NATIVE,
        )

        applyScreenBottomAdConfig(
            parseScreenBottomAdConfig(
                screen,
                GlobalParameterManage.CREATE_PDF_SCREEN,
                GlobalParameterManage.CREATE_PDF_BOTTOM_ADS_SHOW,
                GlobalParameterManage.CREATE_PDF_ADS_TYPE,
                GlobalParameterManage.CREATE_PDF_BANNER_ID,
                GlobalParameterManage.CREATE_PDF_NATIVE_ID,
                fallbackShow,
                fallbackType,
                fallbackBannerId,
                fallbackNativeId,
            ),
            ADSMainClass::setCreatePdfBottomAdsShow,
            ADSMainClass::setCreatePdfAdsType,
            ADSMainClass.CREATE_PDF_SCREEN_BANNER,
            ADSMainClass.CREATE_PDF_SCREEN_NATIVE,
        )

        applyScreenBottomAdConfig(
            parseScreenBottomAdConfig(
                screen,
                GlobalParameterManage.PDF_SELECTION_SCREEN,
                GlobalParameterManage.PDF_SELECTION_BOTTOM_ADS_SHOW,
                GlobalParameterManage.PDF_SELECTION_ADS_TYPE,
                GlobalParameterManage.PDF_SELECTION_BANNER_ID,
                GlobalParameterManage.PDF_SELECTION_NATIVE_ID,
                fallbackShow,
                fallbackType,
                fallbackBannerId,
                fallbackNativeId,
            ),
            ADSMainClass::setPdfSelectionBottomAdsShow,
            ADSMainClass::setPdfSelectionAdsType,
            ADSMainClass.PDF_SELECTION_SCREEN_BANNER,
            ADSMainClass.PDF_SELECTION_SCREEN_NATIVE,
        )

        applyScreenBottomAdConfig(
            parseScreenBottomAdConfig(
                screen,
                GlobalParameterManage.SUCCESSFULLY_CONVERTED_SCREEN,
                GlobalParameterManage.SUCCESSFULLY_CONVERTED_BOTTOM_ADS_SHOW,
                GlobalParameterManage.SUCCESSFULLY_CONVERTED_ADS_TYPE,
                GlobalParameterManage.SUCCESSFULLY_CONVERTED_BANNER_ID,
                GlobalParameterManage.SUCCESSFULLY_CONVERTED_NATIVE_ID,
                fallbackShow,
                fallbackType,
                fallbackBannerId,
                fallbackNativeId,
            ),
            ADSMainClass::setSuccessfullyConvertedBottomAdsShow,
            ADSMainClass::setSuccessfullyConvertedAdsType,
            ADSMainClass.SUCCESSFULLY_CONVERTED_SCREEN_BANNER,
            ADSMainClass.SUCCESSFULLY_CONVERTED_SCREEN_NATIVE,
        )

        applyScreenBottomAdConfig(
            parseScreenBottomAdConfig(
                screen,
                GlobalParameterManage.PDF_SIGNATURE_SCREEN,
                GlobalParameterManage.PDF_SIGNATURE_BOTTOM_ADS_SHOW,
                GlobalParameterManage.PDF_SIGNATURE_ADS_TYPE,
                GlobalParameterManage.PDF_SIGNATURE_BANNER_ID,
                GlobalParameterManage.PDF_SIGNATURE_NATIVE_ID,
                fallbackShow,
                fallbackType,
                fallbackBannerId,
                fallbackNativeId,
            ),
            ADSMainClass::setPdfSignatureBottomAdsShow,
            ADSMainClass::setPdfSignatureAdsType,
            ADSMainClass.PDF_SIGNATURE_SCREEN_BANNER,
            ADSMainClass.PDF_SIGNATURE_SCREEN_NATIVE,
        )
    }

    private fun parseInterAdsShower(context: Context, screen: JSONObject, configJson: JSONObject) {
        val interAdsShower = screen.optJSONObject(GlobalParameterManage.INTER_ADS_SHOWER)
            ?: configJson.optJSONObject(GlobalParameterManage.INTER_ADS_SHOWER)

        Log.e("interAdsShower", "parsed inter_ads_shower: $interAdsShower")

        if (interAdsShower == null) {
            Log.e(
                "interAdsShower",
                "inter_ads_shower missing — place it inside \"screen\" in Firebase Remote Config"
            )
            return
        }

        ADSMainClass.setPermissionScreenInterShow(
            interAdsShower.optBoolean(GlobalParameterManage.PERMISSION_SCREEN_INTER_SHOW, false)
        )
        ADSMainClass.setHomeInterShow(
            interAdsShower.optBoolean(GlobalParameterManage.HOME_INTER_SHOW, false)
        )
        ADSMainClass.setLanguageInterShow(
            interAdsShower.optBoolean(GlobalParameterManage.LANGUAGE_INTER_SHOW, false)
        )
        ADSMainClass.setSettingInterShow(
            interAdsShower.optBoolean(GlobalParameterManage.SETTING_INTER_SHOW, false)
        )
        ADSMainClass.setRecycleBinInterShow(
            interAdsShower.optBoolean(GlobalParameterManage.RECYCLE_BIN_INTER_SHOW, false)
        )
        ADSMainClass.setCreatePdfInterShow(
            interAdsShower.optBoolean(GlobalParameterManage.CREATE_PDF_INTER_SHOW, false)
        )
        ADSMainClass.setSearchInterShow(
            interAdsShower.optBoolean(GlobalParameterManage.SEARCH_INTER_SHOW, false)
        )
        ADSMainClass.setPdfViewerInterShow(
            interAdsShower.optBoolean(GlobalParameterManage.PDF_VIEWER_INTER_SHOW, false)
        )
        ADSMainClass.setPreviewShareInterShow(
            interAdsShower.optBoolean(GlobalParameterManage.PREVIEW_SHARE_INTER_SHOW, false)
        )
        ADSMainClass.setPageSetupInterShow(
            interAdsShower.optBoolean(GlobalParameterManage.PAGE_SETUP_INTER_SHOW, false)
        )
        ADSMainClass.setOrganizeImageInterShow(
            interAdsShower.optBoolean(GlobalParameterManage.ORGANIZE_IMAGE_INTER_SHOW, false)
        )
        ADSMainClass.setEditInterShow(
            interAdsShower.optBoolean(GlobalParameterManage.EDIT_INTER_SHOW, false)
        )
        ADSMainClass.setOfficeViewerInterShow(
            interAdsShower.optBoolean(GlobalParameterManage.OFFICE_VIEWER_INTER_SHOW, false)
        )
        ADSMainClass.setOpenCreatePdfInterShow(
            interAdsShower.optBoolean(GlobalParameterManage.OPEN_CREATE_PDF_INTER_SHOW, false)
        )
        ADSMainClass.setPdfOpenInterShow(
            interAdsShower.optBoolean(GlobalParameterManage.PDF_OPEN_INTER_SHOW, false)
        )
        ADSMainClass.setMergePdfInterShow(
            interAdsShower.optBoolean(GlobalParameterManage.MERGE_PDF_INTER_SHOW, false)
        )
        ADSMainClass.setSplitPdfInterShow(
            interAdsShower.optBoolean(GlobalParameterManage.SPLIT_PDF_INTER_SHOW, false)
        )
        ADSMainClass.setPdfToImageInterShow(
            interAdsShower.optBoolean(GlobalParameterManage.PDF_TO_IMAGE_INTER_SHOW, false)
        )
        ADSMainClass.setFaqInterShow(
            interAdsShower.optBoolean(GlobalParameterManage.FAQ_INTER_SHOW, false)
        )
        ADSMainClass.setSignatureInterShow(
            interAdsShower.optBoolean(GlobalParameterManage.SIGNATURE_INTER_SHOW, false)
        )
        ADSMainClass.setMediaPickerInterShow(
            interAdsShower.optBoolean(GlobalParameterManage.MEDIA_PICKER_INTER_SHOW, false)
        )
        ADSMainClass.setScanInterShow(
            interAdsShower.optBoolean(GlobalParameterManage.SCAN_INTER_SHOW, false)
        )
        ADSMainClass.setWatermarkInterShow(
            interAdsShower.optBoolean(GlobalParameterManage.WATERMARK_INTER_SHOW, false)
        )
    }

    private fun parseQuizAdsConfig(context: Context, configJson: JSONObject) {
        try {
            if (configJson.has(GlobalParameterManage.AD_PRIORITY)
                || configJson.has(GlobalParameterManage.GOOGLE_AD_FAILED_SHOW_QUIZ)
            ) {
                ADSMainClass.applyQuizAdsConfig(configJson)
            }
            if (configJson.has(GlobalParameterManage.LAUNCHER_APP_CLICK_AD_SHOW)
                || configJson.has(GlobalParameterManage.LAUNCHER_APP_COUNT)
                || configJson.has(GlobalParameterManage.LAUNCHER_APP_AD_TYPE)
                || configJson.has(GlobalParameterManage.LAUNCHER_APP_INTERSTITIAL_ID)
                || configJson.has(GlobalParameterManage.IS_LINK_OPEN_APP)
            ) {
                ADSMainClass.applyLauncherAppConfig(configJson)
            }
            val quizAdsDesign = configJson.optJSONObject(GlobalParameterManage.QUIZ_ADS_DESIGN)
            if (quizAdsDesign != null) {
                // Merge root priority flags into design object when present on root
                if (configJson.has(GlobalParameterManage.AD_PRIORITY)) {
                    quizAdsDesign.put(
                        GlobalParameterManage.AD_PRIORITY,
                        configJson.optString(GlobalParameterManage.AD_PRIORITY, "")
                    )
                }
                if (configJson.has(GlobalParameterManage.GOOGLE_AD_FAILED_SHOW_QUIZ)) {
                    quizAdsDesign.put(
                        GlobalParameterManage.GOOGLE_AD_FAILED_SHOW_QUIZ,
                        configJson.optBoolean(
                            GlobalParameterManage.GOOGLE_AD_FAILED_SHOW_QUIZ,
                            false
                        )
                    )
                }
                ADSMainClass.applyQuizAdsConfig(quizAdsDesign)
            }
            val appProxy = configJson.optJSONObject(GlobalParameterManage.APP_PROXY_STRUCTURE)
            if (appProxy != null) {
                ADSMainClass.applyAppProxyConfig(appProxy)
            }
        } catch (e: Exception) {
            Log.e("QuizAds", "parseQuizAdsConfig: ${e.message}")
        }
    }

    /** Ads_Show_countryList / Ads_show_List_Exclude (profile root) and the IP checker URL of AppProxyStructure. */
    private fun parseAdsLocationConfig(context: Context, configJson: JSONObject) {
        val proxy = configJson.optJSONObject(GlobalParameterManage.APP_PROXY_STRUCTURE)
        AdsLocationGate.configure(
            context,
            configJson.opt(GlobalParameterManage.ADS_SHOW_COUNTRY_LIST),
            configJson.opt(GlobalParameterManage.ADS_SHOW_LIST_EXCLUDE),
            proxy?.optString("App_Proxy_Ip_Checker_Url", "")
        )
    }

    private fun parseStartupFlowConfig(context: Context, configJson: JSONObject) {
        val flowSteps = mutableListOf<String>()
        val flowArray = configJson.optJSONArray(GlobalParameterManage.SHOW_SCREEN_FLOW)
            ?: configJson.optJSONArray(GlobalParameterManage.SHOW_INTRO_SCREEN_FLOW)
        if (flowArray != null) {
            for (i in 0 until flowArray.length()) {
                flowSteps.add(flowArray.getString(i))
            }
        }
        StartupFlowManager.saveFlow(context, flowSteps)
        StartupFlowManager.saveDefaultLauncherConfig(
            context,
            force = configJson.optBoolean(GlobalParameterManage.FORCE_DEFAULT_LAUNCHER_SHOW, false)
        )
    }
}

