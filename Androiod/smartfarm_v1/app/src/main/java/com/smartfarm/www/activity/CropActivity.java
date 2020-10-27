package com.smartfarm.www.activity;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaFunctionException;
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaInvokerFactory;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.smartfarm.www.R;
import com.smartfarm.www.appInfo;
import com.smartfarm.www.service.DiseaseResponseclass;
import com.smartfarm.www.service.LambdaFuncInterface;
import com.smartfarm.www.service.RequestClass;
import com.smartfarm.www.service.ResponseClass;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;

public class CropActivity extends Fragment {

    View view;
    String imageFileName;
    TextView Disease_result, Crop_result, Nothing;
    File photoFile;
    private static final int REQUEST_IMAGE_CAPTURE = 672;   //사진파일을  Request코드
    private String imageFilePath;                           //사진이 저장되어있는 파일 경로
    private Uri photoUri;                                   //이미지 자원 식별자

    private final String BUCKET_NAME = "hotsix-smartfarm"; // S3 버킷 이름 (저장소 이름)
    private String imgName;  // 저장할 이미지의 이름

    private CognitoCachingCredentialsProvider credentialsProvider; // 자격 증명 풀
    private TransferObserver uploadObserver; // 파일 업로드 시 모니터링을 하기 위한 객체
    private File FwebImg_Resize; // 썸네일한 이미지 파일 (S3 올리고 사진을 지움)



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.crop_page, container, false);

        Disease_result = view.findViewById(R.id.disease_result);
        Crop_result = view.findViewById(R.id.crop_result);
        Nothing = view.findViewById(R.id.nothing);

        //권한체크
        //bulid.gradle(Module: app)부분에 dependencies에 implementation 'gun0912.ted:tedpermission:2.0.0'라고 선언되어있음
        //안드로이드에서 특정 권한을 물어볼 때 그 창을 쉽게 만들어 낼 수 있음
        TedPermission.with(getActivity().getApplicationContext())
                .setPermissionListener(permissionListener)        //권한이 허용되거나 거부되었을때의 행동을 지정함
                .setRationaleMessage("카메라 권한이 필요합니다.")  //권한을 확인할 때 사용자에게 어떤 메시지를 띄워줄것인지 정하는 것
                .setDeniedMessage("거부하셨습니다.")              //권한을 거부했을 시 나오는 메시지
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)     //어떤 권한을 체크할 지 가져오는 것
                .check();


        view.findViewById(R.id.btn_capture).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);    //카메라 애플리케이션으로 가도록 함
                Nothing.setText("");
                Disease_result.setText("");
                Crop_result.setText("잠시 기다려 주세요");
                //컴포넌트를 실행하지 못하면 앱 작동이 종료되므로 사전에 컴포넌트가 실행가능한지 여부를 판단
                //패키지매니저를 불러와 해당 인텐트가 갖는 컴포넌트가 사용가능한지 확인. 사용가능하다면 null이 아닌 값을 반환
                if(intent.resolveActivity(getActivity().getPackageManager()) != null) {

                    photoFile = null;
                    try {
                        photoFile = createImageFile();
                        //Toast.makeText(getContext(), "test" + photoFile, Toast.LENGTH_SHORT).show();


                        //Disease_result.setText("" + imageFileName);
                    } catch (IOException e) {}
                    //Toast.makeText(getContext(), "test" + photoFile, Toast.LENGTH_SHORT).show();
                    if(photoFile != null) {     //생성한 임시파일이 존재한다면
                        //Android 7.0 이상부터 파일공유 정책이 변경됨. 개인 파일의 보안을 강화하기 위해 개인 디렉토리의 액세스를 제한하여
                        //안드로이드에서는 FileProvider 사용을 권장하고 있다.

                        photoUri = FileProvider.getUriForFile(getActivity().getApplicationContext(), getActivity().getPackageName(), photoFile);    //임시파일의 Uri를 가져옴
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);     //요청 된 이미지 또는 비디오를 저장하는 데 사용할 콘텐츠 확인자 Uri를 나타내는 데 사용되는 Intent-extra의 이름
                        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);

                    }
                }


            }
        });

        return view;
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyMMdd_HHmmSS").format(new Date());      //현재시간을 문자열로 반환
        imageFileName = timeStamp;                                           //파일이름의 형식 지정. 끝에 언더바하는 이유는 사진 저장할 때 뒤에 숫자가 더 붙음
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);                      //외부저장소 고유영역중 사진디렉토리의 주소를 불러옴
        File image = File.createTempFile(imageFileName, ".png", storageDir);                 //임시파일 생성
        imageFilePath = image.getAbsolutePath();                                                    //임시파일의 절대경로 불러옴
        return image;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bitmap bitmap = BitmapFactory.decodeFile(imageFilePath);    //찍은 사진파일을 비트맵으로 변환하여 가져옴

            FwebImg_Resize = ImgResize(photoFile);
            imageFileName = imageFileName + ".png";
            awsS3Upload(FwebImg_Resize, imageFileName);

            ExifInterface exif = null;      //사진에 대한 정보를 가질 수 있는 객체

            try {
                exif = new ExifInterface(imageFilePath);    //찍은 사진의 정보 가져오기
            } catch (IOException e) {
                e.printStackTrace();
            }

            int exifOrientation;
            int exifDegree;

            if(exif != null) {
                exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);    //사진에 대한 정보 가져오기
                exifDegree = exifOrientationToDegrees(exifOrientation);     //사진이 회전되어있는 각도를 가져온다
            } else {
                exifDegree = 0;
            }

            ((ImageView) view.findViewById(R.id.iv_result)).setImageBitmap(rotate(bitmap, exifDegree));  //사진을 회전하여 이미지뷰에 표시
        }
    }

    private int exifOrientationToDegrees(int exifOrientation) {
        if(exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        } else {
            return 0;
        }
    }

    private Bitmap rotate(Bitmap bitmap, float degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    PermissionListener permissionListener = new PermissionListener() {
        @Override
        public void onPermissionGranted() {     //권한이 허용이 되었을 때 작업을 적어주는 것
            Toast.makeText(getContext(), "권한이 허용됨", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPermissionDenied(ArrayList<String> deniedPermissions) {       //권한이 거부되었을 때 작업을 적어주는 것
            Toast.makeText(getContext(), "권한이 거부됨", Toast.LENGTH_SHORT).show();
        }
    };

    //-----------S3 서버 접근 및 반환 함수


    //  S3 사진 업로드 함수
    private void awsS3Upload(File webImg_file, String img_name){
        // AWS 자격인증을 얻는 코드
        // Cognito를 이용하면 개발자 인증서를 앱에 직접 심지 않아도 되어 apk가 털려서 인증서가 유출 될 위험이 없다.
        // 개발자 인증 자격 증명을 사용하면 를 사용하여 사용자 데이터를 동기화하고 AWS 리소스에 액세스하면서도
        // 자신의 기존 인증 프로세스를 통해 사용자를 등록 및 인증할 수 있다.

        credentialsProvider = new CognitoCachingCredentialsProvider(
                getActivity().getApplicationContext(),
                "ap-northeast-2:6d58538f-d438-48f3-bd0e-dc7455119ea3", // 자격 증명 풀 ID
                Regions.AP_NORTHEAST_2  // 물리적인 저장 위치 서울
        );



        // AWS s3를 이용하기 위해 생성된 인증 자격을 통하여 객체를 생성하고
        // 데이터를 송수신 하기 위해 TransferUtility 객체를 생성한다.
        AmazonS3 s3 = new AmazonS3Client(credentialsProvider);
        TransferUtility transferUtility = new TransferUtility(s3, getActivity().getApplicationContext());

        // upload 메서드가 리턴하는 걸 TransferObserver라는 객체에다가 던져준 다음 이걸로
        // TransferListener를 만들면 업로드 현황을 모니터링하면서 몇 퍼센트 업로드가 완료됐는지,
        // 또는 무엇 때문에 업로드에 실패했는지 등을 알 수가 있다.

        uploadObserver = transferUtility.upload(
                BUCKET_NAME,    // 업로드할 버킷 이름
                "user/"+appInfo.S3userID+"/image_crop/image/"+img_name,    // 버킷에 저장할 파일의 이름 확장자명도 붙여줘야댐 png (이름이 key로 쓰임)
                FwebImg_Resize       // 버킷에 저장할 파일
        );

        //전송 상태 또는 진행률이 변경 될 때 알림을받는 데 사용되는 리스너
        uploadObserver.setTransferListener(new TransferListener() {

            // 전송 상태가 변경 될 때 호출됩니다.
            @Override
            public void onStateChanged(int id, TransferState state) {
                if (state == TransferState.COMPLETED) {
                    // 람다 함수 호출
                    awsLambdaConnect(imageFileName);
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

        ClientConfiguration client = new ClientConfiguration ();
        client.setConnectionTimeout(60*1000);
        client.setSocketTimeout(60*1000);
        LambdaInvokerFactory factory = new LambdaInvokerFactory(getActivity().getApplicationContext(),
                Regions.AP_NORTHEAST_2 , credentialsProvider, client);



// 기본 Json 데이터 바인더를 사용하여 Lambda 프록시 객체를 생성
// 구현하여 자체 데이터 바인더를 제공 할 수 있음
// LambdaDataBinder.
        final LambdaFuncInterface myInterface = factory.build(LambdaFuncInterface.class);

        final RequestClass request = new RequestClass(appInfo.S3userID, img_name);

// Lambda 함수 호출은 네트워크 호출을 발생시킵니다.
// 메인 스레드에서 호출되지 않았는지 확인합니다.
        new AsyncTask<RequestClass, Void, DiseaseResponseclass>() {
            @Override
            protected DiseaseResponseclass doInBackground(RequestClass... params) {
                // invoke "echo" method. In case it fails, it will throw a
                // LambdaFunctionException.
                try {
                    return myInterface.crop_classification_v2(params[0]);
                } catch (LambdaFunctionException lfe) {
                    Log.e("Tag", "Failed to invoke echo", lfe);
                    return null;
                }
            }

            String[] test = {"다시 찍어주세요", "콩", "배추", "고추", "벼", "깨", "딸기"};
            String[][] test_d = {{"다시 찍어주세요"}, {"정상", "콩 탄저병", "콩 불마름병","균핵병"}, {"정상", "뿌리 혹병", "노균병", "무름병"},
                    {"정상", "탄저병", "칼슘 부족", "고추 역병"},{"정상", "벼 흰잎마름병", "이삭누룩병", "잎집무늬마름병" },
                    {"정상","마름병", "식물 역병", "흰가루병"}, {"정상", "딸기 탄저병", "잿빛곰팡이병", "잎마름병"}};

            @Override
            protected void onPostExecute(DiseaseResponseclass result) {
                if (result == null) {
                    return;
                }

                //test[result.getRe_vegetable()];

                Log.d("결과", "" + test[result.getRe_vegetable()] + ' ' + test_d[result.getRe_vegetable()][result.getRe_disease()]);
                Crop_result.setText("작물 : "  + test[result.getRe_vegetable()]);
                Disease_result.setText("예측 작물 질병 : " + test_d[result.getRe_vegetable()][result.getRe_disease()]);

            }
        }.execute(request);
    }

    private File ImgResize(File webImg_file){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;

        Bitmap bitmap= BitmapFactory.decodeFile(webImg_file.getPath(), options);

        int width = 150; // 축소시킬 너비
        int height = 150; // 축소시킬 높이
        float rezWidth = bitmap.getWidth();
        float rezHeight = bitmap.getHeight();

        if (rezWidth > width) {
            // 원하는 너비보다 클 경우의 설정
            float mWidth = rezWidth / 100;
            float scale = width/ mWidth;
            rezWidth *= (scale / 100);
            rezHeight *= (scale / 100);
        } else if (rezHeight > height) {
            // 원하는 높이보다 클 경우의 설정
            float mHeight = rezHeight / 100;
            float scale = height/ mHeight;
            rezWidth *= (scale / 100);
            rezHeight *= (scale / 100);
        }

        Bitmap resizedBmp = Bitmap.createScaledBitmap(bitmap, (int) rezWidth, (int) rezHeight, true);

        File ImgResize = SaveBitmapToFile(resizedBmp, "temp.png");

        return ImgResize;
    }

    private File SaveBitmapToFile(Bitmap bitmap, String imgName) {
        // 외부 저장소의 캐시 디렉터리에 temp라는 파일 객체 생성

        Log.d("SaveBitmapToFile","시작");
        File file = new File(getActivity().getExternalCacheDir(), imgName);
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

        Log.d("SaveBitmapToFile","끝");
        return file;
    }

}
