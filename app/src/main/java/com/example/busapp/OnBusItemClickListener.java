package com.example.busapp;

import android.view.View;

public interface OnBusItemClickListener {
    public void onItemClick(BusAdapter.BusViewHolder holder, View view, int position);
}
