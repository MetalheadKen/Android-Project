package tw.edu.ncut.csie.qr_code;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.util.ArrayList;
import java.util.List;


public class insert extends Activity {

        final String url = "http://140.128.88.166:8080/insert.php";// 要加上"http://" 否則會連線失敗
        final String url_con = "http://140.128.88.166:8080/confirm.php";

        Button Insert,sms;
        EditText email,pw1,pw2,name,phone,confirm;
        private int random=0;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            this.setContentView(R.layout.activity_insert);

            //
            email= (EditText) findViewById(R.id.email);
            pw1= (EditText) findViewById(R.id.pw1);
            pw2= (EditText) findViewById(R.id.pw2);
            name= (EditText) findViewById(R.id.name);
            phone= (EditText) findViewById(R.id.phone);
            Insert= (Button) findViewById(R.id.insert);
            confirm=(EditText) findViewById(R.id.confirm);
            sms=(Button) findViewById(R.id.sms);

            //


           Insert.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //
                    if(     email.getText().toString().isEmpty() ||
                            pw1.getText().toString().isEmpty()   ||
                            pw2.getText().toString().isEmpty()   ||
                            name.getText().toString().isEmpty()  ||
                            phone.getText().toString().isEmpty() ||
                            confirm.getText().toString().isEmpty() )
                    {
                        Toast.makeText(insert.this,"尚有資料未填寫喔！", Toast.LENGTH_LONG)
                                .show();
                    }
                    //
                    else if(pw1.getText().toString().equals(pw2.getText().toString()))
                    {
                        new Thread(runnable).start();//啟動執行序runnable
                    }
                    //
                    else
                    {
                        Toast.makeText(insert.this, "「會員密碼」與「確認密碼」必須相同喔!", Toast.LENGTH_LONG)
                                .show();
                    }

                }
            });

            //

            sms.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(!phone.getText().toString().equals("")){
                        /*
                        SmsManager smsManager = SmsManager.getDefault();
                        int ran = (int) (Math.random()*8999+1000);
                        random=ran;
                        //
                        smsManager.sendTextMessage(phone.getText().toString(),
                                null,
                                random+"",
                                PendingIntent.getBroadcast(getApplicationContext(), 0,new Intent(), 0),
                                null);
                        */
                        new Thread(runsms).start();
                    }

                }
            });

        }

        Handler handler_Success = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                Bundle data = msg.getData();
                String val = data.getString("key");//取出key中的字串存入val
                Toast.makeText(insert.this, val, Toast.LENGTH_LONG).show();
                //註冊成功後，要回歸index畫面
                if(val.equals("註冊成功囉"))
                {
                    insert.this.finish();
                }

            }
        };

        Handler handler_Error = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                Bundle data = msg.getData();
                String val = data.getString("key");
                Toast.makeText(getApplicationContext(), val, Toast.LENGTH_LONG).show();
            }
        };

        Handler handler_Nodata = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                Bundle data = msg.getData();
                String val = data.getString("key");
                Toast.makeText(getApplicationContext(), val, Toast.LENGTH_LONG).show();
            }
        };

        ////////////////////////////////////////////////////////////////////////


        Runnable runnable = new Runnable(){
            @Override
            public void run() {
                //
                // TODO: http request.
                //
                Message msg = new Message();
                Bundle data = new Bundle();
                msg.setData(data);
                try
                {
                    //連線到 url網址
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost method = new HttpPost(url);

                    //傳值給PHP
                    List<NameValuePair> vars=new ArrayList< NameValuePair>();
                    vars.add(new BasicNameValuePair("email",email.getText().toString()));
                    vars.add(new BasicNameValuePair("pw",pw1.getText().toString()));
                    vars.add(new BasicNameValuePair("name",name.getText().toString()));
                    vars.add(new BasicNameValuePair("phone",phone.getText().toString()));
                    vars.add(new BasicNameValuePair("confirm",confirm.getText().toString()));
                    method.setEntity(new UrlEncodedFormEntity(vars, HTTP.UTF_8));


                    //接收PHP回傳的資料
                    HttpResponse response = httpclient.execute(method);
                    HttpEntity entity = response.getEntity();

                    if(entity != null){
                        data.putString("key", EntityUtils.toString(entity,"utf-8"));//如果成功將網頁內容存入key
                        handler_Success.sendMessage(msg);
                    }
                    else{
                        data.putString("key","無資料");
                        handler_Nodata.sendMessage(msg);
                    }

                }
                catch(Exception e){
                    data.putString("key","連線失敗");
                    handler_Error.sendMessage(msg);
                }

            }
        };


        ////////////////////////////////////////////////////////////

        Runnable runsms = new Runnable(){

            @Override
            public void run() {
                //
                // TODO: http request.
                //
                Message msg = new Message();
                Bundle data = new Bundle();
                msg.setData(data);
                try
                {
                    //連線到 url網址
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost method = new HttpPost(url_con);

                    //傳值給PHP
                    List<NameValuePair> vars=new ArrayList< NameValuePair>();
                    vars.add(new BasicNameValuePair("random",random+""));
                    vars.add(new BasicNameValuePair("phone",phone.getText().toString()));
                    method.setEntity(new UrlEncodedFormEntity(vars, HTTP.UTF_8));


                    //接收PHP回傳的資料
                    HttpResponse response = httpclient.execute(method);
                    HttpEntity entity = response.getEntity();

                    if(entity != null){
                        data.putString("key", EntityUtils.toString(entity,"utf-8"));//如果成功將網頁內容存入key
                        handler_Success.sendMessage(msg);
                    }
                    else{
                        data.putString("key","無資料");
                        handler_Nodata.sendMessage(msg);
                    }

                }
                catch(Exception e){
                    data.putString("key","連線失敗");
                    handler_Error.sendMessage(msg);
                }

            }
        };
    }
