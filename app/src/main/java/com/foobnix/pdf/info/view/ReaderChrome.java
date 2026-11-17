package com.foobnix.pdf.info.view;

import android.content.res.ColorStateList;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.accessibility.AccessibilityManager;
import android.view.View;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.PopupMenu;
import android.view.Menu;

import com.foobnix.model.AppBook;
import com.foobnix.pdf.info.model.BookCSS;
import org.ebookdroid.common.settings.SettingsManager;
import org.ebookdroid.common.settings.books.SharedBooks;
import org.ebookdroid.droids.foliate.codec.FoliateDocument;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.foobnix.pdf.info.R;
import com.foobnix.model.AppState;
import com.foobnix.pdf.info.wrapper.DocumentController;
import com.foobnix.ui2.ThemeManager;

/** Overlay-only reader controls: changing visibility never changes the page viewport. */
public final class ReaderChrome {
    private final DocumentController dc;
    private final RelativeLayout root;
    private final LinearLayout top, bottom;
    private final TextView title, progress, pages;
    private final SeekBar seek, filterSeek;
    private final CheckBox filterEnabled;
    private final TextView filterAmount;
    private final java.util.List<ImageButton> buttons = new java.util.ArrayList<>();
    private final ImageButton previous, next;
    private Runnable onAutoHide;
    private TextView contents, fontPreset;
    private PopupMenu presetMenu;
    private static final int[] FONT_PRESETS = {14, 16, 18, 20, 22};
    private static final int[] THEME_PRESETS = {AppState.THEME_LIGHT, AppState.THEME_DARK_OLED, AppState.THEME_INK};
    private final Runnable timeout = new Runnable() {
        @Override public void run() {
            AccessibilityManager accessibility = (AccessibilityManager)
                    root.getContext().getSystemService(android.content.Context.ACCESSIBILITY_SERVICE);
            if (presetMenu != null || !root.hasWindowFocus() || seek.isPressed() || filterSeek.isPressed() || top.hasFocus() || bottom.hasFocus()
                    || (accessibility != null && accessibility.isTouchExplorationEnabled())) {
                root.postDelayed(this, 5000);
            } else if (onAutoHide != null) {
                onAutoHide.run();
            }
        }
    };

    public ReaderChrome(DocumentController dc, RelativeLayout root, String bookTitle,
                        Runnable back, View.OnClickListener bookmark,
                        View.OnLongClickListener bookmarkLong, View.OnClickListener appearance,
                        View.OnClickListener more, SeekBar.OnSeekBarChangeListener listener,
                        Runnable previousPage, Runnable nextPage) {
        this.dc = dc;
        this.root = root;
        top = panel(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        button(top, R.drawable.glyphicons_223_chevron_left, R.string.back, v -> back.run());
        title = new TextView(root.getContext());
        title.setText(bookTitle);
        title.setTextSize(14);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setSingleLine(true);
        title.setEllipsize(TextUtils.TruncateAt.END);
        top.addView(title, new LinearLayout.LayoutParams(0, -2, 1));
        ImageButton mark = button(top, R.drawable.glyphicons_73_bookmark, R.string.add_bookmark, bookmark);
        mark.setOnLongClickListener(bookmarkLong);
        TextView aa = new TextView(root.getContext());
        aa.setText("Aa");
        aa.setTextSize(19);
        aa.setGravity(Gravity.CENTER);
        aa.setContentDescription(root.getContext().getString(R.string.appearance_settings));
        aa.setFocusable(true);
        aa.setOnClickListener(appearance);
        aa.setBackgroundResource(R.drawable.redesign_toolbar_button);
        top.addView(aa, new LinearLayout.LayoutParams(dp(48), dp(48)));
        button(top, R.drawable.reader_more, R.string.menu, more);
        bottom = panel(LinearLayout.VERTICAL);
        LinearLayout quick = new LinearLayout(root.getContext());
        quick.setGravity(Gravity.CENTER_VERTICAL);
        quick.setPadding(dp(8), 0, dp(8), 0);
        contents = quickText(quick, root.getContext().getString(R.string.content_of_book));
        contents.setOnClickListener(v -> {
            if (onAutoHide != null) onAutoHide.run();
            DragingDialogs.dialogShowContent(dc.getActivity().findViewById(R.id.anchor), dc);
        });
        button(quick, R.drawable.reader_palette, R.string.settings_palettes, this::showThemePresets);
        fontPreset = quickText(quick, "");
        fontPreset.setOnClickListener(this::showFontPresets);
        bottom.addView(quick);
        updateFontPreset();
        LinearLayout counts = new LinearLayout(root.getContext());
        counts.setPadding(dp(12), dp(8), dp(12), 0);
        progress = new TextView(root.getContext());
        progress.setTextSize(12);
        progress.setTypeface(null, android.graphics.Typeface.BOLD);
        counts.addView(progress, new LinearLayout.LayoutParams(0, -2, 1));
        pages = new TextView(root.getContext());
        pages.setTextSize(12);
        counts.addView(pages, new LinearLayout.LayoutParams(-2, -2));
        bottom.addView(counts);
        LinearLayout filter = new LinearLayout(root.getContext());
        filter.setOrientation(LinearLayout.VERTICAL);
        filter.setPadding(dp(12), 0, dp(12), 0);
        LinearLayout filterCaption = new LinearLayout(root.getContext());
        filterCaption.setGravity(Gravity.CENTER_VERTICAL);
        filterEnabled = new CheckBox(root.getContext());
        filterEnabled.setText(R.string.bluelight_filter);
        filterEnabled.setMinHeight(dp(48));
        filterCaption.addView(filterEnabled, new LinearLayout.LayoutParams(0, -2, 1));
        filterAmount = new TextView(root.getContext());
        filterCaption.addView(filterAmount);
        filter.addView(filterCaption);
        filterSeek = new SeekBar(root.getContext());
        filterSeek.setMax(99);
        filterSeek.setMinimumHeight(dp(48));
        filterSeek.setContentDescription(root.getContext().getString(R.string.bluelight_filter));
        filter.addView(filterSeek, new LinearLayout.LayoutParams(-1, dp(48)));
        filterEnabled.setOnCheckedChangeListener((button, checked) -> {
            BrightnessHelper.isEnableBlueFilter(checked);
            applyFilter();
            scheduleHide();
        });
        filterSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar bar, int value, boolean user) {
                if (!user) return;
                BrightnessHelper.blueLightAlpha(value);
                BrightnessHelper.isEnableBlueFilter(value > 0);
                filterEnabled.setChecked(value > 0);
                filterAmount.setText(value + "%");
                applyFilter();
            }
            @Override public void onStartTrackingTouch(SeekBar bar) { root.removeCallbacks(timeout); }
            @Override public void onStopTrackingTouch(SeekBar bar) {
                AppState.get().save(root.getContext());
                scheduleHide();
            }
        });
        filter.setVisibility(com.foobnix.android.utils.Dips.isEInk() ? View.GONE : View.VISIBLE);
        bottom.addView(filter);
        LinearLayout row = new LinearLayout(root.getContext());
        row.setGravity(Gravity.CENTER_VERTICAL);
        previous = button(row, R.drawable.glyphicons_223_chevron_left, R.string.previous_page, v -> previousPage.run());
        seek = new SeekBar(root.getContext());
        seek.setContentDescription(root.getContext().getString(R.string.go_to_page_dialog));
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onStartTrackingTouch(SeekBar bar) {
                root.removeCallbacks(timeout);
                listener.onStartTrackingTouch(bar);
            }
            @Override public void onProgressChanged(SeekBar bar, int value, boolean user) {
                listener.onProgressChanged(bar, value, user);
            }
            @Override public void onStopTrackingTouch(SeekBar bar) {
                listener.onStopTrackingTouch(bar);
                scheduleHide();
            }
        });
        row.addView(seek, new LinearLayout.LayoutParams(0, dp(48), 1));
        next = button(row, R.drawable.glyphicons_224_chevron_right, R.string.next_page, v -> nextPage.run());
        bottom.addView(row);
        root.addView(top, position(true));
        root.addView(bottom, position(false));
        root.addOnLayoutChangeListener(layoutListener);
        refreshTheme();
        setVisible(false);
    }

    private TextView quickText(LinearLayout row, String text) {
        TextView view = new TextView(root.getContext());
        view.setText(text);
        view.setTextSize(14);
        view.setMinHeight(dp(48));
        view.setMinWidth(dp(48));
        view.setGravity(Gravity.CENTER);
        view.setPadding(dp(12), dp(8), dp(12), dp(8));
        view.setFocusable(true);
        view.setBackgroundResource(R.drawable.reader_quick_button);
        FrameLayout slot = new FrameLayout(root.getContext());
        slot.addView(view, new FrameLayout.LayoutParams(-2, -2, Gravity.CENTER));
        row.addView(slot, new LinearLayout.LayoutParams(0, -2, 1));
        return view;
    }

    private void updateFontPreset() {
        int size = BookCSS.get().fontSize(dc.getCodecDocument() instanceof FoliateDocument);
        fontPreset.setText(size + " ▾");
        fontPreset.setContentDescription(root.getContext().getString(R.string.font_size) + ": " + size);
        fontPreset.setEnabled(dc.isTextFormat());
        fontPreset.setAlpha(dc.isTextFormat() ? 1f : .5f);
    }

    private PopupMenu presetMenu(View anchor) {
        if (presetMenu != null) presetMenu.dismiss();
        root.removeCallbacks(timeout);
        PopupMenu menu = new PopupMenu(root.getContext(), anchor, Gravity.END);
        presetMenu = menu;
        menu.setOnDismissListener(dismissed -> { presetMenu = null; scheduleHide(); });
        return menu;
    }

    private void showThemePresets(View anchor) {
        PopupMenu menu = presetMenu(anchor);
        String[] labels = {"Cream", "OLED Black", "E-Ink Color"};
        for (int i = 0; i < THEME_PRESETS.length; i++) {
            int preset = THEME_PRESETS[i];
            menu.getMenu().add(0, preset, i, labels[i]).setCheckable(true)
                    .setChecked(preset == ThemeManager.readerPreset());
        }
        menu.getMenu().setGroupCheckable(0, true, true);
        menu.setOnMenuItemClickListener(item -> {
            dc.saveCurrentPageAsync();
            AppBook book = SettingsManager.getBookSettings();
            book.readerTheme = item.getItemId();
            SharedBooks.save(book);
            dc.restartActivity();
            return true;
        });
        menu.show();
    }

    private void showFontPresets(View anchor) {
        PopupMenu menu = presetMenu(anchor);
        boolean foliate = dc.getCodecDocument() instanceof FoliateDocument;
        for (int size : FONT_PRESETS) {
            menu.getMenu().add(0, size, Menu.NONE, String.valueOf(size)).setCheckable(true)
                    .setChecked(size == BookCSS.get().fontSize(foliate));
        }
        menu.getMenu().setGroupCheckable(0, true, true);
        menu.setOnMenuItemClickListener(item -> {
            dc.saveCurrentPageAsync();
            BookCSS css = BookCSS.get();
            AppBook book = SettingsManager.getBookSettings();
            css.setFontSize(foliate, item.getItemId());
            css.setAppearanceScope(book, false);
            SharedBooks.save(book);
            dc.restartActivity();
            return true;
        });
        menu.show();
    }

    private void applyFilter() {
        BrightnessHelper.updateOverlay(dc.getActivity().findViewById(R.id.overlay));
    }

    private final View.OnLayoutChangeListener layoutListener = (v,l,t,r,b,ol,ot,or,ob) -> updateInsets();

    private int dp(int value) { return Math.round(value * root.getResources().getDisplayMetrics().density); }
    private LinearLayout panel(int orientation) {
        LinearLayout view = new LinearLayout(root.getContext());
        view.setOrientation(orientation);
        // This panel owns its palette; legacy link tinting forces reader sliders white.
        view.setTag("no_tint");
        view.setPadding(dp(4), dp(4), dp(4), dp(4));
        view.setClickable(true);
        // Consume page taps without becoming an empty keyboard/TalkBack stop.
        view.setFocusable(false);
        view.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        return view;
    }
    private RelativeLayout.LayoutParams position(boolean upper) {
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(-1, -2);
        lp.addRule(upper ? RelativeLayout.ALIGN_PARENT_TOP : RelativeLayout.ALIGN_PARENT_BOTTOM);
        lp.setMargins(dp(12), dp(12), dp(12), dp(12));
        return lp;
    }
    private ImageButton button(LinearLayout row, int icon, int label, View.OnClickListener action) {
        ImageButton view = new ImageButton(root.getContext());
        view.setImageResource(icon);
        view.setContentDescription(root.getContext().getString(label));
        view.setPadding(dp(13), dp(13), dp(13), dp(13));
        view.setBackgroundResource(R.drawable.redesign_toolbar_button);
        view.setFocusable(true);
        view.setOnClickListener(v -> { action.onClick(v); scheduleHide(); });
        row.addView(view, new LinearLayout.LayoutParams(dp(48), dp(48)));
        buttons.add(view);
        return view;
    }
    private void updateInsets() {
        WindowInsetsCompat window = ViewCompat.getRootWindowInsets(root);
        if (window == null) return;
        Insets bars = window.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
        // The legacy root already reserves safe left/right and, in normal mode, bottom insets.
        Insets reserved = DocumentController.keptClearOf(dc.getActivity(), window);
        RelativeLayout.LayoutParams upper = (RelativeLayout.LayoutParams) top.getLayoutParams();
        RelativeLayout.LayoutParams lower = (RelativeLayout.LayoutParams) bottom.getLayoutParams();
        int topMargin = dp(12) + Math.max(0, bars.top - root.getTop());
        int bottomMargin = dp(12) + Math.max(0, bars.bottom - reserved.bottom);
        if (upper.topMargin != topMargin) { upper.topMargin = topMargin; top.setLayoutParams(upper); }
        if (lower.bottomMargin != bottomMargin) { lower.bottomMargin = bottomMargin; bottom.setLayoutParams(lower); }
    }
    public void setOnAutoHide(Runnable action) { onAutoHide = action; }
    private void scheduleHide() {
        root.removeCallbacks(timeout);
        if (top.getVisibility() == View.VISIBLE && !AppState.get().isEnableAccessibility) root.postDelayed(timeout, 8000);
    }
    public void setVisible(boolean visible) {
        top.setVisibility(visible ? View.VISIBLE : View.GONE);
        bottom.setVisibility(visible ? View.VISIBLE : View.GONE);
        if (visible) {
            updateFontPreset();
            filterSeek.setProgress(BrightnessHelper.blueLightAlpha());
            filterAmount.setText(BrightnessHelper.blueLightAlpha() + "%");
            filterEnabled.setChecked(BrightnessHelper.isEnableBlueFilter());
        }
        if (visible) { top.bringToFront(); bottom.bringToFront(); updateInsets(); scheduleHide(); }
        else root.removeCallbacks(timeout);
    }
    public void setTitle(String value) { title.setText(value); }
    public void update(int page, int count) {
        int total = Math.max(1, count);
        int current = Math.max(1, Math.min(page, total));
        progress.setText(Math.round(100f * (current - 1) / Math.max(1, total - 1)) + "%");
        pages.setText(current + " / " + total);
        if (!seek.isPressed()) { seek.setMax(total - 1); seek.setProgress(current - 1); }
        previous.setEnabled(current > 1);
        next.setEnabled(current < total);
    }
    public void refreshTheme() {
        int foreground = ThemeManager.color(root.getContext(), R.attr.appOnSurface);
        int surface = ThemeManager.color(root.getContext(), R.attr.appSurface);
        int outline = ThemeManager.color(root.getContext(), R.attr.appOutline);
        int success = ThemeManager.color(root.getContext(), R.attr.appSuccess);
        int track = androidx.core.graphics.ColorUtils.blendARGB(surface, foreground, .60f);
        for (LinearLayout panel : new LinearLayout[]{top, bottom}) {
            GradientDrawable shape = new GradientDrawable();
            shape.setColor(surface);
            shape.setCornerRadius(dp(24));
            shape.setStroke(dp(1), outline);
            panel.setBackground(shape);
            panel.setElevation(DocumentController.isEinkOrMode(root.getContext()) ? 0 : dp(4));
        }
        contents.setTextColor(foreground);
        fontPreset.setTextColor(foreground);
        title.setTextColor(foreground);
        ((TextView) top.getChildAt(3)).setTextColor(foreground);
        progress.setTextColor(success);
        pages.setTextColor(ThemeManager.color(root.getContext(), R.attr.appOnSurfaceVariant));
        for (ImageButton button : buttons) {
            button.setImageDrawable(button.getDrawable().mutate());
            button.setColorFilter(foreground);
        }
        filterEnabled.setTextColor(foreground);
        filterEnabled.setButtonTintList(ColorStateList.valueOf(success));
        filterAmount.setTextColor(ThemeManager.color(root.getContext(), R.attr.appOnSurfaceVariant));
        filterSeek.setProgressTintList(ColorStateList.valueOf(success));
        filterSeek.setProgressBackgroundTintList(ColorStateList.valueOf(track));
        filterSeek.setThumbTintList(ColorStateList.valueOf(success));
        seek.setProgressTintList(ColorStateList.valueOf(success));
        seek.setProgressBackgroundTintList(ColorStateList.valueOf(track));
        seek.setThumbTintList(ColorStateList.valueOf(success));
    }
    public void destroy() {
        if (presetMenu != null) presetMenu.dismiss();
        root.removeCallbacks(timeout);
        root.removeOnLayoutChangeListener(layoutListener);
    }
}
