package org.fossify.commons.extensions

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.util.TypedValue // Required for theme attribute resolution
import android.view.ViewGroup
import androidx.core.content.ContextCompat // Required for getColor
import androidx.loader.content.CursorLoader // Keep if getGlobalConfig is used elsewhere
import com.google.android.material.color.MaterialColors
import org.fossify.commons.R // This is R from the commons library
import org.fossify.commons.helpers.*
import org.fossify.commons.helpers.MyContentProvider.GLOBAL_THEME_SYSTEM
import org.fossify.commons.models.GlobalConfig
import org.fossify.commons.models.isGlobalThemingEnabled
import org.fossify.commons.views.*

// --- START OF THEME OVERRIDE MODIFICATIONS ---

// Helper to resolve theme attributes gracefully, using commons fallback colors
private fun Context.resolveThemeAttribute(attrResId: Int, commonsFallbackColorResId: Int): Int {
    val typedValue = TypedValue()
    return if (theme.resolveAttribute(attrResId, typedValue, true)) {
        if (typedValue.type >= TypedValue.TYPE_FIRST_COLOR_INT && typedValue.type <= TypedValue.TYPE_LAST_COLOR_INT) {
            typedValue.data // It's a direct color value
        } else if (typedValue.resourceId != 0) {
            try {
                ContextCompat.getColor(this, typedValue.resourceId)
            } catch (e: Exception) {
                // Log.w("ThemeResolver", "Fallback for resourceId ${typedValue.resourceId} from attribute $attrResId", e)
                ContextCompat.getColor(this, commonsFallbackColorResId) // Fallback if resource ID from attr is invalid/not found in current context
            }
        } else {
            // Log.w("ThemeResolver", "Fallback for attribute $attrResId, typedValue.resourceId is 0")
            ContextCompat.getColor(this, commonsFallbackColorResId) // Fallback if attr is not a color or ref
        }
    } else {
        // Log.w("ThemeResolver", "Fallback for attribute $attrResId, attribute not found in theme")
        ContextCompat.getColor(this, commonsFallbackColorResId) // Fallback if attr not found
    }
}

// --- CONTROL FLAGS ---
// Set these to false to make your XML theme the primary source when Material You is not active.
fun Context.isDynamicTheme(): Boolean = false // Original: isSPlus() && baseConfig.isSystemThemeEnabled
fun Context.isBlackAndWhiteTheme(): Boolean = false
fun Context.isWhiteTheme(): Boolean = false // This disables special logic in original getProperPrimaryColor
fun Context.isAutoTheme(): Boolean = false // Original: !isSPlus() && baseConfig.isSystemThemeEnabled

// --- OVERRIDDEN COLOR GETTERS ---
fun Context.getProperTextColor(): Int {
    if (isDynamicTheme()) return resources.getColor(R.color.you_neutral_text_color, theme)
    return resolveThemeAttribute(android.R.attr.textColorPrimary, R.color.commons_default_text_color)
}

fun Context.getProperBackgroundColor(): Int {
    if (isDynamicTheme()) return resources.getColor(R.color.you_background_color, theme)
    return resolveThemeAttribute(android.R.attr.colorBackground, R.color.commons_default_background_color)
}

fun Context.getProperPrimaryColor(): Int {
    if (isDynamicTheme()) return resources.getColor(R.color.you_primary_color, theme)
    // Since isWhiteTheme() etc. are false, this will now default to resolving colorPrimary
    return resolveThemeAttribute(com.google.android.material.R.attr.colorPrimary, R.color.commons_default_primary_color)
}

fun Context.getProperStatusBarColor(): Int {
    if (isDynamicTheme()) return resources.getColor(R.color.you_status_bar_color, theme)
    return resolveThemeAttribute(android.R.attr.statusBarColor, R.color.commons_default_statusbar_color)
}

fun Context.getColoredMaterialStatusBarColor(): Int {
    if (isDynamicTheme()) return resources.getColor(R.color.you_status_bar_color, theme)
    return resolveThemeAttribute(com.google.android.material.R.attr.colorPrimary, R.color.commons_default_primary_color)
}

fun Context.updateTextColors(viewGroup: ViewGroup) {
    val textColor = getProperTextColor()
    val accentColor = resolveThemeAttribute(com.google.android.material.R.attr.colorAccent, R.color.commons_default_accent_color)
    val customViewBackgroundColor = Color.TRANSPARENT // Let XML layouts define background for these views

    val cnt = viewGroup.childCount
    (0 until cnt).map { viewGroup.getChildAt(it) }.forEach {
        when (it) {
            is MyTextView -> it.setColors(textColor, accentColor, customViewBackgroundColor)
            is MyAppCompatSpinner -> it.setColors(textColor, accentColor, customViewBackgroundColor)
            is MyCompatRadioButton -> it.setColors(textColor, accentColor, customViewBackgroundColor)
            is MyAppCompatCheckbox -> it.setColors(textColor, accentColor, customViewBackgroundColor)
            is MyMaterialSwitch -> it.setColors(textColor, accentColor, customViewBackgroundColor)
            is MyEditText -> it.setColors(textColor, accentColor, customViewBackgroundColor)
            is MyAutoCompleteTextView -> it.setColors(textColor, accentColor, customViewBackgroundColor)
            is MyFloatingActionButton -> {
                val fabBgColor = resolveThemeAttribute(com.google.android.material.R.attr.colorSecondary, R.color.commons_default_accent_color)
                val fabIconColor = resolveThemeAttribute(com.google.android.material.R.attr.colorOnSecondary, R.color.commons_default_text_on_accent_color)
                it.setColors(fabIconColor, fabIconColor, fabBgColor)
            }
            is MySeekBar -> it.setColors(textColor, accentColor, customViewBackgroundColor)
            is MyButton -> it.setColors(textColor, accentColor, customViewBackgroundColor)
            is MyTextInputLayout -> it.setColors(textColor, accentColor, customViewBackgroundColor)
            is ViewGroup -> updateTextColors(it)
        }
    }
}

// --- DIALOG AND POPUPMENU THEMES ---
// Ensure R.style.MyDateTimePickerMaterialTheme_Light and R.style.AppTheme_PopupMenuLightStyle
// are defined in commons/res/values/styles.xml (or themes.xml) and are styled for light appearance.
fun Context.getTimePickerDialogTheme(): Int {
    // Retain original Material You logic if isDynamicTheme() was true
    // if (isDynamicTheme()) return if (isSystemInDarkMode()) R.style.MyTimePickerMaterialTheme_Dark else R.style.MyDateTimePickerMaterialTheme
    return R.style.MyDateTimePickerMaterialTheme_Light // Force light
}

fun Context.getDatePickerDialogTheme(): Int {
    // if (isDynamicTheme()) return R.style.MyDateTimePickerMaterialTheme
    return R.style.MyDateTimePickerMaterialTheme_Light // Force light
}

fun Context.getPopupMenuTheme(): Int {
    // if (isDynamicTheme()) return R.style.AppTheme_YouPopupMenuStyle
    return R.style.AppTheme_PopupMenuLightStyle // Force light
}

// --- GLOBAL CONFIG ---
// Effectively disable theme synchronization from "Thank You" app
fun Context.syncGlobalConfig(callback: (() -> Unit)? = null) {
    baseConfig.isGlobalThemeEnabled = false // This prevents Fossify config from being overwritten by global settings
    // Decide if other global settings like showCheckmarksOnSwitches should still sync or be defaulted
    // Forcing it off for simplicity here:
    // baseConfig.showCheckmarksOnSwitches = it.showCheckmarksOnSwitches (if you were to read 'it')
    // If not reading 'it', just set to a default or leave as is from app's own baseConfig.
    // For full override, you might set baseConfig values here to your light theme defaults,
    // but getProper...Color() functions are already changed to not use them primarily.
    callback?.invoke()
}

// withGlobalConfig and getGlobalConfig can remain as they are.
// The important part is that syncGlobalConfig now sets isGlobalThemeEnabled = false.
fun Context.withGlobalConfig(callback: (globalConfig: GlobalConfig?) -> Unit) {
    if (!isThankYouInstalled()) {
        callback(null)
    } else {
        val cursorLoader = getMyContentProviderCursorLoader() // Assuming this is defined in the other Context.kt
        ensureBackgroundThread {
            callback(getGlobalConfig(cursorLoader))
        }
    }
}

fun Context.getGlobalConfig(cursorLoader: CursorLoader): GlobalConfig? {
    val cursor = cursorLoader.loadInBackground()
    cursor?.use {
        if (cursor.moveToFirst()) {
            try {
                return GlobalConfig(
                    themeType = cursor.getIntValue(MyContentProvider.COL_THEME_TYPE),
                    textColor = cursor.getIntValue(MyContentProvider.COL_TEXT_COLOR),
                    backgroundColor = cursor.getIntValue(MyContentProvider.COL_BACKGROUND_COLOR),
                    primaryColor = cursor.getIntValue(MyContentProvider.COL_PRIMARY_COLOR),
                    accentColor = cursor.getIntValue(MyContentProvider.COL_ACCENT_COLOR),
                    appIconColor = cursor.getIntValue(MyContentProvider.COL_APP_ICON_COLOR),
                    showCheckmarksOnSwitches = cursor.getIntValue(MyContentProvider.COL_SHOW_CHECKMARKS_ON_SWITCHES) != 0,
                    lastUpdatedTS = cursor.getIntValue(MyContentProvider.COL_LAST_UPDATED_TS)
                )
            } catch (e: Exception) {
                // Log error or handle
            }
        }
    }
    return null
}


// --- BOTTOM NAVIGATION AND DIALOG BACKGROUNDS ---
@SuppressLint("NewApi")
fun Context.getBottomNavigationBackgroundColor(): Int {
    if (isDynamicTheme()) return resources.getColor(R.color.you_status_bar_color, theme)
    return resolveThemeAttribute(com.google.android.material.R.attr.colorSurface, R.color.commons_default_bottom_nav_bg_color)
}

fun Context.getDialogBackgroundColor(): Int {
    if (isDynamicTheme()) return MaterialColors.getColor(this, com.google.android.material.R.attr.colorSurfaceContainerHigh, Color.TRANSPARENT)
    return resolveThemeAttribute(com.google.android.material.R.attr.colorSurface, R.color.commons_default_dialog_bg_color)
}

// --- END OF THEME OVERRIDE MODIFICATIONS ---

// --- UNMODIFIED ORIGINAL FUNCTIONS from your Context-styling.kt ---
// (These are assumed to be non-UI theme related or correctly handled by the above changes)
fun Context.isSystemInDarkMode() = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_YES != 0

fun Context.checkAppIconColor() {
    val appId = baseConfig.appId
    if (appId.isNotEmpty() && baseConfig.lastIconColor != baseConfig.appIconColor) {
        getAppIconColors().forEachIndexed { index, color ->
            toggleAppIconColor(appId, index, color, false)
        }

        getAppIconColors().forEachIndexed { index, color ->
            if (baseConfig.appIconColor == color) {
                toggleAppIconColor(appId, index, color, true)
            }
        }
    }
}

fun Context.toggleAppIconColor(appId: String, colorIndex: Int, color: Int, enable: Boolean) {
    val className = "${appId.removeSuffix(".debug")}.activities.SplashActivity${appIconColorStrings[colorIndex]}"
    val state = if (enable) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED
    try {
        packageManager.setComponentEnabledSetting(ComponentName(appId, className), state, PackageManager.DONT_KILL_APP)
        if (enable) {
            baseConfig.lastIconColor = color
        }
    } catch (e: Exception) {
        showErrorToast(e) // Assuming showErrorToast is in the other Context.kt
    }
}

fun Context.getAppIconColors() = resources.getIntArray(R.array.md_app_icon_colors).toCollection(ArrayList())
// Make sure appIconColorStrings is defined or accessible, typically in Constants.kt
