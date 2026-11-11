package com.foobnix.ui2;

import android.app.Activity;
import android.content.Context;
import android.util.TypedValue;

import com.foobnix.model.AppProfile;
import com.foobnix.model.AppState;
import com.foobnix.pdf.info.R;
import com.foobnix.pdf.info.TintUtil;

/** Application chrome only; document colors, rendering and reading position stay independent. */
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

    public static void select(Activity activity, int preset) {
        AppState.get().appTheme = preset;
        AppState.get().isSystemThemeColor = false;
        initialize(activity);
        AppProfile.save(activity);
        activity.recreate();
    }
}
