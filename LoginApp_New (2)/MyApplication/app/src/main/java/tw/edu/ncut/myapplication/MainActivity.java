package tw.edu.ncut.myapplication;

import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity implements OnClickListener {
    public UUID UUID_IRT_SERV = UUID.fromString("f000aa00-0451-4000-b000-000000000000");
    public UUID UUID_IRT_DATA = UUID.fromString("f000aa01-0451-4000-b000-000000000000");
    public UUID UUID_IRT_CONF = UUID.fromString("f000aa02-0451-4000-b000-000000000000"); // 0: disable,1: enable

    public UUID CLIENT_CONFIG_DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"); //定義手機的UUID

    public String DeviceName = "SensorTag";
    public BluetoothAdapter BTAdapter;
    public BluetoothDevice BTDevice;
    public BluetoothGatt BTGatt;

    public boolean scanning;
    public Handler handler;
    public Console console;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        console = new Console((TextView) findViewById(R.id.console));
        ((Button) findViewById(R.id.buttonScan)).setOnClickListener(this);
        ((Button) findViewById(R.id.buttonClear)).setOnClickListener(this);

        //初始化Bluetooth adapter，透過BluetoothManager得到一個參考Bluetooth adapter
        BluetoothManager BTManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BTAdapter = BTManager.getAdapter();

        scanning = false;
        handler = new Handler();
    }

    public void onDestroy() {
        if (BTGatt != null) {
            BTGatt.disconnect();
            BTGatt.close();
        }
        super.onDestroy();
    }

    public BluetoothGattCallback GattCallback = new BluetoothGattCallback() {
        int ssstep = 0;

        public void SetupSensorStep(BluetoothGatt gatt) {
            BluetoothGattCharacteristic characteristic;
            BluetoothGattDescriptor descriptor;
            switch (ssstep) {
                case 0:
    /*
     * * Enable IRT Sensor
     */
                    characteristic = gatt.getService(UUID_IRT_SERV).getCharacteristic(UUID_IRT_CONF);
                    characteristic.setValue(new byte[] { 0x01 });
                    gatt.writeCharacteristic(characteristic);
                    break;
                case 1:
    /*
     * * Setup IRT Sensor
     */
                    // Enable local notifications
                    characteristic = gatt.getService(UUID_IRT_SERV).getCharacteristic(UUID_IRT_DATA);
                    gatt.setCharacteristicNotification(characteristic, true);
                    // Enabled remote notifications
                    descriptor = characteristic.getDescriptor(CLIENT_CONFIG_DESCRIPTOR);
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    gatt.writeDescriptor(descriptor);
                    break;
            }
            ssstep++;
        }

        // 偵測GATT client連線或斷線
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) {
                output("Connected to GATT Server");
                gatt.discoverServices();
            } else {
                output("Disconnected from GATT Server");
                gatt.disconnect();
                gatt.close();
            }
        }

        //發現新的服務
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            output("Discover & Config GATT Services");
            ssstep = 0;
            SetupSensorStep(gatt);
        }

        //特徵寫入結果
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic, int status) {
            SetupSensorStep(gatt);
        }

        //描述寫入結果
        public void onDescriptorWrite(BluetoothGatt gatt,
                                      BluetoothGattDescriptor descriptor, int status) {
            SetupSensorStep(gatt);
        }

        //遠端特徵通知結果
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {

            //判斷IR Temperature Data，如果有資料則透過TITOOL類別進行資料轉換，傳回值為攝氏單位
            if (UUID_IRT_DATA.equals(characteristic.getUuid())) {
                double ambient = TITOOL.extractAmbientTemperature(characteristic);
                double target = TITOOL.extractTargetTemperature(characteristic, ambient);
                //target = target * 1.8 + 32; //轉換華氏
                output("@ " + String.format("%.2f", target) + "&deg;C");
            }
        }
    };

    //回報由手機設備掃描過程中發現的LE設備
    public BluetoothAdapter.LeScanCallback DeviceLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        public void onLeScan(final BluetoothDevice device, int rssi,
                             byte[] scanRecord) {
            if (DeviceName.equals(device.getName())) {
                if (BTDevice == null) {
                    BTDevice = device;
                    BTGatt = BTDevice.connectGatt(getApplicationContext(), false, GattCallback); // 連接GATT
                } else {
                    if (BTDevice.getAddress().equals(device.getAddress())) {
                        return;
                    }
                }
                output("*<small> " + device.getName() + ":" + device.getAddress() + ", rssi:" + rssi + "</small>");
            }
        }
    };

    public void BTScan() {
        //檢查設備上是否支持藍牙
        if (BTAdapter == null) {
            output("No Bluetooth Adapter");
            return;
        }

        if (!BTAdapter.isEnabled()) {
            BTAdapter.enable();
        }

        //搜尋BLE藍牙裝置
        if (scanning == false) {
            handler.postDelayed(new Runnable() {
                public void run() {
                    scanning = false;
                    BTAdapter.stopLeScan(DeviceLeScanCallback);
                    output("Stop scanning");
                }
            }, 2000);

            scanning = true;
            BTDevice = null;
            if (BTGatt != null) {
                BTGatt.disconnect();
                BTGatt.close();
            }
            BTGatt = null;

            BTAdapter.startLeScan(DeviceLeScanCallback);
            output("Start scanning");
        }
    }

    //按鍵事件
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buttonScan:
                BTScan();
                break;
            case R.id.buttonClear:
                clear();
                break;
        }
    }

    //訊息輸出到TextView
    public void output(String msg) {
        console.output(msg);
    }
    //清除TextView
    public void clear() {
        console.clear();
    }

    //選單(EXIT)
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_exit:
                if (BTGatt != null) {
                    BTGatt.disconnect();
                    BTGatt.close();
                }
                finish();
                System.exit(0);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}

//溫度轉換，轉換公式請參考TI官方SensorTag User Guide
class TITOOL {
    public static double extractAmbientTemperature(BluetoothGattCharacteristic c) {
        int offset = 2;
        return shortUnsignedAtOffset(c, offset) / 128.0;
    }

    public static double extractTargetTemperature(BluetoothGattCharacteristic c, double ambient) {
        Integer twoByteValue = shortSignedAtOffset(c, 0);

        double Vobj2 = twoByteValue.doubleValue();
        Vobj2 *= 0.00000015625;

        double Tdie = ambient + 273.15;

        double S0 = 5.593E-14; // Calibration factor
        double a1 = 1.75E-3;
        double a2 = -1.678E-5;
        double b0 = -2.94E-5;
        double b1 = -5.7E-7;
        double b2 = 4.63E-9;
        double c2 = 13.4;
        double Tref = 298.15;
        double S = S0 * (1 + a1 * (Tdie - Tref) + a2 * Math.pow((Tdie - Tref), 2));
        double Vos = b0 + b1 * (Tdie - Tref) + b2 * Math.pow((Tdie - Tref), 2);
        double fObj = (Vobj2 - Vos) + c2 * Math.pow((Vobj2 - Vos), 2);
        double tObj = Math.pow(Math.pow(Tdie, 4) + (fObj / S), .25);

        return tObj - 273.15;
    }

    public static Integer shortSignedAtOffset(BluetoothGattCharacteristic c, int offset) {
        Integer lowerByte = c.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset);
        Integer upperByte = c.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT8, offset + 1);

        return (upperByte << 8) + lowerByte;
    }

    public static Integer shortUnsignedAtOffset(BluetoothGattCharacteristic c, int offset) {
        Integer lowerByte = c.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset);
        Integer upperByte = c.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset + 1);

        return (upperByte << 8) + lowerByte;
    }
}
