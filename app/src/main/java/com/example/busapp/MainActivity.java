package com.example.busapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.example.busapp.databinding.ActivityMainBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.Priority;
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
    private double wayLatitude = 0.0, wayLongitude = 0.0;
    private boolean isContinue = false;
    public static final int DEFAULT_LOCATION_REQUEST_PRIORITY = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;
    public static final long DEFAULT_LOCATION_REQUEST_INTERVAL = 20000L;      //20초 사이
    public static final long DEFAULT_LOCATION_REQUEST_FAST_INTERVAL = 10000L; //10초에서
    private TagoThread tagoThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        this.buslist = (TextView) findViewById(R.id.buslist);
        this.txtLocation = (TextView) findViewById(R.id.txtLocation);
        this.button = (Button) findViewById(R.id.button);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this); //위치 정보를 제공하는 클라이언트 객체

        getLocation();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                data = getTagoXmlData();
                buslist.setText(data);
                txtLocation.setText(String.format("%s : %s", wayLatitude, wayLongitude));
                Intent intent = new Intent(MainActivity.this,StopListActivity.class);
                intent.putExtra("latitude", wayLatitude);
                intent.putExtra("longitude", wayLongitude);
                startActivity(intent);
            }
        });
    }//onCreate

    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            super.onLocationResult(locationResult);
            wayLongitude = locationResult.getLastLocation().getLongitude();
            wayLatitude = locationResult.getLastLocation().getLatitude();
            mFusedLocationClient.removeLocationUpdates(locationCallback); //결과 전달 되면 리스너 삭제
        }
    };

    private void getLocation() {
        locationRequest = LocationRequest.create(); //location에 필요한 정보를 정의 하는 객체 생성
        locationRequest.setPriority(DEFAULT_LOCATION_REQUEST_PRIORITY);//위치 정밀도 설정
        locationRequest.setInterval(DEFAULT_LOCATION_REQUEST_INTERVAL); //위치 업데이트 시간 간격을 밀리초 단위로 설정
        locationRequest.setFastestInterval(DEFAULT_LOCATION_REQUEST_FAST_INTERVAL);//위치 업데이트를 
        //앱에 위치 권한이 있는 없는 경우
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    locationRequestCode);//권한 요청

        } else {    //앱에 위치 권한이 있는 경우
            if (isContinue) {
                mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null); //리퀘스트객체를 전달하고 그 결과를 전달 받을 콜백 함수
            } else {
                mFusedLocationClient.getLastLocation().addOnSuccessListener(MainActivity.this, location -> {
                    if (location != null) {
                        wayLatitude = location.getLatitude();
                        wayLongitude = location.getLongitude();
                    } else {
                        mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
                    }
                });
            }
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    //requestPermissions에서 요청한 권한의 결과를 받는다.
    //requestPermissions에서 requestCode를 전달해 연결한다.
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1000: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (isContinue) {
                        mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
                    } else {
                        mFusedLocationClient.getLastLocation().addOnSuccessListener(MainActivity.this, location -> {
                            if (location != null) {
                                wayLatitude = location.getLatitude();
                                wayLongitude = location.getLongitude();
                                txtLocation.setText(String.format("%s - %s", wayLatitude, wayLongitude));
                            } else {
                                mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
                            }
                        });
                    }
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    public String getTagoXmlData() {
//        getLocation();
        String lat_str = Double.toString(wayLatitude);
        String lon_str = Double.toString(wayLongitude);
        StringBuffer buffer = new StringBuffer();
        Context context = getApplicationContext();
        Toast.makeText(context,lat_str +" "+lon_str,Toast.LENGTH_LONG).show();
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


