package com.example.shinseungchan.ssd.views;

import android.content.Context;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.shinseungchan.ssd.R;
import com.example.shinseungchan.ssd.handler.BackPressCloseHandler;
import com.example.shinseungchan.ssd.nertwork.serverUrl;
import com.example.shinseungchan.ssd.router.Router;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;


/**
 *  로그인
 */

public class LoginActivity extends AppCompatActivity {

    public static final String ADDRESS = "/login";

    Router router = new Router(this);
    serverUrl url = new serverUrl();
    private BackPressCloseHandler backPressCloseHandler;

    EditText id_text; // 아이디
    EditText pw_text; // 비밀번호
    Button login_btn; // 로그인 버튼
    Button signup_btn; // 회원가입 버튼
    ConstraintLayout mainLayout; // 바탕 레이아웃

    public String id, pw;
    //데이터 보낼 클래스
    public SendData mSendData = null;
    public String login_msg = null;
    public String return_msg = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        backPressCloseHandler = new BackPressCloseHandler(this);

        id_text = (EditText)findViewById(R.id.id_edittext);
        pw_text = (EditText)findViewById(R.id.pw_edittext);
        login_btn = (Button)findViewById(R.id.login_btn);
        signup_btn = (Button)findViewById(R.id.signup_btn);
        mainLayout = (ConstraintLayout) findViewById(R.id.mainLayout);

        // 로그인 버튼
        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                id = id_text.getText().toString();
                pw = pw_text.getText().toString();

                if(id.isEmpty() || pw.isEmpty()) { // 정보가 다 입력되지 않았을 시
                    Toast.makeText(getBaseContext(), "아이디 또는 비밀번호를 입력하세요", Toast.LENGTH_LONG).show();
                }
                else {
                    login_msg = "login:" + id + ":" + pw;
                    // SendData 클래스 생성
                    mSendData = new SendData();
                    // 패킷 보냄
                    mSendData.start();
                    try {
                        Thread.sleep(1000);
                    }
                    catch (InterruptedException e) {
                        Log.d("로그인", "error : ", e);
                    }
                    Toast.makeText(getBaseContext(), "로그인 되었습니다", Toast.LENGTH_LONG).show();
                    router.openActivity(MainActivity.ADDRESS);
                    finish();
                }
            }
        });
        // 회원가입 버튼
        signup_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                router.openActivity(SignupActivity.ADDRESS);
            }
        });

        // 바탕화면 클릭시 키보드 내림
        mainLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(id_text.getWindowToken(), 0);
                imm.hideSoftInputFromWindow(pw_text.getWindowToken(), 0);
            }
        });
    }

    // 데이터 보내는 쓰레드 클래스
    class SendData extends Thread {
        public void run() {
            try {
                ///UDP 통신용 소켓 생성
                DatagramSocket socket = new DatagramSocket();
                //서버 주소 변수
                InetAddress serverAddr = InetAddress.getByName(url.getsIP());
                /** 보내는 패킷 **/
                //보낼 데이터 생성
                byte[] buf = (login_msg).getBytes();
                //패킷으로 변경
                DatagramPacket packet = new DatagramPacket(buf, buf.length, serverAddr, url.getsPORT());
                Log.d("통신", "send packet : " + login_msg);
                //패킷 전송!
                socket.send(packet);
                Log.d("통신", "send...");
                /** 받는 패킷 **/
                //받는 데이터 생성
                byte[] buf2 = (" ").getBytes();
                //패킷으로 변경
                DatagramPacket packet2 = new DatagramPacket(buf2, buf2.length, serverAddr, url.getsPORT());
                //데이터 수신 대기
                socket.receive(packet2);
                Log.d("통신", "receive...");
                //데이터 수신되었다면 문자열로 변환
                return_msg = new String(packet2.getData()); // 성공 : 1, 실패 0
                Log.d("통신", "receive packet : " + return_msg);
            }
            catch (Exception e) {
                Log.d("통신", "error : ", e);
            }
        }
    }

    // 뒤로 두번 누르면 종료
    @Override
    public void onBackPressed() {
        backPressCloseHandler.onBackPressed();
    }
}