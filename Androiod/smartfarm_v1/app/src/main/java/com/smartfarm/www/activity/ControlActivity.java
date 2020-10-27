package com.smartfarm.www.activity;

/*안드로이드 제어코드
1001:워터펌프 활성화  | 1000:워터펌프 비활성화
2001:환풍기 활성화    | 2000:환풍기 비활성화
3001:LED 활성화       | 3000:LED 비활성화
4***:자동모드 토양수분량 설정
5***:자동모드 습도 조절
9001:자동모드 ON      | 9000:자동모드 OFF
*/

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.smartfarm.www.R;
import com.smartfarm.www.data.EmbeddedData;
import com.smartfarm.www.data.EmbeddedResponse;
import com.smartfarm.www.data.UserInformation;
import com.smartfarm.www.network.RetrofitClient;
import com.smartfarm.www.network.ServiceApi;
import com.smartfarm.www.service.FireForegroundService;
import com.smartfarm.www.service.ObjectForegroundService;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ControlActivity extends Fragment {
    private ConstraintLayout autoLayout, manualLayout;
    private EditText show_temp_change, show_humidity_change, show_soil_change;
    private Button temp_up, temp_down, humidity_up, humidity_down, soil_up, soil_down, auto_change_apply;
    private Switch changeMode, manuel_pump_status, manual_fan_status, manual_LED_status, changeFireDetectionMode, changeObjectDetectionMode;
    private WebView cctvView;           //웹뷰객체
    private WebSettings webSettings;    //웹뷰세팅
    private ServiceApi service;

    private String cctvUrl = "http://192.168.0.39:8081/video.mjpg";    //웹뷰의 주소
    //사전에 세팅한 값
//    private int setTemp = 0;
    private int setHumidity = 0;
//    private int setSoil = 0;

    Socket socket;              //소켓 객체 생성
    ConnectRaspi connectRaspi;  //소켓통신을 위한 스레드객체

    UserInformation userInfo = UserInformation.getUserInformation();
    private int no;
    private int fireDetectionStatus;
    private int objectDetectionStatus;

    private boolean dataUploadComplete;
    private boolean saveSettingUploadComplete;
    Message message = null; // 데이터 로딩 후 메인 UI 업데이트 메시지
    private final int FINISH = 999; // 핸들러 메시지 구분 ID

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.control_page,container,false);

        no = userInfo.getUserNo();
        fireDetectionStatus = 0;
        objectDetectionStatus = 0;

        dataUploadComplete = true;
        saveSettingUploadComplete = true;

        // 핸들러에 보내기 위한 메시지 생성 (중복 메시지 확인하고 덮어씌우거나 없으면 새로 메시지 객체 생성)
        message = mHandler.obtainMessage(); // 핸들러의 메시지 객체 획득
        message.what = FINISH;

        service = RetrofitClient.getClient().create(ServiceApi.class);

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
        changeFireDetectionMode = view.findViewById(R.id.changeFireDetectionMode);      //화재감지 상태선택
        changeObjectDetectionMode = view.findViewById(R.id.changeObjectDetectionMode);  //객체감지 상태선택

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

        show_temp_change.setText("준비중");
        show_soil_change.setText("준비중");

        setWithArduinoStatusAndDetectionStatus(new EmbeddedData(no));
        setWithSetttingValue();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    if(dataUploadComplete && saveSettingUploadComplete) {
                        mHandler.sendMessage(message);
                        break;
                    }
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        return view;
    }


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

    private void turnOnFireService() {
        changeFireDetectionMode.setText("켜짐");
        switchFireDetectionStatus();
        changeDetectionStatus(new EmbeddedData(no, fireDetectionStatus, objectDetectionStatus));
        startFireService();
    }

    private void turnOffFireService() {
        changeFireDetectionMode.setText("꺼짐");
        switchFireDetectionStatus();
        changeDetectionStatus(new EmbeddedData(no, fireDetectionStatus, objectDetectionStatus));
        stopFireService();
    }

    private void turnOnObjectService() {
        changeObjectDetectionMode.setText("켜짐");
        switchObjectDetectionStatus();
        changeDetectionStatus(new EmbeddedData(no, fireDetectionStatus, objectDetectionStatus));
        startObjectService();
    }

    private void turnOffObjectService() {
        changeObjectDetectionMode.setText("꺼짐");
        switchObjectDetectionStatus();
        changeDetectionStatus(new EmbeddedData(no, fireDetectionStatus, objectDetectionStatus));
        stopObjectService();
    }

    private void switchFireDetectionStatus() {
        if(fireDetectionStatus == 1) {
            fireDetectionStatus = 0;
        } else {
            fireDetectionStatus = 1;
        }
    }

    private void switchObjectDetectionStatus() {
        if(objectDetectionStatus == 1) {
            objectDetectionStatus = 0;
        } else {
            objectDetectionStatus = 1;
        }
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case FINISH:
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

                    // 불 감지 자동 모드
                    changeFireDetectionMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if(isChecked) {
                                turnOnFireService();
                            } else {
                                turnOffFireService();
                            }

                            changeFireDetectionMode.setEnabled(false);

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    changeFireDetectionMode.setEnabled(true);
                                }
                            }, 7000);  // 1 초 후에 실행
                        }
                    });

                    // 객체 감지 자동모드드
                   changeObjectDetectionMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if(isChecked) {
                                turnOnObjectService();
                            } else {
                                turnOffObjectService();
                            }

                            changeObjectDetectionMode.setEnabled(false);

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    changeObjectDetectionMode.setEnabled(true);
                                }
                            }, 7000);  // 1 초 후에 실행
                        }
                    });

//                    //온도 상승 하락 버튼 리스너
//                    temp_up.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
//                            setTemp++;
//                            show_temp_change.setText(String.valueOf(setTemp));
//                        }
//                    });
//                    temp_down.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
//                            if(setTemp <=0) {
//                            } else{
//                                setTemp--;
//                                show_temp_change.setText(String.valueOf(setTemp));
//                            }
//                        }
//                    });

                    humidity_up.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            setHumidity++;
                            show_humidity_change.setText(String.valueOf(setHumidity));
                        }
                    });
                    humidity_down.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if(setHumidity <= 0){
                            } else{
                                setHumidity--;
                                show_humidity_change.setText(String.valueOf(setHumidity));
                            }
                        }
                    });

//                    soil_up.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
//                            setSoil++;
//                            show_soil_change.setText(String.valueOf(setSoil));
//                        }
//                    });
//                    soil_down.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
//                            if(setSoil <= 0){
//                            }else{
//                                setSoil--;
//                                show_soil_change.setText(String.valueOf(setSoil));
//                            }
//                        }
//                    });

                    //소켓으로 쏘세요 쏘는부분임
                    auto_change_apply.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String temp_str = show_temp_change.getText().toString(); //온도 값 가져오기
                            String humidity_str = show_humidity_change.getText().toString();    //습도 값 가져오기
                            String soil_str = show_soil_change.getText().toString();            //수분 값 가져오기

//                            connectRaspi = new ConnectRaspi("40" + soil_str);       //소켓통신 송신(기대 토양수분량)
//                            connectRaspi.start();
//
//                            try {
//                                Thread.sleep(200);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }

                            connectRaspi = new ConnectRaspi("50" + humidity_str);   //소켓통신 송신(기대 습도)
                            connectRaspi.start();

                            Toast.makeText(getContext(), "설정값이 적용되었습니다.", Toast.LENGTH_SHORT).show();
                        }
                    });
                    break;
            }
        }
    };

    class ConnectRaspi extends Thread {     //소켓통신을 위한 스레드
        private String ip = "192.168.0.29";  // 서버의 IP 주소
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

    private void setWithArduinoStatusAndDetectionStatus(EmbeddedData data) {
        service.EmbeddedSensorStatusAndDetectionStatus(data).enqueue(new Callback<EmbeddedResponse>() {
            @Override
            public void onResponse(Call<EmbeddedResponse> call, Response<EmbeddedResponse> response) {
                EmbeddedResponse result = response.body();
                if(result.getAutomode() == 1) {
                    changeMode.setChecked(true);
                    changeMode.setText("자동모드");
                    autoLayout.setVisibility(View.VISIBLE);
                } else {
                    changeMode.setChecked(false);
                    changeMode.setText("수동모드");
                    manualLayout.setVisibility(View.VISIBLE);
                }

                if(result.getPump() == 1) {
                    manuel_pump_status.setChecked(true);
                    manuel_pump_status.setText("켜짐");
                } else {
                    manuel_pump_status.setChecked(false);
                    manuel_pump_status.setText("꺼짐");
                }

                if(result.getFan() == 1) {
                    manual_fan_status.setChecked(true);
                    manual_fan_status.setText("켜짐");
                } else {
                    manual_fan_status.setChecked(false);
                    manual_fan_status.setText("꺼짐");
                }

                if(result.getLed() == 1) {
                    manual_LED_status.setChecked(true);
                    manual_LED_status.setText("켜짐");
                } else {
                    manual_LED_status.setChecked(false);
                    manual_LED_status.setText("꺼짐");
                }

                if(result.getFireDetection() == 1) {
                    changeFireDetectionMode.setChecked(true);
                    changeFireDetectionMode.setText("켜짐");
                    fireDetectionStatus = 1;
                } else {
                    changeFireDetectionMode.setChecked(false);
                    changeFireDetectionMode.setText("꺼짐");
                    fireDetectionStatus = 0;
                }

                if(result.getObjectDetection() == 1) {
                    changeObjectDetectionMode.setChecked(true);
                    changeObjectDetectionMode.setText("켜짐");
                    objectDetectionStatus = 1;
                } else {
                    changeObjectDetectionMode.setChecked(false);
                    changeObjectDetectionMode.setText("꺼짐");
                    objectDetectionStatus = 0;
                }
                dataUploadComplete = true;
            }

            @Override
            public void onFailure(Call<EmbeddedResponse> call, Throwable t) {
                Toast.makeText(getContext(), "아두이노 정보를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
                Log.e("아두이노 정보를 불러오지 못했습니다.", t.getMessage());
                dataUploadComplete = true;
            }
        });
    }

    private void setWithSetttingValue() {
        service.EmbeddedSensorRecentData().enqueue(new Callback<EmbeddedResponse>() {

            @Override
            public void onResponse(Call<EmbeddedResponse> call, Response<EmbeddedResponse> response) {
                EmbeddedResponse result = response.body();
                show_temp_change.setText("준비중");
                show_humidity_change.setText("" + result.getHumi());
                show_soil_change.setText("준비중");

//                setTemp;
                setHumidity = result.getHumi();
//                setSoil;

                saveSettingUploadComplete = true;
            }

            @Override
            public void onFailure(Call<EmbeddedResponse> call, Throwable t) {
                Toast.makeText(getActivity(), "사전세팅 정보를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
                Log.e("사전세팅 정보를 불러오지 못했습니다.", t.getMessage());
                saveSettingUploadComplete = true;
            }
        });
    }

    private void changeDetectionStatus(EmbeddedData data) {
        service.EmbeddedSetDetectionStatus(data).enqueue(new Callback<EmbeddedResponse>() {
            @Override
            public void onResponse(Call<EmbeddedResponse> call, Response<EmbeddedResponse> response) { }
            @Override
            public void onFailure(Call<EmbeddedResponse> call, Throwable t) {
                Toast.makeText(getActivity(), "감지기 상태변경을 완료하지 못했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 불 감지 foregroundSercie 시작 함수
    public void startFireService() {
        Intent serviceIntent = new Intent(getContext(), FireForegroundService.class);
        ContextCompat.startForegroundService(getContext(), serviceIntent);
        Log.d("서비스시작","불시작");
    }

    // 불 감지 foregroundSercie 끝내기 함수
    public void stopFireService() {
        Intent serviceIntent = new Intent(getContext(), FireForegroundService.class);
        getContext().stopService(serviceIntent);
        Log.d("서비스끝","불끝");

    }

    // 객체 감지 foregroundSercie 시작 함수
    public void startObjectService() {
        Intent serviceIntent = new Intent(getContext(), ObjectForegroundService.class);
        ContextCompat.startForegroundService(getContext(), serviceIntent);
        Log.d("서비스시작","객체시작");
    }

    // 객체 감지 foregroundSercie 끝내기 함수
    public void stopObjectService() {
        Intent serviceIntent = new Intent(getContext(), ObjectForegroundService.class);
        getContext().stopService(serviceIntent);
        Log.d("서비스끝","객체끝끝");
   }
}