package com.example.busapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class StopAdapter extends RecyclerView.Adapter<StopAdapter.ViewHolder>
                        implements OnStopItemClickListener{
    ArrayList<BusStop> items = new ArrayList<BusStop>();
    OnStopItemClickListener listener;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.stop_item, parent, false);
        return new ViewHolder(itemView, this);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BusStop item = items.get(position);
        holder.setItem(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
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




    public void setOnItemClickListener(OnStopItemClickListener listener){
        this.listener = listener;
    }

    @Override //OnStoptiemClickListener 인터페이스 오버라이드
    public void onItemClick(ViewHolder holder, View view, int position) {
        if(listener != null){
            listener.onItemClick(holder,view,position);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView textView;
        TextView textView2;

        public ViewHolder(View itemView, final OnStopItemClickListener listener){
            super(itemView);
            textView = itemView.findViewById(R.id.textView);
            textView2 = itemView.findViewById(R.id.textView2);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAbsoluteAdapterPosition();
                    if(listener != null){
                        listener.onItemClick(ViewHolder.this, view, position);
                    }
                }
            });
        }

        public void setItem(BusStop item){
            textView.setText(item.getNodeName());
            textView2.setText(item.getNodeNo());
        }

    }
}
