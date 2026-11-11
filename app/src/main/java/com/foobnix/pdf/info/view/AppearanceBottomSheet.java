package com.foobnix.pdf.info.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.foobnix.model.AppBook;
import com.foobnix.model.AppState;
import com.foobnix.pdf.info.R;
import com.foobnix.pdf.info.model.BookCSS;
import com.foobnix.pdf.info.wrapper.DocumentController;
import com.foobnix.ui2.ThemeManager;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.ebookdroid.common.settings.SettingsManager;
import org.ebookdroid.common.settings.books.SharedBooks;
import org.ebookdroid.droids.foliate.codec.FoliateDocument;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/** Edits a private draft; dismissing the sheet never changes the running renderer. */
public class AppearanceBottomSheet extends BottomSheetDialogFragment {
    private static final String TAG = "reader-appearance";
    private DocumentController controller;
    private BookCSS draft;
    private boolean foliate;
    private boolean defaults;
    private int themePreset;

    @Override public int getTheme() {
        return ThemeManager.isEink() ? R.style.ReaderAppearanceDialog_Eink
                : R.style.ReaderAppearanceDialog;
    }

    @NonNull @Override public Dialog onCreateDialog(@Nullable Bundle state) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(state);
        // Set the initial state before attachment, rather than settling an already visible sheet.
        dialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
        dialog.getBehavior().setSkipCollapsed(true);
        if (DocumentController.isEinkOrMode(requireContext())) {
            dialog.getBehavior().setDraggable(false);
            dialog.setDismissWithAnimation(false);
        }
        return dialog;
    }

    public static void show(DocumentController controller, View anchor, Runnable refresh) {
        if (!(controller.getActivity() instanceof FragmentActivity)) return;
        FragmentActivity activity = (FragmentActivity) controller.getActivity();
        if (activity.getSupportFragmentManager().isStateSaved()
                || activity.getSupportFragmentManager().findFragmentByTag(TAG) != null) return;
        AppearanceBottomSheet sheet = new AppearanceBottomSheet();
        sheet.controller = controller;
        sheet.show(activity.getSupportFragmentManager(), TAG);
    }

    public static void showDefaults(FragmentActivity activity) {
        if (activity.getSupportFragmentManager().isStateSaved()
                || activity.getSupportFragmentManager().findFragmentByTag(TAG) != null) return;
        AppearanceBottomSheet sheet = new AppearanceBottomSheet();
        Bundle args = new Bundle();
        args.putBoolean("settingsDefaults", true);
        sheet.setArguments(args);
        sheet.show(activity.getSupportFragmentManager(), TAG);
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup parent,
                             @Nullable Bundle state) {
        Context c = requireDialog().getContext();
        c.getTheme().applyStyle(ThemeManager.overlay(), true);
        LinearLayout root = column(c);
        root.setPadding(dp(20), dp(12), dp(20), dp(20));
        android.graphics.drawable.GradientDrawable surface = new android.graphics.drawable.GradientDrawable();
        surface.setColor(ThemeManager.color(c, R.attr.appSurface));
        float radius = dp(24);
        surface.setCornerRadii(new float[]{radius, radius, radius, radius, 0, 0, 0, 0});
        root.setBackground(surface);
        root.setOutlineProvider(new android.view.ViewOutlineProvider() {
            @Override public void getOutline(View view, android.graphics.Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight() + dp(24), dp(24));
            }
        });
        root.setClipToOutline(true);
        root.setLayoutParams(new ViewGroup.LayoutParams(-1,
                (int) (getResources().getDisplayMetrics().heightPixels * .70f)));
        View handle = new View(c);
        android.graphics.drawable.GradientDrawable grip = new android.graphics.drawable.GradientDrawable();
        grip.setColor(ThemeManager.color(c, R.attr.appOutline));
        grip.setCornerRadius(dp(2));
        handle.setBackground(grip);
        handle.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        LinearLayout.LayoutParams gripParams = new LinearLayout.LayoutParams(dp(32), dp(4));
        gripParams.gravity = android.view.Gravity.CENTER_HORIZONTAL;
        gripParams.bottomMargin = dp(6);
        root.addView(handle, gripParams);
        LinearLayout header = new LinearLayout(c);
        header.setGravity(android.view.Gravity.CENTER_VERTICAL);
        TextView heading = label(R.string.reader_appearance, 20);
        heading.setTypeface(null, android.graphics.Typeface.BOLD);
        header.addView(heading, new LinearLayout.LayoutParams(0, -2, 1));
        ImageButton dismiss = new ImageButton(c);
        dismiss.setImageResource(R.drawable.glyphicons_599_menu_close);
        dismiss.setColorFilter(ThemeManager.color(c, R.attr.appOnSurface));
        dismiss.setBackgroundResource(R.drawable.redesign_toolbar_button);
        dismiss.setPadding(dp(14), dp(14), dp(14), dp(14));
        dismiss.setContentDescription(getString(R.string.close));
        dismiss.setOnClickListener(v -> dismiss());
        header.addView(dismiss, new LinearLayout.LayoutParams(dp(48), dp(48)));
        root.addView(header);
        boolean settingsDefaults = getArguments() != null
                && getArguments().getBoolean("settingsDefaults");
        // A restored fragment has no live document controller; reopening Aa binds the new reader.
        if (controller == null && !settingsDefaults) {
            dismiss();
            return root;
        }
        foliate = settingsDefaults
                ? com.foobnix.model.AppState.get().getEpubEngine()
                    == com.foobnix.model.AppState.EPUB_ENGINE_FOLIATE
                : controller.getCodecDocument() instanceof FoliateDocument;
        draft = new BookCSS();
        draft.applyAppearanceSnapshot(BookCSS.get().appearanceSnapshot());
        if (settingsDefaults) draft.applyAppearanceSnapshot(BookCSS.get().defaultAppearance);
        if (state != null) draft.applyAppearanceSnapshot(state.getString("draft"));
        defaults = settingsDefaults || state != null && state.getBoolean("defaults");
        themePreset = state != null ? state.getInt("themePreset", AppState.get().appTheme)
                : AppState.get().appTheme;
        ScrollView scroll = new ScrollView(c);
        LinearLayout controls = column(c);
        scroll.addView(controls);
        int[] presets = {AppState.THEME_LIGHT, AppState.THEME_DARK_OLED, AppState.THEME_INK};
        int selectedPreset = themePreset == AppState.THEME_LIGHT ? 0
                : themePreset == AppState.THEME_INK ? 2 : 1;
        controls.addView(choice(R.string.settings_palettes,
                new String[]{"Cream", "OLED Black", "E-Ink Color"}, selectedPreset,
                value -> themePreset = presets[value]));
        boolean typography = settingsDefaults || controller.isTextFormat();
        if (!typography) {
            controls.addView(label(R.string.reader_fixed_layout_appearance, 16));
        } else {
            RadioGroup scope = new RadioGroup(c);
            RadioButton book = new RadioButton(c);
            book.setId(View.generateViewId());
            book.setText(R.string.reader_this_book);
            book.setMinHeight(dp(48));
            book.setTextColor(ThemeManager.color(c, R.attr.appOnSurface));
            RadioButton all = new RadioButton(c);
            all.setId(View.generateViewId());
            all.setText(R.string.reader_default_books);
            all.setMinHeight(dp(48));
            all.setTextColor(ThemeManager.color(c, R.attr.appOnSurface));
            scope.addView(book); scope.addView(all);
            scope.check(defaults ? all.getId() : book.getId());
            scope.setOnCheckedChangeListener((group, checked) -> defaults = checked == all.getId());
            if (!settingsDefaults) controls.addView(scope);
            controls.addView(slider(R.string.font_size, draft.fontSize(foliate), 10, 70,
                    value -> draft.setFontSize(foliate, value)));
            controls.addView(slider(R.string.line_spacing, draft.lineHeight(foliate), 10, 30,
                    value -> draft.setLineHeight(foliate, value)));
            controls.addView(fonts());
            controls.addView(slider(R.string.reader_margin_top, draft.marginTop(foliate), 0, 50,
                    value -> draft.setMarginTop(foliate, value)));
            controls.addView(slider(R.string.reader_margin_bottom, draft.marginBottom(foliate), 0, 50,
                    value -> draft.setMarginBottom(foliate, value)));
            controls.addView(slider(R.string.reader_margin_sides, draft.marginLeft(foliate), 0, 50,
                    value -> { draft.setMarginLeft(foliate, value); draft.setMarginRight(foliate, value); }));
            controls.addView(choice(R.string.text_align,
                    new String[]{getString(R.string.width), getString(R.string.left),
                            getString(R.string.right), getString(R.string.center)},
                    draft.textAlign, value -> draft.textAlign = value));
            controls.addView(slider(R.string.indent, draft.textIndent, 0, 30, value -> draft.textIndent = value));
            controls.addView(slider(R.string.paragraph_spacing, draft.paragraphHeight, 0, 20,
                    value -> draft.paragraphHeight = value));
            CheckBox hyphens = new CheckBox(c);
            hyphens.setText(R.string.automatic_hyphenation);
            hyphens.setTextColor(ThemeManager.color(c, R.attr.appOnSurface));
            hyphens.setMinHeight(dp(48));
            hyphens.setChecked(draft.isAutoHypens);
            hyphens.setOnCheckedChangeListener((button, checked) -> draft.isAutoHypens = checked);
            controls.addView(hyphens);
        }
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        Button apply = new Button(c);
        apply.setText(R.string.apply);
        apply.setMinHeight(dp(48));
        apply.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                ThemeManager.color(c, R.attr.appPrimary)));
        apply.setTextColor(ThemeManager.color(c, R.attr.appOnPrimary));
        apply.setOnClickListener(v -> {
            if (settingsDefaults) {
                BookCSS.get().updateDefaultAppearance(requireContext(), draft.appearanceSnapshot());
                dismiss();
                if (themePreset != AppState.get().appTheme) ThemeManager.select(requireActivity(), themePreset);
                return;
            }
            controller.saveCurrentPageAsync();
            if (typography) {
                AppBook settings = SettingsManager.getBookSettings();
                BookCSS css = BookCSS.get();
                css.applyAppearanceSnapshot(draft.appearanceSnapshot());
                css.setAppearanceScope(settings, defaults);
                SharedBooks.save(settings);
            }
            dismiss();
            if (themePreset != AppState.get().appTheme) {
                ThemeManager.select(requireActivity(), themePreset);
            } else if (typography) {
                controller.restartActivity();
            }
        });
        root.addView(apply);
        return root;
    }

    @Override public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        if (getDialog() instanceof BottomSheetDialog) {
            View sheet = getDialog().findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (sheet != null) {
                sheet.setBackgroundColor(android.graphics.Color.TRANSPARENT);
                sheet.setOutlineProvider(getView().getOutlineProvider());
                sheet.setClipToOutline(true);
            }
            if (DocumentController.isEinkOrMode(requireContext())) {
                if (sheet != null) sheet.setElevation(0f);
                if (getDialog().getWindow() != null) getDialog().getWindow().setWindowAnimations(0);
            }
        }
    }

    @Override public void onConfigurationChanged(@NonNull android.content.res.Configuration configuration) {
        super.onConfigurationChanged(configuration);
        View root = getView();
        if (root != null) {
            ViewGroup.LayoutParams params = root.getLayoutParams();
            params.height = (int) (getResources().getDisplayMetrics().heightPixels * .70f);
            root.setLayoutParams(params);
        }
    }

    @Override public void onSaveInstanceState(@NonNull Bundle state) {
        super.onSaveInstanceState(state);
        if (draft != null) state.putString("draft", draft.appearanceSnapshot());
        state.putBoolean("defaults", defaults);
        state.putInt("themePreset", themePreset);
    }

    private interface Change { void set(int value); }

    private View slider(int title, int value, int min, int max, Change change) {
        LinearLayout row = column(requireDialog().getContext());
        String name = getString(title);
        LinearLayout caption = new LinearLayout(requireDialog().getContext());
        caption.setGravity(android.view.Gravity.CENTER_VERTICAL);
        TextView text = label(title, 14);
        text.setMinHeight(dp(28));
        text.setTypeface(null, android.graphics.Typeface.BOLD);
        caption.addView(text, new LinearLayout.LayoutParams(0, -2, 1));
        TextView amount = new TextView(requireDialog().getContext());
        amount.setText(String.valueOf(value));
        amount.setTextSize(14);
        amount.setTextColor(ThemeManager.color(requireDialog().getContext(), R.attr.appPrimary));
        caption.addView(amount);
        SeekBar bar = new SeekBar(requireDialog().getContext());
        bar.setMax(max - min);
        bar.setProgress(Math.max(0, Math.min(max - min, value - min)));
        bar.setMinimumHeight(dp(48));
        bar.setContentDescription(name);
        bar.setProgressTintList(android.content.res.ColorStateList.valueOf(
                ThemeManager.color(requireDialog().getContext(), R.attr.appPrimary)));
        bar.setThumbTintList(android.content.res.ColorStateList.valueOf(
                ThemeManager.color(requireDialog().getContext(), R.attr.appPrimary)));
        bar.setProgressBackgroundTintList(android.content.res.ColorStateList.valueOf(
                ThemeManager.color(requireDialog().getContext(), R.attr.appOutline)));
        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar view, int progress, boolean user) {
                if (user) {
                    change.set(progress + min);
                    amount.setText(String.valueOf(progress + min));
                }
            }
            @Override public void onStartTrackingTouch(SeekBar view) { }
            @Override public void onStopTrackingTouch(SeekBar view) { }
        });
        row.addView(caption); row.addView(bar);
        return row;
    }

    private View fonts() {
        List<String> fonts = new ArrayList<>(BookCSS.get().getAllFonts());
        if (!fonts.contains(draft.normalFont)) fonts.add(0, draft.normalFont);
        String[] names = new String[fonts.size()];
        for (int i = 0; i < names.length; i++) names[i] = new File(fonts.get(i)).getName();
        return choice(R.string.font_name, names, fonts.indexOf(draft.normalFont),
                selected -> draft.normalFont = fonts.get(selected));
    }

    private View choice(int title, String[] choices, int selected, Change change) {
        LinearLayout row = column(requireDialog().getContext());
        row.addView(label(title, 16));
        Spinner spinner = new Spinner(requireDialog().getContext());
        spinner.setAdapter(new ArrayAdapter<>(requireDialog().getContext(), android.R.layout.simple_spinner_dropdown_item, choices));
        spinner.setSelection(Math.max(0, Math.min(selected, choices.length - 1)));
        spinner.setMinimumHeight(dp(48));
        spinner.setContentDescription(getString(title));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onNothingSelected(AdapterView<?> parent) { }
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                change.set(position);
            }
        });
        row.addView(spinner);
        return row;
    }

    private LinearLayout column(Context c) {
        LinearLayout view = new LinearLayout(c);
        view.setOrientation(LinearLayout.VERTICAL);
        return view;
    }

    private TextView label(int text, int size) {
        TextView view = new TextView(requireDialog().getContext());
        view.setText(text); view.setTextSize(size);
        view.setMinHeight(dp(48));
        view.setGravity(android.view.Gravity.CENTER_VERTICAL);
        view.setTextColor(ThemeManager.color(requireContext(), R.attr.appOnSurface));
        return view;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
