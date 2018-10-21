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
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;
import android.os.Message;


import java.text.DecimalFormat;
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
    private List<Double> bigDealArr;
    private List<Double> smallDealArr;
    private View v2;

    Http_Get HG;

    static Handler handler; //宣告成static讓service可以直接使用

    ViewPager pager;
    ArrayList<View> pagerList;

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

        pager = findViewById(R.id.pager);
        LayoutInflater li = getLayoutInflater().from(this);
        View v1 = li.inflate(R.layout.initial_layout,null);
        v2 = li.inflate(R.layout.show_quantity,null);
        pagerList = new ArrayList<View>();
        pagerList.add(v1);
        pagerList.add(v2);
        pager.setAdapter(new MyViewPagerAdapter(pagerList));
        pager.setCurrentItem(0);




        HG = new Http_Get();
        getBtn = (Button) v1.findViewById(R.id.http_get_btn);
        button_2327 = (Button) v1.findViewById(R.id.stock_2327_b);
        button_2492 = (Button) v1.findViewById(R.id.stock_2492_b);
        button_3406 = (Button) v1.findViewById(R.id.stock_3406_b);
        button_6552 = (Button) v1.findViewById(R.id.stock_6552_b);




        //讓多個Button共用一個Listener，在Listener中再去設定各按鈕要做的事
        getBtn.setOnClickListener(btn1Listener);
        button_2327.setOnClickListener(btn1Listener);
        button_2492.setOnClickListener(btn1Listener);
        button_3406.setOnClickListener(btn1Listener);
        button_6552.setOnClickListener(btn1Listener);



        handler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    // 當收到的Message的代號為我們剛剛訂的代號就做下面的動作。
                    case R.integer.sentToMain:
                        String ss = (String)msg.obj;
                        String[] tokens = ss.split(" ");
                        //String[] time = tokens[0].split(":");
                        //timeArr.add((Double.valueOf(time[0])*60*60+Double.valueOf(time[1])*60+Double.valueOf(time[2]))/ (24*60*60)*1000);
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
                            //Log.i("123",String.valueOf(Double.valueOf(ss_up_down_split[0])/(Double.valueOf(ss_up_down_split[0])+Double.valueOf(ss_up_down_split[1]))));
                            double dd = Double.valueOf(ss_up_down_split[0])/(Double.valueOf(ss_up_down_split[0])+Double.valueOf(ss_up_down_split[1]))*100;
                            DecimalFormat df = new DecimalFormat("##.##");
                            textViewUpDown.append(" = "+String.valueOf(df.format(dd))+"%");
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
                    case R.integer.receiveDeal:
                        String ss_deal = (String)msg.obj;
                        TextView textViewDeal = findViewById(R.id.showTextDeal);
                        //textViewSell.setTextColor(android.graphics.Color.GREEN);
                        //textViewSell.append(ss_sell+"\n");
                        String[] string_arr_deal = ss_deal.split(" ");
                        if(string_arr_deal[3].compareTo("1")==0){
                            textViewDeal.append(Html.fromHtml(string_arr_deal[0]+" "+string_arr_deal[1]+" "+"<font color=\"#FF0000\">"+string_arr_deal[2]+"<\font>"+"<br>"));
                        }
                        else {
                            textViewDeal.append(Html.fromHtml(string_arr_deal[0]+" "+string_arr_deal[1]+" "+"<font color=\"#00FF00\">"+string_arr_deal[2]+"<\font>"+"<br>"));
                        }

                        break;
                    case R.integer.receiveBig:
                        String ss_big = (String)msg.obj;
                        //Log.i("123",ss_big);
                        String[ ] ss_big_arr = ss_big.split(" ");
                        String[] big_time = ss_big_arr[0].split(":");
                        timeArr.add((Double.valueOf(big_time[0])*60*60+Double.valueOf(big_time[1])*60+Double.valueOf(big_time[2]))/ (24*60*60)*1000);
                        bigDealArr.add(Double.valueOf(ss_big_arr[1]));
                        if(Integer.valueOf(ss_big_arr[2])==1) {
                            LinearLayout chartContainer = (LinearLayout) v2.findViewById(R.id.chart_container_big);
                            chartContainer.removeAllViews();
                            chartContainer.addView(ChartView(MainActivity.this, "Hello", "Count", timeArr, bigDealArr));
                        }
                        //Log.i("123",ss_big);
                        break;
                    case R.integer.receiveSmall:
                        String ss_small = (String)msg.obj;
                        String[ ] ss_small_arr = ss_small.split(" ");
                        String[] small_time = ss_small_arr[0].split(":");
                        //timeArr.add((Double.valueOf(small_time[0])*60*60+Double.valueOf(small_time[1])*60+Double.valueOf(small_time[2]))/ (24*60*60)*1000);
                        smallDealArr.add(Double.valueOf(ss_small_arr[1]));
                        if(Integer.valueOf(ss_small_arr[2])==1) {
                            LinearLayout chartContainer = (LinearLayout) v2.findViewById(R.id.chart_container_small);
                            chartContainer.removeAllViews();
                            chartContainer.addView(ChartView(MainActivity.this, "Hello", "Count", timeArr, smallDealArr));
                        }
                        break;
                }
            }
        };
    }
    private Button.OnClickListener btn1Listener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.http_get_btn:
                    //setNotification();
                    //while(true) {
                    //setNotification(String.valueOf("123"), -1);
                    for (int i = 0; i < 10000; i++) {

                        final int test_index;
                        test_index = i;
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
                                TextView textViewDeal = findViewById(R.id.showTextDeal);
                                textViewDeal.setText("");
                                timeArr = new ArrayList<>();
                                priceArr = new ArrayList<>();
                                bigDealArr = new ArrayList<>();
                                smallDealArr = new ArrayList<>();
                                EditText editTextStockNumber = findViewById(R.id.stock_number);
                                String targetURL = getUrl.concat(editTextStockNumber.getText().toString() + ".html");
                                EditText editTextRecentNumber = findViewById(R.id.big_number_edit);
                                //getUrl=getUrl.concat(editTextStockNumber.getText().toString()+".html");
                                //HG.Get(targetURL,Integer.valueOf(editTextRecentNumber.getText().toString()),Integer.valueOf(test_index));
                                HG.Get(targetURL, Integer.valueOf(editTextRecentNumber.getText().toString()), test_index);
                            }
                        }, 10000 * i);
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
    };


    public void onClick(View v){
    }

}
