package com.rufus.shredmachine.view.tracking;

import android.content.Context;
import android.graphics.drawable.Animatable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.rufus.shredmachine.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by rzhu on 10/27/2015.
 */
public class ControlBarView extends LinearLayout {

    private boolean isPause;
    private Context context;

    @Bind(R.id.morph)
    View morph;

    @OnClick(R.id.play_pause)
    void togglePlay(){
        if(isPause){
            morph.setBackground(getResources().getDrawable(R.drawable.switch_btn_stop_vector, context.getTheme()));
            ((Animatable) morph.getBackground()).start();
            isPause = false;
        }else {
            morph.setBackground(getResources().getDrawable(R.drawable.switch_btn_start_vector, context.getTheme()));
            ((Animatable) morph.getBackground()).start();
            isPause = true;
        }
    }

    public ControlBarView(Context context) {
        super(context);
        this.context = context;
        LayoutInflater.from(context).inflate(R.layout.track_control_bar_view, this);
    }

    public ControlBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.track_control_bar_view, this);
        ButterKnife.bind(this);
        this.context = context;
        isPause = false;
    }

}
