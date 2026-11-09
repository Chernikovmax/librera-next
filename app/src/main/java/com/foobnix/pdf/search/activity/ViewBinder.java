package com.foobnix.pdf.search.activity;

import android.view.View;

import com.foobnix.android.utils.LOG;
import com.foobnix.pdf.info.R;
import com.foobnix.model.AppState;
import com.foobnix.pdf.info.view.UnderlineImageView;

public class ViewBinder {

    public static void updateBrightness(UnderlineImageView onBC){
        LOG.d("updateBrightness",AppState.get().isShowContrastButton,AppState.get().isEnableBCOptional1);
        onBC.setVisibility(View.GONE);
        if (AppState.get().isShowContrastButton || AppState.get().isEnableBCOptional1) {
            onBC.setVisibility(View.VISIBLE);
        }

        onBC.underline(AppState.get().isEnableBCOptional1);


    }

}


