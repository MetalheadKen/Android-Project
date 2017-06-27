package tw.idv.jameschen.bluetoothlab03BLE;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class MainActivity extends Activity implements OnClickListener, OnItemClickListener {
	public static String TAG = "BLE Finder";
	//
	private Button btnDoScan;
	private Button btnStopScan;
	private ListView lvBtListing;
	//
	private BluetoothAdapter btAdapter;
	private ArrayList<HashMap<String, String>> devices = new ArrayList<HashMap<String, String>>();
	private ArrayList<String> deviceMacList = new ArrayList<String>(); 
	private SimpleAdapter adapter;
 
	// > Android 5.0.1 
	private BluetoothLeScanner mLEScanner;
    private ScanSettings settings;
	private List<ScanFilter> filters;
	//
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//
		btnDoScan = (Button) findViewById(R.id.btnDoScan);
		btnStopScan = (Button) findViewById(R.id.btnStopScan);
		lvBtListing = (ListView) findViewById(R.id.lvBtListing);
		//
		btnDoScan.setOnClickListener(this);
		btnStopScan.setOnClickListener(this);
		// ListView
		adapter = new SimpleAdapter(
				this, 
				devices,
				R.layout.beacon_item, 
				new String[] { "name", "mac_address", "discovery_time", "rssi", "uuid" }, 
				new int[] { R.id.tvDeviceName, R.id.tvMacAddress, R.id.tvLastedDiscoveredTime, R.id.tvRSSI, R.id.tvUUID }
		);
		lvBtListing.setAdapter(adapter);
		lvBtListing.setOnItemClickListener(this);
		
		//BLE
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE Not Supported", Toast.LENGTH_SHORT).show();
            finish();
        }
		// Lab103-1. BT Adapter (BluetoothManager)
		BluetoothManager bluetoothManager = 
				(BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		btAdapter = bluetoothManager.getAdapter();
		//
        if (  btAdapter == null) {
           Toast.makeText(this, "BT�����D�Χ䤣��...�Э��s�ˬd!!", Toast.LENGTH_LONG).show();	
           return;
        }
		
        // Lab103-2.
        if ( ! btAdapter.isEnabled()) {
        	Intent it = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        	startActivityForResult(it, 1999);
        }
	}

	//
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if (requestCode == 1999) {
			String msg = "";
			String title = "";
	       if (resultCode == Activity.RESULT_OK) {
	    	   // enable BT !
	    	   msg = "[Scan] BLE";
	    	   title = "enable BT";
	       }
	       else {
	    	   // BT -- disable !
	    	   msg = "BT -- disable!!";
	    	   title = "BT -- disable!";
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
	
	private boolean bDoScanning;
	
	@Override
	public void onClick(View v) {
		if (v == btnDoScan && !bDoScanning) {
			// Initialize : > Android 5.0.1
            if (Build.VERSION.SDK_INT >= 21) {
                mLEScanner = btAdapter.getBluetoothLeScanner();
                settings = new ScanSettings.Builder()
                			.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                            .build();
                 filters = new ArrayList<ScanFilter>();
            }

			//
			Toast.makeText(this, "Begin scanning BLE Device...", Toast.LENGTH_LONG).show();
			// Lab 102.
			doScanBleDevices();
			//
			bDoScanning = true;
			btnStopScan.setClickable(true);
			btnDoScan.setClickable(false);

		} else if (v == btnStopScan && bDoScanning) {
			//
			Toast.makeText(this, "Stop scanning BLE Device...", Toast.LENGTH_LONG).show();
			// Lab 103.
			cancelScanBluetoothDevices();
			//
			bDoScanning = false;
			btnStopScan.setClickable(false);
			btnDoScan.setClickable(true);
			//
		}
	}

	// Lab 102-1.  Intent Filter
	private void doScanBleDevices() {
 		// 0.
		devices.clear();
		adapter.notifyDataSetChanged();
		deviceMacList.clear();
		// 1. BLE Callback
        if (Build.VERSION.SDK_INT < 21) {
        	btAdapter.startLeScan(myLeScanCallback);
    		// btAdapter.startLeScan( UUID[], myLeScanCallback); // BLE
        	// ex: 0000180d-0000-1000-8000-00805f9b34fb : UUID for heart rate monitors.
        } else {
            mLEScanner.startScan( filters, settings, mScanCallback);
        }
	}

	// Lab 102-2. BT
	// --- LeScanCannback
	private LeScanCallback myLeScanCallback  = new LeScanCallback() {
		
		@Override
		public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
			String msg = String.format(
				   	" Name:%s;\nMac Addres:%s;\nRSSI: %d dBm;",
					device.getName(), device.getAddress(), rssi);
			//
			logBtDevice( device, rssi, scanRecord);
		}
	};
	
	private void logBtDevice( BluetoothDevice device, int rssi, byte[] scanRecord) {
		HashMap<String , String> btmap = new HashMap<String, String>();
		btmap.put("name", device.getName());
		btmap.put("rssi", rssi+"");
		btmap.put("mac_address", device.getAddress());
		btmap.put("discovery_time", new Date().toString());
		ParcelUuid[] uuids = device.getUuids();
		if (uuids != null && uuids.length > 0)
		   btmap.put("uuid", uuids[0].toString());
		else 
		   btmap.put("uuid", "00000000-0000-0000-0000-000000000000");
		//
		int index = deviceMacList.indexOf(device.getAddress());
		if (index == -1) {
		    // a new one
 		    devices.add(btmap);
			deviceMacList.add(device.getAddress());
		}
		else {
			// replace old one !
			devices.remove(index);
			devices.add(index, btmap);
		}
		//
		StringBuffer record = new StringBuffer();
		record.append(String.format("len:%d, data=", scanRecord.length));
		for (int i=0; i<scanRecord.length; i++) {
			record.append(String.format(" 0x%02x", scanRecord[i]));
		}
		Log.i("BLE ScanRecord", record.toString());
		//
		adapter.notifyDataSetChanged();
	}

    private ScanCallback mScanCallback = new ScanCallback() {
        public void onScanResult(int callbackType, ScanResult result) {
        	     Log.i("callbackType", String.valueOf(callbackType));
                 Log.i("result", result.toString());
                 BluetoothDevice btDevice = result.getDevice();
      			 logBtDevice( btDevice, result.getRssi(), result.getScanRecord().getBytes());
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
     
	// Lab 103-1. �������y�{��
	private void cancelScanBluetoothDevices() {
		if (bDoScanning) {
			// ���� BLE ���y
            if (Build.VERSION.SDK_INT < 21) {
            	btAdapter.stopLeScan(myLeScanCallback);
            } else {
                mLEScanner.stopScan(mScanCallback);
            }
		}
	}
	
	//
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		// 
		cancelScanBluetoothDevices();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (bDoScanning) {
			doScanBleDevices();
		}
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		String btMAC = deviceMacList.get(position);
		BluetoothDevice device = btAdapter.getRemoteDevice(btMAC);
		// BLE GATT Server; GattCallback
		// connectToDevice(device);
	}
}
