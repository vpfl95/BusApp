package com.example.busapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class BusListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_list);
        TextView textView2 = findViewById(R.id.textView2);

        Intent intent = getIntent();
        double lat = intent.getDoubleExtra("lat",0);
        double lon = intent.getDoubleExtra("lon",0);
        textView2.setText("위도=" + lat + ", 경도=" + lon);
    }
}