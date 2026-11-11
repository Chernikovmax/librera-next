package com.foobnix.pdf.info.view;

import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.foobnix.pdf.info.R;

import org.junit.Test;
import org.junit.runner.RunWith;

/** Verifies that each app palette remains legible over the supported activity base themes. */
@RunWith(AndroidJUnit4.class)
public class ThemePaletteContrastTest {

    private static final int[] BASE_THEMES = {
            R.style.StyledIndicatorsWhite,
            R.style.StyledIndicatorsBlack
    };
    private static final int[] PALETTE_OVERLAYS = {
            R.style.ThemeOverlay_Librera_Cream,
            R.style.ThemeOverlay_Librera_Oled,
            R.style.ThemeOverlay_Librera_Eink
    };
    private static final String[] PALETTE_NAMES = {"Cream", "OLED", "E-Ink"};

    private static final int[] TEXT_BACKGROUNDS = {
            R.attr.appBackground,
            R.attr.appSurface,
            R.attr.appFeaturedContainer,
            R.attr.appSelectedContainer
    };
    private static final String[] TEXT_BACKGROUND_NAMES = {
            "background", "surface", "featured container", "selected container"
    };
    private static final int[] ICON_BACKGROUNDS = {
            R.attr.appBackground,
            R.attr.appSurface,
            R.attr.appSurfaceVariant,
            R.attr.appFeaturedContainer,
            R.attr.appSelectedContainer
    };
    private static final String[] ICON_BACKGROUND_NAMES = {
            "background", "surface", "surface variant", "featured container", "selected container"
    };

    @Test
    public void textAndPrimaryIconMeetContrastTargets() {
        Context base = InstrumentationRegistry.getInstrumentation().getTargetContext();
        for (int baseTheme : BASE_THEMES) {
            for (int palette = 0; palette < PALETTE_OVERLAYS.length; palette++) {
                ContextThemeWrapper themed = new ContextThemeWrapper(base, baseTheme);
                themed.getTheme().applyStyle(PALETTE_OVERLAYS[palette], true);

                int primary = color(themed, R.attr.appOnSurface);
                int secondary = color(themed, R.attr.appOnSurfaceVariant);
                for (int i = 0; i < TEXT_BACKGROUNDS.length; i++) {
                    int background = color(themed, TEXT_BACKGROUNDS[i]);
                    assertContrast(PALETTE_NAMES[palette] + " primary text on "
                            + TEXT_BACKGROUND_NAMES[i], primary, background, 4.5);
                    assertContrast(PALETTE_NAMES[palette] + " secondary text on "
                            + TEXT_BACKGROUND_NAMES[i], secondary, background, 4.5);
                }

                assertContrast(PALETTE_NAMES[palette] + " onPrimary on primary",
                        color(themed, R.attr.appOnPrimary), color(themed, R.attr.appPrimary), 4.5);

                int icon = color(themed, R.attr.appPrimary);
                for (int i = 0; i < ICON_BACKGROUNDS.length; i++) {
                    assertContrast(PALETTE_NAMES[palette] + " primary icon on "
                            + ICON_BACKGROUND_NAMES[i], icon, color(themed, ICON_BACKGROUNDS[i]), 3.0);
                }
            }
        }
    }

    private static int color(Context context, int attribute) {
        TypedValue value = new TypedValue();
        assertTrue("Theme does not resolve attribute " + context.getResources().getResourceEntryName(attribute),
                context.getTheme().resolveAttribute(attribute, value, true));
        if (value.resourceId != 0) {
            android.content.res.TypedArray resolved = context.obtainStyledAttributes(new int[]{attribute});
            try {
                return resolved.getColor(0, value.data);
            } finally {
                resolved.recycle();
            }
        }
        return value.data;
    }

    private static void assertContrast(String description, int foreground, int background,
                                       double minimum) {
        double ratio = (luminance(foreground) + 0.05) / (luminance(background) + 0.05);
        if (ratio < 1.0) {
            ratio = 1.0 / ratio;
        }
        assertTrue(description + " contrast " + ratio + " is below " + minimum,
                ratio >= minimum);
    }

    private static double luminance(int color) {
        return 0.2126 * linear((color >> 16) & 0xff)
                + 0.7152 * linear((color >> 8) & 0xff)
                + 0.0722 * linear(color & 0xff);
    }

    private static double linear(int channel) {
        double srgb = channel / 255.0;
        return srgb <= 0.04045 ? srgb / 12.92 : Math.pow((srgb + 0.055) / 1.055, 2.4);
    }
}
