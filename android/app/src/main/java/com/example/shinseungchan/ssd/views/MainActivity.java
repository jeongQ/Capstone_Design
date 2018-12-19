package com.example.shinseungchan.ssd.views;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.shinseungchan.ssd.R;
import com.example.shinseungchan.ssd.controller.CameraActivity;
import com.example.shinseungchan.ssd.handler.BackPressCloseHandler;
import com.example.shinseungchan.ssd.nertwork.serverUrl;
import com.example.shinseungchan.ssd.router.Router;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.speech.tts.TextToSpeech.ERROR;

/**
 *  메인화면
 */

public class MainActivity extends AppCompatActivity {

    public static final String ADDRESS = "/main";

    Router router = new Router(this);
    serverUrl url = new serverUrl();
    private BackPressCloseHandler backPressCloseHandler;

    Toolbar toolbar;
    ImageButton brain_btn; // 뇌파 측정 버튼
    ImageButton co2_btn; // co2 측정 버튼
    ImageButton camera_btn; // 카메라 버튼
    ImageButton heartbeat_btn; // 심박수 측정 버튼
    LineChart brain_chart, co2_chart, heart_chart; // 차트

    //데이터 보낼 클래스
    public String brain_send_msg = null;
    public String brain_return_msg = null;
    public String co2_send_msg = null;
    public String co2_return_msg = null;
    public String heart_send_msg = null;
    public String heart_return_msg = null;

    // 그래프 속성
    int DATA_RANGE = 30;
    ArrayList<Entry> brain_entry = new ArrayList<Entry>(); // 뇌파
    ArrayList<Entry> co2_entry = new ArrayList<Entry>(); // co2
    ArrayList<Entry> heart_entry = new ArrayList<Entry>(); // 심박수
    LineDataSet brain_dataSet, co2_dataSet, heart_dataSet;

    // tts
    private TextToSpeech brain_tts;
    private TextToSpeech co2_tts;
    private TextToSpeech heart_tts;

    // 모드 선택
    boolean brain_mode;
    boolean co2_mode;
    boolean heart_mode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        backPressCloseHandler = new BackPressCloseHandler(this);

        toolbar = (Toolbar)findViewById(R.id.toolbar);
        brain_btn = (ImageButton)findViewById(R.id.brain_btn);
        co2_btn = (ImageButton)findViewById(R.id.co2_btn);
        camera_btn = (ImageButton)findViewById(R.id.camera_btn);
        heartbeat_btn = (ImageButton)findViewById(R.id.heartbeat_btn);
        brain_chart = (LineChart)findViewById(R.id.brain_chart);
        co2_chart = (LineChart)findViewById(R.id.co2_chart);
        heart_chart = (LineChart)findViewById(R.id.heart_chart);

        brain_mode = false;
        co2_mode = false;
        heart_mode = false;

        // tts 초기화
        brain_tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != ERROR)
                    brain_tts.setLanguage(Locale.KOREAN);
            }
        });
        // tts 초기화
        co2_tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != ERROR)
                    co2_tts.setLanguage(Locale.KOREAN);
            }
        });
        // tts 초기화
        heart_tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != ERROR)
                    heart_tts.setLanguage(Locale.KOREAN);
            }
        });

        // 뇌파 측정 버튼
        brain_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!brain_mode) {
                    Toast.makeText(getBaseContext(), "뇌파측정 모드 실행", Toast.LENGTH_LONG).show();
                    brain_send_msg = "brain_start";
                    brain_mode = true;
                    brainChartInit();
                    brainThreadStart();
                }
                else {
                    Toast.makeText(getBaseContext(), "뇌파측정 모드 종료", Toast.LENGTH_LONG).show();
                    brain_send_msg = "brain_stop";
                    brain_mode = false;
                    brainChartClear();
                    brainThreadStart();
                    brain_op = 0;
                }
            }
        });
        // co2 측정 버튼
        co2_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!co2_mode) {
                    Toast.makeText(getBaseContext(), "co2 측정 모드 실행", Toast.LENGTH_LONG).show();
                    co2_send_msg = "co2_start";
                    co2_mode = true;
                    co2ChartInit();
                    co2ThreadStart();
                }
                else {
                    Toast.makeText(getBaseContext(), "co2 측정 모드 종료", Toast.LENGTH_LONG).show();
                    co2_send_msg = "co2_stop";
                    co2_mode = false;
                    co2ChartClear();
                    co2ThreadStart();
                    co2_op = 0;
                }
            }
        });
        // 눈동자 인식 버튼
        camera_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getBaseContext(), "안구인식 모드 실행", Toast.LENGTH_LONG).show();
                Toast.makeText(getBaseContext(), "화면을 가로로 돌려주세요", Toast.LENGTH_LONG).show();
                router.openActivity(CameraActivity.ADDRESS);
            }
        });
        // 심박수 측정 버튼
        heartbeat_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!heart_mode) {
                    Toast.makeText(getBaseContext(), "심박수 측정 모드 실행", Toast.LENGTH_LONG).show();
                    heart_send_msg = "heart_start";
                    heart_mode = true;
                    heartChartInit();
                    heartThreadStart();
                    heart_op = 0;
                }
                else {
                    Toast.makeText(getBaseContext(), "심박수 측정 모드 종료", Toast.LENGTH_LONG).show();
                    heart_send_msg = "heart_stop";
                    heart_mode = false;
                    heartChartClear();
                    heartThreadStart();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // TTS 객체 메모리 해제
        if(brain_tts != null){
            brain_tts.stop();
            brain_tts.shutdown();
            brain_tts = null;
        }
        if(co2_tts != null){
            co2_tts.stop();
            co2_tts.shutdown();
            co2_tts = null;
        }
        if(heart_tts != null){
            heart_tts.stop();
            heart_tts.shutdown();
            heart_tts = null;
        }
    }

    /** --------- 그래프 초기화 --------- **/
    // 뇌파 그래프
    private void brainChartInit() {
        brain_chart.setAutoScaleMinMaxEnabled(true);
        for(int i=0; i<DATA_RANGE; i++) {
            brain_entry.add(new Entry(i, 0));
        }
        brain_dataSet = new LineDataSet(brain_entry, "뇌파수치");
        brain_dataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        brain_dataSet.setColor(Color.RED);
        brain_dataSet.setDrawValues(false);
        brain_dataSet.setDrawCircles(false);

        List<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(brain_dataSet);

        Description description = new Description();
        description.setText("");

        LineData data = new LineData(dataSets);
        brain_chart.setDescription(description);
        brain_chart.setData(data);
        brain_chart.invalidate();
    }
    // co2 그래프
    private void co2ChartInit() {
        co2_chart.setAutoScaleMinMaxEnabled(true);
        for(int i=0; i<DATA_RANGE; i++) {
            co2_entry.add(new Entry(i, 0));
        }
        co2_dataSet = new LineDataSet(co2_entry, "co2수치");
        co2_dataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        co2_dataSet.setColor(Color.BLUE);
        co2_dataSet.setDrawValues(false);
        co2_dataSet.setDrawCircles(false);

        List<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(co2_dataSet);

        Description description = new Description();
        description.setText("");

        LineData data = new LineData(dataSets);
        co2_chart.setDescription(description);
        co2_chart.setData(data);
        co2_chart.invalidate();
    }
    // 심박수 그래프
    private void heartChartInit() {
        heart_chart.setAutoScaleMinMaxEnabled(true);
        for(int i=0; i<DATA_RANGE; i++) {
            heart_entry.add(new Entry(i, 0));
        }
        heart_dataSet = new LineDataSet(heart_entry, "심박수");
        heart_dataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        heart_dataSet.setColor(Color.MAGENTA);
        heart_dataSet.setDrawValues(false);
        heart_dataSet.setDrawCircles(false);

        List<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(heart_dataSet);

        Description description = new Description();
        description.setText("");

        LineData data = new LineData(dataSets);
        heart_chart.setDescription(description);
        heart_chart.setData(data);
        heart_chart.invalidate();
    }

    /** -------- 차트 업데이트 -------- **/
    // 뇌파 그래프
    public void brainChartUpdate(int x) {
        if(brain_entry.size() > DATA_RANGE) {
            brain_entry.remove(0);
            for(int i=0; i<DATA_RANGE; i++) {
                brain_entry.get(i).setX(i);
            }
        }
        brain_entry.add(new Entry(brain_entry.size(), x));
        brain_dataSet.notifyDataSetChanged();
        brain_chart.notifyDataSetChanged();
        brain_chart.invalidate();
    }
    // co2 그래프
    public void co2ChartUpdate(int x) {
        if(co2_entry.size() > DATA_RANGE) {
            co2_entry.remove(0);
            for(int i=0; i<DATA_RANGE; i++) {
                co2_entry.get(i).setX(i);
            }
        }
        co2_entry.add(new Entry(co2_entry.size(), x));
        co2_dataSet.notifyDataSetChanged();
        co2_chart.notifyDataSetChanged();
        co2_chart.invalidate();
    }
    // 심박수 그래프
    public void heartChartUpdate(int x) {
        if(heart_entry.size() > DATA_RANGE) {
            heart_entry.remove(0);
            for(int i=0; i<DATA_RANGE; i++) {
                heart_entry.get(i).setX(i);
            }
        }
        heart_entry.add(new Entry(heart_entry.size(), x));
        heart_dataSet.notifyDataSetChanged();
        heart_chart.notifyDataSetChanged();
        heart_chart.invalidate();
    }

    /** --------- 차트 초기화 --------- **/
    // 뇌파 그래프
    public void brainChartClear() {
        brain_entry.clear();
        brain_chart.clear();
        brain_chart.invalidate();
    }
    // co2 그래프
    public void co2ChartClear() {
        co2_entry.clear();
        co2_chart.clear();
        co2_chart.invalidate();
    }
    // 심박수 그래프
    public void heartChartClear() {
        heart_entry.clear();
        heart_chart.clear();
        heart_chart.invalidate();
    }

    /** ------- 그래프 핸들러 ------- **/
    // 뇌파
    Handler brain_handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == 0) {
                try {
                    brainChartUpdate(Integer.parseInt(brain_return_msg));
                }
                catch (NumberFormatException e) {
                    Log.d("통신", "데이터 에러");
                }
            }
        }
    };
    // co2
    Handler co2_handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == 0) {
                try {
                    co2ChartUpdate(Integer.parseInt(co2_return_msg));
                }
                catch (NumberFormatException e) {
                    Log.d("통신", "데이터 에러");
                }
            }
        }
    };
    // 심박수
    Handler heart_handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == 0) {
                try {
                    heartChartUpdate(Integer.parseInt(heart_return_msg));
                }
                catch (NumberFormatException e) {
                    Log.d("통신", "데이터 에러");
                }
            }
        }
    };

    /** -------- 쓰레드 초기화 -------- **/
    int brain_op = 0;
    int co2_op = 0;
    int heart_op = 0;
    // 뇌파
    class brainThread extends Thread {
        @Override
        public void run() {
            try {
                ///UDP 통신용 소켓 생성
                DatagramSocket socket = new DatagramSocket();
                //서버 주소 변수
                InetAddress serverAddr = InetAddress.getByName(url.getsIP());
                /** 보내는 패킷 **/
                //보낼 데이터 생성
                byte[] buf = (brain_send_msg).getBytes();
                //패킷으로 변경
                DatagramPacket packet = new DatagramPacket(buf, buf.length, serverAddr, url.getsPORT());
                Log.d("통신", "brain send packet : " + brain_send_msg);
                /** 받는 패킷 **/
                //받는 데이터 생성
                byte[] buf2 = ("   ").getBytes();
                //패킷으로 변경
                DatagramPacket packet2 = new DatagramPacket(buf2, buf2.length, serverAddr, url.getsPORT());

                while (brain_mode) {
                    //패킷 전송!
                    socket.send(packet);
                    Log.d("통신", "brain packet send...");
                    //데이터 수신 대기
                    socket.receive(packet2);
                    Log.d("통신", "brain packet receive...");

                    //데이터 수신되었다면 문자열로 변환
                    String s = new String(packet2.getData());
                    brain_return_msg = "";
                    for(int i=0; i<s.length(); i++) {
                        if(s.charAt(i) != ' ')
                            brain_return_msg += s.charAt(i);
                    }

                    if(!brain_return_msg.equals("")) {
                        Log.d("통신", "brain receive packet : " + brain_return_msg);
                        brain_handler.sendEmptyMessage(0);
                        if(brain_op == 0) {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    // 알림
                                    if (Integer.parseInt(brain_return_msg) <= 10 && Integer.parseInt(brain_return_msg) > 0) {
                                        brain_op = 1;
                                        // 소리
                                        String alarm = "뇌파이상이 감지 되었습니다 일어나세요.";
                                        brain_tts.setPitch(1.5f);
                                        brain_tts.speak(alarm+alarm+alarm+alarm+alarm+alarm, TextToSpeech.QUEUE_FLUSH, null);
                                        // 다이얼로그
                                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                                        alertDialogBuilder.setTitle("졸음 주의");
                                        alertDialogBuilder
                                                .setIcon(android.R.drawable.ic_dialog_alert)
                                                .setMessage("눌러서 알림 해제")
                                                .setCancelable(false)
                                                .setNegativeButton("확인", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        // 다이얼로그를 취소한다
                                                        brain_tts.stop();
                                                        dialog.cancel();
                                                        brain_op = 0;
                                                    }
                                                });
                                        // 다이얼로그 생성
                                        AlertDialog alertDialog = alertDialogBuilder.create();
                                        // 다이얼로그 보여주기
                                        alertDialog.show();
                                    }
                                }
                            });
                        }
                    }
                    Thread.sleep(1000);
                }
            }
            catch (Exception e) {
                Log.d("통신", "통신 에러 : ", e);
            }
        }
    }
    // co2
    class co2Thread extends Thread {
        @Override
        public void run() {
            try {
                ///UDP 통신용 소켓 생성
                DatagramSocket socket = new DatagramSocket();
                //서버 주소 변수
                InetAddress serverAddr = InetAddress.getByName(url.getsIP());
                /** 보내는 패킷 **/
                //보낼 데이터 생성
                byte[] buf = (co2_send_msg).getBytes();
                //패킷으로 변경
                DatagramPacket packet = new DatagramPacket(buf, buf.length, serverAddr, url.getsPORT());
                Log.d("통신", "co2 send packet : " + co2_send_msg);
                /** 받는 패킷 **/
                //받는 데이터 생성
                byte[] buf2 = ("   ").getBytes();
                //패킷으로 변경
                DatagramPacket packet2 = new DatagramPacket(buf2, buf2.length, serverAddr, url.getsPORT());

                while (co2_mode) {
                    //패킷 전송!
                    socket.send(packet);
                    Log.d("통신", "co2 packet send...");
                    //데이터 수신 대기
                    socket.receive(packet2);
                    Log.d("통신", "co2 packet receive...");

                    //데이터 수신되었다면 문자열로 변환
                    String s = new String(packet2.getData());
                    co2_return_msg = "";
                    for(int i=0; i<s.length(); i++) {
                        if(s.charAt(i) != ' ')
                            co2_return_msg += s.charAt(i);
                    }

                    if(!co2_return_msg.equals("")) {
                        Log.d("통신", "co2 receive packet : " + co2_return_msg);
                        co2_handler.sendEmptyMessage(0);
                        if(co2_op == 0) {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    // 알림
                                    if (Integer.parseInt(co2_return_msg) >= 420) {
                                        co2_op = 1;
                                        // 소리
                                        String alarm = "이산화탄소 농도가 높습니다. 환기를 시켜주세요.";
                                        co2_tts.setPitch(1.5f);
                                        co2_tts.speak(alarm + alarm + alarm + alarm + alarm + alarm, TextToSpeech.QUEUE_FLUSH, null);
                                        // 다이얼로그
                                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                                        alertDialogBuilder.setTitle("졸음 주의");
                                        alertDialogBuilder
                                                .setIcon(android.R.drawable.ic_dialog_alert)
                                                .setMessage("눌러서 알림 해제")
                                                .setCancelable(false)
                                                .setNegativeButton("확인", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        // 다이얼로그를 취소한다
                                                        co2_tts.stop();
                                                        dialog.cancel();
                                                        co2_op = 0;
                                                    }
                                                });
                                        // 다이얼로그 생성
                                        AlertDialog alertDialog = alertDialogBuilder.create();
                                        // 다이얼로그 보여주기
                                        alertDialog.show();
                                    }
                                }
                            });
                        }
                    }
                    Thread.sleep(1000);
                }
            }
            catch (Exception e) {
                Log.d("통신", "통신 에러 : ", e);
            }
        }
    }
    // 심박수
    class heartThread extends Thread {
        @Override
        public void run() {
            try {
                ///UDP 통신용 소켓 생성
                DatagramSocket socket = new DatagramSocket();
                //서버 주소 변수
                InetAddress serverAddr = InetAddress.getByName(url.getsIP());
                /** 보내는 패킷 **/
                //보낼 데이터 생성
                byte[] buf = (heart_send_msg).getBytes();
                //패킷으로 변경
                DatagramPacket packet = new DatagramPacket(buf, buf.length, serverAddr, url.getsPORT());
                Log.d("통신", "heart send packet : " + heart_send_msg);
                /** 받는 패킷 **/
                //받는 데이터 생성
                byte[] buf2 = ("   ").getBytes();
                //패킷으로 변경
                DatagramPacket packet2 = new DatagramPacket(buf2, buf2.length, serverAddr, url.getsPORT());

                while (heart_mode) {
                    //패킷 전송!
                    socket.send(packet);
                    Log.d("통신", "heart packet send...");
                    //데이터 수신 대기
                    socket.receive(packet2);
                    Log.d("통신", "heart packet receive...");

                    //데이터 수신되었다면 문자열로 변환
                    String s = new String(packet2.getData());
                    heart_return_msg = "";
                    for(int i=0; i<s.length(); i++) {
                        if(s.charAt(i) != ' ')
                            heart_return_msg += s.charAt(i);
                    }

                    if(!heart_return_msg.equals("")) {
                        Log.d("통신", "heart receive packet : " + heart_return_msg);
                        heart_handler.sendEmptyMessage(0);
                    }
                    Thread.sleep(2000);
                }
            }
            catch (Exception e) {
                Log.d("통신", "통신 에러 : ", e);
            }
        }
    }

    /** -------- 쓰레드 시작 -------- **/
    // 뇌파
    private void brainThreadStart() {
        brainThread thread = new brainThread();
        thread.setDaemon(true);
        thread.start();
    }
    // co2
    private void co2ThreadStart() {
        co2Thread thread = new co2Thread();
        thread.setDaemon(true);
        thread.start();
    }
    // 심박수
    private void heartThreadStart() {
        heartThread thread = new heartThread();
        thread.setDaemon(true);
        thread.start();
    }

    // 뒤로 두번 누르면 종료
    @Override
    public void onBackPressed() {
        backPressCloseHandler.onBackPressed();
    }
}