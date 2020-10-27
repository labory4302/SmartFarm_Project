package com.smartfarm.www.service;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.smartfarm.www.R;

import uk.co.senab.photoview.PhotoView;

public class Photoservice extends AppCompatActivity {

    String imgPath;
    PhotoView photoView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photoservice);
        View view = getWindow().getDecorView();  // 액티비티의 view 뷰 정보 가져오기
        if (Build.VERSION.SDK_INT >= 21) {
            //21 버전보다 낮으면 검은색 바탕
            getWindow().setStatusBarColor(Color.BLACK);

        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (view != null) {
                // 23 버전 이상일 때 상태바 하얀 색상에 회색 아이콘 색상을 설정

                //view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);  // 밝은 상태바 요청

                getWindow().setStatusBarColor(Color.parseColor("#000000"));

            }

        }


        photoView = (PhotoView) findViewById(R.id.notification_img);
        Intent intent = getIntent();
        imgPath = intent.getExtras().getString("imgPath");


        Bitmap bitmap = BitmapFactory.decodeFile(imgPath);


        photoView.setImageBitmap(bitmap);





    }
}
