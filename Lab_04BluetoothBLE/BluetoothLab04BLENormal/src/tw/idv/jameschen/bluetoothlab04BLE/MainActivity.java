package tw.idv.jameschen.bluetoothlab04BLE;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

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
	private ArrayList<BluetoothGatt> mGattList = new ArrayList<BluetoothGatt>();
	private ArrayList<String> uuidStringList = new ArrayList<String>();
	private HashMap<String, List<BluetoothGattService>> gattServiceMapping = new HashMap<String, List<BluetoothGattService>>();
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
		// 利用 ListView 顯示
		adapter = new SimpleAdapter(
				this, 
				devices,
				R.layout.beacon_item, 
				new String[] { "name", "mac_address", "discovery_time", "rssi", "uuid" }, 
				new int[] { R.id.tvDeviceName, R.id.tvMacAddress, R.id.tvLastedDiscoveredTime, R.id.tvRSSI, R.id.tvUUID }
		);
		lvBtListing.setAdapter(adapter);
		lvBtListing.setOnItemClickListener(this);
		
		// 檢查 BLE 是否支援?
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE Not Supported", Toast.LENGTH_SHORT).show();
            finish();
        }
		// Lab103-1. 取得  BT Adapter (透過 BluetoothManager)		
		BluetoothManager bluetoothManager = 
				(BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		btAdapter = bluetoothManager.getAdapter();
		//
        if (  btAdapter == null) {
           Toast.makeText(this, "BT有問題或找不到...請重新檢查!!", Toast.LENGTH_LONG).show();	
           return;
        }
		
        // Lab103-2. 檢查並啟動藍芽功能 ?
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
	    	   msg = "請點擊[Scan]進行BLE設備掃描";
	    	   title = "藍芽已開啟";
	       }
	       else {
	    	   // BT -- disable !
	    	   msg = "請手動開啟藍芽功能，方可進行設備掃描!!";
	    	   title = "藍芽未開啟";
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
	
	// 紀錄掃描中狀態 !
	private boolean bDoScanning;
	
	@Override
	public void onClick(View v) {
		if (v == btnDoScan && !bDoScanning) {
			// Initialize : > Android 5.0.1  新架構
            if (Build.VERSION.SDK_INT >= 21) {
                mLEScanner = btAdapter.getBluetoothLeScanner();
                settings = new ScanSettings.Builder()
                			.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                            .build();
                 filters = new ArrayList<ScanFilter>();
            }

			//
			Toast.makeText(this, "Begin scanning BLE Device...", Toast.LENGTH_LONG).show();
			// Lab 102. 開始進行BT設備掃描 
			doScanBleDevices();
			//
			bDoScanning = true;
			btnStopScan.setClickable(true);
			btnDoScan.setClickable(false);

		} else if (v == btnStopScan && bDoScanning) {
			//
			Toast.makeText(this, "Stop scanning BLE Device...", Toast.LENGTH_LONG).show();
			// Lab 103. 停止BT設備掃描
			cancelScanBluetoothDevices();
			//
			disconnectAllDevicesGatt();
			//
			bDoScanning = false;
			btnStopScan.setClickable(false);
			btnDoScan.setClickable(true);
			//
		}
	}

	// Lab 102-1. 以 Intent Filter 掃描 BT 設備
	private void doScanBleDevices() {
 		// 0. 清除就有資料
		devices.clear();
		adapter.notifyDataSetChanged();
		deviceMacList.clear();
		// 1. 啟動BLE掃描，完成 Callback 接收模組
        if (Build.VERSION.SDK_INT < 21) {
        	btAdapter.startLeScan(myLeScanCallback);
    		// btAdapter.startLeScan( UUID[], myLeScanCallback); // 指定BLE類型
        	// ex: 0000180d-0000-1000-8000-00805f9b34fb : UUID for heart rate monitors.
        } else {
            mLEScanner.startScan(filters, settings, mScanCallback);
        }
	}

	// Lab 102-2. 建立 BT 廣播接收器
	// --- LeScanCannback
	private LeScanCallback myLeScanCallback  = new LeScanCallback() {
		
		@Override
		public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
			String msg = String.format(
				   	" Name:%s;\nMac Addres:%s;\nRSSI: %d dBm;",
					device.getName(), device.getAddress(), rssi);
			//
			logBtDevice( device, rssi, scanRecord);
			//
			// connectToDevice(device);
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
      			 //
                 // connectToDevice(btDevice);
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
     
	// Lab 103-1. 取消掃描程序
	private void cancelScanBluetoothDevices() {
		if (bDoScanning) {
			// 取消 BLE 掃描
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

	public void connectToDevice(BluetoothDevice device) {
        BluetoothGatt  gatt = device.connectGatt(this, false, myGattCallback);
        mGattList.add( gatt );
    }
	//
	public void disconnectAllDevicesGatt() {
		for (int i=0; i<mGattList.size(); i++) {
			mGattList.get(i).disconnect();
		}
	}

	// 顯示完整 BLE 設備的內容
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		String btMAC = deviceMacList.get(position);
		BluetoothDevice device = btAdapter.getRemoteDevice(btMAC);
		// 連接 BLE 設備的 GATT Server; 系統會不定時呼叫 GattCallback 
		connectToDevice(device);
	}
	
	//
	public ArrayList<String> getUuidListFromGattServices(List<BluetoothGattService> services) {
		ArrayList<String> uuidStringList = new ArrayList<String>();
		for (int i=0; i<services.size(); i++) {
			uuidStringList.add( services.get(i).getUuid().toString() );
		}
		//
		return uuidStringList;
	}
	//
    //
	ArrayList<String> uuids = null;
	HashMap<String, List<String>> characteristicList= new HashMap<String, List<String>>();
	
    private final BluetoothGattCallback myGattCallback = new BluetoothGattCallback() {
        
    	@Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i("onConnectionStateChange", "Status: " + status);
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    Log.i("gattCallback", "STATE_CONNECTED");
                    gatt.discoverServices();
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.e("gattCallback", "STATE_DISCONNECTED");
                    break;
                default:
                    Log.e("gattCallback", "STATE_OTHER");
            }
        }
 
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            String btMacAddress = ""; // BT's MAC Address
        	List<BluetoothGattService> services = gatt.getServices();
            gattServiceMapping.put( btMacAddress, services);
            //
            uuids = getUuidListFromGattServices(services);
            characteristicList.clear();
            //
            //
            for (int i=0; i<services.size(); i++) {
               // 
               Log.i("onServicesDiscovered()", "Service("+i+") UUID: "+services.get(i).getUuid().toString());
               //
               List<BluetoothGattCharacteristic> gattCharacteristics = services.get(i).getCharacteristics();
               List<String> characteristics = new ArrayList<String>();
               for (int j=0; j<gattCharacteristics.size(); j++) {
            	   Log.i("onServicesDiscovered()", "Service("+i+") Characteristic_UUID: "+gattCharacteristics.get(j).getUuid().toString());
            	   characteristics.add(gattCharacteristics.get(j).getUuid().toString());
            	   // (A) manually read
            	   gatt.readCharacteristic(gattCharacteristics.get(j));
            	   // (B) notification(callback) !!
            	   gatt.setCharacteristicNotification(gattCharacteristics.get(j), true);
            	   //
            	   List<BluetoothGattDescriptor> desc = gattCharacteristics.get(j).getDescriptors();
            	   for (int k=0; k<desc.size(); k++) {
            		   Log.i("onServicesDiscovered()", "desc: " +desc.get(k).toString());   
            	   }
               }
               //
               characteristicList.put(services.get(i).getUuid().toString(), characteristics);
            }
            
            // Show UUID Listing !
            runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Intent it = new Intent( MainActivity.this, BleUuidsActivity.class);
					it.putStringArrayListExtra("uuids", uuids);
					// Service UUID --> Characteristic's UUID(s) !!
					Object[] uuids = characteristicList.keySet().toArray();
					for (int idx=0; idx<uuids.length; idx++) {
						String uuid = (String)uuids[idx];
						it.putStringArrayListExtra(uuid, (ArrayList<String>) characteristicList.get(uuid));
					}
					startActivity(it);
				}
			});
            
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
     	    // read the new value 
      	    Log.i("onCharacteristicChanged()", " Characteristic("+characteristic.getUuid().toString()+") value: "+ characteristic.getValue());
      	    
        };
 
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic
                                                 characteristic, int status) {
        	StringBuffer strValue = new StringBuffer();
        	byte[] result = characteristic.getValue();
        	for (int i=0; i<result.length; i++) {
        		strValue.append(String.format(" 0x%02X", result[i]));
        	}
            Log.i("onCharacteristicRead()", characteristic.getUuid().toString()+":"+strValue.toString());
        }
        
        // 改變特徵值!!
        // 設定 BluetoothGattDescriptor 
        //      必要資訊 : characteristic.getDescriptor(….).setValue(…);
		// mBluetoothGatt.writeDescriptor(descriptor);
       
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        	
        };
        
    };

}
