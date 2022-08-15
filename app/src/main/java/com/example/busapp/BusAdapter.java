package com.example.busapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class BusAdapter extends RecyclerView.Adapter<BusAdapter.BusViewHolder>
                        implements OnBusItemClickListener{
    ArrayList<Bus> items = new ArrayList<>();
    OnBusItemClickListener listener;

    @NonNull
    @Override
    public BusViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.bus_item, parent, false);
        return new BusAdapter.BusViewHolder(itemView, this);
    }

    @Override
    public void onBindViewHolder(@NonNull BusViewHolder holder, int position) {
        Bus item = items.get(position);
        holder.setItem(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void addItem(Bus item){
        items.add(item);
    }

    public void setItems(ArrayList<Bus> items){
        this.items = items;
    }

    public Bus getItem(int position){
        return items.get(position);
    }

    public void setItem(int position, Bus item){
        items.set(position,item);
    }


    public void setOnItemClickListener(OnBusItemClickListener listener){
        this.listener = listener;
    }

    @Override
    public void onItemClick(BusViewHolder holder, View view, int position) {
        if(listener != null){
            listener.onItemClick(holder,view,position);
        }
    }

    public static class BusViewHolder extends RecyclerView.ViewHolder{
        TextView txtROUTEID;
        TextView txtREST_STOP_COUNT;
        TextView txtARRIVALTIME;
        //TextView textView3;

        public BusViewHolder(View itemView, final OnBusItemClickListener listener){
            super(itemView);
            txtROUTEID = itemView.findViewById(R.id.txtROUTEID);
            txtREST_STOP_COUNT = itemView.findViewById(R.id.txtREST_STOP_COUNT);
            txtARRIVALTIME = itemView.findViewById(R.id.txtARRIVALTIME);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAbsoluteAdapterPosition();
                    if(listener != null){
                        listener.onItemClick(BusViewHolder.this, view, position);
                    }
                }
            });
        }

        public void setItem(Bus item){
            txtROUTEID.setText(item.getROUTEID());
            txtREST_STOP_COUNT.setText(item.getREST_STOP_COUNT());
            txtARRIVALTIME.setText(item.getARRIVALESTIMATETIME());
            //textView3.setText(item.getBSTOPID());
        }

    }

}
