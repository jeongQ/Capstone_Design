package com.example.shinseungchan.ssd.views;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.shinseungchan.ssd.R;
import com.example.shinseungchan.ssd.nertwork.serverUrl;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 *  회원가입
 */

public class SignupActivity extends AppCompatActivity {

    public static final String ADDRESS = "/login/signup";

    serverUrl url = new serverUrl();

    Toolbar toolbar;
    EditText name_text; // 이름
    RadioGroup radioGroup; // 성별
    EditText year_text, month_text, day_text; // 생년월일
    EditText id_text; // 아이디
    EditText pw_text, pw2_text; // 비밀번호
    ConstraintLayout mainLayout; // 바탕 레이아웃

    String name, year, month, day, id, pw, pw2;
    String sex = "";

    //데이터 보낼 클래스
    public SendData mSendData = null;
    public String signup_msg = null;
    public String return_msg = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        name_text = (EditText) findViewById(R.id.name_edittext);
        radioGroup = (RadioGroup)findViewById(R.id.radioGroup);
        radioGroup.setOnCheckedChangeListener(radioGroupButtonChangeListener);
        year_text = (EditText)findViewById(R.id.year_edittext);
        month_text = (EditText)findViewById(R.id.month_edittext);
        day_text = (EditText)findViewById(R.id.day_edittext);
        id_text = (EditText)findViewById(R.id.id_edittext);
        pw_text = (EditText)findViewById(R.id.pw_edittext);
        pw2_text = (EditText)findViewById(R.id.pw2_edittext);
        mainLayout = (ConstraintLayout) findViewById(R.id.main_layout) ;

        // 완료버튼
        Button signup_btn = (Button)findViewById(R.id.signup_btn);
        signup_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                name = name_text.getText().toString();
                year = year_text.getText().toString();
                month = month_text.getText().toString();
                day = day_text.getText().toString();
                id = id_text.getText().toString();
                pw = pw_text.getText().toString();
                pw2 = pw2_text.getText().toString();

                if(name.isEmpty() || year.isEmpty() || month.isEmpty() || day.isEmpty() || sex.equals("") || id.isEmpty() || pw.isEmpty() || pw2.isEmpty()) // 정보가 다 입력되지 않았을 시
                    Toast.makeText(getBaseContext(), "입력사항을 완성하세요", Toast.LENGTH_LONG).show();
                else {
                    if(!pw.equals(pw2)) { // 비밀번호가 일치하지 않을 시
                        Toast.makeText(getBaseContext(), "비밀번호가 일치하지 않습니다", Toast.LENGTH_LONG).show();
                    }
                    else {
                        signup_msg = "signup:"+name+":"+year+month+day+":"+sex+":"+id+":"+pw;
                        // SendData 클래스 생성
                        mSendData = new SendData();
                        // 패킷 보냄
                        mSendData.start();
                        try {
                            Thread.sleep(1000);
                        }
                        catch (InterruptedException e) {
                            Log.d("회원가입", "error : ", e);
                        }

                        Toast.makeText(getBaseContext(), "회원가입에 성공했습니다", Toast.LENGTH_LONG).show();
                        finish();
                    }
                }
            }
        });

        // 바탕화면 클릭시 키보드 내림
        mainLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(name_text.getWindowToken(), 0);
                imm.hideSoftInputFromWindow(year_text.getWindowToken(), 0);
                imm.hideSoftInputFromWindow(month_text.getWindowToken(), 0);
                imm.hideSoftInputFromWindow(day_text.getWindowToken(), 0);
                imm.hideSoftInputFromWindow(id_text.getWindowToken(), 0);
                imm.hideSoftInputFromWindow(pw_text.getWindowToken(), 0);
                imm.hideSoftInputFromWindow(pw2_text.getWindowToken(), 0);
            }
        });
        // Toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    // 라디오 그룹 클릭 리스너
    RadioGroup.OnCheckedChangeListener radioGroupButtonChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
            if(i == R.id.man_radioButton)
                sex = "male";
            else if(i == R.id.woman_radioButton)
                sex="female";
        }
    };

    //데이터 보내는 쓰레드 클래스
    class SendData extends Thread {
        public void run() {
            try {
                ///UDP 통신용 소켓 생성
                DatagramSocket socket = new DatagramSocket();
                //서버 주소 변수
                InetAddress serverAddr = InetAddress.getByName(url.getsIP());
                byte[] buf = (signup_msg).getBytes();
                //패킷으로 변경
                DatagramPacket packet = new DatagramPacket(buf, buf.length, serverAddr, url.getsPORT());
                Log.d("통신", "send packet : " + signup_msg);
                //패킷 전송!
                socket.send(packet);
                Log.d("통신", "send...");
                //데이터 수신 대기
                socket.receive(packet);
                Log.d("통신", "receive...");
                //데이터 수신되었다면 문자열로 변환
                return_msg = new String(packet.getData());
                Log.d("통신", "receive packet : " + return_msg);
            }
            catch (Exception e) {
                Log.d("통신", "error : ", e);
            }
        }
    }

    // toolbar 클릭이벤트
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:{ // 뒤로가기 버튼
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }
}