package com.foobnix.ui2.fragment;

import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

import com.foobnix.model.AppState;
import com.foobnix.pdf.info.AppsConfig;
import com.foobnix.pdf.info.R;
import com.foobnix.pdf.info.view.AppearanceBottomSheet;
import com.foobnix.ui2.ThemeManager;

import java.util.LinkedHashMap;
import java.util.Map;

/** Page containers preserve the existing controls, listeners and conditional visibility. */
final class SettingsNavigation {
    private static final String[] KEYS = {"appearance", "reading", "library", "sync", "advanced"};
    private static final int[] TITLES = {R.string.settings_appearance, R.string.settings_reading,
            R.string.settings_library, R.string.settings_sync, R.string.settings_advanced};
    private static final int[] SUMMARIES = {R.string.settings_appearance_summary,
            R.string.settings_reading_summary, R.string.settings_library_summary,
            R.string.settings_sync_summary, R.string.settings_advanced_summary};
    private static final int[] ICONS = {R.drawable.navigation_settings, R.drawable.navigation_books,
            R.drawable.navigation_shelves, R.drawable.glyphicons_82_refresh,
            R.drawable.navigation_settings};
    private final FragmentActivity activity;
    private final ScrollView scroll;
    private final TextView title;
    private final ImageView back;
    private final View header;
    private final Map<String, LinearLayout> pages = new LinkedHashMap<>();
    private final Bundle positions;
    private String current;

    SettingsNavigation(FragmentActivity activity, View root, Bundle state) {
        this.activity = activity;
        scroll = root.findViewById(R.id.scroll);
        title = root.findViewById(R.id.recentName);
        back = root.findViewById(R.id.onCloseApp);
        header = root.findViewById(R.id.panelRecent);
        positions = state != null && state.getBundle("settings.positions") != null
                ? state.getBundle("settings.positions") : new Bundle();
        LinearLayout body = (LinearLayout) scroll.getChildAt(0);
        LinearLayout overview = column();
        pages.put("", overview);
        body.setPadding(body.getPaddingLeft(), body.getPaddingTop(),
                body.getPaddingRight(), dp(128));
        for (String key : KEYS) pages.put(key, column());
        pages.put("formats", column());
        LinearLayout hidden = column();
        hidden.setVisibility(View.GONE);
        while (body.getChildCount() > 0) {
            View control = body.getChildAt(0);
            body.removeViewAt(0);
            String tag = String.valueOf(control.getTag());
            String category = tag.startsWith("settings:") ? tag.substring(9) : "advanced";
            if (control.getId() == R.id.settingsSourceActions) category = "library";
            LinearLayout page = pages.get(category);
            (page == null ? hidden : page).addView(control);
            if (control instanceof TextView && ((TextView) control).getText()
                    .toString().equals(activity.getString(R.string.supported_file_formats))) {
                control.setVisibility(View.GONE);
            }
        }
        View scanOptions = pages.get("appearance").findViewById(R.id.moreLybraryettings);
        ((ViewGroup) scanOptions.getParent()).removeView(scanOptions);
        pages.get("library").addView(scanOptions, 0);
        body.addView(hidden);
        for (LinearLayout page : pages.values()) body.addView(page);
        TextView subtitle = text(activity.getString(R.string.settings_overview), 14, false);
        subtitle.setPadding(0, dp(4), 0, dp(20));
        overview.addView(subtitle);
        for (int i = 0; i < KEYS.length; i++) {
            final String key = KEYS[i];
            overview.addView(card(TITLES[i], SUMMARIES[i], ICONS[i], () -> open(key)));
        }
        LinearLayout palettes = column();
        palettes.addView(text(activity.getString(R.string.settings_palettes), 16, true));
        int[] presets = {AppState.THEME_LIGHT, AppState.THEME_DARK_OLED, AppState.THEME_INK};
        String[] names = {"Cream", "OLED Black", "E-Ink Color"};
        int[] backgrounds = {R.color.app_cream_background, R.color.app_oled_background,
                R.color.app_eink_background};
        int[] foregrounds = {R.color.app_cream_on_surface, R.color.app_oled_on_surface,
                R.color.app_eink_on_surface};
        for (int i = 0; i < presets.length; i++) {
            final int preset = presets[i];
            boolean selected = AppState.get().appTheme == preset;
            TextView choice = text((selected ? "✓  " : "") + names[i] + "\nAa — Librera", 17, true);
            choice.setTextColor(androidx.core.content.ContextCompat.getColor(activity, foregrounds[i]));
            choice.setTag("no_tint");
            choice.setPadding(dp(16), dp(16), dp(16), dp(16));
            choice.setBackground(shape(androidx.core.content.ContextCompat.getColor(activity, backgrounds[i]),
                    selected ? color(R.attr.appPrimary) : color(R.attr.appOutline), 18));
            choice.setSelected(selected);
            choice.setOnClickListener(v -> ThemeManager.select(activity, preset));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
            params.bottomMargin = dp(10);
            palettes.addView(choice, params);
        }
        pages.get("appearance").addView(palettes, 0);
        pages.get("reading").addView(card(R.string.settings_defaults,
                R.string.settings_defaults_summary, R.drawable.navigation_books,
                () -> AppearanceBottomSheet.showDefaults(activity)), 0);
        pages.get("library").addView(card(R.string.settings_formats,
                R.string.settings_formats_summary, R.drawable.navigation_books,
                () -> open("formats")));
        if (AppsConfig.IS_FDROID) pages.get("sync").addView(
                text(activity.getString(R.string.settings_sync_unavailable), 16, false));
        current = state == null ? "" : state.getString("settings.category", "");
        if (!pages.containsKey(current)) current = "";
        render();
    }

    boolean back() {
        if (current.isEmpty()) return false;
        open("formats".equals(current) ? "library" : "");
        return true;
    }

    void save(Bundle state) {
        positions.putInt(current, scroll.getScrollY());
        state.putString("settings.category", current);
        state.putBundle("settings.positions", positions);
    }

    void restoreScroll() {
        scroll.post(() -> scroll.scrollTo(0, positions.getInt(current, 0)));
    }

    private void open(String category) {
        positions.putInt(current, scroll.getScrollY());
        current = category;
        render();
        restoreScroll();
        scroll.announceForAccessibility(title.getText());
    }

    private void render() {
        for (Map.Entry<String, LinearLayout> page : pages.entrySet()) {
            page.getValue().setVisibility(page.getKey().equals(current) ? View.VISIBLE : View.GONE);
        }
        int label = R.string.settings;
        for (int i = 0; i < KEYS.length; i++) if (KEYS[i].equals(current)) label = TITLES[i];
        if ("formats".equals(current)) label = R.string.settings_formats;
        title.setText(label);
        title.setTextSize(current.isEmpty() ? 28 : 22);
        back.setImageResource(current.isEmpty() ? R.drawable.glyphicons_599_menu_close
                : R.drawable.glyphicons_223_chevron_left);
        back.setContentDescription(activity.getString(current.isEmpty()
                ? R.string.close_menu : R.string.settings_back));
        applyOverviewHeader();
    }
    void onTintChanged() {
        applyOverviewHeader();
    }

    private void applyOverviewHeader() {
        if (current.isEmpty()) {
            title.setTextColor(color(R.attr.appOnSurface));
            back.setImageTintList(android.content.res.ColorStateList.valueOf(color(R.attr.appOnSurface)));
            header.setBackgroundColor(color(R.attr.appBackground));
        } else {
            title.setTextColor(color(R.attr.appOnPrimary));
            back.setImageTintList(android.content.res.ColorStateList.valueOf(color(R.attr.appOnPrimary)));
            header.setBackgroundColor(color(R.attr.appPrimary));
        }
    }


    private View card(int heading, int summary, int icon, Runnable click) {
        LinearLayout row = new LinearLayout(activity);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(14), dp(14), dp(14), dp(14));
        row.setMinimumHeight(dp(80));
        row.setBackground(shape(color(R.attr.appSurface), color(R.attr.appOutline), 18));
        ImageView image = new ImageView(activity);
        image.setImageResource(icon);
        image.setColorFilter(color(R.attr.appPrimary));
        image.setBackground(shape(color(R.attr.appSurfaceVariant), color(R.attr.appSurfaceVariant), 14));
        image.setPadding(dp(10), dp(10), dp(10), dp(10));
        image.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        row.addView(image, new LinearLayout.LayoutParams(dp(42), dp(42)));
        LinearLayout labels = column();
        labels.setPadding(dp(12), 0, dp(8), 0);
        labels.addView(text(activity.getString(heading), 16, true));
        labels.addView(text(activity.getString(summary), 13, false));
        row.addView(labels, new LinearLayout.LayoutParams(0, -2, 1));
        TextView arrow = text("›", 24, false);
        arrow.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        row.addView(arrow);
        row.setFocusable(true);
        row.setOnClickListener(v -> click.run());
        android.util.TypedValue selectable = new android.util.TypedValue();
        activity.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, selectable, true);
        row.setForeground(activity.getDrawable(selectable.resourceId));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.bottomMargin = dp(12);
        row.setLayoutParams(params);
        return row;
    }

    private LinearLayout column() {
        LinearLayout view = new LinearLayout(activity);
        view.setOrientation(LinearLayout.VERTICAL);
        view.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        return view;
    }

    private TextView text(String value, int size, boolean bold) {
        TextView text = new TextView(activity);
        text.setText(value);
        text.setTextSize(size);
        text.setTextColor(color(bold ? R.attr.appOnSurface : R.attr.appOnSurfaceVariant));
        if (bold) text.setTypeface(null, Typeface.BOLD);
        return text;
    }

    private GradientDrawable shape(int fill, int stroke, int radius) {
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(fill);
        shape.setStroke(dp(1), stroke);
        shape.setCornerRadius(dp(radius));
        return shape;
    }

    private int color(int attr) { return ThemeManager.color(activity, attr); }
    private int dp(int value) {
        return Math.round(value * activity.getResources().getDisplayMetrics().density);
    }
}
