package com.example.daniellin.stocknotify;

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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
                        break;
                    case R.integer.receiveSell:
                        String ss_sell = (String)msg.obj;
                        TextView textViewSell = findViewById(R.id.showTextSell);
                        textViewSell.setTextColor(android.graphics.Color.GREEN);
                        textViewSell.append(ss_sell+"\n");
                        break;
                }
            }
        };
    }


    //依照按下的按鈕去做相對應的任務
    public void onClick(View v){
        switch (v.getId()){
            case R.id.http_get_btn:
                TextView textViewSell = findViewById(R.id.showTextSell);
                textViewSell.setText("");
                TextView textViewBuy = findViewById(R.id.showTextBuy);
                textViewBuy.setText("");
                this.timeArr=new ArrayList<>();
                this.priceArr=new ArrayList<>();
                EditText editTextStockNumber = findViewById(R.id.stock_number);
                String targetURL = getUrl.concat(editTextStockNumber.getText().toString()+".html");
                //getUrl=getUrl.concat(editTextStockNumber.getText().toString()+".html");
                HG.Get(targetURL);
                break;
            case R.id.stock_2327_b:
                EditText editTextStockNumber_2327 = findViewById(R.id.stock_number);
                editTextStockNumber_2327.setText(String.valueOf(2327));
                Log.i("123","123");
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
