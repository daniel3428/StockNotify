package com.example.daniellin.stocknotify;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class Http_Get extends Service {

    private String getUrl;
    private ArrayList<LocalTime> preTimestampTemp;
    private ArrayList<Double> buyPriceArr;
    private ArrayList<Double> sellPriceArr;
    private ArrayList<Double> dealPriceArr;
    private ArrayList<Double> dealMeanPriceArr_1;
    private ArrayList<Double> dealMeanPriceArr_3;
    private ArrayList<Double> dealMeanPriceArr_5;
    private ArrayList<Integer> dealQuantityArr;
    private ArrayList<Integer> upDownArr;
    private ArrayList<Integer> totalBigNumArr;
    private ArrayList<Integer> totalSmallNumArr;
    private int renewIndex;
    private int recentNumber;
    private int bigNumber;

    /*--------------------------------------------------------------------------------------------*/

    /*--------------------------------------------------------------------------------------------*/

    private void calculateMean(int minute) {
        LocalTime localTimeTemp;
        double d_temp;
        int count;

        //Log.i("123",String.valueOf(this.dealQuantityArr.size()));

        for (int i = this.preTimestampTemp.size()-1;i >= 0;i--) {
            localTimeTemp = this.preTimestampTemp.get(i).minusMinutes(minute);

            //Log.i("123",localTimeTemp.toString());
            d_temp=0;
            count=0;
            //Log.i("123",this.dealQuantityArr.get(i).toString());
            //Log.i("123",this.dealPriceArr.get(i).toString());
            for(int j=i;j <= this.preTimestampTemp.size() - 1;j++) {
                //Log.i("123",localTimeTemp.toString());
                //Log.i("321",this.preTimestampTemp.get(j).toString());
                if(localTimeTemp.isBefore(this.preTimestampTemp.get(j))) {
                    //d_temp += this.dealPriceArr.get(j)*this.dealQuantityArr.get(j);
                    //count += this.dealQuantityArr.get(j);
                    d_temp += this.dealPriceArr.get(j);
                    count++;
                    //Log.i("123",String.valueOf(dealPriceArr.get(j)));
                    //Log.i("123",String.valueOf(dealQuantityArr.get(j)));
                }
                else {
                    break;
                }
            }
            //Log.i("123",String.valueOf(count));
            //Log.i("123",localTimeTemp.toString());
            if (count > 0) {
                switch (minute) {
                    case 1:
                        this.dealMeanPriceArr_1.add(d_temp / count);
                        //Log.i("123",String.valueOf(d_temp / count));
                        //Log.i("123",String.valueOf(dealMeanPriceArr_1.get(i)));
                        break;
                    case 3:
                        this.dealMeanPriceArr_3.add(d_temp / count);
                        break;
                    case 5:
                        this.dealMeanPriceArr_5.add(d_temp / count);
                        break;
                    default:
                        break;
                }

                //Log.i("123",dealMeanPriceArr_1.get(i).toString());

                /*
                if(minute ==5) {
                    if (i==0 ) {
                        sentToMainActivity(R.integer.sentToMain,localTimeTemp.toString()+" "+String.valueOf(d_temp / count)+" 1");
                    }
                    else {
                        sentToMainActivity(R.integer.sentToMain,localTimeTemp.toString()+" "+String.valueOf(d_temp / count)+" 0");
                    }

                }
                */


            }
        }
        switch (minute) {
            case 1:
                Collections.reverse(this.dealMeanPriceArr_1);
                break;
            case 3:
                Collections.reverse(this.dealMeanPriceArr_3);
                break;
            case 5:
                Collections.reverse(this.dealMeanPriceArr_5);
                break;
            default:
                break;
        }
        //Log.i("123",String.valueOf(dealMeanPriceArr_1.size()));
        /*if(minute==1){
        for (int i =0;i<preTimestampTemp.size();i++) {
            Log.i("123",String.valueOf(preTimestampTemp.get(i)));
            Log.i("123",String.valueOf(dealMeanPriceArr_1.get(i)));
            Log.i("123",String.valueOf(dealQuantityArr.get(i)));
        }
        }*/
    }

    private void analyzeData() {
        //calculateMean(1);
        calculateMean(1);
        calculateMean(3);
        calculateMean(5);
    }

    private void sentToMainActivity (int number, String ss) {

        Message msg = Message.obtain();
        //設定Message的內容
        msg.what = number;
        msg.obj = ss;
        //使用MainActivity的static handler來丟Message
        MainActivity.handler.sendMessage(msg);
    }

    private void processOneLine(String inputString) {
        int i_temp=0, priceDigit=1,quantityDigit=1;

        if (inputString.indexOf('.',i_temp) > -1) {
            //Log.i("123",inputString.substring(inputString.indexOf('.',0)-2,
            //inputString.indexOf('.',0)+3));
            for (int i=1;i<=4;i++) {
                if(inputString.charAt(inputString.indexOf('.',i_temp)-i) <= '9' &&
                        inputString.charAt(inputString.indexOf('.',i_temp)-i) >= '0') {
                    priceDigit = i;
                }
                else {
                    break;
                }
            }
            if(inputString.indexOf("<!--價量明細 結束-->") < 0) {

                this.buyPriceArr.add(Double.valueOf(inputString.substring(inputString.indexOf('.',i_temp)-priceDigit,
                        inputString.indexOf('.',i_temp)+3)));
                i_temp = inputString.indexOf('.',i_temp) + 1;
                this.sellPriceArr.add(Double.valueOf(inputString.substring(inputString.indexOf('.',i_temp)-priceDigit,
                        inputString.indexOf('.',i_temp)+3)));
                i_temp = inputString.indexOf('.',i_temp) + 1;
                this.dealPriceArr.add(Double.valueOf(inputString.substring(inputString.indexOf('.',i_temp)-priceDigit,
                        inputString.indexOf('.',i_temp)+3)));
            }
            else {
                this.dealPriceArr.add(Double.valueOf(inputString.substring(inputString.indexOf('.',i_temp)-priceDigit,
                        inputString.indexOf('.',i_temp)+3)));
                i_temp = inputString.indexOf("</td><td>",i_temp) + 1;
                //i_temp = inputString.indexOf('.',i_temp) + 1;
            }
            //Log.i("123",inputString.substring(inputString.indexOf("</td><td>",i_temp)));
            for (int i=0;i<3;i++) {
                i_temp = inputString.indexOf("</td><td>",i_temp) + 1;
            }
            //Log.i("123",String.valueOf(this.dealPriceArr.size()));
            //Log.i("123",inputString.substring(inputString.indexOf("</td><td>",i_temp)-8,
            //inputString.indexOf("</td><td>",i_temp)-5));
            if (inputString.charAt(inputString.indexOf("</td><td>",i_temp)-1) <= '9' &&
                    inputString.charAt(inputString.indexOf("</td><td>",i_temp)-1) >= '0') {
                for (int i=1;i<=4;i++) {
                    if(inputString.charAt(inputString.indexOf("</td><td>",i_temp)-i) <= '9' &&
                            inputString.charAt(inputString.indexOf("</td><td>",i_temp)-i) >= '0') {
                        quantityDigit = i;
                    }
                    else {
                        break;
                    }
                }
                this.dealQuantityArr.add(Integer.valueOf(inputString.substring(inputString.indexOf("</td><td>",i_temp)-quantityDigit,
                        inputString.indexOf("</td><td>",i_temp))));

            }
            else {
                for (int i=1;i<=4;i++) {
                    //Log.i("123",inputString.substring(inputString.indexOf("</td><td>",i_temp)-7-i));
                    if(inputString.charAt(inputString.indexOf("</td><td>",i_temp)-7-i) <= '9' &&
                            inputString.charAt(inputString.indexOf("</td><td>",i_temp)-7-i) >= '0') {
                        quantityDigit = i;
                    }
                    else {
                        break;
                    }
                }
                this.dealQuantityArr.add(Integer.valueOf(inputString.substring(inputString.indexOf("</td><td>",i_temp)-7-quantityDigit,
                        inputString.indexOf("</td><td>",i_temp)-7)));
                if(inputString.charAt(inputString.indexOf("</td><td>",i_temp)-7-quantityDigit-7)== 'e') {
                    //Log.i("123",String.valueOf(inputString.charAt(inputString.indexOf("</td><td>",i_temp)-7-quantityDigit-7)));
                    upDownArr.add(1);
                }
                else if(inputString.charAt(inputString.indexOf("</td><td>",i_temp)-7-quantityDigit-7)== '3'){
                    //Log.i("123",String.valueOf(inputString.charAt(inputString.indexOf("</td><td>",i_temp)-7-quantityDigit-7)));
                    upDownArr.add(-1);
                }
                else {
                    upDownArr.add(0);
                }
                //Log.i("123",inputString.substring(inputString.indexOf("</td><td>",i_temp)-7-quantityDigit,
                //inputString.indexOf("</td><td>",i_temp)-7));
            }
            //Log.i("123",String.valueOf(dealQuantityArr.get(dealQuantityArr.size()-1)));
            //Log.i("123",String.valueOf(dealPriceArr.get(dealPriceArr.size()-1)));
        }




    }

    private void converStreamToString(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String line;
        char c_temp;
        boolean rightLine;
        int i_temp;
        int totalNum=0,totalBigBuyNum=0,totalBigSellNum=0,totalSmallBuyNum=0,totalSmallSellNum=0;

        this.preTimestampTemp = new ArrayList<>();
        this.buyPriceArr = new ArrayList<>();
        this.sellPriceArr = new ArrayList<>();
        this.dealPriceArr = new ArrayList<>();
        this.dealMeanPriceArr_1 = new ArrayList<>();
        this.dealMeanPriceArr_3 = new ArrayList<>();
        this.dealMeanPriceArr_5 = new ArrayList<>();
        this.dealQuantityArr = new ArrayList<>();
        this.upDownArr = new ArrayList<>();
        this.totalBigNumArr = new ArrayList<>();
        this.totalSmallNumArr = new ArrayList<>();

        while ((line = reader.readLine()) != null) {
            //Log.i("123",line);
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


                //Log.i("123", String.valueOf(c_temp));

                if (i==2 && c_temp == ':' ) {
                    rightLine = true;
                    this.preTimestampTemp.add(LocalTime.now());
                    //Log.i("wangshu", String.valueOf(this.timestampTemp.getHour()));
                } else if(i==2 && line.indexOf("<!--價量明細 結束-->") > 0) {
                    rightLine = true;
                }
                else {
                    line = line.concat(String.valueOf(c_temp));
                }

            }

            if (rightLine) {
                //Log.i("wangshu", line.substring(line.length()-9,line.length()-1));
                if (line.indexOf("<!--價量明細 結束-->") < 0) {
                    i_temp = Integer.parseInt(line.substring(line.length()-7,line.length()-5));
                    this.preTimestampTemp.set(this.preTimestampTemp.size()-1,
                            this.preTimestampTemp.get(this.preTimestampTemp.size()-1).withHour(i_temp));
                    i_temp = Integer.parseInt(line.substring(line.length()-5,line.length()-3));
                    this.preTimestampTemp.set(this.preTimestampTemp.size()-1,
                            this.preTimestampTemp.get(this.preTimestampTemp.size()-1).withMinute(i_temp));
                    i_temp = Integer.parseInt(line.substring(line.length()-3,line.length()-1));
                    this.preTimestampTemp.set(this.preTimestampTemp.size()-1,
                            this.preTimestampTemp.get(this.preTimestampTemp.size()-1).withSecond(i_temp));
                }
                processOneLine(line);
                //Log.i("wangshu", line);
            }

            if (line.indexOf("<!--價量明細 結束-->") > 0) {
                break;
            }
        }


        this.totalSmallNumArr.add(0);
        this.totalBigNumArr.add(0);

        for(int i=this.preTimestampTemp.size()-2;i>=0;i--) {
            totalNum=totalNum+this.dealQuantityArr.get(i);
            if(this.dealQuantityArr.get(i)>=this.bigNumber) {
                this.totalSmallNumArr.add(this.totalSmallNumArr.get(this.totalSmallNumArr.size()-1));

                if(this.upDownArr.get(i)==1) {
                    totalBigBuyNum = totalBigBuyNum + this.dealQuantityArr.get(i);
                }
                else if (this.upDownArr.get(i)==-1) {
                    totalBigSellNum = totalBigSellNum + this.dealQuantityArr.get(i);
                }
                this.totalBigNumArr.add(totalBigBuyNum-totalBigSellNum);
            }
            else {
                this.totalBigNumArr.add(this.totalBigNumArr.get(this.totalBigNumArr.size()-1));

                if(this.upDownArr.get(i)==1) {
                    totalSmallBuyNum = totalSmallBuyNum + this.dealQuantityArr.get(i);
                }
                else if (this.upDownArr.get(i)==-1) {
                    totalSmallSellNum = totalSmallSellNum + this.dealQuantityArr.get(i);
                }
                this.totalSmallNumArr.add(totalSmallBuyNum-totalSmallSellNum);
            }
            if(i==0) {
                sentToMainActivity(R.integer.receiveBig, this.preTimestampTemp.get(i).toString() + " " + this.totalBigNumArr.get(this.preTimestampTemp.size() - i - 1).toString()+" 1 "+this.dealQuantityArr.get(i));
                sentToMainActivity(R.integer.receiveSmall, this.preTimestampTemp.get(i).toString() + " " + this.totalSmallNumArr.get(this.preTimestampTemp.size() - i - 1).toString()+" 1 "+this.dealQuantityArr.get(i));
            }
            else {
                sentToMainActivity(R.integer.receiveBig, this.preTimestampTemp.get(i).toString() + " " + this.totalBigNumArr.get(this.preTimestampTemp.size() - i - 1).toString()+" 0 "+this.dealQuantityArr.get(i));
                sentToMainActivity(R.integer.receiveSmall, this.preTimestampTemp.get(i).toString() + " " + this.totalSmallNumArr.get(this.preTimestampTemp.size() - i - 1).toString()+" 0 "+this.dealQuantityArr.get(i));
                //Log.i("123",String.valueOf(this.dealQuantityArr.get(i)));
            }
        }
        Collections.reverse(this.totalBigNumArr);
        /*
        for(int i=0;i<this.preTimestampTemp.size();i++) {
            Log.i("123",this.totalBigNumArr.get(i).toString());
            Log.i("321",this.preTimestampTemp.get(i).toString());
        }
        */

        //Log.i("wangshu", String.valueOf(this.preTimestampTemp.size()));
        //Log.i("buy", String.valueOf(this.buyPriceArr.size()));
        //Log.i("sell", String.valueOf(this.sellPriceArr.size()));
        //Log.i("deal", String.valueOf(this.dealPriceArr.size()));

        //Log.i("wangshu", String.valueOf(this.preTimestampTemp.get(749).toString()));
        //Log.i("wangshu", String.valueOf(this.preTimestampTemp.get(748).toString()));
        //Log.i("wangshu", String.valueOf(this.buyPriceArr.size()));
    }

    public void Get(String url,Integer b_number){
        this.renewIndex=0;
        this.recentNumber=200;
        this.bigNumber=b_number;
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
                get.addHeader("If-Modified-Since", "2018-10-01 13:19:00");
                get.addHeader("Cache-Control", "max-age=0");
                //get.addHeader("If-None-Match", "x234dff");

                try {
                    HttpResponse mHttpResponse = httpClient.execute(get);
                    HttpEntity mHttpEntity = mHttpResponse.getEntity();
                    int code = mHttpResponse.getStatusLine().getStatusCode();
                    //Header[] sss = mHttpResponse.getAllHeaders();
                    //for(int i=0;i< sss.length;i++)
                    //Log.i("wangshu", sss[i].toString());
                    if (null != mHttpEntity) {
                        InputStream mInputStream = mHttpEntity.getContent();
                        converStreamToString(mInputStream);
                        //Log.i("wangshu", "請求狀態碼:" + code );
                        mInputStream.close();
                    }
                } catch (ClientProtocolException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                analyzeData();

                predictTrend();

                //Log.i("wangshu", String.valueOf(dealMeanPriceArr.size()));
                //Log.i("wangshu", String.valueOf(dealPriceArr.size()));
                //Log.i("Q", String.valueOf(dealQuantityArr.size()));


            }
        }).start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}