package com.smartfarm.www.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.amazonaws.mobileconnectors.lambdainvoker.LambdaFunctionException;
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaInvokerFactory;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.smartfarm.www.R;
import com.smartfarm.www.activity.MainActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;


public class ForegroundService extends Service {
    public static final String SMARTFARM_CHANNEL_ID = "69981";

    private final String BUCKET_NAME = "hotsix-smartfarm"; // S3 버킷 이름 (저장소 이름)
    private String imgName;  // 저장할 이미지의 이름

    private CognitoCachingCredentialsProvider credentialsProvider; // 자격 증명 풀
    private TransferObserver uploadObserver; // 파일 업로드 시 모니터링을 하기 위한 객체

    private File FwebImg_Resize; // 썸네일한 이미지 파일 (S3 올리고 사진을 지움)

    private Bitmap webImg_bitmap; // 웹캠에서 받아오는 한 프레임 이미지 비트팸

    NotificationChannel channel; // 푸쉬 알림 채널


    // 시스템은 서비스가 처음 생성되었을 때(즉 서비스가 onStartCommand() 또는 onBind()를 호출하기 전에)
    // 이 메서드를 호출하여 일회성 설정 절차를 수행합니다. 서비스가 이미 실행 중인 경우, 이 메서드는 호출되지 않음음    @Override
    public void onCreate() {
        super.onCreate();
    }


    // 서비스는 기본적으로 앱이 구동되는 프로세스의 메인 쓰레드에서 실행된다. 다시 말해서,
    // 서비스는 별도의 프로세스나 쓰레드에서 실행되는 것이 아니기 때문에
    // 서비스에서 CPU 사용량이 많은 작업, 또는 mp3 재생이나 네크워킹과 같은 blocking 작업
    // 한마디로 실행 흐름을 막는 작업(blocking operation)을 하게 되면 액티비티의 동작이 느려질 수 있기 때문에
    // 서비스에서 별도의 쓰레드를 생성하여 그 안에서 작업이 수행되도록 구현해야 한다
    // 이렇게 별도의 쓰레드를 사용함으로써, 응답 지연 문제(ANR)를 예방하고,
    // 앱의 메인 스레드가 사용자와의 상호작용에 집중할 수 있도록 해줍니다. (앱을 사용중일시시)

    class NewRunnable implements Runnable {
        @Override
        public void run() {
//            while (true) {
                // 현재 시간 가져오기.
                long now = System.currentTimeMillis();

                // 현재시간의 날짜 생성하기
                Date date = new Date(now);

                // 내가 원하는 형식으로 포맷해서 시간 가져오기
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH-mm-ss");

                imgName = timeFormat.format(date);

                imgName +=".png";

                // 웹캠에서 현재시간의 사진(1프레임)을 받아옴
                Bitmap webImg_bitmap = webCameraCapture();

                // 받아온 사진 비트맵을 파일로 변환하고 외부 캐시저장소에 원본사진 저장
//                File webImg_file = SaveBitmapToFile(webImg_bitmap, imgName);
//
//                // 통신을 빠르게 하기 위한 이미지 썸네일  (S3에 저장할 이미지의 크기를 작게 한다.)
//                // 썸네일한 이미지를 S3에 전송하기 위해 temp라는 이름으로 임시 저장했음
//                // 전송하고 난 뒤에 지워주어야 함
//                FwebImg_Resize = ImgResize(webImg_file);
//
//                // S3에 파일 업로드
//                awsS3Upload(FwebImg_Resize, imgName);

                // 쓰레드를 어떤 시간마다 실핼할 것인지
                try {
                    Thread.sleep(60000) ;
                } catch (Exception e) {
                    e.printStackTrace() ;
                }
//            }
        }
    }

    class test implements Runnable {
        @Override
        public void run() {
            while (true)
            {
                NotificationSomethings(444);
                Log.d("test","tttttttttttt");
                try {
                    Thread.sleep(5000) ;
                } catch (Exception e) {
                    e.printStackTrace() ;
                }

            }

        }
    }



    // startService()를 호출함으로써 서비스 실행을 요청할 때, 시스템이 호출해주는 콜백 메소드이다.
    // 이 메소드가 실행되면 서비스는 started 상태가 되며, 후면(background)에서 작업을 수행한다.
    // 만약 이 메소드를 구현한다면, 서비스가 할 작업을 모두 마쳤을 때 서비스를 중지하기 위해
    // stopSelf()나 stopService()를 호출하는 부분도 구현해야 한다
    // 이유는 stopSelf()나 stopService()를 호출되기 전까지 서비스는 멈추지 않기 때문 (서비스를 사용하고 싶지 않을때는 대비해서서


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //NotificationSomethings(123);

        NewRunnable nr = new NewRunnable();
        Thread t = new Thread(nr);
        t.start();

        // 푸쉬알림 채널 생성
        creatChannel();

        NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, SMARTFARM_CHANNEL_ID);

        //OREO API 26 이상에서는 채널 필요
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // icon setting 유일한 필수 콘텐츠입니다 푸쉬 알림이 안됩니다.
            builder.setSmallIcon(R.drawable.ic_launcher_foreground); //mipmap 사용시 Oreo 이상에서 시스템 UI 에러남


        }else builder.setSmallIcon(R.mipmap.ic_launcher); // Oreo 이하에서 mipmap 사용하지 않으면 Couldn't create icon: StatusBarIcon 에러남

        // null 아니라는걸 확인
        assert notificationManager != null;


        // 푸쉬알림을 ID로 구분함
        notificationManager.notify(123, builder.build());




        startForeground(123, builder.build());

        // foregorundservice시 강제로 생기는 푸쉬 알림 cancel
        NotificationSomethings(123);









        // 정수형의 값을 리턴해야 해야 하는 이유는
        // 시스템이 메모리가 부족하여 서비스를 종료한 이후에, 다시 여유가 생겼을 때
        // 서비스를 이어서 실행할 것인지 여부를 나타내며, 총 미리 선언된 3가지 정수가 있다.

        // START_NOT_STICKY : 값이 리턴된 후, 시스템이 서비스를 종료시켰다면, 다시 서비스가 실행될 수 있는 여건이 되더라도 서비스를 다시 생성하지 않습니다.

        // START_STICKY : onStartCommand()에서 이 값이 리턴된 후, 시스템이 서비스를 종료시켰다면, 다시 서비스가 실행될 수 있는 여건이 되었을 때,
        //                서비스를 생성하고 onStartCommand()를 호출해 줍니다 intent 값이 null로 되서 초기화 된다.

        // START_REDELIVER_INTENT : START_STICKY와 마찬가지로 Service가 종료 되었을 경우 시스템이 다시 Service를 재시작 시켜 주지만
        //                          intent 값을 그대로 유지 시켜 줍니다.



        return START_NOT_STICKY;
    }


    // 서비스가 더이상 사용되지 않아 종료될 때 호출되는 콜백 메소드입니다.
    // 이 메소드는 서비스의 생명주기에서 가장 마지막에 호출되는 콜백 메소드이기 때문에,
    // 여기에서는 서비스에서 사용하던 리소스들(쓰레드, 등록된 리스너, 리시버 등)을 모두 정리해줘야(clean up) 합니다.
    @Override
    public void onDestroy() {
        super.onDestroy();
    }



    // 마치 클라이언트-서버 와 같이 동작을 하는데 서비스가 서버 역할을 한다.
    // 결론: 서비스가 돌아가는 동안 지속적으로 액티비티와 커뮤니케이션 하기 위해 사용
    // 또한 bind타입 Service는 앱 내부의 기능을 외부로 제공할 때 사용한다.
    //  바인딩을 원하지 않으면 null 리턴
    // 앱 간의 통신 지원 가능하게 해줌 리턴값을 받을수 있다.


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }




    private void NotificationSomethings(int notifyID) {
        NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        // 푸쉬 알림 터치 시 이동할 클래스 설정
        Intent notificationIntent = new Intent(this, MainActivity.class);
        // 기존에 쌓여있던 스택을 모두 없애고 task를 새로 만든다. (activity를 새로 만든다.)
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK) ;

        // PendingIntent는 Intent를 가지고 있는 클래스로, 기본 목적은 다른 애플리케이션(다른 프로세스)의
        // 권한을 허가하여 가지고 있는 Intent를 마치 본인 앱의 프로세스에서 실행하는 것처럼 사용하게 하는 것입니다.
        // Notification으로 작업을 수행할 때 인텐트가 실행되도록 합니다.
        // Notification은 안드로이드 시스템의 NotificationManager가 Intent를 실행합니다.
        // 즉 다른 프로세스에서 수행하기 때문에 Notification으로 Intent수행시 PendingIntent의 사용이 필수 입니다.
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,  PendingIntent.FLAG_UPDATE_CURRENT);


        // NotificationCompat는 푸쉬 알림을 만드는 최신버전의 라이브러리리
        // 생성자의 경우 채널 ID를 제공해야 한다.
        // Android 8.0(API 수준 26) 이상에서는 호환성을 유지하기 위해 필요하지만 이전 버전에서는 무시된다.
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, SMARTFARM_CHANNEL_ID)
                .setContentTitle("화재가 감지되었습니다.")  // 표시할 제목
                .setPriority(Notification.PRIORITY_HIGH) // 알람의 우선순위 최고
                .setContentIntent(pendingIntent) // 사용자가 노티피케이션을 탭시 pendingIntent로 이동하도록 설정
                .setAutoCancel(true); // 푸쉬 알림을 터치하고 난 후에는 지우는 옵션


        //OREO API 26 이상에서는 푸쉬알림 생성 시 채널 필요
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            builder.setSmallIcon(R.drawable.ic_launcher_foreground); //mipmap 사용시 Oreo 이상에서 시스템 UI 에러남


        }else builder.setSmallIcon(R.mipmap.ic_launcher); // Oreo 이하에서 mipmap 사용하지 않으면 Couldn't create icon: StatusBarIcon 에러남

        // null 아니라는걸 확인
        assert notificationManager != null;
        notificationManager.notify(notifyID, builder.build()); // 고유숫자로 노티피케이션 동작시킴

        if(notifyID==123) {
            notificationManager.cancel(notifyID);
        }
    }

    // 푸쉬 알림의 채널 생성 (한번만 만들면 된다.)
    private void creatChannel(){

        CharSequence channelName  = "smartfarm channel";
        String description = "camera detection";
        int importance = NotificationManager.IMPORTANCE_HIGH; // 긴급: 알림음이 울리며 헤드업 알림으로 표시됩니다.

        NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel(SMARTFARM_CHANNEL_ID, channelName, importance);
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);



            // 노티피케이션 채널을 시스템에 등록
            assert notificationManager != null;

            notificationManager.createNotificationChannel(channel);

        }
    }




    // webcam 1프레임 캡쳐
    private Bitmap webCameraCapture() {

        webImg_bitmap=null;

        Thread tr = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //서버에 올려둔 이미지 URL
                    URL url = new URL("http://192.168.0.35:8889/out.jpg");

                    //Web에서 이미지 가져온 후 ImageView에 지정할 Bitmap 만들기
                    /* URLConnection 생성자가 protected로 선언되어 있으므로
                     개발자가 직접 HttpURLConnection 객체 생성 불가 */
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                    /* openConnection()메서드가 리턴하는 urlConnection 객체는
                    HttpURLConnection의 인스턴스가 될 수 있으므로 캐스팅해서 사용한다*/


                    conn.setDoInput(true); //Server 통신에서 입력 가능한 상태로 만듦
                    conn.connect(); //연결된 곳에 접속할 때 (connect() 호출해야 실제 통신 가능함)

                    InputStream is = conn.getInputStream(); //inputStream 값 가져오기

                    webImg_bitmap = BitmapFactory.decodeStream(is); // Bitmap으로 반환

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        });

        tr.start();

        try {
            // 해당 쓰레드가 멈출때까지 멈춤
            tr.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return webImg_bitmap;
    }

    // 비트맵 파일로 변환  (변환할 사진, 경로)
    private File SaveBitmapToFile(Bitmap bitmap, String imgName) {
        // 외부 저장소의 캐시 디렉터리에 temp라는 파일 객체 생성
        File file = new File(this.getExternalCacheDir(), imgName);
        OutputStream out = null;
        try {
            // 빈 파일생성
            file.createNewFile();
            out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                // outstream 닫기
                out.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        return file;
    }

//  S3 사진 업로드 함수
    private void awsS3Upload(File webImg_file, String img_name){
        // AWS 자격인증을 얻는 코드
        // Cognito를 이용하면 개발자 인증서를 앱에 직접 심지 않아도 되어 apk가 털려서 인증서가 유출 될 위험이 없다.
        // 개발자 인증 자격 증명을 사용하면 를 사용하여 사용자 데이터를 동기화하고 AWS 리소스에 액세스하면서도
        // 자신의 기존 인증 프로세스를 통해 사용자를 등록 및 인증할 수 있다.

        credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                "ap-northeast-2:6d58538f-d438-48f3-bd0e-dc7455119ea3", // 자격 증명 풀 ID
                Regions.AP_NORTHEAST_2  // 물리적인 저장 위치 서울
        );


        // AWS s3를 이용하기 위해 생성된 인증 자격을 통하여 객체를 생성하고
        // 데이터를 송수신 하기 위해 TransferUtility 객체를 생성한다.
        AmazonS3 s3 = new AmazonS3Client(credentialsProvider);
        TransferUtility transferUtility = new TransferUtility(s3, getApplicationContext());

        // upload 메서드가 리턴하는 걸 TransferObserver라는 객체에다가 던져준 다음 이걸로
        // TransferListener를 만들면 업로드 현황을 모니터링하면서 몇 퍼센트 업로드가 완료됐는지,
        // 또는 무엇 때문에 업로드에 실패했는지 등을 알 수가 있다.

        uploadObserver = transferUtility.upload(
                BUCKET_NAME,    // 업로드할 버킷 이름
                "image/"+img_name,    // 버킷에 저장할 파일의 이름 확장자명도 붙여줘야댐 png (이름이 key로 쓰임)
                FwebImg_Resize       // 버킷에 저장할 파일
        );

        //전송 상태 또는 진행률이 변경 될 때 알림을받는 데 사용되는 리스너
        uploadObserver.setTransferListener(new TransferListener() {

            // 전송 상태가 변경 될 때 호출됩니다.
            @Override
            public void onStateChanged(int id, TransferState state) {
                if (state == TransferState.COMPLETED) {
                    // 람다 함수 호출
                    awsLambdaConnect(imgName);
                }
            }

            // 전송 중일떄 호출
            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {

            }

            // 전송 중 에러시
            @Override
            public void onError(int id, Exception ex) {

            }
        });

    }

    public void awsLambdaConnect(String img_name){


//      Lambda 프록시를 인스턴스화하는 데 사용할 LambdaInvokerFactory를 생성합니다.

        LambdaInvokerFactory factory = new LambdaInvokerFactory(this.getApplicationContext(),
                Regions.AP_NORTHEAST_2 , credentialsProvider);

// 기본 Json 데이터 바인더를 사용하여 Lambda 프록시 객체를 생성
// 구현하여 자체 데이터 바인더를 제공 할 수 있음
// LambdaDataBinder.
        final LambdaFuncInterface myInterface = factory.build(LambdaFuncInterface.class);

        final RequestClass request = new RequestClass(img_name);

// Lambda 함수 호출은 네트워크 호출을 발생시킵니다.
// 메인 스레드에서 호출되지 않았는지 확인합니다.
        new AsyncTask<RequestClass, Void, ResponseClass>() {
            @Override
            protected ResponseClass doInBackground(RequestClass... params) {
                // invoke "echo" method. In case it fails, it will throw a
                // LambdaFunctionException.
                try {
                    return myInterface.fireDetection(params[0]);
                } catch (LambdaFunctionException lfe) {
                    Log.e("Tag", "Failed to invoke echo", lfe);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(ResponseClass result) {
                if (result == null) {
                    return;
                }

                Log.d("결과", ""+result.getBody());

                if(result.getBody().equals("\"fire\"")){
                    NotificationSomethings(444);
                }

            }
        }.execute(request);
    }


//  이미지 크기 썸네일화
    private File ImgResize(File webImg_file){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;

        Bitmap bitmap= BitmapFactory.decodeFile(webImg_file.getPath(), options);

        int width = 150; // 축소시킬 너비
        int height = 150; // 축소시킬 높이
        float bmpWidth = bitmap.getWidth();
        float bmpHeight = bitmap.getHeight();

        if (bmpWidth > width) {
            // 원하는 너비보다 클 경우의 설정
            float mWidth = bmpWidth / 100;
            float scale = width/ mWidth;
            bmpWidth *= (scale / 100);
            bmpHeight *= (scale / 100);
        } else if (bmpHeight > height) {
            // 원하는 높이보다 클 경우의 설정
            float mHeight = bmpHeight / 100;
            float scale = height/ mHeight;
            bmpWidth *= (scale / 100);
            bmpHeight *= (scale / 100);
        }

        Bitmap resizedBmp = Bitmap.createScaledBitmap(bitmap, (int) bmpWidth, (int) bmpHeight, true);

        File ImgResize = SaveBitmapToFile(resizedBmp, "temp.png");

        return ImgResize;
    }




}