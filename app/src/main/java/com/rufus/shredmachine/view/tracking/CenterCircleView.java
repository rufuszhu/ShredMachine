package com.rufus.shredmachine.view.tracking;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.rufus.shredmachine.R;

import java.util.Timer;
import java.util.Vector;

import butterknife.ButterKnife;
import timber.log.Timber;

/**
 * Created by rufus on 2015-10-30.
 */
public class CenterCircleView extends RelativeLayout {

    public CenterCircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.center_circle_view, this);
        ButterKnife.bind(this);
    }

    public void dim(){
        setAlpha(1.0f);
        animate().alpha(0.3f).setDuration(500).start();
    }

    public void light(){
        setAlpha(0.3f);
        animate().alpha(1f).setDuration(500).start();
    }
}
