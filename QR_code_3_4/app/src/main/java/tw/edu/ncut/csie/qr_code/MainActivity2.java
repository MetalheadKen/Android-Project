package tw.edu.ncut.csie.qr_code;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import static tw.edu.ncut.csie.qr_code.visitflag.user_email;
class visitflag{
    static int visiter_login_flag = 0;
    static int login_flag = 0;
    static String user_email ="";
}
public class MainActivity2 extends AppCompatActivity {
    //private String url_email = "http://192.168.200.126/store_email.php";
    final String url = "http://192.168.200.11/login.php";// 要加上"http://" 否則會連線失敗
    private ProgressDialog d;

    Button btnLogin,btnInsert ,btn_visiter;
    EditText edmail,edPW;
    ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity2_main);

        //
        btnLogin= (Button) findViewById(R.id.btnLogin);
        btnInsert=(Button) findViewById(R.id.btnInsert);
        edmail= (EditText) findViewById(R.id.edEmail);
        edPW= (EditText) findViewById(R.id.edPW);
        btn_visiter = (Button)findViewById(R.id.btn_visiter);
        mProgressBar = (ProgressBar)findViewById(R.id.progressBar);
        mProgressBar.setVisibility(View.GONE);
        //

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                visitflag.visiter_login_flag = 0;

                new Thread(runnable).start();//啟動執行序runnable
                //new Thread(runnable1).start();

                if(visitflag.login_flag == 0){
                    btnLogin.setEnabled(false);
                }else if(visitflag.login_flag == 1){
                    btnLogin.setEnabled(true);
                }
                mProgressBar.setVisibility(View.VISIBLE);

            }
        });
        //
        btnInsert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MainActivity2.this,insert.class);
                startActivity(intent);
            }
        });
        btn_visiter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                visitflag.visiter_login_flag = 1;
                Intent visiter = new Intent();
                visiter.setClass(MainActivity2.this,MainActivity.class);
                startActivity(visiter);
            }
        });

    }

    Handler handler_Success = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String val = data.getString("key");//取出key中的字串存入val

            if (val.equals("登入成功")) {
                user_email = edmail.getText().toString();
                Intent it = new Intent(MainActivity2.this,MainActivity.class);
                startActivity(it);
                btnLogin.setEnabled(true);
            }else{
                btnLogin.setEnabled(true);
            }
            Toast.makeText(MainActivity2.this, val, Toast.LENGTH_LONG).show();
            //d.dismiss();
            mProgressBar.setVisibility(View.INVISIBLE);
        }

    };

    Handler handler_Error = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String val = data.getString("key");
            Toast.makeText(getApplicationContext(), val, Toast.LENGTH_LONG).show();
            d.dismiss();

        }
    };

    Handler handler_Nodata = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String val = data.getString("key");
            Toast.makeText(getApplicationContext(), val, Toast.LENGTH_LONG).show();
            btnLogin.setEnabled(true);
            d.dismiss();
        }
    };

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
                vars.add(new BasicNameValuePair("email",edmail.getText().toString()));
                vars.add(new BasicNameValuePair("pw",edPW.getText().toString()));
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
    /*
    Runnable runnable1 = new Runnable() {
        @Override
        public void run() {
            try {

                HttpClient client = new DefaultHttpClient();
                HttpPost post = new HttpPost(url_email);

                JSONObject json = new JSONObject();
                json.put("email",edmail.getText().toString());
                post.setEntity(new ByteArrayEntity(json.toString().getBytes("UTF8")));
                post.setHeader("json",json.toString());

                Log.e("email33333", String.valueOf(json));

                HttpResponse response = client.execute(post);
                HttpEntity entity = response.getEntity();
                //post.setHeader("json",json.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    };
    */
}

