package com.smartfarm.www.activity;

import android.os.AsyncTask;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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

            // 최고기온 최저기온 가져오기
            Elements temp_em = document.select(".table .today.sevendays .day > .temps");

            // 강수량 가져오기기
            Elements rain_em = document.select(".day .extra b");


            // 일주일치 날씨 최고 온도 최저 온도
            String temp_7day = temp_em.text();

            // 파싱한 문자열에서 온도 빼고 필요없는 부분 지우기
            temp_7day = temp_7day.replaceAll("최저: ","");
            temp_7day = temp_7day.replaceAll("최고: ","");

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




        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }


    //UI 표시할 곳곳
   @Override
    protected void onPostExecute(Map<String, String> map) {

        // 온도/강수량  가져오기
        for( int i = 0; i < 7; i++) {
            Log.d("text : ", i+"일차 온도 : "+map.get("temp"+i)+" 강수량 : "+map.get("rainfall"+i));
        }

    }
}


