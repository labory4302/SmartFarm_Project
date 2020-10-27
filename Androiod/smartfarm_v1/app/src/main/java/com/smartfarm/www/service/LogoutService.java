package com.smartfarm.www.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import com.smartfarm.www.activity.LoginActivity;
import com.smartfarm.www.activity.MypageActivity;
import com.smartfarm.www.data.AccessData;
import com.smartfarm.www.data.AccessResponse;
import com.smartfarm.www.data.UserInformation;
import com.smartfarm.www.network.RetrofitClient;
import com.smartfarm.www.network.ServiceApi;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LogoutService extends Service {

    private ServiceApi service;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        service = RetrofitClient.getClient().create(ServiceApi.class);
        UserInformation userInfo = UserInformation.getUserInformation();

        int check = 0;
        int no = userInfo.getUserNo();
        checkOut(new AccessData(check, no));
        stopSelf();
    }
    private void checkOut(AccessData data){
        service.userLoginCheckOut(data).enqueue(new Callback<AccessResponse>() {
            @Override
            public void onResponse(Call<AccessResponse> call, Response<AccessResponse> response) {
                AccessResponse result = response.body();
            }
            @Override
            public void onFailure(Call<AccessResponse> call, Throwable t) {
                Toast.makeText(LogoutService.this, "로그 아웃 에러 발생", Toast.LENGTH_SHORT).show();
                Log.e("로그 아웃 에러 발생", t.getMessage());
            }
        });
    }
}
