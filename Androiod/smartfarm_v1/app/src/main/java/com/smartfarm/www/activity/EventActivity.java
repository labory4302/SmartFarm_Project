package com.smartfarm.www.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.smartfarm.www.R;
import com.smartfarm.www.data.EventResponse;
import com.smartfarm.www.data.VersionResponse;
import com.smartfarm.www.network.RetrofitClient;
import com.smartfarm.www.network.ServiceApi;
import com.smartfarm.www.service.LogoutService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventActivity extends AppCompatActivity {

    private ServiceApi service;

    TextView event_title, event_contents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_page);

        //자동로그아웃 활성화
        startService(new Intent(this, LogoutService.class));

        service = RetrofitClient.getClient().create(ServiceApi.class);

        event_title = findViewById(R.id.event_title);
        event_contents = findViewById(R.id.event_contents);

        checkEvent();
    }
    private void checkEvent() {
        service.MypageEvent().enqueue(new Callback<EventResponse>() {
            @Override
            public void onResponse(Call<EventResponse> call, Response<EventResponse> response) {
                EventResponse result = response.body();
                event_title.setText(""+result.getEventTitle());
                event_contents.setText(""+result.getEventContents());
            }

            @Override
            public void onFailure(Call<EventResponse> call, Throwable t) {
                Toast.makeText(EventActivity.this, "이벤트 정보를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
                Log.e("이벤트 정보를 불러오지 못했습니다.", t.getMessage());
            }
        });
    }
}
