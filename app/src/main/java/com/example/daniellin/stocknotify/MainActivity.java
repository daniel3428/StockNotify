package com.example.daniellin.stocknotify;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;
import android.os.Message;


import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import static android.media.RingtoneManager.getDefaultUri;
import static com.example.daniellin.stocknotify.LineCharView.ChartView;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    //設定HTTP Get & Post要連線的Url
    private String getUrl = "http://pchome.megatime.com.tw/stock/sto0/ock3/sid";
    private Button getBtn;
    private Button button_2327;
    private Button button_2492;
    private Button button_3406;
    private Button button_6552;
    private List<Double> timeArr;
    private List<Double> priceArr;

    Http_Get HG;

    static Handler handler; //宣告成static讓service可以直接使用

    private void setNotification(String time,int buySell) {
        Intent intent= new Intent();
        intent.setClass(this, MainActivity.class);
        //intent.setAction(MyService.ACTION1);
        //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP
        //| Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),0,intent,PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManager manager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(getApplicationContext());
        builder.setSmallIcon(R.drawable.pika)
                .setWhen(System.currentTimeMillis())
                .setContentText(time)
                .setContentIntent(pendingIntent)
                //.setChannelId("2")
                .setContentInfo("3");
        if(buySell==1) {
            builder.setContentTitle("Buy");
        }
        else {
            builder.setContentTitle("Sell");
        }
        //builder.addAction(111,"ACTION1",pendingIntent);
        builder.setVibrate(new long[] { 1000, 3000, 1000});
        Notification notification = builder.build();

        manager.notify((int)(Math.random()*55446), notification);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);





        HG = new Http_Get();
        getBtn = (Button) findViewById(R.id.http_get_btn);
        button_2327 = (Button) findViewById(R.id.stock_2327_b);
        button_2492 = (Button) findViewById(R.id.stock_2492_b);
        button_3406 = (Button) findViewById(R.id.stock_3406_b);
        button_6552 = (Button) findViewById(R.id.stock_6552_b);


        //讓多個Button共用一個Listener，在Listener中再去設定各按鈕要做的事
        getBtn.setOnClickListener(this);
        button_2327.setOnClickListener(this);
        button_2492.setOnClickListener(this);
        button_3406.setOnClickListener(this);
        button_6552.setOnClickListener(this);



        handler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    // 當收到的Message的代號為我們剛剛訂的代號就做下面的動作。
                    case R.integer.sentToMain:
                        String ss = (String)msg.obj;
                        String[] tokens = ss.split(" ");
                        String[] time = tokens[0].split(":");
                        timeArr.add((Double.valueOf(time[0])*60*60+Double.valueOf(time[1])*60+Double.valueOf(time[2]))/ (24*60*60)*1000);
                        priceArr.add(Double.valueOf(tokens[1]));
                        //Log.i("123",String.valueOf(timeArr.get(0)));
                        //Log.i("123",String.valueOf(priceArr.size()));
                        //Toast.makeText(MainActivity.this, ss,Toast.LENGTH_LONG).show();

                        if (Integer.valueOf(tokens[2])==1) {
                            LinearLayout chartContainer = (LinearLayout) findViewById(R.id.chart_container);
                            chartContainer.removeAllViews();
                            chartContainer.addView(ChartView(MainActivity.this, "Hello", "Count", timeArr, priceArr));
                        }

                        break;
                    case R.integer.receiveBuy:
                        String ss_buy = (String)msg.obj;
                        TextView textViewBuy = findViewById(R.id.showTextBuy);
                        textViewBuy.setTextColor(android.graphics.Color.RED);
                        textViewBuy.append(ss_buy+"\n");
                        String[] time_buy = ss_buy.split(":");
                        if(LocalTime.of(Integer.valueOf(time_buy[0]),Integer.valueOf(time_buy[1]),Integer.valueOf(time_buy[2]))
                                .isAfter(LocalTime.now().minusMinutes(3))) {
                            setNotification(ss_buy,1);
                        }



                        break;
                    case R.integer.receiveSell:
                        String ss_sell = (String)msg.obj;
                        TextView textViewSell = findViewById(R.id.showTextSell);
                        textViewSell.setTextColor(android.graphics.Color.GREEN);
                        textViewSell.append(ss_sell+"\n");
                        String[] time_sell = ss_sell.split(":");
                        if(LocalTime.of(Integer.valueOf(time_sell[0]),Integer.valueOf(time_sell[1]),Integer.valueOf(time_sell[2]))
                                .isAfter(LocalTime.now().minusMinutes(3))) {
                            setNotification(ss_sell, -1);
                        }
                        break;
                    case R.integer.receiveUpDown:
                        String ss_up_down = (String)msg.obj;
                        String[] ss_up_down_split = ss_up_down.split(" ");
                        TextView textViewUpDown=findViewById(R.id.text_view_show_up_down);

                        textViewUpDown.setText(ss_up_down_split[0]+"/"+String.valueOf(Integer.valueOf(ss_up_down_split[0])+Integer.valueOf(ss_up_down_split[1])));
                        if(Integer.valueOf(ss_up_down_split[0])+Integer.valueOf(ss_up_down_split[1])>0){
                            textViewUpDown.append(" = "+String.valueOf(Double.valueOf(ss_up_down_split[0])/(Double.valueOf(ss_up_down_split[0])+Double.valueOf(ss_up_down_split[1])))
                                    .substring(0,7));
                            if(Integer.valueOf(ss_up_down_split[0])>Integer.valueOf(ss_up_down_split[1])) {
                                textViewUpDown.setTextColor(android.graphics.Color.RED);
                            }
                            else {
                                textViewUpDown.setTextColor(android.graphics.Color.GREEN);
                            }
                        }
                        else {

                        }
                        break;
                }
            }
        };
    }


    //依照按下的按鈕去做相對應的任務
    public void onClick(View v){
        switch (v.getId()){
            case R.id.http_get_btn:
                //setNotification();
                //while(true) {
                //setNotification(String.valueOf("123"), -1);
                for(int i=0;i<10000;i++) {

//final int test_index;
//test_index=i;
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            TextView textViewSell = findViewById(R.id.showTextSell);
                            textViewSell.setText("");
                            TextView textViewBuy = findViewById(R.id.showTextBuy);
                            textViewBuy.setText("");
                            TextView textViewUpDown = findViewById(R.id.text_view_show_up_down);
                            textViewUpDown.setText("");
                            timeArr = new ArrayList<>();
                            priceArr = new ArrayList<>();
                            EditText editTextStockNumber = findViewById(R.id.stock_number);
                            String targetURL = getUrl.concat(editTextStockNumber.getText().toString() + ".html");
                            EditText editTextRecentNumber = findViewById(R.id.big_number_edit);
                            //getUrl=getUrl.concat(editTextStockNumber.getText().toString()+".html");
                            //HG.Get(targetURL,Integer.valueOf(editTextRecentNumber.getText().toString()),Integer.valueOf(test_index));
                            HG.Get(targetURL,Integer.valueOf(editTextRecentNumber.getText().toString()));
                        }
                    }, 10000*i);



                }
                break;
            case R.id.stock_2327_b:
                EditText editTextStockNumber_2327 = findViewById(R.id.stock_number);
                editTextStockNumber_2327.setText(String.valueOf(2327));
                //Log.i("123","123");
                break;
            case R.id.stock_2492_b:
                EditText editTextStockNumber_2492 = findViewById(R.id.stock_number);
                editTextStockNumber_2492.setText(String.valueOf(2492));
                break;
            case R.id.stock_3406_b:
                EditText editTextStockNumber_3406 = findViewById(R.id.stock_number);
                editTextStockNumber_3406.setText(String.valueOf(3406));
                break;
            case R.id.stock_6552_b:
                EditText editTextStockNumber_6552 = findViewById(R.id.stock_number);
                editTextStockNumber_6552.setText(String.valueOf(6552));
                break;
            default:
                break;
        }
    }

}
