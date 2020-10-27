package com.smartfarm.www.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.smartfarm.www.R;
import com.smartfarm.www.appInfo;
import com.smartfarm.www.data.EmbeddedData;
import com.smartfarm.www.data.EmbeddedResponse;
import com.smartfarm.www.data.UserInformation;
import com.smartfarm.www.network.RetrofitClient;
import com.smartfarm.www.network.ServiceApi;
import com.smartfarm.www.service.LogoutService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView; // 바텀 네비게이션 뷰
    private FragmentManager fm; //
    private FragmentTransaction ft; //
    private HomeActivity frag1; //각 프래그먼트
    private ControlActivity frag2;
    private CropActivity frag3;
    private MypageActivity frag4;

    //로그인한 유저의 userNo를 가져오기 위한 변수
    UserInformation userInfo = UserInformation.getUserInformation();
    private int no;

    private ServiceApi service; //DB에 접근하기 위한 서비스 인스턴스

    TextView show_temp_live, show_humidity_live, show_soil_live, title_tv; //현재 온,습,수분 뷰

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_page);

        //유저의 userNo를 가져옴
        no = userInfo.getUserNo();

        //서비스 인스턴스 활성화
        service = RetrofitClient.getClient().create(ServiceApi.class);

        //자동로그아웃 활성화
        startService(new Intent(this, LogoutService.class));

        bottomNavigationView = findViewById(R.id.bottomNavi);
        show_temp_live = findViewById(R.id.show_temp);
        show_humidity_live = findViewById(R.id.show_humidity);
        show_soil_live = findViewById(R.id.show_soil);
        title_tv = findViewById(R.id.title_tv);
        show_soil_live.setText("수분\n준비중");

        //DB에 접근하여 온습도 수치를 가져와 표시
        settingSensorValue(new EmbeddedData(no));

        SharedPreferences auto = getSharedPreferences("auto", Activity.MODE_PRIVATE);

        //바텀네비게이션 이벤트
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener()
        {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem)
            {
                switch (menuItem.getItemId())
                {
                    case R.id.tab1:
                        setFrag(0);
                        title_tv.setText("홈");
                        break;
                    case R.id.tab2:
                        setFrag(1);
                        title_tv.setText("제어");
                        break;
                    case R.id.tab3:
                        setFrag(2);
                        title_tv.setText("검색");
                        break;
                    case R.id.tab4:
                        setFrag(3);
                        title_tv.setText("내정보");
                        break;
                }
                return true;
            }
        });

        frag1=new HomeActivity();
        frag2=new ControlActivity();
        frag3=new CropActivity();
        frag4=new MypageActivity();
        setFrag(0); // 첫 프래그먼트 화면 지정
    }

    // 프레그먼트 교체
    private void setFrag(int n)
    {
        fm = getSupportFragmentManager();
        ft = fm.beginTransaction();
        switch (n)
        {
            case 0:
                ft.replace(R.id.Main_Frame,frag1);
                ft.commit();
                break;

            case 1:
                ft.replace(R.id.Main_Frame,frag2);
                ft.commit();
                break;

            case 2:
                ft.replace(R.id.Main_Frame,frag3);
                ft.commit();
                break;
            case 3:
                ft.replace(R.id.Main_Frame,frag4);
                ft.commit();
        }
    }

    //현재 온도와 습도를 가져오기 위한 AWS DB접근 함수
    private void settingSensorValue(EmbeddedData data) {
        service.EmbeddedSensorData(data).enqueue(new Callback<EmbeddedResponse>() {
            @Override
            public void onResponse(Call<EmbeddedResponse> call, Response<EmbeddedResponse> response) {
                EmbeddedResponse result = response.body();
                show_temp_live.setText("습도\n"+result.getRecentHumi()+"%");
                show_humidity_live.setText("온도\n"+result.getTemp()+"℃");
            }
            @Override
            public void onFailure(Call<EmbeddedResponse> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "온,습도 정보를 가져오지 못했습니다.", Toast.LENGTH_SHORT).show();
                show_temp_live.setText("습도 : 오류");
                show_humidity_live.setText("온도 : 오류");
            }
        });
    }
}
