package com.example.busapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
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
    private LocationCallback locationCallback;
    private int locationRequestCode = 1000;
    private double wayLatitude = 0.0, wayLongitude = 0.0;
    private StringBuilder stringBuilder;
    private boolean isContinue = false;

    private TagoThread tagoThread;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        this.buslist = (TextView) findViewById(R.id.buslist);
        this.txtLocation = (TextView) findViewById(R.id.txtLocation);
        this.button = (Button) findViewById(R.id.button);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(20 * 1000);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        wayLatitude = location.getLatitude();
                        wayLongitude = location.getLongitude();
                        if (!isContinue) {
                            txtLocation.setText(String.format(Locale.US, "%s - %s", wayLatitude, wayLongitude));
                        } else {
                            stringBuilder.append(wayLatitude);
                            stringBuilder.append("-");
                            stringBuilder.append(wayLongitude);
                            stringBuilder.append("\n\n");
                            txtLocation.setText(stringBuilder.toString());
                        }
                        if (!isContinue && mFusedLocationClient != null) {
                            mFusedLocationClient.removeLocationUpdates(locationCallback);
                        }
                    }
                }
            }
        };

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
                tagoThread = new TagoThread();
                tagoThread.start();
                getLocation();
                data = tagoThread.getTagoXmlData(wayLatitude,wayLongitude);
                buslist.setText(data);
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

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    locationRequestCode);

        } else {
            if (isContinue) {
                mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
            } else {
                mFusedLocationClient.getLastLocation().addOnSuccessListener(MainActivity.this, location -> {
                    if (location != null) {
                        wayLatitude = location.getLatitude();
                        wayLongitude = location.getLongitude();
                        txtLocation.setText(String.format(Locale.US, "%s - %s", wayLatitude, wayLongitude));
                    } else {
                        mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
                    }
                });
            }
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
                    if (isContinue) {
                        mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
                    } else {
                        mFusedLocationClient.getLastLocation().addOnSuccessListener(MainActivity.this, location -> {
                            if (location != null) {
                                wayLatitude = location.getLatitude();
                                wayLongitude = location.getLongitude();
                                txtLocation.setText(String.format(Locale.US, "%s - %s", wayLatitude, wayLongitude));
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

//    public String getTagoXmlData(double lat, double lon){
//        String lat_str = Double.toString(lat);
//        String lon_str = Double.toString(lon);
//        StringBuffer buffer=new StringBuffer();
//        String str= edit.getText().toString();//EditText에 작성된 Text얻어오기
//
//        String queryUrl="http://apis.data.go.kr/1613000/BusSttnInfoInqireService/getCrdntPrxmtSttnList?serviceKey="
//                +"%2BaCrLa%2Fp1lfYP3wx954IxePqBKnfeZ8EC0pcOupGbRWhxUuOf5HW52ieQEZojO%2FEXE0ES1My6X68c50H4dWVLw%3D%3D"
//                + "&numOfRows=10&pageNo=1&_type=xml"
//                +"&gpsLati="+lat_str+"&gpsLong="+lon_str;
//
//        try {
//            URL url= new URL(queryUrl);//문자열로 된 요청 url을 URL 객체로 생성.
//            InputStream is= url.openStream(); //url위치로 입력스트림 연결
//
//            XmlPullParserFactory factory= XmlPullParserFactory.newInstance();
//            XmlPullParser xpp= factory.newPullParser();
//            xpp.setInput( new InputStreamReader(is, "UTF-8") ); //inputstream 으로부터 xml 입력받기
//
//            String tag;
//
//            xpp.next();
//            int eventType= xpp.getEventType();
//
//            while( eventType != XmlPullParser.END_DOCUMENT ){
//                switch( eventType ){
//                    case XmlPullParser.START_DOCUMENT:
//                        buffer.append("파싱 시작...\n\n");
//                        break;
//
//                    case XmlPullParser.START_TAG:
//                        tag= xpp.getName();//테그 이름 얻어오기
//
//                        if(tag.equals("item")) ;// 첫번째 검색결과
//                        else if(tag.equals("gpslati")){
//                            buffer.append("lat : ");
//                            xpp.next();
//                            buffer.append(xpp.getText());//title 요소의 TEXT 읽어와서 문자열버퍼에 추가
//                            buffer.append("\n"); //줄바꿈 문자 추가
//                        }
//                        else if(tag.equals("gpslong")){
//                            buffer.append("long : ");
//                            xpp.next();
//                            buffer.append(xpp.getText());//category 요소의 TEXT 읽어와서 문자열버퍼에 추가
//                            buffer.append("\n");//줄바꿈 문자 추가
//                        }
//                        else if(tag.equals("nodeid")){
//                            buffer.append("nodeid :");
//                            xpp.next();
//                            buffer.append(xpp.getText());//description 요소의 TEXT 읽어와서 문자열버퍼에 추가
//                            buffer.append("\n");//줄바꿈 문자 추가
//                        }
//                        else if(tag.equals("nodenm")){
//                            buffer.append("nodenm :");
//                            xpp.next();
//                            buffer.append(xpp.getText());//telephone 요소의 TEXT 읽어와서 문자열버퍼에 추가
//                            buffer.append("\n");//줄바꿈 문자 추가
//                        }
//                        else if(tag.equals("nodeno")){
//                            buffer.append("nodeno :");
//                            xpp.next();
//                            buffer.append(xpp.getText());//address 요소의 TEXT 읽어와서 문자열버퍼에 추가
//                            buffer.append("\n");//줄바꿈 문자 추가
//                        }
//                        break;
//
//                    case XmlPullParser.TEXT:
//                        break;
//
//                    case XmlPullParser.END_TAG:
//                        tag= xpp.getName(); //테그 이름 얻어오기
//
//                        if(tag.equals("item")) buffer.append("\n");// 첫번째 검색결과종료..줄바꿈
//                        break;
//                }
//                eventType= xpp.next();
//            }
//        } catch (Exception e) {
//            // TODO Auto-generated catch blocke.printStackTrace();
//        }
//        buffer.append("파싱 끝\n");
//        return buffer.toString();//StringBuffer 문자열 객체 반환
//    }//getXmlData method....

}