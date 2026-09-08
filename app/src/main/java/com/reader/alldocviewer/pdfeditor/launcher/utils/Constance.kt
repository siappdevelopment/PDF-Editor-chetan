package com.reader.alldocviewer.pdfeditor.launcher.utils

object Constance {

    const val CONFIRM_DIALOG = "confirm_dialog"
    const val MERGE_DUPLICATE_CONTACT = "merge_duplicate_contact"
    const val DIAL_PAD_SOUND = "dial_pad_sound"
    const val CALL_FLASH = "call_flash"
    const val SWIPE_ACTION = "swipe_action"
    const val APP_THEME = "app_theme"
    const val APP_THEME_EPOCH = "app_theme_epoch"
    const val DB_NAME = "contact_manager_db"
    const val PREF_NAME = "my_app_prefs"
    const val IS_LOG_IN = "isLogIn"
    const val LANGUAGE_FINISH_AFTER_APPLY = "language_finish_after_apply"
    const val LANGUAGE_EPOCH = "language_epoch"
    const val NUMBER = "Number"
    const val SIM_PREFERENCE = "sim_preference"

    const val LOAD_DATA_COUNT = 200

    const val IS_DIALER = "isDialer"
    const val ACTION_CALL = "action_call"
    const val ACTION_SEND_MESSAGE = "action_send_message"
    const val ACTION_VIDEO_CALL = "action_video_call"
    const val ACTION_INFO = "action_info"
    const val ACTION_CALL_HISTORY = "action_call_history"
    const val ACTION_ADD_TO_CONTACT = "action_add_to_contact"
    const val ACTION_ADD_TAG = "action_add_tag"
    const val ACTION_DELETE = "action_delete"
    const val ACTION_BLOCK_CONTACT = "action_block_contact"
    const val CONTACT_ID = "contactId"
    const val IS_CONTACT_SAVED = "isContactSaved"
    const val DATA_FETCH = "dataFetch"
    const val NAME = "name"
    const val PHOTO_URI = "photo_uri"
    const val WHATSAPP: String = "com.whatsapp"
    const val WHATSAPP_BUSINESS: String = "com.whatsapp.w4b"
    const val DUO: String = "com.google.android.apps.tachyon"
    val videoCallList: Array<String> = arrayOf(WHATSAPP, WHATSAPP_BUSINESS, DUO)
    const val OVERLAY_PERMISSION_SKIP = "overlay_permission_skip"
    const val NOTIFICATION_PERMISSION_SKIP = "notification_permission_skip"
    const val PERMISSION_DIALOG_SHOWN = "permission_dialog_shown"
    /** Default dialer PermissionActivity shown once in startup flow (Allow or Cancel). */
    const val PERMISSION_SCREEN_SHOWN = "permission_screen_shown"
    const val PRIVACY_DATA_CONSENT = "privacy_data_consent"
}