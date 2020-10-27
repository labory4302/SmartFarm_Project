package com.smartfarm.www.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.smartfarm.www.R;
import com.smartfarm.www.appInfo;
import com.smartfarm.www.data.RegisterData;
import com.smartfarm.www.data.RegisterResponse;
import com.smartfarm.www.network.RetrofitClient;
import com.smartfarm.www.network.ServiceApi;
import com.smartfarm.www.service.LogoutService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

//회원가입 폼
public class RegisterActivity extends AppCompatActivity {
    private EditText mNameView;
    private EditText mNickNameView;
    private EditText mEmailView;
    private EditText mIdView;
    private Button mCheckDuplicateIdButton;
    private EditText mPasswordView;
    private EditText mConfirmPasswordView;
    private EditText mLocationView;
    private Button mRegisterButton;
    private ServiceApi service;

    private boolean IdChecked = false;
    private boolean checkComplete = false;
    private int checkStatus = 0;
    //404 : 아이디 중복 체크 에러
    //204 : 아이디 중복
    //200 : 아이디 사용 가능

    Message message = null; // 데이터 로딩 후 메인 UI 업데이트 메시지
    private final int FINISH = 999; // 핸들러 메시지 구분 ID

    View focusIdView = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_page);

        //자동로그아웃 활성화
        startService(new Intent(this, LogoutService.class));

        mNameView = (EditText) findViewById(R.id.register_name);
        mNickNameView = (EditText) findViewById(R.id.register_nickname);
        mEmailView = (EditText) findViewById(R.id.register_email);
        mIdView = (EditText) findViewById(R.id.register_id);
        mCheckDuplicateIdButton = (Button) findViewById(R.id.checkDuplicateId);
        mPasswordView = (EditText) findViewById(R.id.register_pwd);
        mConfirmPasswordView = (EditText) findViewById(R.id.confirm_pwd);
        mLocationView = (EditText) findViewById(R.id.register_location);
        mRegisterButton = (Button) findViewById(R.id.register_Button);

        mNameView.setPadding(20,0,0,0);
        mNickNameView.setPadding(20, 0,0,0);
        mEmailView.setPadding(20,0,0,0);
        mIdView.setPadding(20,0,0,0);
        mPasswordView.setPadding(20,0,0,0);
        mConfirmPasswordView.setPadding(20, 0, 0, 0);
        mLocationView.setPadding(20,0,0,0);

        service = RetrofitClient.getClient().create(ServiceApi.class);

        focusIdView = mIdView;

        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptregister();
            }
        });
        mCheckDuplicateIdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkId();
            }
        });
    }

    private void attemptregister() {
        mNameView.setError(null);
        mNickNameView.setError(null);
        mEmailView.setError(null);
        mIdView.setError(null);
        mPasswordView.setError(null);
        mConfirmPasswordView.setError(null);
        mLocationView.setError(null);

        String name = mNameView.getText().toString();
        String nickname = mNickNameView.getText().toString();
        String email = mEmailView.getText().toString();
        String id = mIdView.getText().toString();
        String password = mPasswordView.getText().toString();
        String confirmPassword = mConfirmPasswordView.getText().toString();
        String location = mLocationView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // ID 유효성 검사
        if (id.isEmpty()) {
            mIdView.setError("ID를 입력해주세요.");
            focusView = mIdView;
            cancel = true;
        } else if (!IdChecked) {
            mIdView.setError("ID중복확인을 해주세요.");
            focusView = mIdView;
            cancel = true;
        }

        // 패스워드의 유효성 검사
        if (password.isEmpty()) {
            mPasswordView.setError("비밀번호를 입력해주세요.");
            focusView = mPasswordView;
            cancel = true;
        } else if (!isPasswordValid(password)) {
            mPasswordView.setError("6자 이상의 비밀번호를 입력해주세요.");
            focusView = mPasswordView;
            cancel = true;
        } else if (confirmPassword.isEmpty()){
            mConfirmPasswordView.setError("비밀번호를 한번 더 입력해주세요.");
            focusView = mConfirmPasswordView;
            cancel = true;
        } else if (confirmPassword.equals(password) == false) {
            mConfirmPasswordView.setError("비밀번호가 일치하지 않습니다.");
            focusView = mConfirmPasswordView;
            cancel = true;
        }

        // 닉네임 유효성 검사
        if (nickname.isEmpty()) {
            mNickNameView.setError("별명을 입력해주세요.");
            focusView = mNickNameView;
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

        // 지역 유효성 검사
        if (location.isEmpty()) {
            mLocationView.setError("지역을 입력해주세요.");
            focusView = mLocationView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            startregister(new RegisterData(name, nickname, email, id, password, location));
        }
    }

    private void checkId() {
        mIdView.setError(null);
        String id = mIdView.getText().toString();

        boolean cancel = false;

        message = mHandler.obtainMessage(); // 핸들러의 메시지 객체 획득
        message.what = FINISH;

        if (id.isEmpty()) {
            mIdView.setError("ID를 입력해주세요.");
            cancel = true;
        } else {
            checkDuplicateId(new RegisterData(id));
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while(true) {
                        if(checkComplete) {
                            mHandler.sendMessage(message);
                            break;
                        }
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    return;
                }
            }).start();
        }
        if (cancel) {
            focusIdView.requestFocus();
        }
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case FINISH:
                    boolean cancel = false;
                    if (checkStatus == 204) {
                        mIdView.setError("중복된 아이디입니다. 다른 아이디를 사용해주세요.");
                        cancel = true;
                    } else if (checkStatus == 404) {
                        cancel = true;
                    }

                    if (cancel) {
                        focusIdView.requestFocus();
                    } else if (checkStatus == 200) {
                        useNonDuplicateId();
                    }

                    Log.d("dddddddddddd", "checkStatus : " + checkStatus);
                    checkStatus = 0;
                    checkComplete = false;
                    break;
            }
        }
    };

//    private void checkId() {
//        mIdView.setError(null);
//        String id = mIdView.getText().toString();
//
//        boolean cancel = false;
//        View focusView = null;
//
//        if (id.isEmpty()) {
//            mIdView.setError("ID를 입력해주세요.");
//            focusView = mIdView;
//            cancel = true;
//        } else {
//            checkDuplicateId(new RegisterData(id));
//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    Log.d("ddddddfffddd", "결과 : " + checkComplete + " 검사코드 : " + checkStatus);
//                    boolean cancel = false;
//                    View focusView = null;
//                    if (checkStatus == 204) {
//                        mIdView.setError("중복된 아이디입니다. 다른 아이디를 사용해주세요.");
//                        focusView = mIdView;
//                        cancel = true;
//                    } else if (checkStatus == 404) {
//                        focusView = mIdView;
//                        cancel = true;
//                    }
//
//                    if (cancel) {
//                        focusView.requestFocus();
//                    } else if (checkStatus == 200) {
//                        useNonDuplicateId();
//                    }
//                    checkStatus = 0;
//                }
//            }, 500);
//        }
//        if (cancel) {
//            focusView.requestFocus();
//        }
//    }

    private void checkDuplicateId(RegisterData data) {
        service.userCheckId(data).enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                RegisterResponse result = response.body();
                if (result.getCode() == 204) {
                    //중복된 아이디
                    checkStatus = 204;
                } else if(result.getCode() == 200) {
                    //사용가능한 아이디
                    checkStatus = 200;
                }
                checkComplete = true;
            }
            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                checkStatus = 404;
                Toast.makeText(RegisterActivity.this, "회원가입 에러 발생", Toast.LENGTH_SHORT).show();
                checkComplete = true;
            }
        });
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

    private void useNonDuplicateId() {
        AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
        builder.setMessage("사용가능한 아이디입니다.\n사용하시겠습니까?");
        builder.setPositiveButton("사용",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        IdChecked = true;
                        mIdView.setClickable(false);
                        mIdView.setFocusable(false);
                        mIdView.setBackgroundColor(Color.parseColor("#CFCFCF"));
                    }
                });
        builder.setNegativeButton("사용하지 않음",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {}
                });
        builder.show();
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() >= 6;
    }
}