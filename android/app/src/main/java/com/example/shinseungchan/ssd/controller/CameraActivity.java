package com.example.shinseungchan.ssd.controller;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.example.shinseungchan.ssd.R;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

import static android.speech.tts.TextToSpeech.ERROR;

/**
 *  얼굴 인식 openCV
 */

public class CameraActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2{

    public static final String ADDRESS = "/main/camera";

    private CameraBridgeViewBase mOpenCvCameraView;
    private Mat matInput;
    private Mat matResult;
    private TextToSpeech tts;

    public native long loadCascade(String cascadeFileName );
    public native int detect(long cascadeClassifier_face, long cascadeClassifier_eye, long matAddrInput, long matAddrResult);
    public long cascadeClassifier_face = 0;
    public long cascadeClassifier_eye = 0;

    static {
        System.loadLibrary("opencv_java3");
        System.loadLibrary("native-lib");
    }

    private void copyFile(String filename) {

        String baseDir = Environment.getExternalStorageDirectory().getPath();
        String pathDir = baseDir + File.separator + filename;
        AssetManager assetManager = this.getAssets();
        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            inputStream = assetManager.open(filename);
            outputStream = new FileOutputStream(pathDir);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            inputStream.close();
            inputStream = null;
            outputStream.flush();
            outputStream.close();
            outputStream = null;
        }
        catch (Exception e) {

        }
    }

    private void read_cascade_file(){

        copyFile("haarcascade_frontalface_alt.xml");
        copyFile("haarcascade_eye_tree_eyeglasses.xml");
        cascadeClassifier_face = loadCascade( "haarcascade_frontalface_alt.xml");
        cascadeClassifier_eye = loadCascade( "haarcascade_eye_tree_eyeglasses.xml");
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_camera);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //퍼미션 상태 확인
            if (!hasPermissions(PERMISSIONS)) {
                //퍼미션 허가 안되어있다면 사용자에게 요청
                requestPermissions(PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            }
            else
                read_cascade_file(); //추가
        }
        else
            read_cascade_file(); //추가

        mOpenCvCameraView = (CameraBridgeViewBase)findViewById(R.id.activity_surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

        mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        mOpenCvCameraView.setCameraIndex(1); // front-camera(1),  back-camera(0)

        // tts 초기화
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != ERROR)
                    tts.setLanguage(Locale.KOREAN);
            }
        });
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this, mLoaderCallback);
        }
        else {
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        // openCV 메모리 해제
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
        // TTS 객체 메모리 해제
        if(tts != null){
            tts.stop();
            tts.shutdown();
            tts = null;
        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    int count = 0;
    int op = 0;
    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        matInput = inputFrame.rgba();

        if (matResult == null)
            matResult = new Mat(matInput.rows(), matInput.cols(), matInput.type());
        Core.flip(matInput, matInput, 1);

        int ret = detect(cascadeClassifier_face, cascadeClassifier_eye, matInput.getNativeObjAddr(), matResult.getNativeObjAddr());
        // 얼굴 인식이 됬으면
        if(ret != 1) {
            // 눈 떳을 경우
            if (ret > 0)
                count = 0;
            // 눈 감았을 경우
            if (ret <= 0)
                ++count;
        }
        // 눈 감았을 때 알림
        if(count >= 3) {
            if(op == 0) {
                op = 1;
                runOnUiThread(new Runnable() {
                    public void run() {
                        // 소리
                        String s = "졸음운전이 감지 되었습니다. 일어나세요.";
                        tts.setPitch(1.5f);
                        tts.speak(s+s+s+s+s+s+s+s+s+s+s+s+s+s+s+s+s+s+s+s, TextToSpeech.QUEUE_FLUSH, null);
                        // 다이얼로그
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(CameraActivity.this);
                        alertDialogBuilder.setTitle("졸음 인식");
                        alertDialogBuilder
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setMessage("일어나세요!!")
                                .setCancelable(false)
                                .setPositiveButton("기능 종료", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        // 프로그램을 종료한다
                                        CameraActivity.this.finish();
                                        tts.stop();
                                        op = 0;
                                    }
                                })
                                .setNegativeButton("확인", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        // 다이얼로그를 취소한다
                                        tts.stop();
                                        dialog.cancel();
                                        op = 0;
                                    }
                                });
                        // 다이얼로그 생성
                        AlertDialog alertDialog = alertDialogBuilder.create();
                        // 다이얼로그 보여주기
                        alertDialog.show();
                    }
                });
            }
        }
        return matResult;
    }

    //여기서부턴 퍼미션 관련 메소드
    static final int PERMISSIONS_REQUEST_CODE = 1000;
    String[] PERMISSIONS  = {"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE"};

    private boolean hasPermissions(String[] permissions) {

        int result;
        //스트링 배열에 있는 퍼미션들의 허가 상태 여부 확인
        for (String perms : permissions){
            result = ContextCompat.checkSelfPermission(this, perms);
            if (result == PackageManager.PERMISSION_DENIED){
                //허가 안된 퍼미션 발견
                return false;
            }
        }
        //모든 퍼미션이 허가되었음
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch(requestCode){
            case PERMISSIONS_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean cameraPermissionAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    // 기존 코드 주석처리
                    boolean writePermissionAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (!cameraPermissionAccepted || !writePermissionAccepted) {
                        showDialogForPermission("앱을 실행하려면 퍼미션을 허가하셔야합니다.");
                        return;
                    }
                    else
                        read_cascade_file();
                }
                break;
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void showDialogForPermission(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder( CameraActivity.this);
        builder.setTitle("알림");
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id){
                requestPermissions(PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                finish();
            }
        });
        builder.create().show();
    }
}