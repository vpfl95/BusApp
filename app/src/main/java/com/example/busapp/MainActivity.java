package com.example.busapp;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;


import com.example.busapp.databinding.ActivityMainBinding;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Locale;

public class MainActivity extends Activity {

    private TextView mTextView;
    private ActivityMainBinding binding;

    private String data;
    private EditText edit;
    private TextView buslist;
    private TextView txtLocation;
    private Button button;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest locationRequest;
    private int locationRequestCode = 1000;
    private double wayLatitude, wayLongitude;
    private StringBuilder stringBuilder;
    private boolean isContinue = false;

    public static final int DEFAULT_LOCATION_REQUEST_PRIORITY = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;
    public static final long DEFAULT_LOCATION_REQUEST_INTERVAL = 20000L;
    public static final long DEFAULT_LOCATION_REQUEST_FAST_INTERVAL = 10000L;
    private TagoThread tagoThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        this.buslist = (TextView) findViewById(R.id.buslist);
        this.txtLocation = (TextView) findViewById(R.id.txtLocation);
        this.button = (Button) findViewById(R.id.button);



//        button.setOnClickListener(view -> {
//            isContinue = true;
//            stringBuilder = new StringBuilder();
//            getLocation();
//            data = getTagoXmlData(wayLatitude,wayLongitude);
//            buslist.setText(data);
//        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                tagoThread = new TagoThread();
//                tagoThread.start();
                checkLocationPermission();
//                data = getTagoXmlData(wayLatitude, wayLatitude);
//                buslist.setText(data);
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        data = getTagoXmlData(wayLatitude,wayLongitude);
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                //buslist.setText("test");
//                            }
//                        });
//                    }
//                }).start();
            }
        });


    }//onCreate


    private void checkLocationPermission(){
        //권한이 없는 경우
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    locationRequestCode);
        } else {    //이미 권한이 있는 경우
            checkLocationSetting();
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1000: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkLocationSetting();
                }
//                else {
//                    androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this);
//                    builder.setTitle("위치 권한이 꺼져있습니다.");
//                    builder.setMessage("[권한] 설정에서 위치 권한을 허용해야 합니다.");
//                    builder.setPositiveButton("설정으로 가기", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            Intent intent = new Intent();
//                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                            Uri uri = Uri.fromParts("package", getPackageName(), null);
//                            intent.setData(uri);
//                            startActivity(intent);
//                        }
//                    }).setNegativeButton("종료", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            finish();
//                        }
//                    });
//                    androidx.appcompat.app.AlertDialog alert = builder.create();
//                    alert.show();
//                }
                break;
            }
        }
    }

    private  void checkLocationSetting(){
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(DEFAULT_LOCATION_REQUEST_PRIORITY);
        locationRequest.setInterval(DEFAULT_LOCATION_REQUEST_INTERVAL);
        locationRequest.setFastestInterval(DEFAULT_LOCATION_REQUEST_FAST_INTERVAL);

        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest).setAlwaysShow(true);
        settingsClient.checkLocationSettings(builder.build())
                .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
                        mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                try {
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(MainActivity.this, 101);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.w(TAG, "unable to start resolution for result due to " + sie.getLocalizedMessage());
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "location settings are inadequate, and cannot be fixed here. Fix in Settings.";
                                Log.e(TAG, errorMessage);
                        }
                    }
                });
    }

    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            wayLatitude = locationResult.getLastLocation().getLatitude();
            wayLongitude = locationResult.getLastLocation().getLongitude();
            mFusedLocationClient.removeLocationUpdates(locationCallback);
            Intent intent = new Intent(MainActivity.this,BusListActivity.class);
            intent.putExtra("lat",wayLatitude);
            intent.putExtra("lon",wayLongitude);
            startActivity(intent);
            finish();

        }
    };


    public String getTagoXmlData(double lat, double lon) {
        String lat_str = String.valueOf(lat);
        String lon_str = String.valueOf(lon);
        StringBuffer buffer = new StringBuffer();
        Context context = getApplicationContext();
        Toast.makeText(context, lat_str + " " + lon_str, Toast.LENGTH_LONG).show();
        Thread t = new Thread(() -> {

            String queryUrl = "http://apis.data.go.kr/1613000/BusSttnInfoInqireService/getCrdntPrxmtSttnList?serviceKey="
                    + "%2BaCrLa%2Fp1lfYP3wx954IxePqBKnfeZ8EC0pcOupGbRWhxUuOf5HW52ieQEZojO%2FEXE0ES1My6X68c50H4dWVLw%3D%3D"
                    + "&numOfRows=10&pageNo=1&_type=xml"
                    + "&gpsLati=" + lat_str + "&gpsLong=" + lon_str;
            try {
                URL url = new URL(queryUrl);//문자열로 된 요청 url을 URL 객체로 생성.
                InputStream is = url.openStream(); //url위치로 입력스트림 연결

                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                XmlPullParser xpp = factory.newPullParser();
                xpp.setInput(new InputStreamReader(is, "UTF-8")); //inputstream 으로부터 xml 입력받기
                String tag;
                xpp.next();
                int eventType = xpp.getEventType();
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    switch (eventType) {
                        case XmlPullParser.START_DOCUMENT:
                            buffer.append("파싱 시작...\n\n");
                            break;

                        case XmlPullParser.START_TAG:
                            tag = xpp.getName();//테그 이름 얻어오기

                            if (tag.equals("item")) ;// 첫번째 검색결과
                            else if (tag.equals("gpslati")) {
                                buffer.append("lat : ");
                                xpp.next();
                                buffer.append(xpp.getText());//title 요소의 TEXT 읽어와서 문자열버퍼에 추가
                                buffer.append("\n"); //줄바꿈 문자 추가
                            } else if (tag.equals("gpslong")) {
                                buffer.append("long : ");
                                xpp.next();
                                buffer.append(xpp.getText());//category 요소의 TEXT 읽어와서 문자열버퍼에 추가
                                buffer.append("\n");//줄바꿈 문자 추가
                            } else if (tag.equals("nodeid")) {
                                buffer.append("nodeid :");
                                xpp.next();
                                buffer.append(xpp.getText());//description 요소의 TEXT 읽어와서 문자열버퍼에 추가
                                buffer.append("\n");//줄바꿈 문자 추가
                            } else if (tag.equals("nodenm")) {
                                buffer.append("nodenm :");
                                xpp.next();
                                buffer.append(xpp.getText());//telephone 요소의 TEXT 읽어와서 문자열버퍼에 추가
                                buffer.append("\n");//줄바꿈 문자 추가
                            } else if (tag.equals("nodeno")) {
                                buffer.append("nodeno :");
                                xpp.next();
                                buffer.append(xpp.getText());//address 요소의 TEXT 읽어와서 문자열버퍼에 추가
                                buffer.append("\n");//줄바꿈 문자 추가
                            }
                            break;

                        case XmlPullParser.TEXT:
                            break;

                        case XmlPullParser.END_TAG:
                            tag = xpp.getName(); //테그 이름 얻어오기

                            if (tag.equals("item")) buffer.append("\n");// 첫번째 검색결과종료..줄바꿈
                            break;
                    }
                    eventType = xpp.next();
                }
            } catch (Exception e) {
                // TODO Auto-generated catch blocke.printStackTrace();
            }
            buffer.append("파싱 끝\n");
        });
        try {
            t.start();
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return buffer.toString();//StringBuffer 문자열 객체 반환
    }
}



