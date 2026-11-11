package com.foobnix.ui2;

import android.app.Activity;
import android.content.Context;
import android.util.TypedValue;

import com.foobnix.model.AppProfile;
import com.foobnix.model.AppState;
import com.foobnix.pdf.info.R;
import com.foobnix.pdf.info.TintUtil;
import com.foobnix.sys.ImageExtractor;

/** Applies the interface palette; explicitly selecting a preset also sets reading colors. */
public final class ThemeManager {
    private ThemeManager() {
    }

    public static boolean isLight() {
        return AppState.get().appTheme == AppState.THEME_LIGHT || isEink();
    }

    public static boolean isEink() {
        return AppState.get().appTheme == AppState.THEME_INK;
    }

    public static int overlay() {
        if (isEink()) return R.style.ThemeOverlay_Librera_Eink;
        return isLight() ? R.style.ThemeOverlay_Librera_Cream : R.style.ThemeOverlay_Librera_Oled;
    }

    public static String name() {
        if (isEink()) return "E-Ink Color";
        return isLight() ? "Cream" : "OLED Black";
    }

    public static void apply(Activity activity) {
        activity.setTheme(isLight() ? R.style.StyledIndicatorsWhite : R.style.StyledIndicatorsBlack);
        activity.getTheme().applyStyle(overlay(), true);
        TintUtil.color = color(activity, R.attr.appPrimary);
    }

    public static int color(Context context, int attribute) {
        TypedValue value = new TypedValue();
        if (!context.getTheme().resolveAttribute(attribute, value, true)) {
            throw new IllegalArgumentException("Missing theme attribute " + attribute);
        }
        return value.resourceId != 0 ? context.getColor(value.resourceId) : value.data;
    }

    public static void initialize(Context context) {
        // AppProfile also loads during attachBaseContext, before the Activity has a base.
        android.content.res.Resources.Theme theme = context.getResources().newTheme();
        theme.applyStyle(overlay(), true);
        TypedValue primary = new TypedValue();
        theme.resolveAttribute(R.attr.appPrimary, primary, true);
        TintUtil.color = context.getResources().getColor(primary.resourceId, theme);
    }

    public static void select(Activity activity, int preset, Runnable reopen) {
        AppState.get().appTheme = preset;
        AppState.get().isSystemThemeColor = false;
        // Use palette resources, not the Activity theme that still belongs to the previous preset.
        boolean day = isLight();
        AppState.get().isDayNotInvert = day;
        int background = activity.getColor(isEink() ? R.color.app_eink_reader_background
                : day ? R.color.app_cream_reader_background : R.color.app_oled_reader_background);
        int text = activity.getColor(isEink() ? R.color.app_eink_on_surface
                : day ? R.color.app_cream_on_surface : R.color.app_oled_on_surface);
        if (day) {
            AppState.get().colorDayBg = background;
            AppState.get().colorDayText = text;
            AppState.get().colorDayForeground = AppState.COLOR_DAY_FG;
            AppState.get().isUseBGImageDay = false;
        } else {
            AppState.get().colorNigthBg = background;
            AppState.get().colorNigthText = text;
            AppState.get().colorNigthForeground = AppState.COLOR_NIGHT_FG;
            AppState.get().isUseBGImageNight = false;
        }
        initialize(activity);
        AppProfile.save(activity);
        // A document cached by the library still carries the previous preset's stylesheet.
        ImageExtractor.clearCodeDocument();
        reopen.run();
    }
}
