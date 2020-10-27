package com.smartfarm.www.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.smartfarm.www.R;
import com.smartfarm.www.appInfo;
import com.smartfarm.www.data.AccessData;
import com.smartfarm.www.data.AccessResponse;
import com.smartfarm.www.data.LoginData;
import com.smartfarm.www.data.LoginResponse;
import com.smartfarm.www.data.RegisterData;
import com.smartfarm.www.data.RegisterResponse;
import com.smartfarm.www.data.UserInformation;
import com.smartfarm.www.network.RetrofitClient;
import com.smartfarm.www.network.ServiceApi;
import com.smartfarm.www.service.LogoutService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private EditText mIdView, mPasswordView;
    private Button mLoginButton, mregisterButton, testLoginbt;
    private CheckBox autoLogin_CheckBox;
    private ServiceApi service;
    private String autoLoginId, autoLoginPwd;
    private int userLogincheck, userNo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);
        service = RetrofitClient.getClient().create(ServiceApi.class);

        //자동로그아웃 활성화
        startService(new Intent(this, LogoutService.class));

        mIdView = (EditText) findViewById(R.id.login_id);
        mPasswordView = (EditText) findViewById(R.id.login_pwd);
        mLoginButton = (Button) findViewById(R.id.Login_Button);
        mregisterButton = (Button) findViewById(R.id.Register_Button);
        testLoginbt = (Button)findViewById(R.id.test_login_bt);
        autoLogin_CheckBox = (CheckBox)findViewById(R.id.autoLogin_CheckBox);

        mIdView.setPadding(100,0,0,0);
        mPasswordView.setPadding(100,0,0,0);

        SharedPreferences auto = getSharedPreferences("auto", Activity.MODE_PRIVATE);
        autoLoginId = auto.getString("inputId", null);
        autoLoginPwd = auto.getString("inputPwd", null);

        mPasswordView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE){
                    if(autoLogin_CheckBox.isChecked()){
                        attemptLogin_auto();
                        UserInformation userInfo = UserInformation.getUserInformation();
                        int userlogincheck = userInfo.getUserLoginCheck();
                        int userno = userInfo.getUserNo();
                        checkIn(new AccessData(userlogincheck, userno));
                    } else{
                        attemptLogin_nonauto();
                        UserInformation userInfo = UserInformation.getUserInformation();
                        int userlogincheck = userInfo.getUserLoginCheck();
                        int userno = userInfo.getUserNo();
                        checkIn(new AccessData(userlogincheck, userno));
                    }
                }
                return false;
            }
        });

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(autoLogin_CheckBox.isChecked() && mIdView.getText() != null && mPasswordView.getText() != null ){
                    attemptLogin_auto();
//                    UserInformation userInfo = UserInformation.getUserInformation();
//                    int userlogincheck = userInfo.getUserLoginCheck();
//                    int userno = userInfo.getUserNo();
//                    checkIn(new AccessData(userlogincheck, userno));
                } else{
                    attemptLogin_nonauto();
//                    UserInformation userInfo = UserInformation.getUserInformation();
//                    int userlogincheck = userInfo.getUserLoginCheck();
//                    int userno = userInfo.getUserNo();
//                    checkIn(new AccessData(userlogincheck, userno));
                }
            }
        });

        mregisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(intent);
            }
        });

        testLoginbt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });

        if(autoLogin_CheckBox.isChecked() && mIdView.getText().toString() != null ){
            mIdView.setText(autoLoginId);
            mPasswordView.setText(autoLoginPwd);
            mLoginButton.performClick();
        }

    }

    private void attemptLogin_nonauto() {
        mIdView.setError(null);
        mPasswordView.setError(null);

        String id = mIdView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // 패스워드의 유효성 검사
        if (password.isEmpty()) {
            mIdView.setError("비밀번호를 입력해주세요.");
            focusView = mIdView;
            cancel = true;
        } else if (!isPasswordValid(password)) {
            mPasswordView.setError("6자 이상의 비밀번호를 입력해주세요.");
            focusView = mPasswordView;
            cancel = true;
        }

        // 아이디의 유효성 검사
        if (id.isEmpty()) {
            mIdView.setError("아이디를 입력해주세요.");
            focusView = mIdView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            startLogin_nonauto(new LoginData(id, password));
        }
    }

    private void attemptLogin_auto() {
        mIdView.setError(null);
        mPasswordView.setError(null);

        String id = mIdView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // 패스워드의 유효성 검사
        if (password.isEmpty()) {
            mIdView.setError("비밀번호를 입력해주세요.");
            focusView = mIdView;
            cancel = true;
        } else if (!isPasswordValid(password)) {
            mPasswordView.setError("6자 이상의 비밀번호를 입력해주세요.");
            focusView = mPasswordView;
            cancel = true;
        }

        // 아이디의 유효성 검사
        if (id.isEmpty()) {
            mIdView.setError("아이디를 입력해주세요.");
            focusView = mIdView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            startLogin_auto(new LoginData(id, password));
        }
    }

    private void startLogin_nonauto(LoginData data) {
        service.userLogin(data).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                LoginResponse result = response.body();

                //싱글톤 패턴에 유저정보 저장
                UserInformation userInfo = UserInformation.getUserInformation();
                userInfo.setUserName(result.getUserName());
                userInfo.setUserNickName(result.getUserNickName());
                userInfo.setUserEmail(result.getUserEmail());
                userInfo.setUserID(result.getUserID());
                userInfo.setUserPwd(result.getUserPwd());
                userInfo.setUserLocation(result.getUserLocation());
                userInfo.setUserNo(result.getUserNo());
                userInfo.setUserLoginCheck(result.getUserLoginCheck());

                appInfo.S3userID = result.getUserID(); // foreground 서비스에 이용하기 위한 넣기 (가벼운 메모리를 위해)

                int check = userInfo.getUserLoginCheck();
                int no = userInfo.getUserNo();
                if(/*check == 0*/true){
                    check = 1;
                    checkIn(new AccessData(check, no));
                } else if (check == 1){
                    Toast.makeText(LoginActivity.this, "다른 기기에서 접속중입니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "로그인 에러 발생", Toast.LENGTH_SHORT).show();
                Log.e("로그인 에러 발생", t.getMessage());
            }
        });
    }

    private void startLogin_auto(LoginData data) {
        service.userLogin(data).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                LoginResponse result = response.body();

                //싱글톤 패턴에 유저정보 저장
                UserInformation userInfo = UserInformation.getUserInformation();
                userInfo.setUserName(result.getUserName());
                userInfo.setUserNickName(result.getUserNickName());
                userInfo.setUserEmail(result.getUserEmail());
                userInfo.setUserID(result.getUserID());
                userInfo.setUserPwd(result.getUserPwd());
                userInfo.setUserLocation(result.getUserLocation());
                userInfo.setUserNo(result.getUserNo());
                userInfo.setUserLoginCheck(result.getUserLoginCheck());

                appInfo.S3userID = result.getUserID(); // foreground 서비스에 이용하기 위한 넣기 (가벼운 메모리를 위해)

                //Auto Login function using sharedpreference
                SharedPreferences auto = getSharedPreferences("auto", Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = auto.edit();
                editor.putString("inputId", result.getUserID());
                editor.putString("inputPwd", result.getUserPwd());
                editor.commit();

                int check = userInfo.getUserLoginCheck();
                int no = userInfo.getUserNo();
                if(/*check == 0*/true){
                    check = 1;
                    checkIn(new AccessData(check, no));
                } else if (check == 1){
                    Toast.makeText(LoginActivity.this, "다른 기기에서 접속중입니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "로그인 에러 발생", Toast.LENGTH_SHORT).show();
                Log.e("로그인 에러 발생", t.getMessage());
            }
        });
    }

    private void checkIn(AccessData data){
        service.userLoginCheckIn(data).enqueue(new Callback<AccessResponse>() {
            @Override
            public void onResponse(Call<AccessResponse> call, Response<AccessResponse> response) {
                AccessResponse result = response.body();
                if (result.getCode() == 200) {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
            @Override
            public void onFailure(Call<AccessResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "접속 에러 발생", Toast.LENGTH_SHORT).show();
                Log.e("접속 에러 발생", t.getMessage());
            }
        });
    }

    private boolean isPasswordValid(String password) {
        return password.length() >= 6;
    }
}
