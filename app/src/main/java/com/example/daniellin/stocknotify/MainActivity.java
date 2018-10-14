package com.example.daniellin.stocknotify;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.os.Handler;
import android.os.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.example.daniellin.stocknotify.LineCharView.ChartView;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    //設定HTTP Get & Post要連線的Url
    private String getUrl = "http://pchome.megatime.com.tw/stock/sto0/ock3/sid6552.html";
    private Button getBtn;
    private List<Double> timeArr;
    private List<Double> priceArr;

    Http_Get HG;

    static Handler handler; //宣告成static讓service可以直接使用

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);





        HG = new Http_Get();
        getBtn = (Button) findViewById(R.id.http_get_btn);


        //讓多個Button共用一個Listener，在Listener中再去設定各按鈕要做的事
        getBtn.setOnClickListener(this);

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
                }
            }
        };
    }


    //依照按下的按鈕去做相對應的任務
    public void onClick(View v){
        switch (v.getId()){
            case R.id.http_get_btn:
                this.timeArr=new ArrayList<>();
                this.priceArr=new ArrayList<>();
                HG.Get(getUrl);
                break;
        }
    }

}
