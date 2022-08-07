package com.example.busapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.view.InputDeviceCompat;
import androidx.core.view.MotionEventCompat;
import androidx.core.view.ViewConfigurationCompat;
import androidx.wear.widget.WearableLinearLayoutManager;
import androidx.wear.widget.WearableRecyclerView;

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
import java.util.StringTokenizer;

public class MainActivity extends Activity {

    private TextView mTextView;
    private ActivityMainBinding binding;

    private String data;
    private TextView txtLocation;
    private Button button;
    private StopAdapter adapter;
    WearableRecyclerView recyclerView;
    Handler handler = new Handler();

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest locationRequest;
    private int locationRequestCode = 1000;
    private double wayLatitude = 0.0, wayLongitude = 0.0;
    private boolean isContinue = false;
    public static final int DEFAULT_LOCATION_REQUEST_PRIORITY = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;
    public static final long DEFAULT_LOCATION_REQUEST_INTERVAL = 20000L;      //20초 사이
    public static final long DEFAULT_LOCATION_REQUEST_FAST_INTERVAL = 10000L; //10초에서

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        recyclerView = findViewById(R.id.wearableRecyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setEdgeItemsCenteringEnabled(true);
        recyclerView.setLayoutManager(new WearableLinearLayoutManager(this));
        adapter = new StopAdapter();

        recyclerView.setOnGenericMotionListener(new View.OnGenericMotionListener() {
            @Override
            public boolean onGenericMotion(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction()==MotionEvent.ACTION_SCROLL &&
                motionEvent.isFromSource(InputDeviceCompat.SOURCE_ROTARY_ENCODER)
                ){
                    float delta = -motionEvent.getAxisValue(MotionEventCompat.AXIS_SCROLL) *
                            ViewConfigurationCompat.getScaledVerticalScrollFactor(
                                    ViewConfiguration.get(view.getContext()),view.getContext()
                            );
                    view.scrollBy(0,Math.round(delta));
                    return true;
                }
                return false;
            }
        });

        this.txtLocation = (TextView) findViewById(R.id.txtLocation);
        this.button = (Button) findViewById(R.id.button);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this); //위치 정보를 제공하는 클라이언트 객체

        getLocation();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                txtLocation.setText(String.format("%s : %s", wayLatitude, wayLongitude));
                TagoThread tagoThread = new TagoThread();
                tagoThread.start();
//                Intent intent = new Intent(MainActivity.this,StopListActivity.class);
//                intent.putExtra("latitude", wayLatitude);
//                intent.putExtra("longitude", wayLongitude);
//                startActivity(intent);
            }
        });
    }//onCreate

    class TagoThread extends Thread{
        public void run(){
            String lat_str = Double.toString(wayLatitude);
            String lon_str = Double.toString(wayLongitude);
            StringBuffer buffer = new StringBuffer();

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
                            else if (tag.equals("nodeid")) {
                                //buffer.append("nodeid :");
                                xpp.next();

                                //Log.d("ID",xpp.getText());
                                buffer.append(xpp.getText());//description 요소의 TEXT 읽어와서 문자열버퍼에 추가
                                buffer.append(",");//줄바꿈 문자 추가
                            } else if (tag.equals("nodenm")) {
                                //buffer.append("nodenm :");
                                xpp.next();

                                //Log.d("NM",xpp.getText());
                                buffer.append(xpp.getText());//telephone 요소의 TEXT 읽어와서 문자열버퍼에 추가
                                //buffer.append("\n");//줄바꿈 문자 추가
                            }
                            break;

                        case XmlPullParser.TEXT:
                            break;

                        case XmlPullParser.END_TAG:
                            tag = xpp.getName(); //테그 이름 얻어오기
                            if (tag.equals("item")) buffer.append(",");// 첫번째 검색결과종료..줄바꿈
                            break;
                    }
                    eventType = xpp.next();
                }
            } catch (Exception e) {
                // TODO Auto-generated catch blocke.printStackTrace();
            }
            //buffer.append("파싱 끝\n");

            Log.d("data",buffer.toString());
            data = buffer.toString();
            StringTokenizer st = new StringTokenizer(data,",");
            while(st.hasMoreTokens()){
                String id = st.nextToken();
                String nm = st.nextToken();
                adapter.addItem(new BusStop(id,nm));
            }
            //return buffer.toString();//StringBuffer 문자열 객체 반환
            handler.post(new Runnable() {
                @Override
                public void run() {
                    recyclerView.setAdapter(adapter);
                }
            });

        }
    }


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

}


