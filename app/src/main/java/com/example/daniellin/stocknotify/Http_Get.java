package com.example.daniellin.stocknotify;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalTime;
import java.util.ArrayList;

public class Http_Get extends Service {

    private String getUrl;

    private ArrayList<LocalTime> preTimestampTemp;

    private void processOneLine(String inputString) {

    }

    private void converStreamToString(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String line;
        char c_temp;
        boolean rightLine;
        int i_temp;

        this.preTimestampTemp = new ArrayList<>();

        while ((line = reader.readLine()) != null) {
            if(line.compareTo("<!--價量明細 開始-->") == 0) {
                break;
            }
        }
        //Timestamp d = new Timestamp(System.currentTimeMillis());
        //Log.i("timeis", d.toString());
        while(true) {
            line = String.valueOf("");
            rightLine = false;
            c_temp = (char)reader.read();
            while (c_temp != ':') {
                line = line.concat(String.valueOf(c_temp));
                c_temp = (char)reader.read();
                //Log.i("wangshu", String.valueOf(c_temp));
                //break;
            }

            for (int i=0;i<6;i++) {
                c_temp = (char)reader.read();
                //line = line.concat(String.valueOf(c_temp));
                if (i==2 && c_temp == ':') {
                    rightLine = true;
                    this.preTimestampTemp.add(LocalTime.now());
                    //Log.i("wangshu", String.valueOf(this.timestampTemp.getHour()));
                }else {
                    line = line.concat(String.valueOf(c_temp));
                }
            }
            if (rightLine) {
                //Log.i("wangshu", line.substring(line.length()-9,line.length()-1));
                i_temp = Integer.parseInt(line.substring(line.length()-7,line.length()-5));
                this.preTimestampTemp.set(this.preTimestampTemp.size()-1,
                        this.preTimestampTemp.get(this.preTimestampTemp.size()-1).withHour(i_temp));
                i_temp = Integer.parseInt(line.substring(line.length()-5,line.length()-3));
                this.preTimestampTemp.set(this.preTimestampTemp.size()-1,
                        this.preTimestampTemp.get(this.preTimestampTemp.size()-1).withMinute(i_temp));
                i_temp = Integer.parseInt(line.substring(line.length()-3,line.length()-1));
                this.preTimestampTemp.set(this.preTimestampTemp.size()-1,
                        this.preTimestampTemp.get(this.preTimestampTemp.size()-1).withSecond(i_temp));
                processOneLine(line);
                //Log.i("wangshu", line);
            }

            if (line.indexOf("<!--價量明細 結束-->") > 0) {
                break;
            }
        }
        for(int i=0;i<this.preTimestampTemp.size();i++) {
            Log.i("wangshu", this.preTimestampTemp.get(i).toString());
        }

    }

    public void Get(String url){
        this.getUrl = url;

        new Thread(new Runnable() {

            @Override
            public void run() {
                HttpParams mDefaultHttpParams = new BasicHttpParams();
                //设置连接超时
                HttpConnectionParams.setConnectionTimeout(mDefaultHttpParams, 15000);
                //设置请求超时
                HttpConnectionParams.setSoTimeout(mDefaultHttpParams, 15000);
                HttpConnectionParams.setTcpNoDelay(mDefaultHttpParams, true);
                HttpProtocolParams.setVersion(mDefaultHttpParams, HttpVersion.HTTP_1_1);
                HttpProtocolParams.setContentCharset(mDefaultHttpParams, HTTP.UTF_8);
                //持续握手
                HttpProtocolParams.setUseExpectContinue(mDefaultHttpParams, true);

                //建立HttpClient物件
                HttpClient httpClient = new DefaultHttpClient(mDefaultHttpParams);
                //建立Http Get，並給予要連線的Url
                HttpGet get = new HttpGet(getUrl);
                get.addHeader("Connection", "Keep-Alive");
                get.addHeader("Accept-Encoding", "deflate");
                get.addHeader("Accept", "text/html");
                get.addHeader("Accept-Language", "zh-TW,zh;q=0.9,en-US;q=0.8,en;q=0.7");
                get.addHeader("Referer", "http://pchome.megatime.com.tw/stock/sto0/ock3/sid6552.html");

                try {
                    HttpResponse mHttpResponse = httpClient.execute(get);
                    HttpEntity mHttpEntity = mHttpResponse.getEntity();
                    int code = mHttpResponse.getStatusLine().getStatusCode();
                    if (null != mHttpEntity) {
                        InputStream mInputStream = mHttpEntity.getContent();
                        converStreamToString(mInputStream);
                        //Log.i("wangshu", "請求狀態碼:" + code + "\n請求結果:\n" + respose);
                        mInputStream.close();
                    }
                } catch (ClientProtocolException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}