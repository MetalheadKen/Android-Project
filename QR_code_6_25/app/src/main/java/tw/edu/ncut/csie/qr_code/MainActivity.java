package tw.edu.ncut.csie.qr_code;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static tw.edu.ncut.csie.qr_code.visitflag.user_email;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private Activity mainactivity;
    private TextView tv_1,tv_cost;
    private Button scan_btn ,get_btn ,btn_checkout;
    private WebView webView ;
    private ListView lv;
    private MyAdapter adapter;
    private ArrayAdapter<String> padapter;
    private ArrayAdapter<String> feadapter;
    private ArrayAdapter<String> tcadapter;

    private int total=0 , unit_price = 0,max_cost=0 ,over_cost = 0;
    private String number="" , item_name="",email , favor_list="";
    private View v,p,fe ;
    private int quantityArray[] = new int[20];
    private int priceArray[] = new int[20];

    //
    private String item_nameArray[] = new String[20];
    private String allitem_nameArray[] = new String[20];

    //
    private int quantity ,warning_flag=0 , alert_flag = 0, count = 0 /*, visiter_login_flag = 0*/ ;
    private String geturltitle, tel = "0911884674";
    private ConstraintLayout warning_bg;
    private ArrayList<String> content = new ArrayList<String>();
    //---比價陣列
    private String array_price[]  = new String[20];
    private String array_name[]  = new String[20];
    //private String price_url ="https://ezprice.com.tw/s/"+item_name+"/price/";
    private ArrayList<String> priceList = new ArrayList<String>();
    private ArrayList<String> fe_priceList = new ArrayList<String>();
    private ArrayList<String> tc_priceList = new ArrayList<String>();
    private String name = "";
    //----
    private Vibrator mVibrator;
    private JSONArray things ,amount , price ,priceJS ,get_Ifavor,get_Iprice;
    private String url_post = "http://140.128.88.166:8080/buy_record.php";
    private String price2[] = new String[20];
    //--
    private static Boolean isExit = false;
    private static Boolean hasTask = false;

    private String purchasing_check="";

    //---
    private int alert3Count = 30;
    private String result;
    private String get_onsale_url="http://140.128.88.166:8080/favorite.php";
    //private String get_onsale_url="http://192.168.200.66/savealerttime.php/";

    private String favor_item[] = new String[10];
    private String favor_price[] = new String[10];

    ProgressBar mProgressBar;
    //------------------------------------------------避免讓程式因為按下 Back 鍵而關閉------------------------------------------

    Timer timerExit = new Timer();
    TimerTask task = new TimerTask() {
        @Override
        public void run() {
            isExit = false;
            hasTask = true;
        }
    };

    //---Bluetooth
    private BluetoothAdapter btAdapter;

    private BluetoothLeScanner mLEScanner;
    private ScanSettings settings;
    private List<ScanFilter> filters = new ArrayList<>();

    private IBeaconService iBeaconService = new IBeaconService();

    private String[] old_product_major_minor = new String[2];
    private String[] new_product_major_minor;

    @Override
    public boolean onKeyDown(int keyCode ,  KeyEvent event){
        if(keyCode == KeyEvent.KEYCODE_BACK){
           if(isExit == false){
               isExit = true;
               Toast.makeText(this,"再按一次登出此APP",Toast.LENGTH_SHORT).show();
               if(!hasTask){
                   timerExit.schedule(task,1500);
               }
           }else{
               finish();
               System.exit(0);
           }
        }
        return false;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //-----------------------訪客不能使用的功能------------------------------------
        if(visitflag.visiter_login_flag == 1 ){ //訪客登入
            AlertDialog.Builder visiter = new AlertDialog.Builder(MainActivity.this);
            visiter.setTitle("訪客注意事項");
            visiter.setMessage("使用訪客登入部分功能無法使用\n          1.購買紀錄\n          2.喜好推播");
            visiter.setPositiveButton("OK",null);
            visiter.setNegativeButton("註冊", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent reg = new Intent(MainActivity.this, insert.class);
                    startActivity(reg);
                }
            });
            visiter.show();
        }

        if(visitflag.visiter_login_flag == 0 ){ //會員登入
            new Thread(favorrunnable).start();
        }

        findview();
        final ArrayList<Map<String,String>> list = new ArrayList<Map<String,String>>();
        adapter = new MyAdapter(this, list , R.layout.listview_use);
        padapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,priceList);
        feadapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,fe_priceList);
        tcadapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,tc_priceList);

        mVibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
        //-------------------------------------------------WebView的設定選項-------------------------------------------------------------
        WebSettings webSettings = webView.getSettings();
        webSettings.setAppCacheEnabled(true);//--
        String appCacheDir = this.getApplicationContext().getDir("cache", Context.MODE_PRIVATE).getPath();
        webSettings.setAppCachePath(appCacheDir);

        webSettings.setJavaScriptEnabled(true);
        webSettings.setSupportZoom(true);
        webSettings.setDomStorageEnabled(true);

        mainactivity=this;
        get_btn.setEnabled(false);
        //----------------------------------------------WebView載入完成後的動作-------------------------------------------------------
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                if(!webView.getSettings().getLoadsImagesAutomatically()) {
                    webView.getSettings().setLoadsImagesAutomatically(true);
                }
                geturltitle=view.getTitle();
                get_btn.setEnabled(true);
                Log.e("Scan QR code name",geturltitle);
            };
        });
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        //-------------------------------------------------------------結帳----------------------------------------------------------------------
        btn_checkout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(visitflag.visiter_login_flag == 1 ){
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("警告")
                            .setIcon(R.drawable.alert)
                            .setMessage("您尚未註冊喔，是否要註冊一個新帳號呢?")
                            .setPositiveButton("否", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .setNegativeButton("註冊", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent it = new Intent(MainActivity.this,insert.class);
                                    startActivity(it);
                                }
                            })
                            .show();
                    //Log.e("visiter_login_flag", String.valueOf(visitflag.visiter_login_flag));
                }else{
                    purchasing_check="";
                    for(int i=0; i<count; i++){
                        purchasing_check += item_nameArray[i] + " * " + quantityArray[i] + "=" + price2[i] +"元\r\n" ;
                    }
                   // Log.e("purchasing_check",purchasing_check);

                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("結帳清單")
                            .setIcon(R.drawable.cashier)
                            .setMessage("確定要結帳嗎? \r\n" + purchasing_check +"\r\n"+ "總金額為：" + String.format("%d", total) + "元")
                            .setPositiveButton("結帳", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    new Thread(postrunnable).start();
                                    btn_checkout.setEnabled(false);
                                }

                            })
                            .show();
                  //  Log.e("visiter_login_flag", String.valueOf(visitflag.visiter_login_flag));

                }
            }
        });

        //-----------------------------------------------------------------------------------------------------------------------------------------
        get_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                priceList.clear();
                fe_priceList.clear();
                tc_priceList.clear();

                btn_checkout.setEnabled(true);
                //-------------------------------------------把資料丟到Map-------------------------------------------------------------
                content.add(geturltitle);
                Map<String, String> item = new HashMap<String, String>();
                item.put("title", geturltitle);
                Log.e("name in item map",geturltitle);

                list.add(item);
                Log.e("list all item", String.valueOf(list));
                lv.setAdapter(adapter);
                //-----------------------------------------------計算單價------------------------------------------------------------------
                number = "";
                for (int i = 0; i < geturltitle.length(); i++) {
                    for (int j = 0; j < 10; j++) {
                        if (String.valueOf(geturltitle.charAt(i)).equals(String.valueOf(j))) {
                            number += geturltitle.charAt(i);
                            unit_price = Integer.valueOf(number);
                        }
                    }
                }
                //-------------------------------------------------取中文-------------------------------------------------------------------
                item_name = "";
                for (int i = 0; i < geturltitle.length(); i++) {
                    if ((String.valueOf(geturltitle.charAt(i)).getBytes().length != (String.valueOf(geturltitle.charAt(i)).length()))){
                        item_name += geturltitle.charAt(i);

                    }
                }
                //-----------------------------------------------將價錢、數量丟入陣列------------------------------------------------
                int position = content.indexOf(geturltitle);
                allitem_nameArray[position] = geturltitle;
                //----price
                priceArray[position] = unit_price;
                Log.e("position", String.valueOf(position));
                Log.e("priceArray", String.valueOf(priceArray));
                int price_count =0;
                for(int i = 0; priceArray[i] != 0; i++) {
                    price_count++;
                }
                String price_Array[] = new String[price_count];
                for(int i = 0; priceArray[i] != 0; i++) {
                    price_Array[i] = String.valueOf(priceArray[i]);
                }

                price = new JSONArray(Arrays.asList(price_Array));
                Log.e("priceArray" , String.valueOf(price));
                //------------------Java Array or ArrayList to Json Array in android--------------------------------------------------
                //--quantity
                quantityArray[position] = quantity;
                Log.e("quantityArray" , String.valueOf(quantity));
                item_nameArray[position] = item_name;
                //--------------------------------------------------- Refresh edittext-------------------------------------------------------
                for(int i = 0; i < position; i++) {
                    Map<String, String> item2 = new HashMap<String, String>();
                    item2.put("title", allitem_nameArray[i]);
                    // Log.e("title2222",allitem_nameArray[i]);
                    item2.put("count", quantityArray[i] + "");
                    Log.e("quantity22222", String.valueOf(quantityArray[i]));

                    list.remove(i);
                    list.add(i, item2);
                }
                adapter.notifyDataSetChanged();
                //-----------------------------------------------計算總花費---------------------------------------------------------------
                count += 1;
                get_btn.setEnabled(false);
            }
        });

        //-------------------------讀取手機內建的 cord掃描器-----------------------------
        scan_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentIntegrator scan = new IntentIntegrator(mainactivity);
                //設置二維碼掃描屏幕的參數
                scan.addExtra("SCAN_WIDTH",800); //掃描框成正方形
                scan.addExtra("SCAN_HEIGHT", 800);
                scan.addExtra("RESULT_DISPLAY_DURATION_MS", 3000L);
                scan.initiateScan();
            }
        });

        //--------------------------------------------------------alert people cost--------------------------------------------
        if(alert_flag == 1){
            if (max_cost < total && warning_flag == 0) {
                // warning_bg.setBackgroundResource(0);

                warning_flag = 1;
                Log.e("warning_flag1", String.valueOf(warning_flag));
                Log.e("over_cost", String.valueOf(over_cost));

                new Thread(){
                    @Override
                    public void run() {
                        try {
                            sleep(1500);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    over_cost = total - max_cost;
                                    if(over_cost > 0){
                                        AlertDialog.Builder m = new AlertDialog.Builder(MainActivity.this);
                                        m.setTitle("");
                                        m.setMessage("您的消費預算以超出：" + over_cost + "元");
                                        m.setPositiveButton("OK", null);
                                        m.show();
                                    }
                                }
                            });
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();

            }else if (max_cost < total && warning_flag == 1) {
                warning_flag = 2;
                warning_bg.setBackgroundResource(R.drawable.warning);
                Log.e("warning_flag2", String.valueOf(warning_flag));
                //----------停0.3秒 震動1秒 3次
                mVibrator.vibrate(new long[]{300, 1000, 300, 1000, 300, 1000}, -1);
                //
                //warning_flag = 0;
            }else if (max_cost < total && warning_flag == 2) {
                warning_flag = 3;
                Log.e("warning_flag3", String.valueOf(warning_flag));
                get_btn.setEnabled(false);
                scan_btn.setEnabled(false);
                new Thread(){
                    @Override
                    public void run() {
                        try {
                            sleep(1000);
                            runOnUiThread(new Thread(){
                                @Override
                                public void run() {
                                    AlertDialog.Builder alert3 = new AlertDialog.Builder(MainActivity.this);
                                    alert3.setTitle("嚴重超支警告!!!");
                                    alert3.setIcon(R.drawable.money_fly);
                                    alert3.setMessage("您超支次數過多，請等待30秒，考慮清楚後再購買！！！");
                                    alert3.show();
                                }
                            });
                            for(int i = 30; i >= 0; i--){
                                sleep(1000);
                                runOnUiThread(new Thread(){
                                    @Override
                                    public void run() {
                                        scan_btn.setText(alert3Count +"");
                                        alert3Count -= 1;
                                        Log.e("alert3Count", String.valueOf(alert3Count));
                                    }
                                });

                            }
                            runOnUiThread(new Thread(){
                                @Override
                                public void run() {
                                    scan_btn.setText("SCANNER");
                                    if(scan_btn.getText().equals("SCANNER")){
                                        scan_btn.setEnabled(true);

                                        alert3Count = 30;
                                        warning_bg.setBackgroundResource(R.drawable.white);
                                    }
                                }
                            });

                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();

                warning_flag = 0;
                Log.e("warning_flag0", String.valueOf(warning_flag));
            }
        }


        //--Bluetooth
        BluetoothInitialize();
        doScanBleDevices();
    }

    private void findview() {
        get_btn = (Button)findViewById(R.id.get_btn);
        btn_checkout = (Button)findViewById(R.id.btn_checkout);
        webView = (WebView) findViewById(R.id.webView);
        lv = (ListView)findViewById(R.id.lv);
        tv_1 = (TextView)findViewById(R.id.tv_1);
        tv_cost = (TextView)findViewById(R.id.tv_cost);
        scan_btn = (Button)findViewById(R.id.scan_btn);
        warning_bg = (ConstraintLayout)findViewById(R.id.activity_main);
        mProgressBar = (ProgressBar)findViewById(R.id.progressBar);
        mProgressBar.setVisibility(View.GONE);

    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent){
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if(scanningResult!=null){
            String scanContent=scanningResult.getContents();
            if(Build.VERSION.SDK_INT >= 19) {
                webView.getSettings().setLoadsImagesAutomatically(true);
            } else {
                webView.getSettings().setLoadsImagesAutomatically(false);
            }
            webView.loadUrl(scanContent);
        }else{
            Toast.makeText(getApplicationContext(),"nothing",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, final int position, long id) {
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
/*
            case R.id.menu_scanner:
                //-------------------------讀取手機內建的 cord掃描器-----------------------------
                IntentIntegrator scan = new IntentIntegrator(mainactivity);
                //設置二維碼掃描屏幕的參數
                scan.addExtra("SCAN_WIDTH",800); //掃描框成正方形
                scan.addExtra("SCAN_HEIGHT", 800);
                scan.addExtra("RESULT_DISPLAY_DURATION_MS", 3000L);
                scan.initiateScan();
                break;
*/
            case R.id.menu_maxCost:
                alert_flag = 1;
                //-------------------------最大預算Layout-------------------------------
                // //inflate目的是把自己設計xml的Layout轉成View，作用類似於findViewById，它用於一個沒有被載入或者想要動態
                LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
                v = inflater.inflate(R.layout.alertdialog_use, null);
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("請輸入您的預算")
                        .setIcon(R.drawable.maxbudget)
                        .setView(v)//---using AlertDialog if you want to define yours AlertDialog , must use setView to set Layout
                        .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                EditText ed_cost = (EditText)(v.findViewById(R.id.editText1));
                                if(!ed_cost.getText().toString().equals("")){
                                    tv_cost.setText("最高預算為:"+ed_cost.getText()+"元");
                                    max_cost = Integer.parseInt(ed_cost.getText().toString());
                                }
                            }
                        })
                        .show();
                break;
            case R.id.menu_maturity:
                Intent itMaturity = new Intent(MainActivity.this, MaturityActivity.class);
                startActivity(itMaturity);
                break;
            case R.id.menu_logout:
                visitflag.login_flag = 1;
                finish();
                /*
                Intent it = new Intent(MainActivity.this,MainActivity2.class);
                startActivity(it);
                */
                break;
            case R.id.menu_onSale:
                Intent onSale = new Intent(MainActivity.this,BargainActivity.class);
                startActivity(onSale);
                break;
            case R.id.menu_record:
                Intent record = new Intent(MainActivity.this,recordActivity.class);
                startActivity(record);
                break;
            case R.id.menu_map:
                Intent map = new Intent(MainActivity.this,RoutePlanningActivity.class);
                startActivity(map);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);

    }

    //----------------------------------------------------------------自訂Adapter----------------------------------------------------------------
    private class MyAdapter extends BaseAdapter{
        private  Activity activity;
        private  List<Map<String, String>> list;
        private  int layout;
        private Map<String, String> item;

        public MyAdapter(Activity activity, List<Map<String, String>> list, int listview_use) {
            this.activity = activity;
            this.list = list;
            this.layout = listview_use;
        }
        //---------------------------------------------------------------刪除item---------------------------------------------------------------
        public void removeItem(int index){
            list.remove(index);
        }

        @Override
        public int getCount() {
            if(list != null)
                return list.size();//幾筆資料
            else
                return 0;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        private Integer index = -1;
        @Override
        public View getView(final int position, View convertView, ViewGroup viewGroup) {
            if(convertView == null) {
                convertView = activity.getLayoutInflater().inflate(this.layout, null);//吹一個出來( 有格子了 還沒有資料)
            }
            final TextView tv_item = (TextView) convertView.findViewById(R.id.tv_item);
            final EditText ed_quantity = (EditText) convertView.findViewById(R.id.ed_quantity);
            tv_item.setTextSize(17);

            item = list.get(position);

            tv_item.setText(item.get("title"));
            ed_quantity.setText(item.get("count"));
            //
            tv_item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    priceList.clear();
                    fe_priceList.clear();
                    tc_priceList.clear();


                    item_name = item_nameArray[position];
                    //Log.e("tv_item.setOnClickListener" ,item_name);

                    new Thread(runnable).start();
                    new Thread(ferunnable).start();
                    new Thread(tcrunnable).start();

                    mProgressBar.setVisibility(View.VISIBLE);
                    if(!array_name.equals("")){
                        AlertDialog.Builder del = new AlertDialog.Builder(MainActivity.this);
                        del.setIcon(R.drawable.info);
                        del.setTitle("目前商品資訊");
                        del.setMessage(item_nameArray[position]);
                        del.setPositiveButton("刪除此商品", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface arg0, int arg1) {
                                Log.e("DEL position", position + "");
                                //adapter.notifyDataSetChanged();
                                list.clear();
                                adapter.notifyDataSetChanged();

                                content.remove(position);

                                Log.e("count_delete", String.valueOf(count));

                                for(int i = position; i < count -1; i++) {
                                    //Move Forward
                                    //price2[i] = price2[i+1];

                                    item_nameArray[i] = item_nameArray[i + 1];
                                    allitem_nameArray[i] = allitem_nameArray[i + 1];
                                    quantityArray[i] = quantityArray[i + 1];
                                    priceArray[i] = priceArray[i + 1];

                                    Log.e("name detete", allitem_nameArray[i] + "");
                                    //Log.e("price detete", priceArray[i] + "");
                                    //Log.e("quantity detete", quantityArray[i] + "");
                                }

                                //price2[count -1] = null;

                                item_nameArray[count -1] = null;
                                allitem_nameArray[count -1] = null;
                                quantityArray[count -1] = 0;
                                priceArray[count -1] = 0;
                                ////
                                /*
                                List<int[]> listChange = Arrays.asList(priceArray);
                                listChange.remove(count );
                                String pa[] = new String[listChange.size()];
                                listChange.toArray(pa);
                                for(int i=0; i<pa.length; i++){
                                    priceArray[i] = Integer.parseInt(pa[i]);
                                    Log.e("pa",pa[i]);
                                }
                                */

                                //Log.e("item_nameArray[count]",allitem_nameArray[count]);
                                //Log.e("quantityArray[count -1]", String.valueOf(quantityArray[count]));
                                //Log.e("priceArray[count -1]", String.valueOf(priceArray[count]));
                                Log.e("priceArray_DEL", String.valueOf(priceArray));
                                Log.e("quantityArray_DEL", String.valueOf(quantityArray));
                                //---------------------------------------- Refresh edittext------------------------------------------------
                                for(int i = 0; i < count -1; i++) {
                                    Map<String, String> item2 = new HashMap<String, String>();
                                    item2.put("title", allitem_nameArray[i]);
                                    //Log.e("title detete",allitem_nameArray[i]);

                                    item2.put("count", quantityArray[i] + "");
                                    //Log.e("quantity detete", String.valueOf(quantityArray[i]));

                                    list.add(i,item2);
                                }
                                adapter.notifyDataSetChanged();
                                //-----------------------------------------------------------------------------------------------------------
                                total = 0;
                                for (int i = 0; priceArray[i] != 0/*count -1*/; i++) {
                                    total += quantityArray[i] * priceArray[i];
                                    Log.e("quantityArray",quantityArray[i]+"");
                                    Log.e("priceArray",priceArray[i]+"");
                                }

                                Log.e("total---------", String.valueOf(quantityArray[position] * priceArray[position]));

                                tv_1.setText("總金額為:" + String.format("%d",total) + "元" );
                                Log.e("total_delete", String.valueOf(total));
                                count -= 1;

                            }
                        });
                        del.setNeutralButton("廣域查詢", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
                                p = inflater.inflate(R.layout.parity_use, null);

                                AlertDialog.Builder all = new AlertDialog.Builder(MainActivity.this);
                                all.setIcon(R.drawable.taiwan);
                                all.setTitle("價格資訊");
                                all.setView(p);//---using AlertDialog if you want to define yours AlertDialog , must use setView to set Layout
                                all.show();

                                ListView parity_View = (ListView)(p.findViewById(R.id.parity_View));
                                parity_View.setAdapter(padapter);

                            }
                        });

                        del.setNegativeButton("區域查詢", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
                                fe = inflater.inflate(R.layout.parity2_use, null);
                                new AlertDialog.Builder(MainActivity.this)
                                        .setTitle("價格資訊")
                                        .setView(fe)
                                        .show();
                                ListView lv_fe = (ListView)(fe.findViewById(R.id.lv_fe));
                                lv_fe.setAdapter(feadapter);
                                ListView lv_tc = (ListView)(fe.findViewById(R.id.lv_tc));
                                lv_tc.setAdapter(tcadapter);
                            }
                        });
                        del.show();
                        mProgressBar.setVisibility(View.INVISIBLE);
                    }
                }
            });
            ed_quantity.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    total = 0;
                    for (int i = 0; priceArray[i] != 0; i++) {
                        total += quantityArray[i] * priceArray[i];
                        Log.e("beforeTextChanged",quantityArray[i]+" ,"+priceArray[i]);
                    }
                }
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }
                @Override
                public void afterTextChanged(Editable s) {

                    if(!ed_quantity.getText().toString().equals("")) {
                        quantity = Integer.valueOf(ed_quantity.getText().toString());
                        quantityArray[position]=quantity;

                        //
                        for(int i=0; i<count; i++){
                            price2[i] = String.valueOf(quantityArray[i] * priceArray[i]);
                            Log.e("price2 q*p", quantityArray[i] +"*"+ priceArray[i]);
                        }

                        total = 0;
                        for (int i = 0; priceArray[i] != 0; i++) {
                            total += quantityArray[i] * priceArray[i];
                            Log.e("afterTextChanged", priceArray[i] +","+ quantityArray[i]);
                        }
                    }
                    else{
                        quantity = 0;
                    }

                    tv_1.setText("總金額為:" + String.format("%d",total) + "元" );
                    Log.e("total---afterchange", String.valueOf(total));

                    //--------------------------------------------------------alert people cost--------------------------------------------
                    if(alert_flag == 1){
                        if (max_cost < total && warning_flag == 0) {
                            // warning_bg.setBackgroundResource(0);

                            warning_flag = 1;
                            Log.e("warning_flag1", String.valueOf(warning_flag));
                            Log.e("over_cost", String.valueOf(over_cost));

                            new Thread(){
                                @Override
                                public void run() {
                                    try {
                                        sleep(1500);
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                over_cost = 0;
                                                over_cost = total - max_cost;
                                                if(over_cost > 0){
                                                    AlertDialog.Builder m = new AlertDialog.Builder(MainActivity.this);
                                                    m.setTitle("");
                                                    m.setMessage("您的消費預算以超出：" + over_cost + "元");
                                                    m.setPositiveButton("OK", null);
                                                    m.show();
                                                }
                                            }
                                        });
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }.start();

                        }else if (max_cost < total && warning_flag == 1) {
                            warning_flag = 2;
                            warning_bg.setBackgroundResource(R.drawable.warning);
                            Log.e("warning_flag2", String.valueOf(warning_flag));
                            //----------停0.3秒 震動1秒 3次
                            mVibrator.vibrate(new long[]{300, 1000, 300, 1000, 300, 1000}, -1);
                            //
                            //warning_flag = 0;
                        }else if (max_cost < total && warning_flag == 2) {
                            warning_flag = 3;
                            Log.e("warning_flag3", String.valueOf(warning_flag));
                            get_btn.setEnabled(false);
                            scan_btn.setEnabled(false);
                            new Thread(){
                                @Override
                                public void run() {
                                    try {
                                        sleep(1000);
                                        runOnUiThread(new Thread(){
                                            @Override
                                            public void run() {
                                                AlertDialog.Builder alert3 = new AlertDialog.Builder(MainActivity.this);
                                                alert3.setTitle("嚴重超支警告!!!");
                                                alert3.setIcon(R.drawable.money_fly);
                                                alert3.setMessage("您超支次數過多，請等待30秒，考慮清楚後再購買！！！");
                                                alert3.show();
                                            }
                                        });
                                        for(int i = 30; i >= 0; i--){
                                            sleep(1000);
                                            runOnUiThread(new Thread(){
                                                @Override
                                                public void run() {
                                                    scan_btn.setText(alert3Count +"");
                                                    alert3Count -= 1;
                                                    Log.e("alert3Count", String.valueOf(alert3Count));
                                                }
                                            });

                                        }
                                        runOnUiThread(new Thread(){
                                            @Override
                                            public void run() {
                                                scan_btn.setText("SCANNER");
                                                if(scan_btn.getText().equals("SCANNER")){
                                                    scan_btn.setEnabled(true);

                                                    alert3Count = 30;
                                                    warning_bg.setBackgroundResource(R.drawable.white);
                                                }
                                            }
                                        });

                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }.start();

                            warning_flag = 0;
                            Log.e("warning_flag0", String.valueOf(warning_flag));
                        }
                    }

                }
            });
            if(item.get(position) != null){
                //ed_quantity.setText(item.get(position));
                //ed_quantity.setText(quantityArray[position]);
                //ed_quantity.getTag(position).s
            }
            return convertView;
        }
    }
    Runnable runnable = new Runnable(){
        @Override
        public void run(){
            try {
                String price_url ="https://ezprice.com.tw/s/"+item_name+"/price/";
                //String price_url ="https://ezprice.com.tw/s/"+"富士蜜蘋果"+"/price/";
                Log.e("aaa", price_url);
                Document doc = Jsoup.connect(price_url).get();
                Elements item_names = doc.select("h4");//div.pd-name
                Elements item_prices = doc.select("span.num");

                int count = 0;
                for(Element itemname : item_names){
                    array_name[count] = itemname.text();
                    Log.e("count" ,array_name[count]);
                    count ++;
                    //name += itemname.text();
                    // priceList.add(itemname.text());
                }
                int count2 = 0;
                for(Element itemprice : item_prices){
                    //name += itemname.text();
                    array_price[count2] = itemprice.text();
                    Log.e("price" , array_price[count2]);
                    //Log.e("price2" , itemprice.text());

                    //priceList.add(itemprice.text());
                    count2 ++;
                }
                for(int i=0 ; i < count;i ++){
                    priceList.add(array_name[i] + " " + "【  " +array_price[i] +" 】" );
                }

                priceList.add("------------------------------------------------------------------------------------------------");
                name = "";
                Log.e("XXXXXXXXXXXXX", "XXXXXXXXXXXXX");
                Log.e("priceList", String.valueOf(priceList));

            }catch(Exception e){
                e.printStackTrace();
            }
        }
    };

    //--
    Runnable ferunnable = new Runnable(){
        @Override
        public void run(){
            try {

                String price_url ="http://www.gohappy.com.tw/ec2/search?sid=12&hotNum=0&search=" + item_name;
                Log.e("aaa", price_url);
                Document doc = Jsoup.connect(price_url).get();
                Elements item_names = doc.select("h3");//div.pd-name
                Elements item_prices = doc.select("table.price-table");

                int count = 0;
                for(Element itemname : item_names){
                    array_name[count] = itemname.text();
                    count ++;

                }
                int count2 = 0;
                for(Element itemprice : item_prices){
                    //name += itemname.text();
                    array_price[count2] = itemprice.text();
                    count2 ++;
                }
                for(int i=0 ; i < count;i ++){
                    fe_priceList.add(array_name[i] + " " + "【  " +array_price[i] +" 】" );
                }

                fe_priceList.add("----------------------------------------------------------");
                name = "";

            }catch(Exception e){
                e.printStackTrace();
            }
        }
    };
    Runnable tcrunnable = new Runnable(){
        @Override
        public void run(){
            try {

                String price_url ="http://www.gohappy.com.tw/ec2/search?sid=145&hotNum=0&search=" + item_name;
                Log.e("aaa", price_url);
                Document doc = Jsoup.connect(price_url).get();
                Elements item_names = doc.select("h3");//div.pd-name
                Elements item_prices = doc.select("table.price-table");

                int count = 0;
                for(Element itemname : item_names){
                    array_name[count] = itemname.text();
                    count ++;

                }
                int count2 = 0;
                for(Element itemprice : item_prices){
                    //name += itemname.text();
                    array_price[count2] = itemprice.text();
                    count2 ++;
                }
                for(int i=0 ; i < count;i ++){
                    tc_priceList.add(array_name[i] + " " + "【  " +array_price[i] +" 】" );
                }

                tc_priceList.add("----------------------------------------------------------");
                name = "";

            }catch(Exception e){
                e.printStackTrace();
            }
        }
    };
    Runnable postrunnable = new Runnable() {
        @Override
        public void run() {
            try {
                int priceJScount = 0;
                for(int i=0; price2[i] != null ; i++){
                    priceJScount++;
                }
                String priceJSON[] = new String[priceJScount];
                for(int i = 0; price2[i] != null; i++) {
                    priceJSON[i] = price2[i];
                }
                priceJS = new JSONArray(Arrays.asList(priceJSON));
                Log.e("price2", String.valueOf(priceJS));

                ///////////////////////////////////////////////////
                int countArray = 0;
                for(int i = 0; item_nameArray[i] != null; i++) {
                    countArray++;
                }
                String item_name_Array[] = new String[countArray];
                for(int i = 0; item_nameArray[i] != null; i++) {
                    item_name_Array[i] = item_nameArray[i];
                }
                things = new JSONArray(Arrays.asList(item_name_Array));
                Log.e("JSON item name", String.valueOf(things));

                ///////////////////////////////////////////////////
                int countArrayq = 0;
                for(int i = 0; quantityArray[i] != 0; i++) {
                    countArrayq++;
                }

                String quantity_Array[] = new String[countArrayq];
                for(int i = 0; quantityArray[i] != 0; i++) {
                    quantity_Array[i] = quantityArray[i] + "";
                }
                amount = new JSONArray(Arrays.asList(quantity_Array));
                Log.e("JSON Quentity", String.valueOf(amount));

                ///////////////////////////////////////////////////
                HttpClient client = new DefaultHttpClient();
                HttpPost post = new HttpPost(url_post);

                JSONObject json = new JSONObject();
                json.put("email",user_email);
                json.put("things",things);
                json.put("amounts",amount);
                json.put("price",priceJS);

                post.setEntity(new ByteArrayEntity(json.toString().getBytes("UTF8")));
                post.setHeader("json",json.toString());

                Log.e("JSONDATA", String.valueOf(json));


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

    Runnable favorrunnable = new Runnable() {
        @Override
        public void run() {
            try {

                HttpClient client = new DefaultHttpClient();
                HttpPost post = new HttpPost(get_onsale_url);

                List<NameValuePair> vars=new ArrayList< NameValuePair>();
                vars.add(new BasicNameValuePair("email",user_email));
                Log.e("email222", String.valueOf(vars));

                post.setEntity(new UrlEncodedFormEntity(vars, HTTP.UTF_8));

                HttpResponse response = client.execute(post);
                HttpEntity entity = response.getEntity();
                //post.setHeader("json",json.toString());

                // Get Something
                result = EntityUtils.toString(entity);
                //Log.e("result",result);

                JSONObject j_thing = new JSONObject(result);
                JSONArray jsp = j_thing.getJSONArray("thing");
                JSONArray jspp = j_thing.getJSONArray("price");

                //Log.e("jsp",jsp.toString());
                //Log.e("jspp",jspp.toString());

                String thingArray[] = new String[jsp.length()];
                String priceArray[] = new String[jspp.length()];

                favor_list = "";
                for(int i=0;i<jsp.length();i++) {
                    thingArray[i] = jsp.getString(i);
                    priceArray[i] = jspp.getString(i);

                    favor_list +=  thingArray[i] +"  NT"+ priceArray[i] + "\n";
                    Log.e("favor_list",favor_list);
                    Log.e("thingArray", thingArray[i]+"");
                    Log.e("priceArray", priceArray[i]+"");
                }
                if(!favor_list.isEmpty()){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            while (favor_list.isEmpty()){;}//空轉，等抓完資料，show Dialog
                            //----------------------------------------------------------特價告知----------------------------------------------------------------
                            if(favor_list != null){
                                AlertDialog.Builder favor = new AlertDialog.Builder(MainActivity.this);
                                favor.setIcon(R.drawable.today_on_sale);
                                favor.setTitle("您購買過的商品，今天有特價喔");
                                //favor.setIcon();
                                favor.setMessage(favor_list);
                                Log.e("favor_list_dialog",favor_list);
                                favor.show();
                            }
                        }
                    });
                }



            } catch (UnsupportedEncodingException e) {
               Log.e("AA",e.getMessage()+"");
            } catch (ClientProtocolException e) {
                e.printStackTrace();
                Log.e("AA",e.getMessage()+"");
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("AA",e.getMessage()+"");
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("AA",e.getMessage()+"");
            }

        }
    };

    //--Bluetooth
    Runnable GetProductInformationRunnable = new Runnable() {
        @Override
        public void run() {
            Message msg = new Message();
            Bundle data = new Bundle();
            msg.setData(data);

            try {
                //連線到 url網址
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost method = new HttpPost(IBeaconGlobal.product_information_url);

                //傳值給PHP
                List<NameValuePair> vars = new ArrayList< NameValuePair>();
                vars.add(new BasicNameValuePair("major", new_product_major_minor[0]));
                vars.add(new BasicNameValuePair("minor", new_product_major_minor[1]));
                method.setEntity(new UrlEncodedFormEntity(vars, HTTP.UTF_8));

                //接收PHP回傳的資料
                HttpResponse response = httpclient.execute(method);
                HttpEntity entity = response.getEntity();

                String record_result = EntityUtils.toString(entity);

                Log.e("entity2", record_result);

                JSONObject j_thing = new JSONObject(record_result);

                JSONArray record_JSA = j_thing.getJSONArray("push");

                if (record_JSA != null && record_JSA.length()  != 0) {
                    IBeaconGlobal.product_push_information = "";

                    for (int i = 0; i < record_JSA.length(); i++){
                        IBeaconGlobal.product_push_information += record_JSA.getString(i) + "\n";
                        Log.e("all_uuid", IBeaconGlobal.product_push_information);
                    }

                    IBeaconGlobal.product_push_information = IBeaconGlobal.product_push_information.substring(0, IBeaconGlobal.product_push_information.length() - 2);
                }

                //Send Message To Handler
                if(record_JSA != null && record_JSA.length() != 0) {
                    data.putString("product_id", IBeaconGlobal.product_push_id + "");
                    data.putString("product_information", IBeaconGlobal.product_push_information);
                    handler_Success.sendMessage(msg);

                    IBeaconGlobal.product_push_id = (IBeaconGlobal.product_push_id + 1) % 2;
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

    Handler handler_Success = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();

            int id = Integer.parseInt(data.getString("product_id"));
            String information = data.getString("product_information");

            ShowProductNotification(id, information);
        }

    };

    private void BluetoothInitialize() {
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

        if (Build.VERSION.SDK_INT >= 21) {
            mLEScanner = btAdapter.getBluetoothLeScanner();
            settings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build();
            filters = new ArrayList<ScanFilter>();

            if ((null) == IBeaconGlobal.all_product_mac_address) return;

            for (int i = 0; i < IBeaconGlobal.all_product_mac_address.length; i++) {
                ScanFilter.Builder scanFilter = new ScanFilter.Builder().setDeviceAddress(IBeaconGlobal.all_product_mac_address[i]);

                filters.add(scanFilter.build());
            }
        }
    }

    public void doScanBleDevices() {
        iBeaconService.devices.clear();
        iBeaconService.deviceMacList.clear();

        // BLE Callback
        if (Build.VERSION.SDK_INT < 21) {
            btAdapter.startLeScan(myLeScanCallback);
            // btAdapter.startLeScan( UUID[], myLeScanCallback);
            // ex: 0000180d-0000-1000-8000-00805f9b34fb : UUID for heart rate monitors.
        } else {
            mLEScanner.startScan(filters, settings, mScanCallback);
        }
    }

    public void cancelScanBluetoothDevices() {
        if (Build.VERSION.SDK_INT < 21) {
            btAdapter.stopLeScan(myLeScanCallback);
        } else {
            mLEScanner.stopScan(mScanCallback);
        }
    }

    // --- LeScanCannback
    private BluetoothAdapter.LeScanCallback myLeScanCallback  = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            iBeaconService.GetBluetoothDeviceDetail( device, rssi, scanRecord);
        }
    };

    private ScanCallback mScanCallback = new ScanCallback() {
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice btDevice = result.getDevice();

            iBeaconService.GetBluetoothDeviceDetail( btDevice, result.getRssi(), result.getScanRecord().getBytes() );
            new_product_major_minor = iBeaconService.GetProductInformation( btDevice, IBeaconGlobal.all_product_uuid);

            if (new_product_major_minor == null) return;

            if (!new_product_major_minor[0].equals(old_product_major_minor[0]) && !new_product_major_minor[1].equals(old_product_major_minor[1])) {
                old_product_major_minor[0] = new_product_major_minor[0];
                old_product_major_minor[1] = new_product_major_minor[1];

                new Thread(GetProductInformationRunnable).start();
            }
        }

        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult sr : results) {
                Log.i("ScanResult - Results", sr.toString());
            }
        }

        public void onScanFailed(int errorCode) {
            Log.e("Scan Failed", "Error Code: " + errorCode);
        }
    };

    private void ShowProductNotification(int product_id, String product_information) {
        // 建立震動效果，陣列中元素依序為停止、震動的時間，單位是毫秒
        long[] vibrate_effect = {1000, 500, 1000, 400, 1000, 300, 1000, 200, 1000, 100};

        int notifyID = product_id;    // 通知的識別號碼
        final boolean autoCancel = true; // 點擊通知後是否要自動移除掉通知

        final int requestCode = notifyID;   // PendingIntent的Request Code

        final Intent intent = getIntent(); // 目前Activity的Intent
        final int flags = PendingIntent.FLAG_CANCEL_CURRENT; // ONE_SHOT：PendingIntent只使用一次；CANCEL_CURRENT：PendingIntent執行前會先結束掉之前的；NO_CREATE：沿用先前的PendingIntent，不建立新的PendingIntent；UPDATE_CURRENT：更新先前PendingIntent所帶的額外資料，並繼續沿用
        final PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), requestCode, intent, flags); // 取得PendingIntent

        final Notification.BigTextStyle bigTextStyle = new Notification.BigTextStyle(); // 建立BigTextStyle
        bigTextStyle.setBigContentTitle("附近有商品特價喔！"); // 當BigTextStyle顯示時，用BigTextStyle的setBigContentTitle覆蓋setContentTitle的設定
        bigTextStyle.bigText(product_information); // 設定BigTextStyle的文字內容

        //通知什麼商品有特價
        final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE); // 取得系統的通知服務
        final Notification notification =
                new Notification.Builder(getApplicationContext())
                        .setSmallIcon(R.drawable.onsale)
                        .setContentTitle("附近有商品特價喔！")
                        .setContentText(product_information)
                        .setContentIntent(pendingIntent)
                        .setPriority(Notification.PRIORITY_HIGH)
                        .setVibrate(vibrate_effect)
                        .setAutoCancel(autoCancel)
                        .setStyle(bigTextStyle)
                        .build(); // 建立通知

        notificationManager.notify(notifyID, notification); // 發送通知

        //通知有商品特價，請查看　
        notifyID = IBeaconGlobal.PRODUCT_PUSH_ID;

        final Notification notification_information =
                new Notification.Builder(getApplicationContext())
                        .setSmallIcon(R.drawable.onsale)
                        .setContentTitle("附近有商品特價喔！")
                        .setContentText("請查看以下特價商品")
                        .setContentIntent(pendingIntent)
                        .setPriority(Notification.PRIORITY_HIGH)
                        .setVibrate(vibrate_effect)
                        .build(); // 建立通知

        notificationManager.notify(notifyID, notification_information); // 發送通知
    }

    @Override
    protected void onPause() {
        super.onPause();

        //cancelScanBluetoothDevices();
    }

    @Override
    protected void onResume() {
        super.onResume();

        //doScanBleDevices();
    }


}