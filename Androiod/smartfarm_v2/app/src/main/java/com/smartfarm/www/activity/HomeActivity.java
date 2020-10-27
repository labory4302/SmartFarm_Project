package com.smartfarm.www.activity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.smartfarm.www.R;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class HomeActivity extends Fragment {
    View view;
    private ListView listView;
    private ListViewAdapter listViewAdapter;

    TextView tb1[] = new TextView[7];
    TextView tb2[] = new TextView[7];

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.home_page,container,false);

        listViewAdapter = new ListViewAdapter();

        listView = (ListView) view.findViewById(R.id.listview);

        for(int i=0; i<=6; i++){
            int resId = getResources().getIdentifier("today"+i,"id", getContext().getPackageName());
            tb1[i] = (TextView) view.findViewById(resId);
        }
        for(int i=0; i<=6; i++){
            int resId = getResources().getIdentifier("weather"+i,"id", getContext().getPackageName());
            tb2[i] = (TextView) view.findViewById(resId);
        }



        listViewAdapter.addItem("테스트1","내용1");
        listViewAdapter.addItem("테스트2","내용2");
        listViewAdapter.addItem("테스트3","내용3");
        listViewAdapter.addItem("테스트4","내용4");
        listViewAdapter.addItem("테스트5","내용5");

        listView.setAdapter(listViewAdapter);


     //  new GetWeatherTask().execute();


//        listViewAdapter.notifyDataSetChanged();
        return view;
    }

    // 날씨 최고 온도, 최저온도, 강수량 가져오는 class
    // 날씨 최고 온도, 최저온도, 강수량 가져오는 class
    public class GetWeatherTask extends AsyncTask<Void, Void, Map<String,String>> {

        @Override
        protected Map<String, String> doInBackground(Void... params) {
            //파싱한 결과값을 담을 해쉬맵
            Map<String,String> result = new HashMap<String,String>();
            try {

                // 날씨 URL 가져오기
                Document document = Jsoup.connect("https://freemeteo.kr/weather/seoul/7-days/list/?gid=1835848&language=korean&country=south-korea").get();


                //오늘 포함해서 7일 날씨 가져오기
                Elements test_em = document.select(".table .today.sevendays .day .icon span");

                // 최고기온 최저기온 가져오기
                Elements temp_em = document.select(".table .today.sevendays .day > .temps");

                // 강수량 가져오기기
                Elements rain_em = document.select(".day .extra b");
                //

                // 일주일치 날씨 최고 온도 최저 온도
                String temp_7day = temp_em.text();

                String test_7 = test_em.toString();

                // 파싱한 문자열에서 온도 빼고 필요없는 부분 지우기
                temp_7day = temp_7day.replaceAll("최저: ","");
                temp_7day = temp_7day.replaceAll("최고: ","");

                String delete = "<span class=\"wicon w78x73 \" data-icon=\"";
                String delete2 = "\"></span>";
                test_7 = test_7.replaceAll(delete,"");
                test_7 = test_7.replaceAll(delete2,"");
                //
                String testDay[] = test_7.split("\n");

                // 띄어쓰기로 일주일치 온도를 분류
                String tempDay[]  = temp_7day.split(" ");

                // 띄어쓰기로 일주일치 평균강수량을 분류
                String rainfallDay[]  = rain_em.text().split(" ");


                // 온도 파싱한 내용 담기
                for(int i=0; i<tempDay.length; i++){
                    //Log.d("text : ", "content : "+tempDay[i]);
                    result.put("temp"+i, tempDay[i]);
                }
                // 강수량 파싱한 내용 담기
                for(int i=0; i<rainfallDay.length; i++){
                    //Log.d("text : ", "content : "+rainfallDay[i]);
                    result.put("rainfall"+i, rainfallDay[i]);
                }
                //
                for(int i=0; i<testDay.length; i++){
                    result.put("test"+i, testDay[i]);
                }


            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        }

        //UI 표시할 곳곳
        @Override
        protected void onPostExecute(Map<String, String> map) {
            // 온도/강수량  가져오기
            Double rainfall_day[] = new Double[7];
            for (int i = 0; i < 7; i++) {
                Log.i("text : ", i+"일차 날씨 : "+map.get("test"+(i*2)));
                String temp_temp = map.get("temp" + i);
                rainfall_day[i] = Double.parseDouble(map.get("rainfall" + i));

                String temp_temp_hl[] = temp_temp.split("°C");
                float avg_temp = (Float.parseFloat(temp_temp_hl[0])+Float.parseFloat(temp_temp_hl[1]))/2;
//                Log.d("찍히냐?",""+avg_temp+", "+map.get("test"+i));
                tb1[i].setText(""+avg_temp+"℃");

                if(Integer.parseInt(map.get("test"+(i*2))) <= 1){
                    tb2[i].setText("맑음");
                }else if(Integer.parseInt(map.get("test"+(i*2))) <= 2){
                    tb2[i].setText("구름 조금");
                }else if(Integer.parseInt(map.get("test"+(i*2))) <= 3){
                    tb2[i].setText("구름 많음");
                }else if(Integer.parseInt(map.get("test"+(i*2))) <= 4){
                    tb2[i].setText("흐림");
                }else if(Integer.parseInt(map.get("test"+(i*2))) <= 5){
                    tb2[i].setText("비 확률");
                }else if(Integer.parseInt(map.get("test"+(i*2))) <= 6){
                    tb2[i].setText("비 조금");
                }else if(Integer.parseInt(map.get("test"+(i*2))) <= 7){
                    tb2[i].setText("비옴");
                }else if(Integer.parseInt(map.get("test"+(i*2))) <= 8){
                    tb2[i].setText("폭우");
                }else if(Integer.parseInt(map.get("test"+(i*2))) <= 9) {
                    tb2[i].setText("모름");
                }else{
                    tb2[i].setText("오류");
                }
            }
        }
    }
}
