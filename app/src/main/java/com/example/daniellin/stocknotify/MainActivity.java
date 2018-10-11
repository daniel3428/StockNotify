package com.example.daniellin.stocknotify;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.os.Handler;
import android.os.Message;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    //設定HTTP Get & Post要連線的Url
    private String getUrl = "http://pchome.megatime.com.tw/stock/sto0/ock3/sid6552.html";

    private Button getBtn;

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

        //接收service傳出Post的到的回傳訊息，並透過Toast顯示出來
        handler = new Handler(){
            public void handleMessage(Message msg){
                switch (msg.what){
                    case 123:
                        String ss = (String)msg.obj;
                        Toast.makeText(MainActivity.this, ss.length(),Toast.LENGTH_LONG).show();
                        break;
                        default:
                            break;
                }
            }
        };
    }


    //依照按下的按鈕去做相對應的任務
    public void onClick(View v){
        switch (v.getId()){
            case R.id.http_get_btn:
                HG.Get(getUrl);
                break;
        }
    }
}
