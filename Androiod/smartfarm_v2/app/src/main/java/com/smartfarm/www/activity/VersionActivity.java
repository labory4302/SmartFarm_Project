package com.smartfarm.www.activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.smartfarm.www.R;
import com.smartfarm.www.network.RetrofitClient;
import com.smartfarm.www.network.ServiceApi;
import com.smartfarm.www.data.VersionData;
import com.smartfarm.www.data.VersionResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VersionActivity extends AppCompatActivity {

    TextView version_no, version_info;
    private ServiceApi service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.version_page);

        version_no = findViewById(R.id.version_no);
        version_info = findViewById(R.id.version_info);

        service = RetrofitClient.getClient().create(ServiceApi.class);

        checkVersion();
    }

    private void checkVersion() {
        service.MypageVersion().enqueue(new Callback<VersionResponse>() {
            @Override
            public void onResponse(Call<VersionResponse> call, Response<VersionResponse> response) {
                VersionResponse result = response.body();
                version_no.setText(""+result.getVersion());
                version_info.setText(""+result.getVersionInformation());
            }

            @Override
            public void onFailure(Call<VersionResponse> call, Throwable t) {
                Toast.makeText(VersionActivity.this, "버전 정보를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
                Log.e("버전 정보를 불러오지 못했습니다.", t.getMessage());
            }
        });
    }
}
