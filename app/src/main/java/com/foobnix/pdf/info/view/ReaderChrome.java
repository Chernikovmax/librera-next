package com.foobnix.pdf.info.view;

import android.content.res.ColorStateList;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.accessibility.AccessibilityManager;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
    private final SeekBar seek;
    private final java.util.List<ImageButton> buttons = new java.util.ArrayList<>();
    private final ImageButton previous, next;
    private Runnable onAutoHide;
    private final Runnable timeout = new Runnable() {
        @Override public void run() {
            AccessibilityManager accessibility = (AccessibilityManager)
                    root.getContext().getSystemService(android.content.Context.ACCESSIBILITY_SERVICE);
            if (!root.hasWindowFocus() || seek.isPressed() || top.hasFocus() || bottom.hasFocus()
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

    private final View.OnLayoutChangeListener layoutListener = (v,l,t,r,b,ol,ot,or,ob) -> updateInsets();

    private int dp(int value) { return Math.round(value * root.getResources().getDisplayMetrics().density); }
    private LinearLayout panel(int orientation) {
        LinearLayout view = new LinearLayout(root.getContext());
        view.setOrientation(orientation);
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
        for (LinearLayout panel : new LinearLayout[]{top, bottom}) {
            GradientDrawable shape = new GradientDrawable();
            shape.setColor(surface);
            shape.setCornerRadius(dp(24));
            shape.setStroke(dp(1), outline);
            panel.setBackground(shape);
            panel.setElevation(DocumentController.isEinkOrMode(root.getContext()) ? 0 : dp(4));
        }
        title.setTextColor(foreground);
        ((TextView) top.getChildAt(3)).setTextColor(foreground);
        progress.setTextColor(success);
        pages.setTextColor(ThemeManager.color(root.getContext(), R.attr.appOnSurfaceVariant));
        for (ImageButton button : buttons) {
            button.setImageDrawable(button.getDrawable().mutate());
            button.setColorFilter(foreground);
        }
        seek.setProgressTintList(ColorStateList.valueOf(success));
        seek.setProgressBackgroundTintList(ColorStateList.valueOf(outline));
        seek.setThumbTintList(ColorStateList.valueOf(success));
    }
    public void destroy() { root.removeCallbacks(timeout); root.removeOnLayoutChangeListener(layoutListener); }
}
