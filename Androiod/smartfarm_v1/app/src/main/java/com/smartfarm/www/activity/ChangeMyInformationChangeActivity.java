package com.smartfarm.www.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.smartfarm.www.R;
import com.smartfarm.www.data.RegisterData;
import com.smartfarm.www.data.RegisterResponse;
import com.smartfarm.www.data.UserInformation;
import com.smartfarm.www.network.RetrofitClient;
import com.smartfarm.www.network.ServiceApi;
import com.smartfarm.www.service.LogoutService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangeMyInformationChangeActivity extends AppCompatActivity {
    EditText changemyinformation_change_name,
            changemyinformation_change_nickname,
            changemyinformation_change_email,
            changemyinformation_change_id,
            changemyinformation_change_pwd,
            changemyinformation_change_location;
    Button changemyinformation_change_button, change_info_change_back_bt;

    private ServiceApi service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.changemyinformation_change_page);
        service = RetrofitClient.getClient().create(ServiceApi.class);
        UserInformation userInfo = UserInformation.getUserInformation();

        //자동로그아웃 활성화
        startService(new Intent(this, LogoutService.class));

        changemyinformation_change_name = (EditText) findViewById(R.id.changemyinformation_change_name);
        changemyinformation_change_nickname = (EditText) findViewById(R.id.changemyinformation_change_nickname);
        changemyinformation_change_email = (EditText) findViewById(R.id.changemyinformation_change_email);
        changemyinformation_change_id = (EditText) findViewById(R.id.changemyinformation_change_id);
        changemyinformation_change_pwd = (EditText) findViewById(R.id.changemyinformation_change_pwd);
        changemyinformation_change_location = (EditText) findViewById(R.id.changemyinformation_change_location);
        change_info_change_back_bt = (Button) findViewById(R.id.change_info_change_bt);

        changemyinformation_change_name.setPadding(20,0,0,0);
        changemyinformation_change_nickname.setPadding(20,0,0,0);
        changemyinformation_change_email.setPadding(20,0,0,0);
        changemyinformation_change_id.setPadding(20,0,0,0);
        changemyinformation_change_pwd.setPadding(20,0,0,0);
        changemyinformation_change_location.setPadding(20,0,0,0);

        changemyinformation_change_name.setText(userInfo.getUserName());
        changemyinformation_change_nickname.setText(userInfo.getUserNickName());
        changemyinformation_change_email.setText(userInfo.getUserEmail());
        changemyinformation_change_id.setText(userInfo.getUserID());
        changemyinformation_change_pwd.setText(userInfo.getUserPwd());
        changemyinformation_change_location.setText(userInfo.getUserLocation());

        changemyinformation_change_button = (Button) findViewById(R.id.changemyinformation_change_button);

        change_info_change_back_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        changemyinformation_change_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String name = changemyinformation_change_name.getText().toString();
                final String nickname = changemyinformation_change_nickname.getText().toString();
                final String email = changemyinformation_change_email.getText().toString();
                final String id = changemyinformation_change_id.getText().toString();
                final String pwd = changemyinformation_change_pwd.getText().toString();
                final String location = changemyinformation_change_location.getText().toString();

                UserInformation userInfo1 = UserInformation.getUserInformation();
                final int no = userInfo1.getUserNo();

                changeInformation(new RegisterData(name, nickname, email, id, pwd, location, no));

                UserInformation userInfo2 = UserInformation.getUserInformation();

                userInfo2.setUserName(name);
                userInfo2.setUserNickName(nickname);
                userInfo2.setUserEmail(email);
                userInfo2.setUserID(id);
                userInfo2.setUserPwd(pwd);
                userInfo2.setUserLocation(location);

                finish();
            }
        });
    }

    private void changeInformation(RegisterData data){
        service.MypageChangeMyInformation(data).enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                RegisterResponse result = response.body();

                Toast.makeText(ChangeMyInformationChangeActivity.this, "개인정보가 수정되었습니다.", Toast.LENGTH_SHORT).show();
                if (result.getCode() == 200) {
                    finish();
                }
            }
            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                Toast.makeText(ChangeMyInformationChangeActivity.this, "개인정보수정 에러 발생", Toast.LENGTH_SHORT).show();
                Log.e("개인정보수정 에러 발생", t.getMessage());
            }
        });
    }
}
