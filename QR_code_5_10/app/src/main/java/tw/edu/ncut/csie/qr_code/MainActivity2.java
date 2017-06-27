package tw.edu.ncut.csie.qr_code;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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
import org.json.JSONArray;
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
    private String url_email = "http://192.168.200.126/favorite.php";
    final String url = "http://192.168.200.126/login.php";// 要加上"http://" 否則會連線失敗

    Button btnLogin,btnInsert ,btn_visiter;
    EditText edmail,edPW;
    ProgressBar mProgressBar;
    //--紀錄帳密
    private SharedPreferences sp;

    //--Bluetooth
    private BluetoothAdapter btAdapter;

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

                String email = edmail.getText().toString();
                String password = edPW.getText().toString();
                sp = getSharedPreferences( "account", MODE_PRIVATE);
                sp.edit().putString("email", email).commit();
                sp.edit().putString("password", password).commit();


                new Thread(runnable).start();//啟動執行序runnable
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


        sp = getSharedPreferences( "account", MODE_PRIVATE);
        String email = sp.getString("email", "");
        String password = sp.getString("password", "");
        edmail.setText(email);
        edPW.setText(password);

        //--Bluetooth
        new Thread(GetBeaconMacAddressRunnable).start();
        new Thread(GetBeaconUUIDRunnable).start();

        BluetoothInitialize();
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

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mProgressBar.setVisibility(View.INVISIBLE);
                        btnLogin.setEnabled(true);
                    }
                });

            }

        }
    };


    //--BlueTooth
    Runnable GetBeaconMacAddressRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                HttpClient client = new DefaultHttpClient();
                HttpPost post = new HttpPost(IBeaconGlobal.beacon_mac_address_url);

                HttpResponse response = client.execute(post);
                final HttpEntity entity = response.getEntity();
                Log.e("entity", String.valueOf(entity));

                String record_result = EntityUtils.toString(entity);

                JSONObject j_thing = new JSONObject(record_result);

                JSONArray record_JSA = j_thing.getJSONArray("mac");
                IBeaconGlobal.all_product_mac_address = new String[record_JSA.length()];

                if (record_JSA != null) {
                    for (int i = 0; i < record_JSA.length(); i++){
                        IBeaconGlobal.all_product_mac_address[i] = record_JSA.getString(i);
                        Log.e("all_mac_address", IBeaconGlobal.all_product_mac_address[i]);
                    }
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    Runnable GetBeaconUUIDRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                HttpClient client = new DefaultHttpClient();
                HttpPost post = new HttpPost(IBeaconGlobal.beacon_uuid_url);

                HttpResponse response = client.execute(post);
                final HttpEntity entity = response.getEntity();
                Log.e("entity", String.valueOf(entity));

                String record_result = EntityUtils.toString(entity);

                JSONObject j_thing = new JSONObject(record_result);

                JSONArray record_JSA = j_thing.getJSONArray("uuid");
                IBeaconGlobal.all_product_uuid = new String[record_JSA.length()];

                if (record_JSA != null) {
                    for (int i = 0; i < record_JSA.length(); i++){
                        IBeaconGlobal.all_product_uuid[i] = record_JSA.get(i).toString();
                        Log.e("all_uuid", IBeaconGlobal.all_product_uuid[i]);
                    }
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private void BluetoothInitialize() {
        // Check BLE Is Support Or Not
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE Not Supported", Toast.LENGTH_SHORT).show();
            finish();
        }

        BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = bluetoothManager.getAdapter();
        //
        if (  btAdapter == null) {
            Toast.makeText(this, "Bluetooth Open Failure!!", Toast.LENGTH_LONG).show();
            return;
        }

        if ( ! btAdapter.isEnabled()) {
            btAdapter.enable();
        }
    }


}

