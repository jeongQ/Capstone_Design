package com.example.shinseungchan.ssd.router;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import com.example.shinseungchan.ssd.controller.CameraActivity;
import com.example.shinseungchan.ssd.views.LoginActivity;
import com.example.shinseungchan.ssd.views.MainActivity;
import com.example.shinseungchan.ssd.views.SignupActivity;

/**
 *  라우터
 */

public class Router {

    private AppCompatActivity currentActivity;

    public Router(AppCompatActivity currentActivity) {
        this.currentActivity = currentActivity;
    }

    /** Activity 실행 **/
    public void openActivity(String address) {

        Intent intent;
        switch(address) {
            case LoginActivity.ADDRESS:
                intent = new Intent(currentActivity, LoginActivity.class);
                break;
            case SignupActivity.ADDRESS:
                intent = new Intent(currentActivity, SignupActivity.class);
                break;
            case CameraActivity.ADDRESS:
                intent = new Intent(currentActivity, CameraActivity.class);
                break;
            //TODO: other cases
            default:
                // malformed address
                //TODO: Report to backend error log system
                intent = new Intent(currentActivity, MainActivity.class);
        }
        currentActivity.startActivity(intent);
    }
}
