package com.example.busapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class StopAdapter extends RecyclerView.Adapter<StopAdapter.ViewHolder>{
    ArrayList<BusStop> items = new ArrayList<BusStop>();

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.stop_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BusStop item = items.get(position);
        holder.setItem(item);
    }

    public void addItem(BusStop item){
        items.add(item);
    }

    public void setItems(ArrayList<BusStop> items){
        this.items = items;
    }

    public BusStop getItem(int position){
        return items.get(position);
    }

    public void setItem(int position, BusStop item){
        items.set(position,item);
    }


    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView textView;
        TextView textView2;

        public ViewHolder(View itemView){
            super(itemView);
            textView = itemView.findViewById(R.id.textView);
            textView2 = itemView.findViewById(R.id.textView2);
        }

        public void setItem(BusStop item){
            textView.setText(item.getNodeName());
            textView2.setText(item.getNodeId());
        }

    }
}
