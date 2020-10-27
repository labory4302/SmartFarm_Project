package com.smartfarm.www.network;

import com.smartfarm.www.activity.LoginActivity;
import com.smartfarm.www.data.MypageData;
import com.smartfarm.www.data.MypageResponse;
import com.smartfarm.www.data.RegisterData;
import com.smartfarm.www.data.RegisterResponse;
import com.smartfarm.www.data.LoginData;
import com.smartfarm.www.data.LoginResponse;
import com.smartfarm.www.data.VersionData;
import com.smartfarm.www.data.VersionResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ServiceApi {
    @POST("/user/login")
    Call<LoginResponse> userLogin(@Body LoginData data);

    @POST("/user/register")
    Call<RegisterResponse> userRegister(@Body RegisterData data);

    @POST("/mypage/changemyinformation")
    Call<RegisterResponse> MypageChangeMyInformation(@Body RegisterData data);

    @POST("/mypage/version")
    Call<VersionResponse> MypageVersion();
}