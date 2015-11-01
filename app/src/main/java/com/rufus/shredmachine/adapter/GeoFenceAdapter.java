package com.rufus.shredmachine.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rufus.shredmachine.R;
import com.rufus.shredmachine.model.GeofenceData;
import com.rufus.shredmachine.view.geofence.GeoFenceItemView;

import java.util.Collections;
import java.util.List;

public class GeoFenceAdapter extends RecyclerView.Adapter<GeoFenceAdapter.ViewHolder> {

    public interface GeoFenceClickListener {
        void onRepositoryClick(GeofenceData geofenceData);
    }

    private final GeoFenceClickListener geoFenceClickListener;

    private List<GeofenceData> geofenceDatas;

    public GeoFenceAdapter(List<GeofenceData> geofenceDatas, GeoFenceClickListener geoFenceClickListener) {
        this.geoFenceClickListener = geoFenceClickListener;
        this.geofenceDatas = geofenceDatas;
        setHasStableIds(true);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        GeoFenceItemView view = (GeoFenceItemView) LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.geo_fence_item_view, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        viewHolder.bindTo(geofenceDatas.get(i));
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return geofenceDatas.size();
    }

    public final class ViewHolder extends RecyclerView.ViewHolder {
        public final GeoFenceItemView itemView;

        public ViewHolder(GeoFenceItemView itemView) {
            super(itemView);
            this.itemView = itemView;
            this.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    GeofenceData geofence = geofenceDatas.get(getAdapterPosition());
                    geoFenceClickListener.onRepositoryClick(geofence);
                }
            });
        }


        public void bindTo(GeofenceData repository) {
            itemView.bindTo(repository);
        }
    }
}
