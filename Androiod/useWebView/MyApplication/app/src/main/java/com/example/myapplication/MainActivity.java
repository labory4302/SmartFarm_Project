package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Picture;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {
    private String mUrl = "http://192.168.0.19:8081/video.mjpg";
    private WebView mWebView; // 웹뷰 선언
    private WebSettings mWebSettings; //웹뷰세팅
    private Button mButton;
    private Button mButton2;
    private ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mWebView = (WebView) findViewById(R.id.webView);
        mButton = (Button) findViewById(R.id.button);
        mImageView = (ImageView) findViewById(R.id.imageView);
        mButton2 = (Button) findViewById(R.id.button2);

        mWebView.setWebViewClient(new WebViewClient()); // 클릭시 새창 안뜨게
        mWebSettings = mWebView.getSettings(); //세부 세팅 등록
        mWebSettings.setJavaScriptEnabled(true); // 웹페이지 자바스클비트 허용 여부
        mWebSettings.setSupportMultipleWindows(false); // 새창 띄우기 허용 여부
        mWebSettings.setJavaScriptCanOpenWindowsAutomatically(false); // 자바스크립트 새창 띄우기(멀티뷰) 허용 여부
        mWebSettings.setLoadWithOverviewMode(true); // 메타태그 허용 여부
        mWebSettings.setUseWideViewPort(true); // 화면 사이즈 맞추기 허용 여부
        mWebSettings.setSupportZoom(true); // 화면 줌 허용 여부
        mWebSettings.setBuiltInZoomControls(true); // 화면 확대 축소 허용 여부
        mWebSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN); // 컨텐츠 사이즈 맞추기
        mWebSettings.setCacheMode(WebSettings.LOAD_NO_CACHE); // 브라우저 캐시 허용 여부
        mWebSettings.setDomStorageEnabled(true); // 로컬저장소 허용 여부

        mWebView.loadUrl(mUrl); // 웹뷰에 표시할 웹사이트 주소, 웹뷰 시작

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                screenshotSharing();
            }
        });
        mButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });
    }

    public void screenshotSharing() {
        //step 1) 스크린샷 찍어 Bitmap 객체로 변환
        mWebView.setDrawingCacheEnabled(true);
        //Bitmap screenshot = mAppView.getDrawingCache(); //현재 나와있는 화면만 저장
        //실험코드
        Bitmap screenshot = Bitmap. createBitmap(mWebView.getWidth(), mWebView.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(screenshot);
        mWebView.draw(c);

        //step 2) Bitmap객체를 파일로 저장
        String filename = "screenShot.png";
        Uri imageUri = null;

        try {
            File f = new File(getApplicationContext().getExternalCacheDir(), filename);

            f.createNewFile();
            imageUri = Uri. fromFile(f);
            OutputStream outStream = new FileOutputStream(f);

            screenshot.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            outStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mWebView.setDrawingCacheEnabled(false);
    }

    public void uploadImage() {
        String path = getExternalCacheDir() + "/screenShot.png";
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        Drawable drawable = (Drawable)(new BitmapDrawable(bitmap));
        mImageView.setImageDrawable(drawable);
    }
}