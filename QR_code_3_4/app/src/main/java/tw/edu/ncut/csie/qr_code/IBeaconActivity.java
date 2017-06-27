package tw.edu.ncut.csie.qr_code;

import android.app.Activity;
import android.app.AlertDialog;
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
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by user on 2017/5/1.
 */

public class IBeaconActivity extends Activity {
    private BluetoothAdapter btAdapter;

    // > Android 5.0.1
    private BluetoothLeScanner mLEScanner;
    private ScanSettings settings;
    private List<ScanFilter> filters;

    private IBeaconService iBeaconService = new IBeaconService();

    public String origin_coordinate = "";

    public void Initialize() {
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
            Intent it = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(it, 1999);
        }

        if (Build.VERSION.SDK_INT >= 21) {
            mLEScanner = btAdapter.getBluetoothLeScanner();
            settings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build();
            filters = new ArrayList<ScanFilter>();

            //ScanFilter.Builder filterBuilder = new ScanFilter.Builder();
            //filters.add(filterBuilder.build());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        if (requestCode == 1999) {
            String msg = "";
            String title = "";
            if (resultCode == Activity.RESULT_OK) {
                // enable BT !
                msg = "enable BT !";
                title = "enable BT !";
            }
            else {
                // BT -- disable !
                msg = "BT -- disable !";
                title = "BT -- disable !";
            }
            //
            new AlertDialog.Builder(this)
                    .setTitle(title)
                    .setMessage(msg)
                    .setNeutralButton("OK", null)
                    .show();
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
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
            origin_coordinate = iBeaconService.GetCoordinate( device, rssi, scanRecord);
        }
    };

    private ScanCallback mScanCallback = new ScanCallback() {
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice btDevice = result.getDevice();

            //Cancel MI and MI Band Connection
            if(!btDevice.getAddress().equals("88:0F:10:34:9A:62") && !btDevice.getAddress().equals("D5:3F:BA:96:96:50")  && !btDevice.getAddress().equals("EB:40:15:9D:32:82")
                    && !btDevice.getAddress().equals("69:3F:D1:49:73:C2"))
                origin_coordinate = iBeaconService.GetCoordinate( btDevice, result.getRssi(), result.getScanRecord().getBytes());
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
}
