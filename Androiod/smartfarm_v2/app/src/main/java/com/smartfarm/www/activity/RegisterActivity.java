package com.smartfarm.www.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.smartfarm.www.R;
import com.smartfarm.www.data.RegisterData;
import com.smartfarm.www.data.RegisterResponse;
import com.smartfarm.www.network.RetrofitClient;
import com.smartfarm.www.network.ServiceApi;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

//회원가입 폼
public class RegisterActivity extends AppCompatActivity {
    private EditText mNameView;
    private EditText mNickNameView;
    private EditText mEmailView;
    private EditText mIdView;
    private EditText mPasswordView;
    private EditText mLocationView;
    private Button mRegisterButton;
    private ServiceApi service;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_page);

        mNameView = (EditText) findViewById(R.id.register_name);
        mNickNameView = (EditText) findViewById(R.id.register_nickname);
        mEmailView = (EditText) findViewById(R.id.register_email);
        mIdView = (EditText) findViewById(R.id.register_id);
        mPasswordView = (EditText) findViewById(R.id.register_pwd);
        mLocationView = (EditText) findViewById(R.id.register_location);
        mRegisterButton = (Button) findViewById(R.id.register_Button);

        service = RetrofitClient.getClient().create(ServiceApi.class);

        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptregister();
            }
        });
    }

    private void attemptregister() {
        mNameView.setError(null);
        mNickNameView.setError(null);
        mEmailView.setError(null);
        mIdView.setError(null);
        mPasswordView.setError(null);
        mLocationView.setError(null);

        String name = mNameView.getText().toString();
        String nickname = mNickNameView.getText().toString();
        String email = mEmailView.getText().toString();
        String id = mIdView.getText().toString();
        String password = mPasswordView.getText().toString();
        String location = mLocationView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // 패스워드의 유효성 검사
        if (password.isEmpty()) {
            mEmailView.setError("비밀번호를 입력해주세요.");
            focusView = mEmailView;
            cancel = true;
        } else if (!isPasswordValid(password)) {
            mPasswordView.setError("6자 이상의 비밀번호를 입력해주세요.");
            focusView = mPasswordView;
            cancel = true;
        }

        // 이메일의 유효성 검사
        if (email.isEmpty()) {
            mEmailView.setError("이메일을 입력해주세요.");
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError("@를 포함한 유효한 이메일을 입력해주세요.");
            focusView = mEmailView;
            cancel = true;
        }

        // 이름의 유효성 검사
        if (name.isEmpty()) {
            mNameView.setError("이름을 입력해주세요.");
            focusView = mNameView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            startregister(new RegisterData(name, nickname, email, id, password, location));
        }
    }

    private void startregister(RegisterData data) {
        service.userRegister(data).enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                RegisterResponse result = response.body();
                Toast.makeText(RegisterActivity.this, result.getMessage(), Toast.LENGTH_SHORT).show();

                if (result.getCode() == 200) {
                    finish();
                }
            }

            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, "회원가입 에러 발생", Toast.LENGTH_SHORT).show();
                Log.e("회원가입 에러 발생", t.getMessage());
            }
        });
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() >= 6;
    }
}