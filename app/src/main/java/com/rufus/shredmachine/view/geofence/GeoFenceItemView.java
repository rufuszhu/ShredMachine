package com.rufus.shredmachine.view.geofence;

import android.content.Context;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.rufus.shredmachine.R;
import com.rufus.shredmachine.model.GeofenceData;

import butterknife.Bind;
import butterknife.ButterKnife;

public final class GeoFenceItemView extends RelativeLayout {
    @Bind(R.id.geofence_item_nickname)
    TextView nickNameView;

    public GeoFenceItemView(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
    }

    public void bindTo(GeofenceData geofenceData) {
        nickNameView.setText(geofenceData.nickName);
    }
}
