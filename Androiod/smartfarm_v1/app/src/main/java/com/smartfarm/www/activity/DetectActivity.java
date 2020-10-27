package com.smartfarm.www.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.smartfarm.www.R;
import com.smartfarm.www.service.LogoutService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DetectActivity extends AppCompatActivity {

    private ListView listView;
    private DetectListViewAdapter listViewAdapter;
    Button backButton;
    Map<String, String> resultMap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detect_page);

        //자동로그아웃 활성화
        startService(new Intent(this, LogoutService.class));

        listViewAdapter = new DetectListViewAdapter();

        listView = (ListView)findViewById(R.id.detect_listview);
        backButton = findViewById(R.id.back_button);

        listView.setAdapter(listViewAdapter);

        getLog();

        listViewAdapter.notifyDataSetChanged();

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }
    private void getLog(){
        SharedPreferences FireLog = getSharedPreferences("FireLog", Activity.MODE_PRIVATE);
        Map fireMap = FireLog.getAll(); // 저장된 값을 다가져오기

        //Map 은 HashMap이 구현 하는 인터페이스
        resultMap = new HashMap();



        // 탐지된 불 로그기록이 있으면 값을 담으라는 뜻
        if (fireMap.size() >=2){
            resultMap.putAll(fireMap);
        }

        SharedPreferences ObjectLog = getSharedPreferences("ObjectLog", Activity.MODE_PRIVATE);
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
            StringBuffer time_day= new StringBuffer(time[0]);
            time_time.insert(2,"시 ");
            time_day.insert(2,"월 ");
//            Log.d("결과","과연 : "+time);

            if (content[0].equals("fire")){
                listViewAdapter.addItem("/fire/image/" + image_title + ".png", ""+time_day.toString()+"일 "+time_time.toString()+"분","화재가 감지되었습니다.");
            }else{
                listViewAdapter.addItem("/object/detect/" + image_title + ".png", ""+time_day.toString()+"일 "+time_time.toString()+"분","물체가 감지되었습니다.");
            }


            count++;

            // 로그 보여줄 최대 갯수
            if(count==11){
                break;
            }
        }

        if (fireMap.size() <2 && ObjectMap.size() < 2){
            listViewAdapter.addItem(null , "현재 위험이 감지되지 않았습니다.","");
        }


        listView.setAdapter(listViewAdapter);

    }
}
