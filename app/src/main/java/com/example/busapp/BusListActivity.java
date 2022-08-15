package com.example.busapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.TextView;

import androidx.core.view.InputDeviceCompat;
import androidx.core.view.MotionEventCompat;
import androidx.core.view.ViewConfigurationCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.wear.widget.WearableLinearLayoutManager;
import androidx.wear.widget.WearableRecyclerView;

import com.example.busapp.databinding.ActivityBusListBinding;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.StringTokenizer;

public class BusListActivity extends Activity {

    private TextView mTextView;
    private ActivityBusListBinding binding;

    private String nodeId;
    private String nodeName;
    private  String data;
    private BusAdapter adapter;
    WearableRecyclerView BusrecyclerView;
    Handler handler = new Handler();
    private TextView textView3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        binding = ActivityBusListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        CustomScrollingLayoutCallback customScrollingLayoutCallback = new BusListActivity.CustomScrollingLayoutCallback();
        textView3 = (TextView)findViewById(R.id.textView3);
        BusrecyclerView = findViewById(R.id.BusRecyclerView);
        BusrecyclerView.setHasFixedSize(true);
        BusrecyclerView.setEdgeItemsCenteringEnabled(true);
        BusrecyclerView.setLayoutManager(new WearableLinearLayoutManager(this, customScrollingLayoutCallback));

        //rotary input
        BusrecyclerView.setOnGenericMotionListener(new View.OnGenericMotionListener() {
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

        adapter = new BusAdapter();

        Intent intent = getIntent();
        nodeId = intent.getStringExtra("nodeId");
        nodeName = intent.getStringExtra("nodeName");
        textView3.setText(nodeName);
        Log.d("ID",nodeId);
        Log.d("NAME",nodeName);
        IncheonBus incheonThread = new IncheonBus();
        incheonThread.start();
    }

    class IncheonBus extends Thread{
        public void run(){
            String subnodeId = nodeId.substring(3);
            Log.d("subnodeId",subnodeId);
            StringBuffer buffer = new StringBuffer();
            String queryUrl = "http://apis.data.go.kr/6280000/busArrivalService/getAllRouteBusArrivalList?serviceKey="
                    + "%2BaCrLa%2Fp1lfYP3wx954IxePqBKnfeZ8EC0pcOupGbRWhxUuOf5HW52ieQEZojO%2FEXE0ES1My6X68c50H4dWVLw%3D%3D"
                    + "&pageNo=1&numOfRows=10"
                    + "&bstopId=" + subnodeId;
            try {
                URL url = new URL(queryUrl);//문자열로 된 요청 url을 URL 객체로 생성.
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                //URLConnection urlConnection = url.openConnection();
                InputStream is = conn.getInputStream(); //url위치로 입력스트림 연결
                Log.d("시작","시작");
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

                            if (tag.equals("itemList"));// 첫번째 검색결과
                            else if (tag.equals("ARRIVALESTIMATETIME")) {
                                //buffer.append("nodeid :");
                                xpp.next();

                                Log.d("arrivalestimatetime",xpp.getText());
                                buffer.append(xpp.getText());//description 요소의 TEXT 읽어와서 문자열버퍼에 추가
                                buffer.append(",");//줄바꿈 문자 추가
                            } else if (tag.equals("REST_STOP_COUNT")) {
                                //buffer.append("nodenm :");
                                xpp.next();

                                Log.d("REST_STOP_COUNT",xpp.getText());
                                buffer.append(xpp.getText());//telephone 요소의 TEXT 읽어와서 문자열버퍼에 추가
                                buffer.append(",");//줄바꿈 문자 추가
                            }else if (tag.equals("ROUTEID")) {
                                //buffer.append("nodenm :");
                                xpp.next();

                                Log.d("ROUTEID",xpp.getText());
                                buffer.append(xpp.getText());//telephone 요소의 TEXT 읽어와서 문자열버퍼에 추가

                            }
                            break;

                        case XmlPullParser.TEXT:
                            break;

                        case XmlPullParser.END_TAG:
                            tag = xpp.getName(); //테그 이름 얻어오기
                            if (tag.equals("itemList")) buffer.append(",");// 첫번째 검색결과종료..줄바꿈
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
                String arrivalTime = st.nextToken();
                String restSTop = st.nextToken();
                String routeID = st.nextToken();
                adapter.addItem(new Bus(routeID,restSTop,arrivalTime,nodeName));
            }
            //return buffer.toString();//StringBuffer 문자열 객체 반환
            handler.post(new Runnable() {

                @Override
                public void run() {
                    BusrecyclerView.setAdapter(adapter);
                }
            });

        }
    }

    //Create a curved layout
    public class CustomScrollingLayoutCallback extends WearableLinearLayoutManager.LayoutCallback {
        /** How much should we scale the icon at most. */
        private static final float MAX_ICON_PROGRESS = 0.65f;

        private float progressToCenter;

        @Override
        public void onLayoutFinished(View child, RecyclerView parent) {

            // Figure out % progress from top to bottom
            float centerOffset = ((float) child.getHeight() / 2.0f) / (float) parent.getHeight();
            float yRelativeToCenterOffset = (child.getY() / parent.getHeight()) + centerOffset;

            // Normalize for center
            progressToCenter = Math.abs(0.5f - yRelativeToCenterOffset);
            // Adjust to the maximum scale
            progressToCenter = Math.min(progressToCenter, MAX_ICON_PROGRESS);

            child.setScaleX(1 - progressToCenter);
            child.setScaleY(1 - progressToCenter);
        }
    }

}