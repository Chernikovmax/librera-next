package com.foobnix.ui2;

import android.annotation.TargetApi;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.FragmentActivity;

import com.foobnix.android.utils.Dips;
import com.foobnix.android.utils.LOG;
import com.foobnix.model.AppState;
import com.foobnix.pdf.info.IMG;
import com.foobnix.pdf.info.R;
import com.foobnix.sys.TempHolder;
import com.foobnix.tts.TTSEngine;
import com.foobnix.tts.TTSNotification;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public abstract class ReaderFragmentActivity extends FragmentActivity {

    Handler handler;

    boolean doubleBackToExitPressedOnce = false;

    public abstract void onFinishActivity();

    /**
     * A touch handler that empties a layout while a second finger is down on another of its
     * children leaves the platform's dispatch loop holding a target it has just recycled, and the
     * next step of that loop reads its null child (ViewGroup.resetCancelNextUpFlag). The layout has
     * already let go of its targets by then, so dropping this one event is all it costs.
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        try {
            return super.dispatchTouchEvent(ev);
        } catch (NullPointerException e) {
            LOG.e(e);
            return true;
        }
    }

    @Override
    protected void onCreate(Bundle arg0) {
        if (AppState.get().isSystemThemeColor) {
            AppState.get().appTheme = Dips.isDarkThemeOn() ? AppState.THEME_DARK : AppState.THEME_LIGHT;
        }

        if (AppState.get().appTheme == AppState.THEME_LIGHT || AppState.get().appTheme == AppState.THEME_INK) {
            setTheme(R.style.StyledIndicatorsWhite);
        } else {
            setTheme(R.style.StyledIndicatorsBlack);
        }
        super.onCreate(arg0);

        handler = new Handler(Looper.getMainLooper());

        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                onBackPressedAction();
            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        LOG.d("ReaderFragmentActivity onSaveInstanceState before", outState);

        if (outState != null) {
            outState.clear();
        }
        LOG.d("ReaderFragmentActivity onSaveInstanceState after", outState);
        super.onSaveInstanceState(outState);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        LOG.d("ReaderFragmentActivity onRestoreInstanceState before", savedInstanceState);

        if (savedInstanceState != null) {
            savedInstanceState.clear();
        }
        LOG.d("ReaderFragmentActivity onRestoreInstanceState after", savedInstanceState);

        super.onRestoreInstanceState(savedInstanceState);
    }

    public void finishActivityAfterReaderAction() {
        TempHolder.get().loadingCancelled.set(true);
        IMG.pauseRequests(this);
        TTSNotification.hideNotification();
        TTSEngine.get().shutdown();
        onFinishActivity();
    }

    public void onBackPressedAction() {

        LOG.d("onBackPressed", "doubleBackToExitPressedOnce", doubleBackToExitPressedOnce);
        if (doubleBackToExitPressedOnce) {
            handler.removeCallbacksAndMessages(null);
            onBackPressedFinishImpl();
            return;
        }
        this.doubleBackToExitPressedOnce = true;

        if (this instanceof MainTabs2) {
            if (handler != null) {
                handler.postDelayed(() -> {
                    doubleBackToExitPressedOnce = false;
                    LOG.d("onBackPressed", "timer", doubleBackToExitPressedOnce);
                    onBackPressedImpl();
                }, 500);

            }
        } else {
            onBackPressedImpl();
        }
    }

    public abstract void onBackPressedImpl();

    public abstract void onBackPressedFinishImpl();
}
