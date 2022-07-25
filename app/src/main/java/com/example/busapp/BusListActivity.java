package com.example.busapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.example.busapp.databinding.ActivityBusListBinding;

public class BusListActivity extends Activity {

    private TextView mTextView;
    private ActivityBusListBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityBusListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mTextView = binding.text;

        TextView textView = findViewById(R.id.text);

        Intent intent =getIntent();
        double latitude = intent.getDoubleExtra("latitude", 0);
        double longitude = intent.getDoubleExtra("longitude", 0);
        textView.setText("위도=" + latitude + ", 경도=" + longitude);
    }
}