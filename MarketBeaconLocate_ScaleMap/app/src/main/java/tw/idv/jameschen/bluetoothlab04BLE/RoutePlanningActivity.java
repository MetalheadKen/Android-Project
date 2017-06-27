package tw.idv.jameschen.bluetoothlab04BLE;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class RoutePlanningActivity extends Activity implements View.OnClickListener, SensorEventListener {

    private Button btnPath;
    private Socket socket;
    private Spinner spOrigin, spDestination;
    private MapView mapView;
    private SensorManager sensor;
    private Sensor accelerometer;                   /* 加速度傳感器 */
    private Sensor magnetic;                        /* 地磁場傳感器 */

    private byte [] route = new byte[64];
    private String  recv = "";

    private int rotation, orientation;
    private float[] accelerometerValues = new float[3];
    private float[] magneticValues      = new float[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_route_planning);

        GetViews();

        ArrayAdapter<CharSequence> PlaceApapter = ArrayAdapter.createFromResource(this, R.array.place_array, android.R.layout.simple_spinner_item);
        PlaceApapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spOrigin.setAdapter(PlaceApapter);
        spDestination.setAdapter(PlaceApapter);
        //
        spOrigin.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {

                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                }
        );

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
    }

    private void GetViews() {
        btnPath       = (Button) findViewById(R.id.btnPath);
        spOrigin      = (Spinner) findViewById(R.id.spOrigin);
        spDestination = (Spinner) findViewById(R.id.spDestination);
        mapView       = (MapView) findViewById(R.id.imgMap);
        sensor        = (SensorManager) getSystemService(SENSOR_SERVICE);
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

    private String GetArrayIndex(int place) {
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

    @Override
    public void onClick(View v) {
        new Thread() {
            public void run() {
                int origin, destination;

                origin = StringToInteger(spOrigin.getSelectedItem().toString().getBytes());
                destination = StringToInteger(spDestination.getSelectedItem().toString().getBytes());

                /* 送出 Array Coordinate */
                OutputStream ost = null;
                try {
                    ost = socket.getOutputStream();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(ost));
                try {
                    bw.write(GetArrayIndex(origin) + " " + GetArrayIndex(destination) + "\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    bw.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                /* 接收規劃後的路徑 */
                GetRouteThread();
            }
        }.start();
    }

    private void GetRouteThread() {
        InputStream ist = null;

        try {
            ist = socket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(ist));
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disconnect();
    }

    private void disconnect() {
        // 需補上[例外處理] – try … catch(Exception ex) …
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        showToast("離線ok!");
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

    @Override
    public void onPause() {
        super.onPause();
        sensor.unregisterListener(RoutePlanningActivity.this, accelerometer);
        sensor.unregisterListener(RoutePlanningActivity.this, magnetic);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensor.registerListener(RoutePlanningActivity.this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensor.registerListener(RoutePlanningActivity.this, magnetic, SensorManager.SENSOR_DELAY_NORMAL);
    }
}
