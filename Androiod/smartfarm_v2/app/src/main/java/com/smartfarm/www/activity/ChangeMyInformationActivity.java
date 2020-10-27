package com.smartfarm.www.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.smartfarm.www.R;
import com.smartfarm.www.data.LoginResponse;
import com.smartfarm.www.data.UserInformation;
import com.smartfarm.www.network.RetrofitClient;
import com.smartfarm.www.network.ServiceApi;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangeMyInformationActivity extends AppCompatActivity {

    TextView changemyinformation_name, changemyinformation_nickname, changemyinformation_email, changemyinformation_id, changemyinformation_pwd, changemyinformation_location;
    Button changemyinformation_button;
    private ServiceApi service;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.changemyinformation_page);
        service = RetrofitClient.getClient().create(ServiceApi.class);

        changemyinformation_name = findViewById(R.id.changemyinformation_name);
        changemyinformation_nickname = findViewById(R.id.changemyinformation_nickname);
        changemyinformation_email = findViewById(R.id.changemyinformation_email);
        changemyinformation_id = findViewById(R.id.changemyinformation_id);
        changemyinformation_pwd = findViewById(R.id.changemyinformation_pwd);
        changemyinformation_location = findViewById(R.id.changemyinformation_location);

        UserInformation userInfo = UserInformation.getUserInformation();

        changemyinformation_name.setText(userInfo.getUserName());
        changemyinformation_nickname.setText(userInfo.getUserNickName());
        changemyinformation_email.setText(userInfo.getUserEmail());
        changemyinformation_id.setText(userInfo.getUserID());
        changemyinformation_pwd.setText(userInfo.getUserPwd());
        changemyinformation_location.setText(userInfo.getUserLocation());

        changemyinformation_button = (Button) findViewById(R.id.changemyinformation_button);
        changemyinformation_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ChangeMyInformationChangeActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
