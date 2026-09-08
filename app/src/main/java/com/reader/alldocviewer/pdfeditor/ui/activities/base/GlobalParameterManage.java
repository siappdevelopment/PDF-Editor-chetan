package com.reader.alldocviewer.pdfeditor.ui.activities.base;

/**
 * Firebase Remote Config JSON keys — screen-wise constants.
 * All Firebase config keys are defined here and used from BaseSplashActivity.
 */
public class GlobalParameterManage {

    private GlobalParameterManage() {
    }

    // ==================== Root Config user types ====================
    /** Organic / non-marketing user config block */
    public static final String NORMAL_USER = "normal_user";
    /** Marketing / paid-campaign user config block */
    public static final String PAID_USER = "paid_user";

    // ==================== Root Config (configJson) ====================
    /** App privacy policy URL */
    public static final String APP_PRIVACY_POLICY = "App_PrivacyPolicy";
    /** Splash screen ad load type (Appopen / Inter / etc.) */
    public static final String SPLASH_AD_LOAD_TYPE = "Splash_Ad_LoadType";
    /** Total ads click count limit */
    public static final String ADS_CLICK = "ADSClick";
    /** Back button click count limit for ads */
    public static final String BACKS_CLICK = "BacksClick";
    /** Native ad show frequency per page */
    public static final String NATIVE_BY_PAGE = "Native_ByPage";
    /** Enable / disable all ads (ads blocker) */
    public static final String ADS_BLOCKS = "AdsBlocks";
    /** Show coming soon screen */
    public static final String COMING_SOON = "Coming_Soon";
    /** Show exit dialog with ad */
    public static final String EXIT_DIALOG = "ExitDialog";
    /** Enable Firebase analytics collection */
    public static final String FIREBASE_ANALYTICS = "Firebaseanalytics";
    /** Show update coming soon screen */
    public static final String UPDATE_COMING_SOON = "Update_Coming_Soon";
    public static final String consent_form_show_screen = "consent_form_show_screen";
    /** Global interstitial ads load type */
    public static final String INTER_ADS_LOAD_TYPE = "inter_ads_load_type";
    /** Global native ads load type */
    public static final String NATIVE_ADS_LOAD_TYPE = "native_ads_load_type";
    /** Global banner ads load type */
    public static final String BANNER_ADS_LOAD_TYPE = "banner_ads_load_type";
    /** Global app open ads load type */
    public static final String APPOPEN_ADS_LOAD_TYPE = "appopen_ads_load_type";
    /** Startup intro screen flow steps array */
    public static final String SHOW_INTRO_SCREEN_FLOW = "show_intro_screen_flow";
    /** Show overlay permission notification */
    public static final String OVERLAY_PERMISSION_NOTIFICATION_SHOW = "overlay_permission_notification_show";
    /** Overlay permission notification show days count */
    public static final String OVERLAY_PERMISSION_NOTIFICATION_DAYS_SHOW_COUNT = "overlay_permission_notification_days_show_count";

    // ==================== inter_ads_shower ====================
    /** Screen-wise interstitial ad show flags (click and back) — excludes splash */
    public static final String INTER_ADS_SHOWER = "inter_ads_shower";
    /** Show interstitial on permission screen (click and back) */
    public static final String PERMISSION_SCREEN_INTER_SHOW = "permission_screen_inter_show";
    /** Show interstitial on home screen (click and back) */
    public static final String HOME_INTER_SHOW = "home_inter_show";
    /** Show interstitial on language screen (click and back) */
    public static final String LANGUAGE_INTER_SHOW = "language_inter_show";
    /** Show interstitial on settings screen (click and back) */
    public static final String SETTING_INTER_SHOW = "setting_inter_show";
    /** Show interstitial on recycle bin screen (click and back) */
    public static final String RECYCLE_BIN_INTER_SHOW = "recycle_bin_inter_show";
    /** Show interstitial on create PDF screen (click and back) */
    public static final String CREATE_PDF_INTER_SHOW = "create_pdf_inter_show";
    /** Show interstitial on search files screen (click and back) */
    public static final String SEARCH_INTER_SHOW = "search_inter_show";
    /** Show interstitial on PDF viewer screen (click and back) */
    public static final String PDF_VIEWER_INTER_SHOW = "pdf_viewer_inter_show";
    /** Show interstitial on preview/share screen (click and back) */
    public static final String PREVIEW_SHARE_INTER_SHOW = "preview_share_inter_show";
    /** Show interstitial on page setup screen (click and back) */
    public static final String PAGE_SETUP_INTER_SHOW = "page_setup_inter_show";
    /** Show interstitial on organize image screen (click and back) */
    public static final String ORGANIZE_IMAGE_INTER_SHOW = "organize_image_inter_show";
    /** Show interstitial on edit screen (click and back) */
    public static final String EDIT_INTER_SHOW = "edit_inter_show";
    /** Show interstitial on office viewer screen (click and back) */
    public static final String OFFICE_VIEWER_INTER_SHOW = "office_viewer_inter_show";
    /** Show interstitial on open/create PDF screen (click and back) */
    public static final String OPEN_CREATE_PDF_INTER_SHOW = "open_create_pdf_inter_show";
    /** Show interstitial on PDF open screen (click and back) */
    public static final String PDF_OPEN_INTER_SHOW = "pdf_open_inter_show";
    /** Show interstitial on merge PDF screen (click and back) */
    public static final String MERGE_PDF_INTER_SHOW = "merge_pdf_inter_show";
    /** Show interstitial on split PDF screen (click and back) */
    public static final String SPLIT_PDF_INTER_SHOW = "split_pdf_inter_show";
    /** Show interstitial on PDF to image screen (click and back) */
    public static final String PDF_TO_IMAGE_INTER_SHOW = "pdf_to_image_inter_show";
    /** Show interstitial on FAQ screen (click and back) */
    public static final String FAQ_INTER_SHOW = "faq_inter_show";
    /** Show interstitial on signature screen (click and back) */
    public static final String SIGNATURE_INTER_SHOW = "signature_inter_show";
    /** Show interstitial on media picker screen (click and back) */
    public static final String MEDIA_PICKER_INTER_SHOW = "media_picker_inter_show";
    /** Show interstitial on scan screen (click and back) */
    public static final String SCAN_INTER_SHOW = "scan_inter_show";
    /** Show interstitial on watermark screen (click and back) */
    public static final String WATERMARK_INTER_SHOW = "watermark_inter_show";

    // ==================== screen ====================
    /** Parent JSON object — all screen-wise ad configs */
    public static final String SCREEN = "screen";

    // ==================== ads_layout ====================
    /** Native ad layout styling — button margin, light/dark mode colors */
    public static final String ADS_LAYOUT = "ads_layout";
    public static final String LIGHT_MODE = "light_mode";
    public static final String DARK_MODE = "dark_mode";
    /** Native ad button margin */
    public static final String BUTTON_MARGIN = "button_margin";
    /** Native ad button background color */
    public static final String NATIVE_ADS_BUTTON_COLOR = "Native_Ads_Button_Color";
    /** Native ad background color */
    public static final String NATIVE_AD_BACKGROUND_COLOR = "Native_Ad_Background_Color";
    /** Native ad all text color */
    public static final String NATIVE_AD_ALL_TEXT_COLOR = "Native_Ad_All_Text_Color";
    /** Native ad button text color */
    public static final String NATIVE_AD_BUTTON_TEXT_COLOR = "Native_Ad_Button_Text_Color";

    // ==================== splash_screen ====================
    /** Splash screen — app open, banner, native ad IDs and ad type */
    public static final String SPLASH_SCREEN = "splash_screen";
    /** Show app open ad on splash */
    public static final String SPLASH_APP_OPEN_ADS_SHOW = "splash_app_open_ads_show";
    /** App open ad show after this open count */
    public static final String SPLASH_APP_OPEN_AFTER_COUNT = "splash_app_open_after_count";
    /** Show app open ad when app returns from background */
    public static final String APP_OPEN_SHOW_IN_BACKGROUND = "app_open_show_in_background";
    /** Daily app open ad show limit */
    public static final String APP_OPEN_DAILY_SHOW_COUNT = "app_open_daily_show_count";
    public static final String APP_OPEN_ID = "app_open_id";
    public static final String SPLASH_BANNER_ID = "splash_banner_id";
    public static final String SPLASH_NATIVE_ID = "splash_native_id";
    public static final String SPLASH_ADS_TYPE = "splash_ads_type";
    /** Show bottom ad on splash screen */
    public static final String SPLASH_SCREEN_BOTTOM_AD_SHOW = "splash_screen_bottom_ad_show";

    // ==================== language_screen ====================
    /** Language selection screen — ad type and ad unit IDs */
    public static final String LANGUAGE_SCREEN = "language_screen";
    /** Navigate from splash to language screen */
    public static final String SPLASH_TO_LANGUAGE = "splash_to_language";
    /** Show bottom ad on language screen */
    public static final String LANGUAGE_SCREEN_BOTTOM_AD_SHOW = "language_screen_bottom_ad_show";
    public static final String LANGUAGE_ADS_TYPE = "language_ads_type";
    /** Show interstitial ad on language screen */
    public static final String LANGUAGE_INTER_ADS_SHOW = "language_inter_ads_show";
    public static final String LANGUAGE_BANNER_ID = "language_banner_id";
    public static final String LANGUAGE_NATIVE_ID = "language_native_id";

    // ==================== onboarding_intro ====================
    /** Onboarding / intro screens — ad type and ad unit IDs */
    public static final String ONBOARDING_INTRO = "onboarding_intro";
    /** Number of onboarding intro screens to show */
    public static final String ONBOARDING_COUNT_SHOW = "onboarding_count_show";
    public static final String ONBOARDING_ADS_TYPE = "onboarding_ads_type";
    /** Show interstitial ad on onboarding screen */
    public static final String ONBOARDING_INTER_ADS_SHOW = "onboarding_inter_ads_show";
    /** Show bottom ad on onboarding screen */
    public static final String ONBOARDING_SCREEN_BOTTOM_AD_SHOW = "onboarding_screen_bottom_ad_show";
    public static final String ONBOARDING_NATIVE_ID = "onboarding_native_id";
    public static final String ONBOARDING_BANNER_ID = "onboarding_banner_id";

    // ==================== permission_screen ====================
    /** Permission screen — ad type and ad unit IDs */
    public static final String PERMISSION_SCREEN = "permission_screen";
    /** Show small (banner/native) ad on permission screen */
    public static final String PERMISSION_SMALL_ADS_SHOW = "permission_small_ads_show";
    public static final String PERMISSION_ADS_TYPE = "permission_ads_type";
    public static final String PERMISSION_NATIVE_ID = "permission_native_id";
    public static final String PERMISSION_BANNER_ID = "permission_banner_id";
    /** Show AppOpen/Inter ad after DefaultAppActivity Set as Default button result */
    public static final String DEFAULT_PERMISSION_BUTTON_ADS_SHOW = "default_permission_button_ads_show";
    /** Ad type for DefaultAppActivity button result — "Appopen" or "Inter" */
    public static final String DEFAULT_PERMISSION_BUTTON_ADS_TYPE = "default_permission_button_ads_type";

    // ==================== default_permission_screem ====================
    /** Default app permission screen — bottom ad type and ad unit IDs */
    public static final String DEFAULT_PERMISSION_SCREEM = "default_permission_screem";
    /** Show bottom ad on default permission screen */
    public static final String DEFAULT_PERMISSION_BOTTOM_ADS_SHOW = "default_permission_bottom_ads_show";
    public static final String DEFAULT_PERMISSION_ADS_TYPE = "default_permission_ads_type";
    public static final String DEFAULT_PERMISSION_BANNER_ID = "default_permission_banner_id";
    public static final String DEFAULT_PERMISSION_NATIVE_ID = "default_permission_native_id";

    // ==================== home_screen ====================
    /** Home screen — bottom ad type and ad unit IDs */
    public static final String HOME_SCREEN = "home_screen";
    /** Show bottom ad on home screen */
    public static final String HOME_SCREEN_BOTTOM_SHOW = "home_screen_bottom_show";
    public static final String HOME_SCREEN_ADS_TYPE = "home_screen_ads_type";
    public static final String HOME_BANNER_ID = "home_banner_id";
    public static final String HOME_NATIVE_ID = "home_native_id";
    /** Show native ad at RecyclerView position 0 on home file tabs */
    public static final String FIRST_NATIVE_POSITION1 = "first_native_position1";
    /** Show native ad at RecyclerView position 4 on home file tabs */
    public static final String FIRST_NATIVE_POSITION3 = "first_native_position3";
    /** Native ad unit id for list position 0 */
    public static final String HOME_NATIVE_LIST_ID1 = "home_native_list_id1";
    /** Native ad unit id for list position 4 */
    public static final String HOME_NATIVE_LIST_ID2 = "home_native_list_id2";
    /** Shared native ad unit id for home file list (fallback) */
    public static final String HOME_NATIVE_LIST_ID = "home_native_list_id";
    /** Show interstitial on bottom nav tab click */
    public static final String HOME_BOTTOM_NAV_INTER_SHOW = "home_bottom_nav_inter_show";
    /** Bottom nav click count before interstitial */
    public static final String HOME_BOTTOM_COUNT = "home_bottom_count";
    /** Show default-launcher custom dialog on Home screen */
    public static final String CUSTOM_DIALOG_SHOW = "custom_dialog_show";
    /** Hours to wait after last dialog before a new window */
    public static final String CUSTOM_AFTER_HOUR_SHOW = "custom_after_hour_show";
    /** Max dialog shows within each hour window */
    public static final String CUSTOM_HOUR_COUNT = "custom_hour_count";

    // ==================== launcher_home_screen ====================
    /** Launcher home screen — swipe left interstitial and bottom ad config */
    public static final String LAUNCHER_HOME_SCREEN = "launcher_home_screen";
    /** Show interstitial on swipe left to home screen */
    public static final String SWIPE_INTER_SHOW = "swipe_inter_show";
    /** Swipe count before interstitial */
    public static final String SWIPE_INTER_COUNT = "swipe_inter_count";
    /** Show bottom ad on launcher home screen */
    public static final String LAUNCHER_BOTTOM_ADS_SHOW = "launcher_bottom_ads_show";
    /** Launcher home screen bottom ad type (banner / native) */
    public static final String LAUNCHER_ADS_TYPE = "launcher_ads_type";
    public static final String LAUNCHER_BANNER_ID = "launcher_banner_id";
    public static final String LAUNCHER_NATIVE_ID = "launcher_native_id";
    /** Show bottom ad on launcher apps drawer when recent apps exist */
    public static final String BOTTOM_DRAWER_ADS_SHOW = "bottom_drawer_ads_show";
    /** Max bottom drawer ad impressions per quota window */
    public static final String DAILY_ADS_LIMIT = "daily_ads_limit";
    /** Hour window used to spread daily ad impressions (e.g. 24 = one day) */
    public static final String DAILY_HOUR_QUOTA_AFTER_ADS = "daily_hour_quota_after_ads";

    // ==================== recent_launcher_screen ====================
    /** Launcher recent screen — bottom ad type and ad unit IDs */
    public static final String RECENT_LAUNCHER_SCREEN = "recent_launcher_screen";
    /** Show bottom ad on launcher recent screen */
    public static final String RECENT_BOTTOM_ADS_SHOW = "recent_bottom_ads_show";
    /** Launcher recent screen bottom ad type (banner / native) */
    public static final String RECENT_ADS_TYPE = "recent_ads_type";
    public static final String RECENT_BANNER_ID = "recent_banner_id";
    public static final String RECENT_NATIVE_ID = "recent_native_id";

    // ==================== contact_details_screen ====================
    /** Contact details screen — bottom ad type and ad unit IDs */
    public static final String CONTACT_DETAILS_SCREEN = "contact_details_screen";
    /** Show bottom ad on contact details screen */
    public static final String CONTACT_DETAILS_SCREEN_BOTTOM_ADS_SHOW = "contact_details_screen_bottom_ads_show";
    public static final String CONTACT_DETAILS_SCREEN_ADS_TYPE = "contact_details_screen_ads_type";
    public static final String CONTACT_DETAILS_SCREEN_BANNER_ID = "contact_details_screen_banner_id";
    public static final String CONTACT_DETAILS_SCREEN_NATIVE_ID = "contact_details_screen_native_id";

    // ==================== pdf_viewer_screen ====================
    /** PDF viewer screen — bottom ad type and ad unit IDs */
    public static final String PDF_VIEWER_SCREEN = "pdf_viewer_screen";
    /** Show bottom ad on PDF viewer screen */
    public static final String PDF_VIEWER_SCREEN_BOTTOM_ADS_SHOW = "pdf_viewer_screen_bottom_ads_show";
    public static final String PDF_VIEWER_SCREEN_ADS_TYPE = "pdf_viewer_screen_ads_type";
    public static final String PDF_VIEWER_SCREEN_BANNER_ID = "pdf_viewer_screen_banner_id";
    public static final String PDF_VIEWER_SCREEN_NATIVE_ID = "pdf_viewer_screen_native_id";

    // ==================== recycle_bin_screen ====================
    /** Recycle bin screen — bottom ad type and ad unit IDs */
    public static final String RECYCLE_BIN_SCREEN = "recycle_bin_screen";
    public static final String RECYCLE_BIN_BOTTOM_ADS_SHOW = "recycle_bin_bottom_ads_show";
    public static final String RECYCLE_BIN_ADS_TYPE = "recycle_bin_ads_type";
    public static final String RECYCLE_BIN_BANNER_ID = "recycle_bin_banner_id";
    public static final String RECYCLE_BIN_NATIVE_ID = "recycle_bin_native_id";

    // ==================== search_all_files_screen ====================
    /** Search all files screen — bottom ad type and ad unit IDs */
    public static final String SEARCH_ALL_FILES_SCREEN = "search_all_files_screen";
    public static final String SEARCH_ALL_FILES_BOTTOM_ADS_SHOW = "search_all_files_bottom_ads_show";
    public static final String SEARCH_ALL_FILES_ADS_TYPE = "search_all_files_ads_type";
    public static final String SEARCH_ALL_FILES_BANNER_ID = "search_all_files_banner_id";
    public static final String SEARCH_ALL_FILES_NATIVE_ID = "search_all_files_native_id";

    // ==================== faq_screen ====================
    /** FAQ screen — bottom ad type and ad unit IDs */
    public static final String FAQ_SCREEN = "faq_screen";
    public static final String FAQ_BOTTOM_ADS_SHOW = "faq_bottom_ads_show";
    public static final String FAQ_ADS_TYPE = "faq_ads_type";
    public static final String FAQ_BANNER_ID = "faq_banner_id";
    public static final String FAQ_NATIVE_ID = "faq_native_id";

    // ==================== pdf_edit_screen ====================
    /** PDF edit screen — bottom ad type and ad unit IDs */
    public static final String PDF_EDIT_SCREEN = "pdf_edit_screen";
    public static final String PDF_EDIT_BOTTOM_ADS_SHOW = "pdf_edit_bottom_ads_show";
    public static final String PDF_EDIT_ADS_TYPE = "pdf_edit_ads_type";
    public static final String PDF_EDIT_BANNER_ID = "pdf_edit_banner_id";
    public static final String PDF_EDIT_NATIVE_ID = "pdf_edit_native_id";

    // ==================== pdf_manage_pages_screen ====================
    /** PDF manage pages screen — bottom ad type and ad unit IDs */
    public static final String PDF_MANAGE_PAGES_SCREEN = "pdf_manage_pages_screen";
    public static final String PDF_MANAGE_PAGES_BOTTOM_ADS_SHOW = "pdf_manage_pages_bottom_ads_show";
    public static final String PDF_MANAGE_PAGES_ADS_TYPE = "pdf_manage_pages_ads_type";
    public static final String PDF_MANAGE_PAGES_BANNER_ID = "pdf_manage_pages_banner_id";
    public static final String PDF_MANAGE_PAGES_NATIVE_ID = "pdf_manage_pages_native_id";

    // ==================== page_setup_screen ====================
    /** Page setup screen — bottom ad type and ad unit IDs */
    public static final String PAGE_SETUP_SCREEN = "page_setup_screen";
    public static final String PAGE_SETUP_BOTTOM_ADS_SHOW = "page_setup_bottom_ads_show";
    public static final String PAGE_SETUP_ADS_TYPE = "page_setup_ads_type";
    public static final String PAGE_SETUP_BANNER_ID = "page_setup_banner_id";
    public static final String PAGE_SETUP_NATIVE_ID = "page_setup_native_id";

    // ==================== create_pdf_screen ====================
    /** Create PDF screen — bottom ad type and ad unit IDs */
    public static final String CREATE_PDF_SCREEN = "create_pdf_screen";
    public static final String CREATE_PDF_BOTTOM_ADS_SHOW = "create_pdf_bottom_ads_show";
    public static final String CREATE_PDF_ADS_TYPE = "create_pdf_ads_type";
    public static final String CREATE_PDF_BANNER_ID = "create_pdf_banner_id";
    public static final String CREATE_PDF_NATIVE_ID = "create_pdf_native_id";

    // ==================== pdf_selection_screen ====================
    /** PDF/media selection screen — bottom ad type and ad unit IDs */
    public static final String PDF_SELECTION_SCREEN = "pdf_selection_screen";
    public static final String PDF_SELECTION_BOTTOM_ADS_SHOW = "pdf_selection_bottom_ads_show";
    public static final String PDF_SELECTION_ADS_TYPE = "pdf_selection_ads_type";
    public static final String PDF_SELECTION_BANNER_ID = "pdf_selection_banner_id";
    public static final String PDF_SELECTION_NATIVE_ID = "pdf_selection_native_id";

    // ==================== successfully_converted_screen ====================
    /** Successfully converted screen — bottom ad type and ad unit IDs */
    public static final String SUCCESSFULLY_CONVERTED_SCREEN = "successfully_converted_screen";
    public static final String SUCCESSFULLY_CONVERTED_BOTTOM_ADS_SHOW = "successfully_converted_bottom_ads_show";
    public static final String SUCCESSFULLY_CONVERTED_ADS_TYPE = "successfully_converted_ads_type";
    public static final String SUCCESSFULLY_CONVERTED_BANNER_ID = "successfully_converted_banner_id";
    public static final String SUCCESSFULLY_CONVERTED_NATIVE_ID = "successfully_converted_native_id";

    // ==================== pdf_signature_screen ====================
    /** PDF signature screen — bottom ad type and ad unit IDs */
    public static final String PDF_SIGNATURE_SCREEN = "pdf_signature_screen";
    public static final String PDF_SIGNATURE_BOTTOM_ADS_SHOW = "pdf_signature_bottom_ads_show";
    public static final String PDF_SIGNATURE_ADS_TYPE = "pdf_signature_ads_type";
    public static final String PDF_SIGNATURE_BANNER_ID = "pdf_signature_banner_id";
    public static final String PDF_SIGNATURE_NATIVE_ID = "pdf_signature_native_id";

    // ==================== setting_screen ====================
    /** Settings screen — bottom ad type and ad unit IDs */
    public static final String SETTING_SCREEN = "setting_screen";
    /** Show bottom ad on settings screen */
    public static final String SETTING_BOTTOM_ADS_SHOW = "setting_bottom_ads_show";
    public static final String SETTING_ADS_TYPE = "setting_ads_type";
    public static final String SETTING_BANNER_ID = "setting_banner_id";
    public static final String SETTING_NATIVE_ID = "setting_native_id";

    // ==================== callend_screen ====================
    /** Call end screen — bottom ads, notification countries, interstitial config */
    public static final String CALLEND_SCREEN = "callend_screen";
    /** Show call end screen after call */
    public static final String IS_CALLEND_SHOW = "is_callend_show";
    /** Show bottom ad on call end screen */
    public static final String IS_CALLEND_BOTTOM_AD_SHOW = "is_callend_bottom_ad_show";
    public static final String CALLEND_BOTTOM_ADS_TYPE = "callend_bottom_ads_type";
    public static final String CALLEND_BANNER_AD_ID = "callend_banner_ad_id";
    public static final String CALLEND_NATIVE_AD_ID = "callend_native_ad_id";
    /** Days after install to show notification */
    public static final String NOTIFICATION_INSTALL_DAYS = "notification_install_days";
    /** Days after install to show call notification */
    public static final String NOTIFICATION_CALL_INSTALL_DAYS = "notification_call_install_days";
    /** Days after install to show call overlay notification */
    public static final String NOTIFICATION_CALL_OVERLAY_INSTALL_DAYS = "notification_call_overlay_install_days";
    /** Get user country via IP address */
    public static final String COUNTRY_GET_WITH_IP = "country_get_with_ip";
    /** Show all allow permission in FB notification */
    public static final String ALL_ALLOW_PERMISSION_SHOW_FB_NOTIFICATION = "All_allow_permission_show_fb_notification";
    /** Show close button on full native ads */
    public static final String CLOSE_BUTTON_SHOW_ON_FULL_NATIVE_ADS = "close_button_show_on_full_native_ads";
    /** Notification allowed countries list */
    public static final String NOTIFICATION_COUNTRY = "notification_country";
    /** Call notification allowed countries list */
    public static final String NOTIFICATION_CALL_COUNTRY = "notification_call_country";
    /** Call overlay notification allowed countries list */
    public static final String NOTIFICATION_CALL_OVERLAY_COUNTRY = "notification_call_overlay_country";

    // ==================== call_end_bacK_inter (inside callend_screen) ====================
    /** Call end back interstitial — ad type, ID, and show countries */
    public static final String CALL_END_BACK_INTER = "call_end_bacK_inter";
    /** Show interstitial ad on call end back press */
    public static final String CALL_END_INTER_ADS_SHOW = "call_end_inter_ads_show";
    /** Show back ads on notification screen */
    public static final String NOTIFICATION_SCREEN_BACK_ADS_SHOW = "notification_screen_back_ads_show";
    public static final String CALL_END_INTER_ADS_TYPE = "call_end_inter_ads_type";
    /** Days count for call end interstitial */
    public static final String CALL_END_INTER_DAY_COUNT = "call_end_inter_day_count";
    /** Call end screen reopen count before interstitial */
    public static final String CALLEND_AGAIN_OPEN_COUNT = "callend_again_open_count";
    /** Total active show count for call end interstitial */
    public static final String CALL_END_INTER_ACTIVE_TOTAL_SHOW_COUNT = "call_end_inter_active_total_show_count";
    public static final String CALLEND_INTER_AD_ID = "callend_inter_ad_id";
    /** Countries where call end interstitial ads are shown */
    public static final String CALL_END_INTER_ADS_SHOW_COUNTRY = "call_end_inter_ads_show_country";

    // ==================== other_screen ====================
    /** Other / global screens — interstitial timing, shared native/banner IDs, exit ad */
    public static final String OTHER_SCREEN = "other_screen";
    /** First interstitial show delay time */
    public static final String INTER_FIRST_TIME = "inter_first_time";
    /** Second interstitial show delay time */
    public static final String INTER_SECOND_TIME = "inter_second_time";
    public static final String OTHER_NATIVE_ID = "other_native_id";
    public static final String OTHER_BANNER_ID = "other_banner_id";
    /** App-level interstitial ads load type (inside other_screen) */
    public static final String OTHER_INTER_ADS_LOAD_TYPE = "inter_ads_load_type";
    public static final String OTHER_BOTTOM_ADS_TYPE = "other_bottom_ads_type";
    /** Show bottom ad on other screens */
    public static final String OTHER_BOTTOM_ADS_SHOW = "other_bottom_ads_show";
    /** Show interstitial ads globally */
    public static final String INTER_ADS_SHOW = "inter_ads_show";
    /** Show interstitial ad on back press */
    public static final String INTER_ADS_SHOW_ON_BACK = "inter_ads_show_on_back";
    /** Exit dialog native ad unit ID */
    public static final String EXIT_NATIVE_ID = "exit_native_id";
    /** Show custom No Watermark reward dialog (default true) */
    public static final String WATERMARK_DIALOG_SHOW = "watermark_dialog_show";
    /** AdMob Rewarded Ad unit ID for No Watermark unlock */
    public static final String WATERMARK_REWARD_ADS_UNIT = "watermark_reward_ads_unit";

    // ==================== update_app ====================
    /** In-app update dialog — update type (Flexible / Immediate) */
    public static final String UPDATE_APP = "update_app";
    /** Show in-app update dialog */
    public static final String IN_APP_UPDATE_SHOW = "in_app_update_show";
    public static final String IN_APP_UPDATE_TYPE = "in_app_update_type";
    /** Daily show count for in-app update dialog */
    public static final String IN_APP_DIALOG_DAILY_SHOW_COUNT = "in_appp_dailog_daily_show_count";

    // ==================== Quiz ads ====================
    /** Ad priority — GOOGLE or QUIZ */
    public static final String AD_PRIORITY = "Ad_Priority";
    /** When Google ad fails, show Quiz ad */
    public static final String GOOGLE_AD_FAILED_SHOW_QUIZ = "Google_Ad_Failed_Show_Quiz";
    /** Quiz ad creative / copy JSON object */
    public static final String QUIZ_ADS_DESIGN = "QuizAdsDesign";
    /** Quiz link + proxy geo routing JSON object */
    public static final String APP_PROXY_STRUCTURE = "AppProxyStructure";
    /** Show ad when user clicks an app in launcher drawer */
    public static final String LAUNCHER_APP_CLICK_AD_SHOW = "LauncherApp_Click_Ad_Show";
    /** App click count threshold before showing launcher click ad */
    public static final String LAUNCHER_APP_COUNT = "LauncherApp_Count";
    /** Launcher click ad type — Google_Inter, Google_App_Open, Quiz_Inter, etc. */
    public static final String LAUNCHER_APP_AD_TYPE = "LauncherApp_Ad_Type";
    /** Google interstitial unit ID for launcher app click ad */
    public static final String LAUNCHER_APP_INTERSTITIAL_ID = "LauncherApp_Interstitial_Id";
    /** Open quiz links in-app (Custom Tabs) vs external browser */
    public static final String IS_LINK_OPEN_APP = "isLinkOpenApp";
    /** When true, open quiz link directly; when false, show quiz interstitial dialog */
    public static final String IS_QUIZ_BROWSER_SHOW = "isQuizBrowserShow";
}
