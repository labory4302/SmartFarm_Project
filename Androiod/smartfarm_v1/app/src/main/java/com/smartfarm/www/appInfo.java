package com.smartfarm.www;

import android.app.Activity;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaFunctionException;
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaInvokerFactory;
import com.amazonaws.regions.Regions;
import com.smartfarm.www.service.LambdaFuncInterface;
import com.smartfarm.www.service.RequestClass;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class appInfo extends Application {

    public static final String SMARTFARM_FIRE_CHANNEL_ID = "69981";
    public static final String SMARTFARM_OBJECT_CHANNEL_ID = "77777";
    public static Map<String,String> weatherMap = null;
    public static String cabbage[] = null;
    public static String rice[] = null;
    public static String bean[] = null;
    public static String redPepper[] = null;
    public static String strawberry[] = null;
    public static String today[] = null;
    public static String day[] = null;
    public static String S3userID = null; // AWS S3 유저 폴더 저장 경로
    NotificationChannel channel; // 푸쉬 알림 채널 객체

    @Override
    public void onCreate() {
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat timeFormat = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat timeFormat1 = new SimpleDateFormat("yyyy-MM-dd");
        String currentTime = timeFormat.format(date);
        String currentTime1 = timeFormat1.format(date);

        today = currentTime1.split("-");

        SharedPreferences appInfoPref = getSharedPreferences("appInfoPref", Activity.MODE_PRIVATE);
        String appInfoPrefTime = appInfoPref.getString("date", null);


        day=new String[7]; // 일주일 날짜 가져올 변수
        cabbage=new String[7];
        rice=new String[7];
        bean=new String[7];
        redPepper=new String[7];
        strawberry=new String[7];

        // 오늘 접속 기록이 있을떄 (날짜, 날씨, 소도매)
        if(currentTime.equals(appInfoPrefTime) == true) {
            new uplaodSaveAppInfo().execute();
        } else {
            // 오늘 처음 접속할떄
            new GetWeatherTask().execute();
        }

        // fire 채널 만들기
        CharSequence channelName  = "smartfarm fire channel";
        String description = "fire detection";
        int importance = NotificationManager.IMPORTANCE_HIGH; // 긴급: 알림음이 울리며 헤드업 알림으로 표시됩니다.

        NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        Uri soundUri= Uri.parse("android.resource://"+getPackageName()+"/"+R.raw.fire); //내가 가져온 음악파일을 넣어준다.

        // 음악을 넣기 위한 설정
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                .build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel(SMARTFARM_FIRE_CHANNEL_ID, channelName, importance);
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            channel.setSound(soundUri, audioAttributes);

            // 노티피케이션 채널을 시스템에 등록
            assert notificationManager != null;

            notificationManager.createNotificationChannel(channel);
        }

        // object 채널 만들기
        channelName  = "smartfarm object channel";
        description = "object detection";

        soundUri= Uri.parse("android.resource://"+getPackageName()+"/"+R.raw.object); //내가 가져온 음악파일을 넣어준다.

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel(SMARTFARM_OBJECT_CHANNEL_ID, channelName, importance);
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            channel.setSound(soundUri, audioAttributes);

            // 노티피케이션 채널을 시스템에 등록
            assert notificationManager != null;
            notificationManager.createNotificationChannel(channel);

        }

        super.onCreate();
    }

    // 날씨 최고 온도, 최저온도, 강수량 가져오는 class
    public class GetWeatherTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            //파싱한 결과값을 담을 해쉬맵
            Map<String,String> result = new HashMap<String,String>();
            try {
                // 날씨 URL 가져오기
                Document document = Jsoup.connect("https://freemeteo.kr/weather/seoul/7-days/list/?gid=1835848&language=korean&country=south-korea").get();

                //7일 날짜 가져오기
                Elements date_em = document.select(".table .today.sevendays .day > .title span");

                //7일 요일 가져오기
                Elements day_em = document.select(".table .today.sevendays .day .title b");

                //오늘 포함해서 7일 날씨 가져오기
                Elements test_em = document.select(".table .today.sevendays .day .icon span");

                // 최고기온 최저기온 가져오기
                Elements temp_em = document.select(".table .today.sevendays .day > .temps");

                // 강수량 가져오기기
                Elements rain_em = document.select(".day .extra b");

                //일주일간 날짜
                String date_7day = date_em.text();

                // 일주일치 날씨 최고 온도 최저 온도
                String temp_7day = temp_em.text();

                //일주일 날씨
                String test_7 = test_em.toString();

                //일주일 요일
                String day_7day = day_em.text();
                Log.d("aaaaaaa"," : "+day_7day);

                // 파싱한 문자열에서 온도 빼고 필요없는 부분 지우기
                temp_7day = temp_7day.replaceAll("최저: ","");
                temp_7day = temp_7day.replaceAll("최고: ","");

                String delete = "<span class=\"wicon w78x73 \" data-icon=\"";
                String delete2 = "\"></span>";
                test_7 = test_7.replaceAll(delete,"");
                test_7 = test_7.replaceAll(delete2,"");

                //날짜
                String dateDay[] = date_7day.split(" ");

                // 날씨 아이콘
                String testDay[] = test_7.split("\n");

                // 띄어쓰기로 일주일치 온도를 분류
                String tempDay[] = temp_7day.split(" ");

                // 띄어쓰기로 일주일치 평균강수량을 분류
                String rainfallDay[] = rain_em.text().split(" ");


                for(int i=0; i<day.length; i++){
                    day[i] = dateDay[i*2];
                }

                SharedPreferences appInfoPref = getSharedPreferences("appInfoPref", Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = appInfoPref.edit();
                // 온도 파싱한 내용 담기
                for(int i=0; i<tempDay.length; i++){
                    //Log.d("text : ", "content : "+tempDay[i]);
                    result.put("temp"+i, tempDay[i]);
                    editor.putString("temp"+i, tempDay[i]);
                }
                // 강수량 파싱한 내용 담기
                for(int i=0; i<rainfallDay.length; i++){
                    //Log.d("text : ", "content : "+rainfallDay[i]);
                    result.put("rainfall"+i, rainfallDay[i]);
                    editor.putString("rainfall"+i, rainfallDay[i]);
                }
                //
                for(int i=0; i<testDay.length; i++){
                    result.put("test"+i, testDay[i]);
                    editor.putString("test"+i, testDay[i]);
                }
                for(int i=0; i<day.length; i++){
                    editor.putString("date"+i, day[i]);
                }
                editor.apply();

                //일주일치 작물 파싱
                awsLambdaConnect(temp_7day.replaceAll(" ","").replaceAll("°C","%")+"/"+rain_em.text());

            } catch (IOException e) {
                e.printStackTrace();
            }
            weatherMap = result;
            return null;
        }
    }

    public void awsLambdaConnect(String weather){

//      Lambda 프록시를 인스턴스화하는 데 사용할 LambdaInvokerFactory를 생성합니다.

        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                "ap-northeast-2:6d58538f-d438-48f3-bd0e-dc7455119ea3", // 자격 증명 풀 ID
                Regions.AP_NORTHEAST_2  // 물리적인 저장 위치 서울
        );

        ClientConfiguration client = new ClientConfiguration ();
        client.setConnectionTimeout(60*1000);
        client.setSocketTimeout(60*1000);
        LambdaInvokerFactory factory = new LambdaInvokerFactory(this.getApplicationContext(),
                Regions.AP_NORTHEAST_2 , credentialsProvider, client);



// 기본 Json 데이터 바인더를 사용하여 Lambda 프록시 객체를 생성
// 구현하여 자체 데이터 바인더를 제공 할 수 있음
// LambdaDataBinder.
        final LambdaFuncInterface myInterface = factory.build(LambdaFuncInterface.class);

        final RequestClass request = new RequestClass("",weather);

// Lambda 함수 호출은 네트워크 호출을 발생시킵니다.
// 메인 스레드에서 호출되지 않았는지 확인합니다.
        new AsyncTask<RequestClass, Void, retailResponse>() {
            @Override
            protected retailResponse doInBackground(RequestClass... params) {
                // invoke "echo" method. In case it fails, it will throw a
                // LambdaFunctionException.
                try {
                    return myInterface.retailPrediction(params[0]);


                } catch (LambdaFunctionException lfe) {
                    Log.e("Tag", "Failed to invoke echo", lfe);
                    return null;
                }
            }


            @Override
            protected void onPostExecute(retailResponse result) {
                if (result == null) {
                    return;
                }

                Log.d("배추결과", ""+result.getCabbage_result());
                Log.d("쌀결과", ""+result.getRice_result());
                Log.d("콩결과", ""+result.getBean_result());
                Log.d("고추결과", ""+result.getRedPepper_result());
                Log.d("딸기결과", ""+result.getStrawberry_result());

                cabbage = result.getCabbage_result().split(",");
                rice = result.getRice_result().split(",");
                bean = result.getBean_result().split(",");
                redPepper = result.getRedPepper_result().split(",");
                strawberry = result.getStrawberry_result().split(",");

                long now = System.currentTimeMillis();
                Date date = new Date(now);
                SimpleDateFormat timeFormat = new SimpleDateFormat("yyyyMMdd");
                String time = timeFormat.format(date);

                SharedPreferences appInfoPref = getSharedPreferences("appInfoPref", Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = appInfoPref.edit();
                for (int i=0; i<7; i++){
                    editor.putString("cabbage"+i, cabbage[i]);
                    editor.putString("rice"+i, rice[i]);
                    editor.putString("bean"+i, bean[i]);
                    editor.putString("redPepper"+i, redPepper[i]);
                    editor.putString("strawberry"+i, strawberry[i]);
                }
                editor.putString("date", time);
                editor.apply();
            }
        }.execute(request);
    }

    public class uplaodSaveAppInfo extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... parms) {

            SharedPreferences appInfoPref = getSharedPreferences("appInfoPref", Activity.MODE_PRIVATE);

            Map<String,String> result = new HashMap<String,String>();
            for(int i=0; i<7; i++){
                result.put("temp"+i, appInfoPref.getString("temp"+i, null));
            }
            for(int i=0; i<7; i++){
                result.put("rainfall"+i, appInfoPref.getString("rainfall"+i, null));
            }
            for(int i=0; i<14; i++){
                result.put("test"+i, appInfoPref.getString("test"+i, null));
            }
            weatherMap = result;

            for(int i=0; i<7; i++){
                day[i] = appInfoPref.getString("date"+i, null);
            }

            for (int i=0; i<7; i++){
                cabbage[i] = appInfoPref.getString("cabbage"+i, null);
                rice[i] = appInfoPref.getString("rice"+i, null);
                bean[i] = appInfoPref.getString("bean"+i, null);
                redPepper[i] = appInfoPref.getString("redPepper"+i, null);
                strawberry[i] = appInfoPref.getString("strawberry"+i, null);
            }

            Log.d("dddddddddd", "디버그디버그"+strawberry);

            return null;
        }
    }


}