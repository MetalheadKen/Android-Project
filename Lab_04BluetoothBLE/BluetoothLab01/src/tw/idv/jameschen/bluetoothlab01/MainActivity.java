package tw.idv.jameschen.bluetoothlab01;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {
	public static String TAG = "BlueTooth Finder";
	//
	private Button btnDoScan;
	private Button btnStopScan;
	private Button btnUnpair;
	private ListView lvBtListing;
	//
	private BluetoothAdapter btAdapter;
	private ArrayList<HashMap<String, String>> devices = new ArrayList<HashMap<String, String>>();
	private TreeSet<String> deviceMacList = new TreeSet<String>(); 
	private SimpleAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//
		btnDoScan = (Button) findViewById(R.id.btnDoScan);
		btnStopScan = (Button) findViewById(R.id.btnStopScan);
		btnUnpair = (Button) findViewById(R.id.btnUnpair);
		lvBtListing = (ListView) findViewById(R.id.lvBtListing);
		//
		btnDoScan.setOnClickListener(this);
		btnStopScan.setOnClickListener(this);
		btnUnpair.setOnClickListener(this);
		// �Q�� ListView ���
		adapter = new SimpleAdapter(
				this, 
				devices,
				android.R.layout.simple_list_item_2, 
				new String[] { "name", "mac_address" }, 
				new int[] { android.R.id.text1,android.R.id.text2 }
		);
		lvBtListing.setAdapter(adapter);
		
		// Lab101-1. ���o  BT Adapter		
        // btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (  btAdapter == null) {
           Toast.makeText(this, "BT�����D�Χ䤣��...�Э��s�ˬd!!", Toast.LENGTH_LONG).show();	
           return;
        }
		
        // Lab101-2. �ˬd�ñҰ��Ūޥ\�� ?
        if ( ! btAdapter.isEnabled()) {
        	
        }

	}

	//
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if (requestCode == 1999) {
	       if (resultCode == Activity.RESULT_OK) {
	    	   // enable BT !
	       }
	       else {
	    	   // BT -- disable !
	       }
		}
		else { 
		   super.onActivityResult(requestCode, resultCode, data);
		}
	}
	
	// �������y�����A !
	private boolean bDoScanning;

	@Override
	public void onClick(View v) {
		if (v == btnDoScan && !bDoScanning) {
			//
			Toast.makeText(this, "Begin scanning BT Device...", Toast.LENGTH_LONG).show();
			// Lab 102. �}�l�i��BT�]�Ʊ��y 
			doScanBluetoothDevices();
			//
			bDoScanning = true;
			btnStopScan.setClickable(true);
			btnDoScan.setClickable(false);

		} else if (v == btnStopScan && bDoScanning) {
			//
			Toast.makeText(this, "Stop scanning BT Device...", Toast.LENGTH_LONG).show();
			// Lab 103. ����BT�]�Ʊ��y
			cancelScanBluetoothDevices();
			//
			bDoScanning = false;
			btnStopScan.setClickable(false);
			btnDoScan.setClickable(true);
			//
		} else if (v == btnUnpair) {
			// �����Ҧ�BT�]�ƪ��t��
			this.unpairAllDevices();
		}
	}

	// Lab 102-1. �H Intent Filter ���y BT �]��
	private void doScanBluetoothDevices() {
		// 1. �����U�s������
		
		// 2. �Ұ��Ūޱ��y����

	}

	// Lab 102-2. �إ� BT �s��������
	private final BroadcastReceiver mBtReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			// Toast.makeText(MainActivity.this, "In BroadcastReceiver",
			// Toast.LENGTH_SHORT).show();
			String action = intent.getAction();
			// Case 1. �}�l  Discovery 
			// Case 2. ����  Discovery 
			// Case 3. �o�{  BT device
			// Case ...
			if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)) {
				// �M���¦����
				devices.clear();
				adapter.notifyDataSetChanged();
				deviceMacList.clear();
			}
			else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
				unregisterReceiver(mBtReceiver);
			}
			else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				Bundle data = intent.getExtras(); // EXTRA_DEVICE, EXTRA_RSSI
				// ���o������ơA�m�J�������x�s�Ŷ�
				
			}
		}
	};

	// Lab 103-1. �������y�{��
	private void cancelScanBluetoothDevices() {
		if (bDoScanning || btAdapter.isDiscovering()) {
			// �������y
			// �ϵ��U
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
			doScanBluetoothDevices();
		}
	}

	// ---------------------------------------------
	// ���ê� BT ��ƩI�s -- �b���Ӫ������i��|����!!
	// ---------------------------------------------
	private boolean waitingForBonding = false;

	private void pairDevice(BluetoothDevice device) {
		try {
			Log.d(TAG, "Start Pairing...");
			//
			waitingForBonding = true;
			Method m = device.getClass()
					.getMethod("createBond", (Class[]) null);
			m.invoke(device, (Object[]) null);
			waitingForBonding = false;
			Log.d(TAG, "Pairing finished." + device.getName());
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
	}

	private void unpairDevice(BluetoothDevice device) {
		try {
			Method m = device.getClass()
					.getMethod("removeBond", (Class[]) null);
			m.invoke(device, (Object[]) null);
			Log.d(TAG, "Un-Pairing finished:" + device.getName());
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
	}
	//
	private void unpairAllDevices() {
	       // Get the local Bluetooth adapter
        BluetoothAdapter mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        // Get a set of currently paired devices
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

        // If there are paired devices, add each one to the ArrayAdapter
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                  this.unpairDevice(device);
            }
        } 
	}
	//------------------------------------------------------
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
