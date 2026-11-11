package com.foobnix.pdf.search.activity;

import static org.junit.Assert.assertEquals;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.foobnix.model.AppSP;
import com.foobnix.model.AppState;

import org.greenrobot.eventbus.EventBus;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class PageBackgroundDeviceTest {
    @Test
    public void shortEpubUsesBookBackgroundWhilePdfKeepsItsSurround() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            AppState state = AppState.get();
            String path = AppSP.get().lastBookPath;
            boolean day = state.isDayNotInvert;
            boolean image = state.isUseBGImageNight;
            int background = state.colorNigthBg;
            int surround = state.colorNigthForeground;
            PageImaveView view = new PageImaveView(
                    InstrumentationRegistry.getInstrumentation().getTargetContext(), null);
            Matrix matrix = new Matrix(view.imageMatrix());
            Bitmap page = Bitmap.createBitmap(100, 20, Bitmap.Config.ARGB_8888);
            Bitmap screen = Bitmap.createBitmap(100, 200, Bitmap.Config.ARGB_8888);
            try {
                state.isDayNotInvert = false;
                state.isUseBGImageNight = false;
                state.colorNigthBg = Color.BLACK;
                state.colorNigthForeground = Color.GRAY;
                page.eraseColor(Color.BLACK);
                view.layout(0, 0, 100, 200);
                view.addBitmap(page);
                view.imageMatrix().reset();
                AppSP.get().lastBookPath = "/test/short.epub";
                view.draw(new Canvas(screen));
                assertEquals("EPUB page", Color.BLACK, screen.getPixel(50, 10));
                assertEquals("Empty space below EPUB", Color.BLACK, screen.getPixel(50, 190));
                AppSP.get().lastBookPath = "/test/short.pdf";
                view.draw(new Canvas(screen));
                assertEquals("PDF surround", Color.GRAY, screen.getPixel(50, 190));
            } finally {
                AppSP.get().lastBookPath = path;
                state.isDayNotInvert = day;
                state.isUseBGImageNight = image;
                state.colorNigthBg = background;
                state.colorNigthForeground = surround;
                view.imageMatrix().set(matrix);
                EventBus.getDefault().unregister(view);
                page.recycle();
                screen.recycle();
            }
        });
    }
}
