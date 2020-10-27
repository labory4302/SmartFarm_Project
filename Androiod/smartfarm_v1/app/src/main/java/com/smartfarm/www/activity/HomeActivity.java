package com.smartfarm.www.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.smartfarm.www.R;
import com.smartfarm.www.appInfo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class HomeActivity extends Fragment {
    View view;
    private ListView listView;
    private ListViewAdapter listViewAdapter;
    private TextView kind, unit;

    TextView tb1[] = new TextView[7];
    ImageView tb2[] = new ImageView[7];
    TextView day_tv[] = new TextView[7];
    TextView dayday_tv[] = new TextView[7];
    TextView price_day[] = new TextView[7];

    ImageButton strawberry,rice,beans,chili,cabbage;
    TextView showDate;

    ProgressDialog dialog= null; //dialog 데이터 로딩
    Message message = null; // 데이터 로딩 후 메인 UI 업데이트 메시지

    private final int FINISH = 999; // 핸들러 메시지 구분 ID

    Map<String, String> resultMap; // 로그를 정렬하기 위한 해쉬맵

    ImageView weather; //easter
    MediaPlayer easterEgg1;
    int count=0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.home_page,container,false);

        // easter
        weather = (ImageView) view.findViewById(R.id.weather0);
        weather.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(count>=4) {
                    easterEgg1 = MediaPlayer.create(getContext(), R.raw.easteregg1);
                    easterEgg1.start();
                }
                count++;
                return false;
            }
        });

        listViewAdapter = new ListViewAdapter();



        listView = (ListView) view.findViewById(R.id.listview);
        cabbage = view.findViewById(R.id.cabbage_bt);
        rice = view.findViewById(R.id.rice_bt);
        beans = view.findViewById(R.id.beans_bt);
        chili = view.findViewById(R.id.chili_bt);
        strawberry = view.findViewById(R.id.strawberry_bt);
        showDate = view.findViewById(R.id.show_date);
        unit = view.findViewById(R.id.unit);


        // 로그기록 가져오기
        getLog();

        showDate.setText(appInfo.today[1]+"월"+appInfo.today[2]+"일");

        kind = view.findViewById(R.id.kind);


        for (int i = 1; i<7; i++){
            int resId = getResources().getIdentifier("day"+i,"id",getContext().getPackageName());
            day_tv[i] = (TextView) view.findViewById(resId);
        }
        for(int i=0; i<7; i++){
            int resId = getResources().getIdentifier("dayday"+i,"id", getContext().getPackageName());
            dayday_tv[i] = (TextView) view.findViewById(resId);
        }

        for(int i=0; i<=6; i++){
            int resId = getResources().getIdentifier("today"+i,"id", getContext().getPackageName());
            tb1[i] = (TextView) view.findViewById(resId);
        }
        for(int i=0; i<=6; i++){
            int resId = getResources().getIdentifier("weather"+i,"id", getContext().getPackageName());
            tb2[i] = (ImageView) view.findViewById(resId);
        }
        for (int i = 0; i<7; i++){
            int resId = getResources().getIdentifier("price_day"+i,"id",getContext().getPackageName());
            price_day[i] = (TextView) view.findViewById(resId);
        }

        if (appInfo.strawberry==null){
            //로딩창 실행
            dialog = new ProgressDialog(getContext());
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setMessage("데이터 확인중");
            dialog.show();// 프로그레스바 시작`

            // 핸들러에 보내기 위한 메시지 생성 (중복 메시지 확인하고 덮어씌우거나 없으면 새로 메시지 객체 생성)
            message = mHandler.obtainMessage(); // 핸들러의 메시지 객체 획득
            message.what = FINISH;

            // 쓰레드는 View 자원들에 직접 접근이 불가
            // 쓰레드로부터 전달받은 메세지들을 '메세지 큐(Message Queue)' 를 이용해 순차적으로 관리
            // 쓰레드 / 메시지 큐 / handler 통신해서 메인 스레드가 일을 순차적으로 처리함
            // 데이터 다 들어왔는지 확인 쓰레드
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while(true){
                        if (appInfo.strawberry != null) {
                            // 데이터 로딩 롼료시 UI 변경 메시지 전송

                            //메세지 큐에 데이터보냄냄
                            mHandler.sendMessage(message);
                            break;
                        }
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    return;
                }
            }).start();

        }else{
            getWeather();

            kind.setText("배추");
            unit.setText("(1포기)");
            for(int i=0; i<7; i++){
                price_day[i].setText(appInfo.cabbage[i]+"원");
            }
            cabbage.setImageResource(R.drawable.cabbage);
            rice.setImageResource(R.drawable.rice_unclick);
            beans.setImageResource(R.drawable.beans_unclick);
            chili.setImageResource(R.drawable.chili_unclick);
            strawberry.setImageResource(R.drawable.strawberry_unclick);

            cabbage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    kind.setText("배추");
                    unit.setText("(1포기)");
                    cabbage.setImageResource(R.drawable.cabbage);
                    rice.setImageResource(R.drawable.rice_unclick);
                    beans.setImageResource(R.drawable.beans_unclick);
                    chili.setImageResource(R.drawable.chili_unclick);
                    strawberry.setImageResource(R.drawable.strawberry_unclick);
                    for(int i=0; i<7; i++){
                        price_day[i].setText(appInfo.cabbage[i]+"원");
                    }
                }
            });
            rice.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    kind.setText("쌀");
                    unit.setText("(20Kg)");
                    cabbage.setImageResource(R.drawable.cabbage_unclick);
                    rice.setImageResource(R.drawable.rice);
                    beans.setImageResource(R.drawable.beans_unclick);
                    chili.setImageResource(R.drawable.chili_unclick);
                    strawberry.setImageResource(R.drawable.strawberry_unclick);
                    for(int i=0; i<7; i++){
                        price_day[i].setText(appInfo.rice[i]+"원");
                    }
                }
            });
            beans.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    kind.setText("콩");
                    unit.setText("(500g)");
                    cabbage.setImageResource(R.drawable.cabbage_unclick);
                    rice.setImageResource(R.drawable.rice_unclick);
                    beans.setImageResource(R.drawable.beans);
                    chili.setImageResource(R.drawable.chili_unclick);
                    strawberry.setImageResource(R.drawable.strawberry_unclick);
                    for(int i=0; i<7; i++){
                        price_day[i].setText(appInfo.bean[i]+"원");
                    }
                }
            });
            chili.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    kind.setText("홍고추");
                    unit.setText("(100g)");
                    cabbage.setImageResource(R.drawable.cabbage_unclick);
                    rice.setImageResource(R.drawable.rice_unclick);
                    beans.setImageResource(R.drawable.beans_unclick);
                    chili.setImageResource(R.drawable.chili);
                    strawberry.setImageResource(R.drawable.strawberry_unclick);
                    for(int i=0; i<7; i++){
                        price_day[i].setText(appInfo.redPepper[i]+"원");
                    }
                }
            });
            strawberry.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    kind.setText("딸기");
                    unit.setText("(100g)");
                    cabbage.setImageResource(R.drawable.cabbage_unclick);
                    rice.setImageResource(R.drawable.rice_unclick);
                    beans.setImageResource(R.drawable.beans_unclick);
                    chili.setImageResource(R.drawable.chili_unclick);
                    strawberry.setImageResource(R.drawable.strawberry);
                    for(int i=0; i<7; i++){
                        price_day[i].setText(appInfo.strawberry[i]+"원");
                    }
                }
            });


        }




        return view;
    }

    // 데이터 전송 완료시 UI 변경을 위한 핸들러
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case FINISH :

                    getWeather();


                    for(int i=0; i<7; i++){
                        price_day[i].setText(appInfo.cabbage[i]+"원");
                    }
                    cabbage.setImageResource(R.drawable.cabbage);
                    rice.setImageResource(R.drawable.rice_unclick);
                    beans.setImageResource(R.drawable.beans_unclick);
                    chili.setImageResource(R.drawable.chili_unclick);
                    strawberry.setImageResource(R.drawable.strawberry_unclick);

                    cabbage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            kind.setText("배추");
                            unit.setText("(1포기)");
                            cabbage.setImageResource(R.drawable.cabbage);
                            rice.setImageResource(R.drawable.rice_unclick);
                            beans.setImageResource(R.drawable.beans_unclick);
                            chili.setImageResource(R.drawable.chili_unclick);
                            strawberry.setImageResource(R.drawable.strawberry_unclick);
                            for(int i=0; i<7; i++){
                                price_day[i].setText(appInfo.cabbage[i]+"원");
                            }
                        }
                    });
                    rice.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            kind.setText("쌀");
                            unit.setText("(20Kg)");
                            cabbage.setImageResource(R.drawable.cabbage_unclick);
                            rice.setImageResource(R.drawable.rice);
                            beans.setImageResource(R.drawable.beans_unclick);
                            chili.setImageResource(R.drawable.chili_unclick);
                            strawberry.setImageResource(R.drawable.strawberry_unclick);
                            for(int i=0; i<7; i++){
                                price_day[i].setText(appInfo.rice[i]+"원");
                            }
                        }
                    });
                    beans.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            kind.setText("콩");
                            unit.setText("(500g)");
                            cabbage.setImageResource(R.drawable.cabbage_unclick);
                            rice.setImageResource(R.drawable.rice_unclick);
                            beans.setImageResource(R.drawable.beans);
                            chili.setImageResource(R.drawable.chili_unclick);
                            strawberry.setImageResource(R.drawable.strawberry_unclick);
                            for(int i=0; i<7; i++){
                                price_day[i].setText(appInfo.bean[i]+"원");
                            }
                        }
                    });
                    chili.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            kind.setText("홍고추");
                            unit.setText("(100g)");
                            cabbage.setImageResource(R.drawable.cabbage_unclick);
                            rice.setImageResource(R.drawable.rice_unclick);
                            beans.setImageResource(R.drawable.beans_unclick);
                            chili.setImageResource(R.drawable.chili);
                            strawberry.setImageResource(R.drawable.strawberry_unclick);
                            for(int i=0; i<7; i++){
                                price_day[i].setText(appInfo.redPepper[i]+"원");
                            }
                        }
                    });
                    strawberry.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            kind.setText("딸기");
                            unit.setText("(100g)");
                            cabbage.setImageResource(R.drawable.cabbage_unclick);
                            rice.setImageResource(R.drawable.rice_unclick);
                            beans.setImageResource(R.drawable.beans_unclick);
                            chili.setImageResource(R.drawable.chili_unclick);
                            strawberry.setImageResource(R.drawable.strawberry);
                            for(int i=0; i<7; i++){
                                price_day[i].setText(appInfo.strawberry[i]+"원");
                            }
                        }
                    });


                    dialog.cancel();
                    break ;
                // TODO : add case.
            }
        }
    } ;

    // 탐지된 로그 가져오기
    private void getLog(){
        SharedPreferences FireLog = getContext().getSharedPreferences("FireLog", Activity.MODE_PRIVATE);
        Map fireMap = FireLog.getAll(); // 저장된 값을 다가져오기

        //Map 은 HashMap이 구현 하는 인터페이스
        resultMap = new HashMap();



        // 탐지된 불 로그기록이 있으면 값을 담으라는 뜻
        if (fireMap.size() >=2){
            resultMap.putAll(fireMap);
        }

        SharedPreferences ObjectLog = getContext().getSharedPreferences("ObjectLog", Activity.MODE_PRIVATE);
        Map ObjectMap = ObjectLog.getAll(); // 저장된 값을 다가져오기

        // 탐지된 객체 로그기록이 있으면 값을 담으라는 뜻
        if (ObjectMap.size() >=2){
            resultMap.putAll(ObjectMap);
        }


        Log.d("tag","사이즈 : " + resultMap.size());

        // 정렬하기 전에 방해되는 int이면서 필요없는 length 항목 지우기



        resultMap.remove("fireLog_length");
        resultMap.remove("objectLog_length");

        List<String> keySetList = new ArrayList<>(resultMap.keySet());

        // 내림차순 정렬하기 제일최근 5가지만 보여주기 위해
        Collections.sort(keySetList, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return resultMap.get(o2).compareTo(resultMap.get(o1));
            }
        });

        // 로그 갯수를 위한 변수
        int count=1;

        for(String key : keySetList) {
            String image_title = resultMap.get(key);
            String content[] = key.split("_");
            String time[] = image_title.split("-");
            StringBuffer time_time = new StringBuffer(time[1]);
            time_time.insert(2," : ");

//            Log.d("결과","과연 : "+time);

            if (content[0].equals("fire")){
                listViewAdapter.addItem(time_time.toString(), "화재가 감지되었습니다.");
            }else{
                listViewAdapter.addItem(time_time.toString(), "물체가 감지되었습니다.");
            }


            count++;

            // 로그 보여줄 최대 갯수
            if(count==11){
                break;
            }
        }

        if (fireMap.size() <2 && ObjectMap.size() < 2){
            listViewAdapter.addItem(null , "현재 위험이 감지되지 않았습니다.");
        }


        listView.setAdapter(listViewAdapter);

    }



    //날씨값 설정
    public void getWeather(){
        long now = System.currentTimeMillis();
        // 현재시간의 날짜 생성하기
        Date date = new Date(now);
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH");
        int time = Integer.valueOf(timeFormat.format(date));

        showDate.setText(appInfo.today[1]+"월"+appInfo.today[2]+"일");

        for (int i =1; i<7; i++) {
            day_tv[i].setText(appInfo.day[i] + "일");
        }
        for (int i=1; i<7; i++){
            dayday_tv[i].setText(appInfo.day[i] + "일");
        }

        Map<String,String> map = appInfo.weatherMap;
        // 온도/강수량  가져오기
        Double rainfall_day[] = new Double[7];
        for (int i = 0; i < 7; i++) {
            Log.i("text : ", i+"일차 날씨 : "+map.get("test"+(i*2)));
            String temp_temp = map.get("temp" + i);
            rainfall_day[i] = Double.parseDouble(map.get("rainfall" + i));

            String temp_temp_hl[] = temp_temp.split("°C");


            if(i==0 && (time>=18 && time<=24) || (time>=0 && time<=6 )) {
                float avg_temp = (Float.parseFloat(temp_temp_hl[0]));
                tb1[i].setText("" + avg_temp + "℃");
                tb2[i].setImageResource(R.drawable.sun_icon);//
                // 0일차 날씨 : 4" data-night="true
                continue;
            }else {
                float avg_temp = (Float.parseFloat(temp_temp_hl[0]) + Float.parseFloat(temp_temp_hl[1])) / 2;
                tb1[i].setText("" + avg_temp + "℃");
            }

            if(Integer.parseInt(map.get("test"+(i*2))) == 1){
                tb2[i].setImageResource(R.drawable.sun_icon);//                     tb2[i].setText("맑음");
            }else if(Integer.parseInt(map.get("test"+(i*2))) == 2){
                tb2[i].setImageResource(R.drawable.cloudy_icon);//                  tb2[i].setText("구름 조금");
            }else if(Integer.parseInt(map.get("test"+(i*2))) == 3){
                tb2[i].setImageResource(R.drawable.cloud_icon);//                   tb2[i].setText("구름 많음");
            }else if(Integer.parseInt(map.get("test"+(i*2))) == 4){
                tb2[i].setImageResource(R.drawable.cloud_icon);//                   tb2[i].setText("흐림");
            }else if(Integer.parseInt(map.get("test"+(i*2))) == 5){
                tb2[i].setImageResource(R.drawable.soft_rain_icon);//               tb2[i].setText("비 확률");
            }else if(Integer.parseInt(map.get("test"+(i*2))) == 6){
                tb2[i].setImageResource(R.drawable.rain_icon);//                    tb2[i].setText("비 조금");
            }else if(Integer.parseInt(map.get("test"+(i*2))) == 7){
                tb2[i].setImageResource(R.drawable.hard_rain_icon);//               tb2[i].setText("비옴");
            }else if(Integer.parseInt(map.get("test"+(i*2))) == 8){
                tb2[i].setImageResource(R.drawable.hard_rain_icon);//               tb2[i].setText("폭우");
            }else if(Integer.parseInt(map.get("test"+(i*2))) == 9) {
                tb2[i].setImageResource(R.drawable.sun_icon);//                     tb2[i].setText("모름");
            }else if(Integer.parseInt(map.get("test"+(i*2))) == 30) {
                tb2[i].setImageResource(R.drawable.soft_rain_icon);//               tb2[i].setText("비올듯");
            }else if(Integer.parseInt(map.get("test"+(i*2))) == 31) {
                tb2[i].setImageResource(R.drawable.rain_icon);//                    tb2[i].setText("약한 비");
            }else if(Integer.parseInt(map.get("test"+(i*2))) == 32) {
                tb2[i].setImageResource(R.drawable.hard_rain_icon);//               tb2[i].setText("비옴");
            }else if(Integer.parseInt(map.get("test"+(i*2))) == 33) {
                tb2[i].setImageResource(R.drawable.hard_rain_icon);//               tb2[i].setText("큰 소나기");
            }else if(Integer.parseInt(map.get("test"+(i*2))) == 34) {
                tb2[i].setImageResource(R.drawable.storm_rain_icon);//              tb2[i].setText("비오고 천둥도 침");
            }else{
                tb2[i].setImageResource(R.drawable.sun_icon);//                     tb2[i].setText("오류");
            }
        }
    }


}