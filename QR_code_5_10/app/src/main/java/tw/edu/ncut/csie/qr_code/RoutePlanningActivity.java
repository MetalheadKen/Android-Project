package tw.edu.ncut.csie.qr_code;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class RoutePlanningActivity extends Activity implements View.OnClickListener, SensorEventListener {

    private IBeaconService iBeaconService = new IBeaconService();

    private BluetoothAdapter btAdapter;

    // > Android 5.0.1
    private BluetoothLeScanner mLEScanner;
    private ScanSettings settings;
    private List<ScanFilter> filters;

    private Context context;

    public int [] origin_coordinate;

    private Button btnPath;
    private Socket socket;
    private Spinner spDestination;
    private MapView mapView;
    private SensorManager sensor;
    private Sensor accelerometer;                   /* 加速度傳感器 */
    private Sensor magnetic;                        /* 地磁場傳感器 */

    private byte [] route = new byte[64];
    private String recv = "";

    private int rotation, orientation;
    private float[] accelerometerValues = new float[3];
    private float[] magneticValues      = new float[3];

    private String[] old_product_major_minor = new String[2];
    private String[] new_product_major_minor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_route_planning);

        GetViews();

        ArrayAdapter<CharSequence> PlaceApapter = ArrayAdapter.createFromResource(this, R.array.place_array, android.R.layout.simple_spinner_item);
        PlaceApapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spDestination.setAdapter(PlaceApapter);
        //
        spDestination.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {

                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                }
        );

        /* 取得螢幕的顯示方向來改變東西南北的方位 */
        rotation = RoutePlanningActivity.this.getWindowManager().getDefaultDisplay().getRotation();

        /* 註冊 Sensor */
        accelerometer = sensor.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetic = sensor.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        sensor.registerListener(RoutePlanningActivity.this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensor.registerListener(RoutePlanningActivity.this, magnetic, SensorManager.SENSOR_DELAY_NORMAL);
        //
        btnPath.setOnClickListener(this);

        /* Connect The Socket */
        new Thread() {
            public void run() {
                String ipAddr = "192.168.200.176";
                int port = 8998;

                try {
                    socket = new Socket(ipAddr, port);
                    showToast("連線成功");
                }
                catch (UnknownHostException ex) {
                    showToast("連線失敗:" + ex.getMessage());
                }
                catch (IOException e) {
                    showToast("連線失敗" + e.getMessage());
                }
            }
        }.start();

        BluetoothInitialize();
        doScanBleDevices();
    }

    private void GetViews() {
        btnPath       = (Button) findViewById(R.id.btnPath);
        spDestination = (Spinner) findViewById(R.id.spDestination);
        mapView       = (MapView) findViewById(R.id.imgMap);
        sensor        = (SensorManager) getSystemService(SENSOR_SERVICE);
    }

    public void BluetoothInitialize() {
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

        if (Build.VERSION.SDK_INT >= 21) {
            mLEScanner = btAdapter.getBluetoothLeScanner();
            settings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build();
            filters = new ArrayList<ScanFilter>();

            /*if ((null) == IBeaconGlobal.all_product_mac_address) return;

            for (int i = 0; i < IBeaconGlobal.all_product_mac_address.length; i++) {
                ScanFilter.Builder scanFilter = new ScanFilter.Builder().setDeviceAddress(IBeaconGlobal.all_product_mac_address[i]);

                filters.add(scanFilter.build());
            }*/
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

    private int StringToInteger(byte string[]) {
        int number = 0;

        for (int i = 0; i < string.length; i++) {
            if (string[i] >= 0x30 && string[i] <= 0x39) {
                number = number * 10 + string[i] - 0x30;
            }
        }

        return number;
    }

    private String PositionToArrayIndex(int place) {
        switch (place) {
            case 401:
            case 406:
                return "8,3";
            case 403:
            case 407:
                return "8,5";
            case 404:
            case 408:
                return "8,6";
            case 405:
            case 463:
                return "8,8";
            case 409:
                return "4,6";
            case 410:
                return "3,6";
            case 411:
                return "2,6";
            case 412:
                return "1,6";
            case 413:
                return "1,12";
            case 414:
                return "2,12";
            case 415:
                return "3,12";
            case 416:
                return "4,12";
            case 4171:
                return "8,13";
            case 4172:
            case 422:
                return "8,11";
            case 418:
            case 427:
                return "8,16";
            case 4191:
            case 429:
                return "8,18";
            case 4192:
            case 430:
                return "8,19";
            case 4193:
            case 420:
            case 431:
                return "8,20";
            case 421:
                return "8,10";
            case 423:
                return "8,12";
            case 424:
                return "8,13";
            case 425:
                return "8,14";
            case 426:
                return "8,15";
            case 428:
                return "8,17";
            case 432:
                return "8,21";
            case 433:
                return "4,19";
            case 434:
                return "3,19";
            case 4341:
                return "2,19";
            case 435:
                return "1,19";
            case 436:
                return "1,25";
            case 437:
                return "2,25";
            case 439:
                return "4,25";
            case 440:
                return "8,25";
            case 4401:
            case 444:
                return "8,27";
            case 441:
            case 445:
                return "8,28";
            case 442:
                return "8,29";
            case 443:
            case 472:
                return "8,23";
            case 4431:
                return "8,26";
            case 446:
                return "8,30";
            case 462:
                return "8,7";
            case 473:
                return "8,24";
        }

        return null;
    }

    BufferedWriter bw;
    BufferedReader br;

    @Override
    public void onClick(View v) {
        IBeaconGlobal.route_planning_flag = !IBeaconGlobal.route_planning_flag;

        OutputStream ost = null;

        try { ost = socket.getOutputStream(); }
        catch (IOException e) { e.printStackTrace(); }
        bw = new BufferedWriter(new OutputStreamWriter(ost));

        InputStream ist = null;

        try { ist = socket.getInputStream(); }
        catch (IOException e) { e.printStackTrace(); }
        br = new BufferedReader(new InputStreamReader(ist));

        int destination  = StringToInteger(spDestination.getSelectedItem().toString().getBytes());
        final String end = PositionToArrayIndex(destination);

        new Thread() {
            public void run() {
                /* 送出 Array Coordinate */
                while (IBeaconGlobal.route_planning_flag) {

                    try {
                        Thread.sleep(500);
                        Log.e("Send Thread", "Please print");

                        String start = CoordinateToArrayIndex(origin_coordinate);

                        if (start.equals("")) continue;

                        bw.write(start + " " + end + "\n");
                        bw.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    /* 接收規劃後的路徑 */
                    GetRouteThread();
                }
            }
        }.start();
    }

    private String CoordinateToArrayIndex(int [] coordinate) {
        if (coordinate == null) return "";

        // O O O O O O O O
        if (coordinate[0] >= 1157 && coordinate[0] <= 1192 && coordinate[1] <= 80 && coordinate[1] <= 160) {
            return "8,1";
        } else if (coordinate[0] >= 1126 && coordinate[0] <= 1157 && coordinate[1] >= 80 && coordinate[1] <= 160) {
            return "8,2";
        } else if (coordinate[0] >= 1094 && coordinate[0] <= 1126 && coordinate[1] >= 80 && coordinate[1] <= 160) {
            return "8,3";
        } else if (coordinate[0] >= 1060 && coordinate[0] <= 1094 && coordinate[1] >= 80 && coordinate[1] <= 160) {
            return "8,4";
        } else if (coordinate[0] >= 1017 && coordinate[0] <= 1060 && coordinate[1] >= 80 && coordinate[1] <= 160) {
            return "8,5";
        } else if (coordinate[0] >= 975 && coordinate[0] <= 1017 && coordinate[1] >= 80 && coordinate[1] <= 160) {
            return "8,6";
        } else if (coordinate[0] >= 923 && coordinate[0] <= 975 && coordinate[1] >= 80 && coordinate[1] <= 160) {
            return "8,7";
        } else if (coordinate[0] >= 873 && coordinate[0] <= 923 && coordinate[1] >= 80 && coordinate[1] <= 160) {
            return "8,8";
        }
        // O O O O O O O O O
        //                 O
        //                 O
        //                 O
        else if (coordinate[0] >= 821 && coordinate[0] <= 873 && coordinate[1] >= 112 && coordinate[1] <= 142) {
            return "8,9";
        } else if (coordinate[0] >= 821 && coordinate[0] <= 873 && coordinate[1] >= 93 && coordinate[1] <= 112) {
            return "9,9";
        } else if (coordinate[0] >= 821 && coordinate[0] <= 873 && coordinate[1] >= 75 && coordinate[1] <= 93) {
            return "10,9";
        } else if (coordinate[0] >= 821 && coordinate[0] <= 873 && coordinate[1] >= 45 && coordinate[1] <= 75) {
            return "11,9";
        }
        //                 O
        //                 O
        // O O O O O O O O O
        //                 O
        //                 O
        //                 O
        else if (coordinate[0] >= 821 && coordinate[0] <= 873 && coordinate[1] >= 142 && coordinate[1] <= 174) {
            return "7,9";
        } else if (coordinate[0] >= 821 && coordinate[0] <= 873 && coordinate[1] >= 174 && coordinate[1] <= 200) {
            return "6,9";
        }
        //
        //             O O O O O
        //                 O
        //                 O
        // O O O O O O O O O
        //                 O
        //                 O
        //                 O
        else if (coordinate[0] >= 875 && coordinate[0] <= 906 && coordinate[1] >= 200 && coordinate[1] <= 262) {
            return "5,7";
        } else if (coordinate[0] >= 856 && coordinate[0] <= 875 && coordinate[1] >= 200 && coordinate[1] <= 247) {
            return "5,8";
        } else if (coordinate[0] >= 834 && coordinate[0] <= 856 && coordinate[1] >= 200 && coordinate[1] <= 247) {
            return "5,9";
        } else if (coordinate[0] >= 816 && coordinate[0] <= 834 && coordinate[1] >= 200 && coordinate[1] <= 247) {
            return "5,10";
        } else if (coordinate[0] >= 786 && coordinate[0] <= 816 && coordinate[1] >= 200 && coordinate[1] <= 262) {
            return "5,11";
        }
        //           O
        //           O
        //           O
        //           O
        //             O O O O O
        //                 O
        //                 O
        // O O O O O O O O O
        //                 O
        //                 O
        //                 O
        else if (coordinate[0] >= 857 && coordinate[0] <= 912 && coordinate[1] >= 262 && coordinate[1] <= 294) {
            return "4,6";
        } else if (coordinate[0] >= 857 && coordinate[0] <= 912 && coordinate[1] >= 294 && coordinate[1] <= 326) {
            return "3,6";
        } else if (coordinate[0] >= 857 && coordinate[0] <= 912 && coordinate[1] >= 326 && coordinate[1] <= 357) {
            return "2,6";
        } else if (coordinate[0] >= 857 && coordinate[0] <= 912 && coordinate[1] >= 357 && coordinate[1] <= 403) {
            return "1,6";
        }
        //           O           O
        //           O           O
        //           O           O
        //           O           O
        //             O O O O O
        //                 O
        //                 O
        // O O O O O O O O O
        //                 O
        //                 O
        //                 O
        else if (coordinate[0] >= 778 && coordinate[0] <= 828 && coordinate[1] >= 262 && coordinate[1] <= 294) {
            return "4,12";
        } else if (coordinate[0] >= 778 && coordinate[0] <= 828 && coordinate[1] >= 294 && coordinate[1] <= 326) {
            return "3,12";
        } else if (coordinate[0] >= 778 && coordinate[0] <= 828 && coordinate[1] >= 326 && coordinate[1] <= 357) {
            return "2,12";
        } else if (coordinate[0] >= 778 && coordinate[0] <= 828 && coordinate[1] >= 357 && coordinate[1] <= 403) {
            return "1,12";
        }
        //           O           O
        //           O           O
        //           O           O
        //           O           O
        //             O O O O O
        //                 O
        //                 O
        // O O O O O O O O O O O O O O O O O O O O O O O O O O O O O O
        //                 O
        //                 O
        //                 O
        else if (coordinate[0] >= 787 && coordinate[0] <= 821 /*&& coordinate[1] >= 80 && coordinate[1] <= 160*/) {
            return "8,10";
        } else if (coordinate[0] >= 756 && coordinate[0] <= 787 /*&& coordinate[1] >= 80 && coordinate[1] <= 160*/) {
            return "8,11";
        } else if (coordinate[0] >= 724 && coordinate[0] <= 756 /*&& coordinate[1] >= 80 && coordinate[1] <= 160*/) {
            return "8,12";
        } else if (coordinate[0] >= 692 && coordinate[0] <= 724 /*&& coordinate[1] >= 80 && coordinate[1] <= 160*/) {
            return "8,13";
        } else if (coordinate[0] >= 661 && coordinate[0] <= 692 /*&& coordinate[1] >= 80 && coordinate[1] <= 160*/) {
            return "8,14";
        } else if (coordinate[0] >= 629 && coordinate[0] <= 661 /*&& coordinate[1] >= 80 && coordinate[1] <= 160*/) {
            return "8,15";
        } else if (coordinate[0] >= 597 && coordinate[0] <= 629 /*&& coordinate[1] >= 80 && coordinate[1] <= 160*/) {
            return "8,16";
        } else if (coordinate[0] >= 565 && coordinate[0] <= 597 /*&& coordinate[1] >= 80 && coordinate[1] <= 160*/) {
            return "8,17";
        } else if (coordinate[0] >= 534 && coordinate[0] <= 565 /*&& coordinate[1] >= 80 && coordinate[1] <= 160*/) {
            return "8,18";
        } else if (coordinate[0] >= 502 && coordinate[0] <= 534 /*&& coordinate[1] >= 80 && coordinate[1] <= 160*/) {
            return "8,19";
        } else if (coordinate[0] >= 428 && coordinate[0] <= 502 /*&& coordinate[1] >= 80 && coordinate[1] <= 160*/) {
            return "8,20";
        } else if (coordinate[0] >= 380 && coordinate[0] <= 428 /*&& coordinate[1] >= 80 && coordinate[1] <= 160*/) {
            return "8,21";
        } else if (coordinate[0] >= 324 && coordinate[0] <= 380 /*&& coordinate[1] >= 80 && coordinate[1] <= 160*/) {
            return "8,22";
        } else if (coordinate[0] >= 268 && coordinate[0] <= 324 /*&& coordinate[1] >= 80 && coordinate[1] <= 160*/) {
            return "8,23";
        } else if (coordinate[0] >= 234 && coordinate[0] <= 268 /*&& coordinate[1] >= 80 && coordinate[1] <= 160*/) {
            return "8,24";
        } else if (coordinate[0] >= 205 && coordinate[0] <= 234 /*&& coordinate[1] >= 80 && coordinate[1] <= 160*/) {
            return "8,25";
        } else if (coordinate[0] >= 174 && coordinate[0] <= 205 /*&& coordinate[1] >= 80 && coordinate[1] <= 160*/) {
            return "8,26";
        } else if (coordinate[0] >= 128 && coordinate[0] <= 174 /*&& coordinate[1] >= 80 && coordinate[1] <= 160*/) {
            return "8,27";
        } else if (coordinate[0] >= 75 && coordinate[0] <= 128 /*&& coordinate[1] >= 80 && coordinate[1] <= 160*/) {
            return "8,28";
        } else if (coordinate[0] >= 47 && coordinate[0] <= 75 /*&& coordinate[1] >= 80 && coordinate[1] <= 160*/) {
            return "8,29";
        } else if (coordinate[0] >= 6 && coordinate[0] <= 47 /*&& coordinate[1] >= 80 && coordinate[1] <= 160*/) {
            return "8,30";
        }
        //           O           O
        //           O           O
        //           O           O
        //           O           O
        //             O O O O O
        //                 O
        //                 O
        // O O O O O O O O O O O O O O O O O O O O O O O O O O O O O O
        //                 O                         O
        //                 O                         O
        //                 O                         O
        else if (coordinate[0] >= 324 && coordinate[0] <= 380 && coordinate[1] >= 93 && coordinate[1] <= 112) {
            return "9,22";
        } else if (coordinate[0] >= 324 && coordinate[0] <= 380 && coordinate[1] >= 75 && coordinate[1] <= 93) {
            return "10,22";
        } else if (coordinate[0] >= 324 && coordinate[0] <= 380 && coordinate[1] >= 45 && coordinate[1] <= 75) {
            return "11,22";
        }
        //           O           O
        //           O           O
        //           O           O
        //           O           O
        //             O O O O O
        //                 O                         O
        //                 O                         O
        // O O O O O O O O O O O O O O O O O O O O O O O O O O O O O O
        //                 O                         O
        //                 O                         O
        //                 O                         O
        else if (coordinate[0] >= 324 && coordinate[0] <= 380 && coordinate[1] >= 142 && coordinate[1] <= 174) {
            return "7,22";
        } else if (coordinate[0] >= 324 && coordinate[0] <= 380 && coordinate[1] >= 174 && coordinate[1] <= 200) {
            return "6,22";
        }
        //           O           O
        //           O           O
        //           O           O
        //           O           O
        //             O O O O O                 O O O O O
        //                 O                         O
        //                 O                         O
        // O O O O O O O O O O O O O O O O O O O O O O O O O O O O O O
        //                 O                         O
        //                 O                         O
        //                 O                         O
        else if (coordinate[0] >= 379 && coordinate[0] <= 416 && coordinate[1] >= 200 && coordinate[1] <= 262) {
            return "5,20";
        } else if (coordinate[0] >= 364 && coordinate[0] <= 379 && coordinate[1] >= 200 && coordinate[1] <= 251) {
            return "5,21";
        } else if (coordinate[0] >= 345 && coordinate[0] <= 364 && coordinate[1] >= 200 && coordinate[1] <= 251) {
            return "5,22";
        } else if (coordinate[0] >= 328 && coordinate[0] <= 345 && coordinate[1] >= 200 && coordinate[1] <= 251) {
            return "5,23";
        } else if (coordinate[0] >= 298 && coordinate[0] <= 328 && coordinate[1] >= 200 && coordinate[1] <= 262) {
            return "5,24";
        }
        //           O           O             O
        //           O           O             O
        //           O           O             O
        //           O           O             O
        //             O O O O O                 O O O O O
        //                 O                         O
        //                 O                         O
        // O O O O O O O O O O O O O O O O O O O O O O O O O O O O O O
        //                 O                         O
        //                 O                         O
        //                 O                         O
        else if (coordinate[0] >= 369 && coordinate[0] <= 423 && coordinate[1] >= 262 && coordinate[1] <= 294) {
            return "4,19";
        } else if (coordinate[0] >= 369 && coordinate[0] <= 423 && coordinate[1] >= 294 && coordinate[1] <= 327) {
            return "3,19";
        } else if (coordinate[0] >= 369 && coordinate[0] <= 423 && coordinate[1] >= 327 && coordinate[1] <= 357) {
            return "3,19";
        } else if (coordinate[0] >= 369 && coordinate[0] <= 423 && coordinate[1] >= 357 && coordinate[1] <= 407) {
            return "1,19";
        }
        //           O           O             O           O
        //           O           O             O           O
        //           O           O             O           O
        //           O           O             O           O
        //             O O O O O                 O O O O O
        //                 O                         O
        //                 O                         O
        // O O O O O O O O O O O O O O O O O O O O O O O O O O O O O O
        //                 O                         O
        //                 O                         O
        //                 O                         O
        else if (coordinate[0] >= 286 && coordinate[0] <= 344 && coordinate[1] >= 262 && coordinate[1] <= 294) {
            return "4,25";
        } else if (coordinate[0] >= 286 && coordinate[0] <= 344 && coordinate[1] >= 294 && coordinate[1] <= 327) {
            return "3,25";
        } else if (coordinate[0] >= 286 && coordinate[0] <= 344 && coordinate[1] >= 327 && coordinate[1] <= 357) {
            return "2,25";
        } else if (coordinate[0] >= 286 && coordinate[0] <= 344 && coordinate[1] >= 357 && coordinate[1] <= 407) {
            return "1,25";
        }

        return "";
    }

    private void GetRouteThread() {
        try {
            recv = br.readLine();
            if (recv != "")
            {
                route = recv.getBytes();

                /* 畫出路徑 */
                int [][] coordinate = new int[route.length][2];
                for (int i = 0; i < route.length; i++) {
                    coordinate[i] = GetImageCoordinate(route[i] - 0x30);
                }

                /* 起點改為原始數據 */
                coordinate[0][0] = origin_coordinate[0];
                coordinate[0][1] = origin_coordinate[1];

                mapView.DrawRoute(coordinate, route.length);
                Log.e("route", "send " + route.length);

                recv = "";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int [] GetImageCoordinate(int index) {
        int [] coordinate = new int[2];

        switch (index) {
            case 0:
                coordinate[0] = 1162;
                coordinate[1] = 123;
                return coordinate;
            case 1:
                coordinate[0] = 1135;
                coordinate[1] = 123;
                return coordinate;
            case 2:
                coordinate[0] = 1114;
                coordinate[1] = 123;
                return coordinate;
            case 3:
                coordinate[0] = 1066;
                coordinate[1] = 123;
                return coordinate;
            case 4:
                coordinate[0] = 1039;
                coordinate[1] = 123;
                return coordinate;
            case 5:
                coordinate[0] = 997;
                coordinate[1] = 123;
                return coordinate;
            case 6:
                coordinate[0] = 951;
                coordinate[1] = 123;
                return coordinate;
            case 7:
                coordinate[0] = 910;
                coordinate[1] = 123;
                return coordinate;
            case 8:
                coordinate[0] = 845;
                coordinate[1] = 123;
                return coordinate;
            case 9:
                coordinate[0] = 887;
                coordinate[1] = 362;
                return coordinate;
            case 10:
                coordinate[0] = 887;
                coordinate[1] = 338;
                return coordinate;
            case 11:
                coordinate[0] = 887;
                coordinate[1] = 306;
                return coordinate;
            case 12:
                coordinate[0] = 887;
                coordinate[1] = 277;
                return coordinate;
            case 13:
                coordinate[0] = 887;
                coordinate[1] = 217;
                return coordinate;
            case 14:
                coordinate[0] = 862;
                coordinate[1] = 217;
                return coordinate;
            case 15:
                coordinate[0] = 845;
                coordinate[1] = 156;
                return coordinate;
            case 16:
                coordinate[0] = 845;
                coordinate[1] = 217;
                return coordinate;
            case 17:
                coordinate[0] = 829;
                coordinate[1] = 217;
                return coordinate;
            case 18:
                coordinate[0] = 804;
                coordinate[1] = 217;
                return coordinate;
            case 19:
                coordinate[0] = 804;
                coordinate[1] = 277;
                return coordinate;
            case 20:
                coordinate[0] = 804;
                coordinate[1] = 306;
                return coordinate;
            case 21:
                coordinate[0] = 804;
                coordinate[1] = 338;
                return coordinate;
            case 22:
                coordinate[0] = 804;
                coordinate[1] = 362;
                return coordinate;
            case 23:
                coordinate[0] = 845;
                coordinate[1] = 66;
                return coordinate;
            case 24:
                coordinate[0] = 845;
                coordinate[1] = 85;
                return coordinate;
            case 25:
                coordinate[0] = 845;
                coordinate[1] = 102;
                return coordinate;
            case 26:
                coordinate[0] = 807;
                coordinate[1] = 123;
                return coordinate;
            case 27:
                coordinate[0] = 772;
                coordinate[1] = 123;
                return coordinate;
            case 28:
                coordinate[0] = 740;
                coordinate[1] = 123;
                return coordinate;
            case 29:
                coordinate[0] = 710;
                coordinate[1] = 123;
                return coordinate;
            case 30:
                coordinate[0] = 677;
                coordinate[1] = 123;
                return coordinate;
            case 31:
                coordinate[0] = 646;
                coordinate[1] = 123;
                return coordinate;
            case 32:
                coordinate[0] = 32;
                coordinate[1] = 123;
                return coordinate;
            case 33:
                coordinate[0] = 63;
                coordinate[1] = 123;
                return coordinate;
            case 34:
                coordinate[0] = 94;
                coordinate[1] = 123;
                return coordinate;
            case 35:
                coordinate[0] = 154;
                coordinate[1] = 123;
                return coordinate;
            case 36:
                coordinate[0] = 193;
                coordinate[1] = 123;
                return coordinate;
            case 37:
                coordinate[0] = 221;
                coordinate[1] = 123;
                return coordinate;
            case 38:
                coordinate[0] = 249;
                coordinate[1] = 123;
                return coordinate;
            case 39:
                coordinate[0] = 287;
                coordinate[1] = 123;
                return coordinate;
            case 40:
                coordinate[0] = 354;
                coordinate[1] = 123;
                return coordinate;
            case 41:
                coordinate[0] = 312;
                coordinate[1] = 368;
                return coordinate;
            case 42:
                coordinate[0] = 312;
                coordinate[1] = 334;
                return coordinate;
            case 43:
                coordinate[0] = 312;
                coordinate[1] = 304;
                return coordinate;
            case 44:
                coordinate[0] = 312;
                coordinate[1] = 289;
                return coordinate;
            case 45:
                coordinate[0] = 312;
                coordinate[1] = 217;
                return coordinate;
            case 46:
                coordinate[0] = 337;
                coordinate[1] = 217;
                return coordinate;
            case 47:
                coordinate[0] = 354;
                coordinate[1] = 186;
                return coordinate;
            case 48:
                coordinate[0] = 354;
                coordinate[1] = 217;
                return coordinate;
            case 49:
                coordinate[0] = 372;
                coordinate[1] = 217;
                return coordinate;
            case 50:
                coordinate[0] = 396;
                coordinate[1] = 217;
                return coordinate;
            case 51:
                coordinate[0] = 396;
                coordinate[1] = 289;
                return coordinate;
            case 52:
                coordinate[0] = 396;
                coordinate[1] = 304;
                return coordinate;
            case 53:
                coordinate[0] = 396;
                coordinate[1] = 334;
                return coordinate;
            case 54:
                coordinate[0] = 396;
                coordinate[1] = 368;
                return coordinate;
            case 55:
                coordinate[0] = 354;
                coordinate[1] = 66;
                return coordinate;
            case 56:
                coordinate[0] = 354;
                coordinate[1] = 85;
                return coordinate;
            case 57:
                coordinate[0] = 354;
                coordinate[1] = 102;
                return coordinate;
            case 58:
                coordinate[0] = 395;
                coordinate[1] = 123;
                return coordinate;
            case 59:
                coordinate[0] = 472;
                coordinate[1] = 123;
                return coordinate;
            case 60:
                coordinate[0] = 523;
                coordinate[1] = 123;
                return coordinate;
            case 61:
                coordinate[0] = 550;
                coordinate[1] = 123;
                return coordinate;
            case 62:
                coordinate[0] = 585;
                coordinate[1] = 123;
                return coordinate;
            case 63:
                coordinate[0] = 610;
                coordinate[1] = 123;
                return coordinate;
            case 65:
                coordinate[0] = 999;
                coordinate[1] = 999;
                return coordinate;
        }

        return null;
    }

    public void cancelScanBluetoothDevices() {
        if (Build.VERSION.SDK_INT < 21) {
            btAdapter.stopLeScan(myLeScanCallback);
        } else {
            mLEScanner.stopScan(mScanCallback);
        }
    }

    private void disconnect() {
        // 需補上[例外處理] – try … catch(Exception ex) …
        try {
            socket.close();
            socket = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showToast(final String msg) {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(RoutePlanningActivity.this, msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    /* Sensor 回傳新的值，使方向改變 */
    public void onSensorChanged(SensorEvent sensorEvent) {
        /* values[0]：Z軸，Sensor方位，北：0、東：90、南：180、西：270
         * values[1]：X軸，Sensor傾斜度(抬起手機頂部，X軸的值會變動)
         * values[2]：Y軸，Sensor滾動角度(側邊翻轉) */

        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            accelerometerValues = sensorEvent.values;
        }
        if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            magneticValues = sensorEvent.values;
        }

        orientation = ((int) calculateOrientation() + 90 * rotation) % 360;

        mapView.DrawArrow(orientation);

        Log.e("orient", "" + orientation);
    }

    /* 計算手機之方向 */
    private float calculateOrientation() {
        float [] values = new float[3];
        float [] R      = new float[9];

        /* 取得旋轉矩陣，儲存的是磁場和加速度的數據 */
        sensor.getRotationMatrix(R, null, accelerometerValues, magneticValues);
        /* 利用旋轉矩陣計算方向 */
        sensor.getOrientation(R, values);
        /* 取得角度 */
        values[0] = (float) Math.toDegrees(values[0]);

        return (values[0] > 0) ? values[0] : 360 + values[0];
    }

    @Override
    /* 改變精確度 */
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    // --- LeScanCannback
    private BluetoothAdapter.LeScanCallback myLeScanCallback  = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            origin_coordinate = iBeaconService.GetPosition( device, rssi, scanRecord);
        }
    };

    private ScanCallback mScanCallback = new ScanCallback() {
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice btDevice = result.getDevice();

            origin_coordinate = iBeaconService.GetPosition( btDevice, result.getRssi(), result.getScanRecord().getBytes());

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

                if (new_product_major_minor == null) return;

                //傳值給PHP
                List<NameValuePair> vars = new ArrayList<NameValuePair>();
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

                if (record_JSA != null) {
                    IBeaconGlobal.product_push_information = "";

                    for (int i = 0; i < record_JSA.length(); i++){
                        IBeaconGlobal.product_push_information += record_JSA.getString(i) + "\n";
                        Log.e("all_uuid", IBeaconGlobal.product_push_information);
                    }

                    if (IBeaconGlobal.product_push_information.length() - 2 <= 0) return;

                    IBeaconGlobal.product_push_information = IBeaconGlobal.product_push_information.substring(0, IBeaconGlobal.product_push_information.length() - 2);
                }

                //Send Message To Handler
                if(record_JSA != null) {
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

    @Override
    public void onPause() {
        super.onPause();
        sensor.unregisterListener(RoutePlanningActivity.this, accelerometer);
        sensor.unregisterListener(RoutePlanningActivity.this, magnetic);

        //cancelScanBluetoothDevices();
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensor.registerListener(RoutePlanningActivity.this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensor.registerListener(RoutePlanningActivity.this, magnetic, SensorManager.SENSOR_DELAY_NORMAL);

        //doScanBleDevices();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        IBeaconGlobal.route_planning_flag = false;

        if (socket == null) return;

        disconnect();
    }
}
