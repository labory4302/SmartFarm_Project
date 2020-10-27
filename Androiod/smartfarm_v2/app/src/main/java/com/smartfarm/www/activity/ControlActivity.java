package com.smartfarm.www.activity;

/*안드로이드 제어코드
1001:워터펌프 활성화  | 1000:워터펌프 비활성화
2001:환풍기 활성화    | 2000:환풍기 비활성화
3001:LED 활성화       | 3000:LED 비활성화
4***:자동모드 토양수분량 설정
5***:자동모드 습도 조절
9001:자동모드 ON      | 9000:자동모드 OFF
*/

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.smartfarm.www.R;
import com.smartfarm.www.service.ForegroundService;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import com.smartfarm.www.service.ForegroundService;

public class ControlActivity extends Fragment {
    private LinearLayout autoLayout, manualLayout;
    private EditText show_temp_change, show_humidity_change, show_soil_change;
    private Button temp_up, temp_down, humidity_up, humidity_down, soil_up, soil_down, auto_change_apply;
    private Switch changeMode, manuel_pump_status, manual_fan_status, manual_LED_status;
    private WebView cctvView;           //웹뷰객체
    private WebSettings webSettings;    //웹뷰세팅

    private String cctvUrl = "http://192.168.0.19:8081/video.mjpg";    //웹뷰의 주소

    //자동모드 임시 온습도,토양수분 디폴트값
    private int temp = 20;
    private int humidity = 30;
    private int soil = 20;

    Socket socket;              //소켓 객체 생성
    ConnectRaspi connectRaspi;  //소켓통신을 위한 스레드객체

    Button fire_button; // 불 딥러닝 테스트용 버튼

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.control_page,container,false);

        fire_button = (Button) view.findViewById(R.id.test1);

        fire_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startService();
            }
        });

        //웹뷰 세팅 및 웹뷰 시작 부분
        cctvView = view.findViewById(R.id.webView);         //웹뷰 인스턴스
        cctvView.setWebViewClient(new WebViewClient());     //클릭시 새창 안뜨게 하기
        webSettings = cctvView.getSettings();               //세부 세팅 등록
        webSettings.setJavaScriptEnabled(true);             //웹페이지 자바스클비트 허용 여부
        webSettings.setSupportMultipleWindows(false);       //새창 띄우기 허용 여부
        webSettings.setJavaScriptCanOpenWindowsAutomatically(false);    //자바스크립트 새창 띄우기(멀티뷰) 허용 여부
        webSettings.setLoadWithOverviewMode(true);          //메타태그 허용 여부
        webSettings.setUseWideViewPort(true);               //화면 사이즈 맞추기 허용 여부
        webSettings.setSupportZoom(true);                   //화면 줌 허용 여부
        webSettings.setBuiltInZoomControls(true);           //화면 확대 축소 허용 여부
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);  //컨텐츠 사이즈 맞추기
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);//브라우저 캐시 허용 여부
        webSettings.setDomStorageEnabled(true);             //로컬저장소 허용 여부
        cctvView.loadUrl(cctvUrl);  //웹뷰에 표시할 웹사이트 주소, 웹뷰 시작

        changeMode = view.findViewById(R.id.changeMode);                    //모드 변경 스위치
        manuel_pump_status = view.findViewById(R.id.manuel_pump_status);    //수동모드에서의 펌프 상태선택
        manual_fan_status = view.findViewById(R.id.manuel_fan_status);      //수동모드에서의 환풍기 상태선택
        manual_LED_status = view.findViewById(R.id.manuel_LED_status);      //수동모드에서의 조명 상태선택

        autoLayout = view.findViewById(R.id.auto_layout);       //자동모드 화면
        manualLayout = view.findViewById(R.id.manual_layout);   //수동모드 화면

        temp_up = view.findViewById(R.id.temp_up);              //자동모드 온도값 상승
        temp_down = view.findViewById(R.id.temp_down);          //자동모드 온도값 하강
        humidity_up = view.findViewById(R.id.humidity_up);      //자동모드 습도값 상승
        humidity_down = view.findViewById(R.id.humidity_down);  //자동모드 습도값 하강
        soil_up = view.findViewById(R.id.soil_up);              //자동모드 토양수분값 상승
        soil_down = view.findViewById(R.id.soil_down);          //자동모드 토양수분값 하강
        auto_change_apply = view.findViewById(R.id.auto_change_apply);  //자동모드 설정값 전송

        show_temp_change = view.findViewById(R.id.show_temp_change);        //자동모드 온도값 표시
        show_humidity_change = view.findViewById(R.id.show_humidity_change);//자동모드 습도값 표시
        show_soil_change = view.findViewById(R.id.show_soil_change);        //자동모드 토양수분값 표시

        //자동, 수동모드 선택
        changeMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    setAutoMode();
                } else {
                    setMenualMode();
                }
            }
        });

        //수동모드에서의 펌프 상태 제어
        manuel_pump_status.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    turnOnPump();
                } else {
                    turnOffPump();
                }
            }
        });

        //수동모드에서의 환풍기 상태 제어
        manual_fan_status.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    turnOnFan();
                } else {
                    turnOffFan();
                }
            }
        });

        //수동모드에서의 조명 상태 제어
        manual_LED_status.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    turnOnLED();
                } else {
                    turnOffLED();
                }
            }
        });


        //온도 상승 하락 버튼 리스너
        temp_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                temp++;
                show_temp_change.setText(String.valueOf(temp));
            }
        });
        temp_down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(temp <=0) {
                } else{
                    temp--;
                    show_temp_change.setText(String.valueOf(temp));
                }
            }
        });
        humidity_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                humidity++;
                show_humidity_change.setText(String.valueOf(humidity));
            }
        });
        humidity_down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(humidity <= 0){
                } else{
                    humidity--;
                    show_humidity_change.setText(String.valueOf(humidity));
                }
            }
        });
        soil_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                soil++;
                show_soil_change.setText(String.valueOf(soil));
            }
        });
        soil_down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(soil <= 0){
                }else{
                    soil--;
                    show_soil_change.setText(String.valueOf(soil));
                }
            }
        });



        //소켓으로 쏘세요 쏘는부분임
        auto_change_apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String temp_str = show_temp_change.getText().toString(); //온도 값 가져오기
                String humidity_str = show_humidity_change.getText().toString();    //습도 값 가져오기
                String soil_str = show_soil_change.getText().toString();            //수분 값 가져오기

                connectRaspi = new ConnectRaspi("40" + soil_str);       //소켓통신 송신(기대 토양수분량)
                connectRaspi.start();

                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                connectRaspi = new ConnectRaspi("50" + humidity_str);   //소켓통신 송신(기대 습도)
                connectRaspi.start();
            }
        });

        return view;
    }


    //웹뷰의 화면을 캡쳐하여 저장
    public void screenshotSharing() {
        //스크린샷 찍어 Bitmap 객체로 변환
        cctvView.setDrawingCacheEnabled(true);

        Bitmap screenshot = Bitmap. createBitmap(cctvView.getWidth(), cctvView.getHeight(), Bitmap.Config.ARGB_8888);   //캡쳐화면을 저장할 비트맵을 생성
        Canvas c = new Canvas(screenshot);      //캔버스를 생성
        cctvView.draw(c);                       //웹뷰의 화면을 캔버스에 그림

        //Bitmap객체를 파일로 저장
        String filename = "screenShot.png";
        Uri imageUri = null;

        try {
            File f = new File(getActivity().getExternalCacheDir(), filename);

            f.createNewFile();
            imageUri = Uri. fromFile(f);
            OutputStream outStream = new FileOutputStream(f);

            screenshot.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            outStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        cctvView.setDrawingCacheEnabled(false);
    }

//    //웹뷰에서 찍은 사진을 불러오기
//    private void uploadImage() {
//        String path = getActivity().getExternalCacheDir() + "/screenShot.png";
//        Bitmap bitmap = BitmapFactory.decodeFile(path);
//        Drawable drawable = (Drawable)(new BitmapDrawable(bitmap));
//        mImageView.setImageDrawable(drawable);
//    }

//    public void onStop() {
//        super.onStop();
//        try {
//            socket.close();     //종료시 소켓도 닫아주어야한다.
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    private void setAutoMode() {
        changeMode.setText("자동모드");
        autoLayout.setVisibility(View.VISIBLE);
        manualLayout.setVisibility(View.GONE);
        connectRaspi = new ConnectRaspi("9001");
        connectRaspi.start();   //스레드 시작
    }

    private void setMenualMode() {
        changeMode.setText("수동모드");
        manualLayout.setVisibility(View.VISIBLE);
        autoLayout.setVisibility(View.GONE);
        connectRaspi = new ConnectRaspi("9000");
        connectRaspi.start();
    }

    private void turnOnPump() {
        manuel_pump_status.setText("켜짐");
        connectRaspi = new ConnectRaspi("1001");    //소켓통신 송신
        connectRaspi.start();
    }

    private void turnOffPump() {
        manuel_pump_status.setText("꺼짐");
        connectRaspi = new ConnectRaspi("1000");    //소켓통신 송신
        connectRaspi.start();
    }

    private void turnOnFan() {
        manual_fan_status.setText("켜짐");
        connectRaspi = new ConnectRaspi("2001");    //소켓통신 송신
        connectRaspi.start();
    }

    private void turnOffFan() {
        manual_fan_status.setText("꺼짐");
        connectRaspi = new ConnectRaspi("2000");    //소켓통신 송신
        connectRaspi.start();
    }

    private void turnOnLED() {
        manual_LED_status.setText("켜짐");
        connectRaspi = new ConnectRaspi("3001");    //소켓통신 송신
        connectRaspi.start();
    }

    private void turnOffLED() {
        manual_LED_status.setText("꺼짐");
        connectRaspi = new ConnectRaspi("3000");    //소켓통신 송신
        connectRaspi.start();
    }

    class ConnectRaspi extends Thread {     //소켓통신을 위한 스레드
        private String ip = "192.168.0.7";  // 서버의 IP 주소
        private int port = 9999;            // PORT번호를 꼭 라즈베리파이와 맞추어 주어야한다.
        private String sendMessage;         //송신할 데이터

        ConnectRaspi(String sendMessage) {
            this.sendMessage = sendMessage; //쓰레드를 생성할 때 코드를 입력받음
        }

        public void run() {
            try {   //소켓 생성
                InetAddress serverAddr = InetAddress.getByName(ip); //IP주소를 가져온다.
                socket = new Socket(serverAddr, port);              //소켓에 IP와 포트번호 할당

                //데이터 전송
                PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);
                out.println(sendMessage);
                socket.close();
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    // foregroundSercie 시작 함수
    public void startService() {
        Intent serviceIntent = new Intent(getContext(), ForegroundService.class);
        ContextCompat.startForegroundService(getContext(), serviceIntent);
    }
}

