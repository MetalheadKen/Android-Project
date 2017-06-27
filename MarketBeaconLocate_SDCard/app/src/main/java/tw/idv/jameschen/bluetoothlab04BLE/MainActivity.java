package tw.idv.jameschen.bluetoothlab04BLE;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import android.annotation.SuppressLint;
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
import android.widget.TextView;
import android.widget.Toast;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class MainActivity extends Activity implements OnClickListener, OnItemClickListener {
	private static final int MAX_DEVICE_COUNT = 50;
	private static final int MAX_RSSI_COUNT = 30;
	private static final int MAX_GAUSSIAN_COUNT = 3;
	private static final int MAX_COORDINATE_COUNT = 3;

	private static final int RADIUS = 0;
	private static final int X_COORDINATE = 1;
	private static final int Y_COORDINATE = 2;

	public static String TAG = "BLE Finder";
	//
	private Button btnDoScan;
	private Button btnStopScan;
	private Button btnMap;
	private ListView lvBtListing;
	//
	private TextView tvTraingle;
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
		btnMap = (Button) findViewById(R.id.btnMap);
		lvBtListing = (ListView) findViewById(R.id.lvBtListing);
		//
		tvTraingle = (TextView) findViewById(R.id.tvTriangle);
		//
		btnDoScan.setOnClickListener(this);
		btnStopScan.setOnClickListener(this);
		btnMap.setOnClickListener(this);
		// ListView
		adapter = new SimpleAdapter(
				this,
				devices,
				R.layout.beacon_item,
				new String[] { "name", "mac_address", "discovery_time", "rssi", "uuid", "txpower", "major", "minor", "distance" },
				new int[] { R.id.tvDeviceName, R.id.tvMacAddress, R.id.tvLastedDiscoveredTime, R.id.tvRSSI, R.id.tvUUID, R.id.tvTxPower, R.id.tvMajor, R.id.tvMinor, R.id.tvDistance }
		);
		lvBtListing.setAdapter(adapter);
		lvBtListing.setOnItemClickListener(this);

		// BLE
		if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			Toast.makeText(this, "BLE Not Supported", Toast.LENGTH_SHORT).show();
			finish();
		}
		// Lab103-1.BT Adapter (BluetoothManager)
		BluetoothManager bluetoothManager =
				(BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		btAdapter = bluetoothManager.getAdapter();
		//
		if (  btAdapter == null) {
			Toast.makeText(this, "BT�����D�Χ䤣��...�Э��s�ˬd!!", Toast.LENGTH_LONG).show();
			return;
		}

		// Lab103-2
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

	private boolean bDoScanning;

	@Override
	public void onClick(View v) {
		if (v == btnDoScan && !bDoScanning) {
			// Initialize : > Android 5.0.1  �s�[�c
			if (Build.VERSION.SDK_INT >= 21) {
				mLEScanner = btAdapter.getBluetoothLeScanner();
				settings = new ScanSettings.Builder()
						.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
						.build();
				filters = new ArrayList<ScanFilter>();

				//ScanFilter.Builder filterBuilder = new ScanFilter.Builder();
				//filters.add(filterBuilder.build());
				Log.e("aaa", "aaa");
			}

			//
			Toast.makeText(this, "Begin scanning BLE Device...", Toast.LENGTH_LONG).show();
			// Lab 102. �}�l�i��BT�]�Ʊ��y
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
			disconnectAllDevicesGatt();
			//
			bDoScanning = false;
			btnStopScan.setClickable(false);
			btnDoScan.setClickable(true);
			//
			//Clear Array
			for(int i = 0; i < MAX_DEVICE_COUNT; i++)
			{
				beacon_rssi_index[i] = 0;
				for(int j = 0; j < MAX_RSSI_COUNT; j++)
					beacon_rssi[i][j] = 0;
			}
			//
			tvTraingle.setText("目前定位座標為：");
		} else if (v == btnMap) {
			Intent it = new Intent(MainActivity.this, RoutePlanningActivity.class);
			startActivity(it);
		}
	}

	// Lab 102-1 Intent Filter BT
	private void doScanBleDevices() {

		devices.clear();
		adapter.notifyDataSetChanged();
		deviceMacList.clear();
		// BLE Callback
		if (Build.VERSION.SDK_INT < 21) {
			btAdapter.startLeScan(myLeScanCallback);
			// btAdapter.startLeScan( UUID[], myLeScanCallback);
			// ex: 0000180d-0000-1000-8000-00805f9b34fb : UUID for heart rate monitors.
			Log.e("vvv", "vvv");
		} else {
			mLEScanner.startScan(filters, settings, mScanCallback);
			Log.e("nnn", "nnn");
		}
	}

	// Lab 102-2
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
			connectToDevice(device);
		}
	};

	int rssi_filter, rssi_mix_filter;
	private void logBtDevice( BluetoothDevice device, int rssi, byte[] scanRecord) {
		int check = 0;
		//從最後面開始解析封包
		for (check = scanRecord.length - 33; check >= 0; check--) {
			if (scanRecord[check] != 0x00)
				break;
		}

		if(check < 20) return;

		HashMap<String, String> btmap = new HashMap<String, String>();
		btmap.put("name", device.getName());
		//btmap.put("rssi", rssi+"");
		btmap.put("mac_address", device.getAddress());
		btmap.put("discovery_time", new Date().toString());
		btmap.put("txpower", "TxPower：" + String.format("%02X", scanRecord[check]));
		btmap.put("major", "Major-Minor：" + String.format("%02d", scanRecord[check - 3] + scanRecord[check - 4]));
		btmap.put("minor", String.format("%02d", scanRecord[check - 2] + scanRecord[check - 1]));
		ParcelUuid[] uuids = device.getUuids();

		//
		StoreRssi(device, rssi);
		//
		//if(count == 0)
		{
			rssi_filter = MeanFilter(device, 1);
			rssi_mix_filter = MeanFilter(device, 0);
		}
		btmap.put("rssi", String.format("Rssi：%d", rssi));

		//
		String uuid = "";
		for (int i = check - 20; i <= check - 5; i++)
		{
			if ((i == check - 10) || (i == check - 12) || (i == check - 14) || (i == check - 16))
				uuid += "-";

			uuid += String.format("%02X", scanRecord[i]);
		}
		btmap.put("uuid", uuid);

		btmap.put("distance", "Dist：高斯濾波" + getDistance(scanRecord[check], rssi_filter) +
								"\nDist：混和濾波" + getDistance(scanRecord[check], rssi_mix_filter));

		//
		int index = deviceMacList.indexOf(device.getAddress());
		if (index == -1) {
			// a new one
			devices.add(btmap);
			deviceMacList.add(device.getAddress());
		} else {
			// replace old one !
			devices.remove(index);
			devices.add(index, btmap);
		}
		//
		StringBuffer record = new StringBuffer();
		record.append(String.format("len:%d, data=", scanRecord.length));
		for (int i = 0; i < scanRecord.length; i++) {
			record.append(String.format(" 0x%02x", scanRecord[i]));
		}
		Log.i("BLE ScanRecord", record.toString());
		//
		adapter.notifyDataSetChanged();

		TraingularPosition(device, getDistance(scanRecord[check], rssi_mix_filter), scanRecord[check - 3] + scanRecord[check - 4], scanRecord[check - 2] + scanRecord[check - 1]);
	}

	float [][] min_coordinate = new float [MAX_COORDINATE_COUNT][3]; //Three Min Device Store Distance、X Coordinate、Y Coordinate
	float [][] beacon_distance = new float [MAX_DEVICE_COUNT][3];	  //Every Device Store Distance、X Coordinate、Y Coordinate
	float axis1[] = new float [3]; //Circle1, Circle2 Equation			  //Store X Coefficient、Y Coefficient、Constant
	float axis2[] = new float [3]; //Circle2, Circle3 Equation
	float axis3[] = new float [3]; //Circle1, Circle3 Equation
	int down_count = 0;
	private void TraingularPosition(BluetoothDevice device, String distances, int x_point, int y_point) {
		int rssi_index = deviceMacList.indexOf(device.getAddress());

		//Store Distance、X Coordinate、Y Coordinate
		beacon_distance[rssi_index][RADIUS] 		 = Float.parseFloat(distances);
		beacon_distance[rssi_index][X_COORDINATE]	 = x_point;
		beacon_distance[rssi_index][Y_COORDINATE]	 = y_point;

		//Sort
		for(int i = 0; i < MAX_DEVICE_COUNT; i++)
		{
			for(int j = 0; j < MAX_DEVICE_COUNT - 1; j++)
			{
				if(beacon_distance[j][RADIUS] > beacon_distance[j + 1][RADIUS])
				{
					float temp = beacon_distance[j][RADIUS];
					beacon_distance[j][RADIUS] = beacon_distance[j + 1][RADIUS];
					beacon_distance[j + 1][RADIUS] = temp;

					temp = beacon_distance[j][X_COORDINATE];
					beacon_distance[j][X_COORDINATE] = beacon_distance[j + 1][X_COORDINATE];
					beacon_distance[j + 1][X_COORDINATE] = temp;

					temp = beacon_distance[j][Y_COORDINATE];
					beacon_distance[j][Y_COORDINATE] = beacon_distance[j + 1][Y_COORDINATE];
					beacon_distance[j + 1][Y_COORDINATE] = temp;
				}
			}
		}

		//Pass The 250 Times Function Call
		if(down_count++ < 250) return;

		//Find The Three Shortest Distance
		for(int index = 0; index < MAX_DEVICE_COUNT - 2; index++)
		{
			if (beacon_distance[index][RADIUS] > 0)
			{
				min_coordinate[0][RADIUS] 		   	= beacon_distance[index][RADIUS];
				min_coordinate[0][X_COORDINATE] 	= beacon_distance[index][X_COORDINATE];
				min_coordinate[0][Y_COORDINATE] 	= beacon_distance[index][Y_COORDINATE];

				min_coordinate[1][RADIUS] 		  	= beacon_distance[index + 1][RADIUS];
				min_coordinate[1][X_COORDINATE] 	= beacon_distance[index + 1][X_COORDINATE];
				min_coordinate[1][Y_COORDINATE] 	= beacon_distance[index + 1][Y_COORDINATE];

				min_coordinate[2][RADIUS]			= beacon_distance[index + 2][RADIUS];
				min_coordinate[2][X_COORDINATE]	= beacon_distance[index + 2][X_COORDINATE];
				min_coordinate[2][Y_COORDINATE] 	= beacon_distance[index + 2][Y_COORDINATE];

				break;
			}
		}

		//Linear Equation of Two Circle Twice Intersect
		//If Y Coefficient Both Are Equal, Then Do Exception Process
		//
		if(min_coordinate[0][Y_COORDINATE] != min_coordinate[1][Y_COORDINATE])
		{
			//Circle1 Equation - Circle2 Equation = Root Axis1
			//Y = M * K + X, M = (X1 - X2) / (Y2 - Y1), K = (R1 * R1 - R2 * R2 + X2 * X2 - X1 * X1 + Y2 * Y2 - Y1 * Y1) / (2 * Y2 - 2 * Y1)
			axis1[0] = (min_coordinate[0][X_COORDINATE] - min_coordinate[1][X_COORDINATE]) / (min_coordinate[1][Y_COORDINATE] - min_coordinate[0][Y_COORDINATE]);
			axis1[1] = -1;
			axis1[2] = ((min_coordinate[0][RADIUS] * min_coordinate[0][RADIUS]) - (min_coordinate[1][RADIUS] * min_coordinate[1][RADIUS])
					+ (min_coordinate[1][X_COORDINATE] * min_coordinate[1][X_COORDINATE]) - (min_coordinate[0][X_COORDINATE] * min_coordinate[0][X_COORDINATE])
					+ (min_coordinate[1][Y_COORDINATE] * min_coordinate[1][Y_COORDINATE]) - (min_coordinate[0][Y_COORDINATE] * min_coordinate[0][Y_COORDINATE]))
					/ (2 * (min_coordinate[1][Y_COORDINATE] - min_coordinate[0][Y_COORDINATE]));
		}
		else
		{
			//Circle1 Equation - Circle2 Equation = Root Axis1
			//Y = M * K + X, M = (X1 - X2), K = (R1 * R1 - R2 * R2 + X2 * X2 - X1 * X1) / (2)
			axis1[0] = (min_coordinate[0][X_COORDINATE] - min_coordinate[1][X_COORDINATE]);
			axis1[1] = -1;
			axis1[2] = ((min_coordinate[0][RADIUS] * min_coordinate[0][RADIUS]) - (min_coordinate[1][RADIUS] * min_coordinate[1][RADIUS])
					+ (min_coordinate[1][X_COORDINATE] * min_coordinate[1][X_COORDINATE]) - (min_coordinate[0][X_COORDINATE] * min_coordinate[0][X_COORDINATE]))
					/ (2);
		}

		if(min_coordinate[1][Y_COORDINATE] != min_coordinate[2][Y_COORDINATE])
		{
			//Circle2 Equation - Circle3 Equation = Root Axis2
			//Y = M * K + X, M = (X2 - X3) / (Y3 - Y2), K = (R2 * R2 - R3 * R3 + X3 * X3 - X2 * X2 + Y3 * Y3 - Y2 * Y2) / (2 * Y3 - 2 * Y2)
			axis2[0] = (min_coordinate[1][X_COORDINATE] - min_coordinate[2][X_COORDINATE]) / (min_coordinate[2][Y_COORDINATE] - min_coordinate[1][Y_COORDINATE]);
			axis2[1] = -1;
			axis2[2] = ((min_coordinate[1][RADIUS] * min_coordinate[1][RADIUS]) - (min_coordinate[2][RADIUS] * min_coordinate[2][RADIUS])
					+ (min_coordinate[2][X_COORDINATE] * min_coordinate[2][X_COORDINATE]) - (min_coordinate[1][X_COORDINATE] * min_coordinate[1][X_COORDINATE])
					+ (min_coordinate[2][Y_COORDINATE] * min_coordinate[2][Y_COORDINATE]) - (min_coordinate[1][Y_COORDINATE] * min_coordinate[1][Y_COORDINATE]))
					/ (2 * (min_coordinate[2][Y_COORDINATE] - min_coordinate[1][Y_COORDINATE]));
		}
		else
		{
			//Circle2 Equation - Circle3 Equation = Root Axis2
			//Y = M * K + X, M = (X1 - X2), K = (R1 * R1 - R2 * R2 + X2 * X2 - X1 * X1) / (2)
			axis1[0] = (min_coordinate[0][X_COORDINATE] - min_coordinate[1][X_COORDINATE]);
			axis1[1] = -1;
			axis1[2] = ((min_coordinate[0][RADIUS] * min_coordinate[0][RADIUS]) - (min_coordinate[1][RADIUS] * min_coordinate[1][RADIUS])
					+ (min_coordinate[1][X_COORDINATE] * min_coordinate[1][X_COORDINATE]) - (min_coordinate[0][X_COORDINATE] * min_coordinate[0][X_COORDINATE]))
					/ (2);
		}

		if(min_coordinate[0][Y_COORDINATE] != min_coordinate[2][Y_COORDINATE])
		{
			//Circle1 Equation - Circle3 Equation = Root Axis3
			//Y = M * K + X, M = (X1 - X3) / (Y3 - Y1), K = (R1 * R1 - R3 * R3 + X3 * X3 - X1 * X1 + Y3 * Y3 - Y1 * Y1) / (2 * Y3 - 2 * Y1)
			axis3[0] = (min_coordinate[0][X_COORDINATE] - min_coordinate[2][X_COORDINATE]) / (min_coordinate[2][Y_COORDINATE] - min_coordinate[0][Y_COORDINATE]);
			axis3[1] = -1;
			axis3[2] = ((min_coordinate[0][RADIUS] * min_coordinate[0][RADIUS]) - (min_coordinate[2][RADIUS] * min_coordinate[2][RADIUS])
					+ (min_coordinate[2][X_COORDINATE] * min_coordinate[2][X_COORDINATE]) - (min_coordinate[0][X_COORDINATE] * min_coordinate[0][X_COORDINATE])
					+ (min_coordinate[2][Y_COORDINATE] * min_coordinate[2][Y_COORDINATE]) - (min_coordinate[0][Y_COORDINATE] * min_coordinate[0][Y_COORDINATE]))
					/ (2 * (min_coordinate[2][Y_COORDINATE] - min_coordinate[0][Y_COORDINATE]));
		}
		else
		{
			//Circle1 Equation - Circle3 Equation = Root Axis3
			//Y = M * K + X, M = (X1 - X2), K = (R1 * R1 - R2 * R2 + X2 * X2 - X1 * X1) / (2)
			axis1[0] = (min_coordinate[0][X_COORDINATE] - min_coordinate[1][X_COORDINATE]);
			axis1[1] = -1;
			axis1[2] = ((min_coordinate[0][RADIUS] * min_coordinate[0][RADIUS]) - (min_coordinate[1][RADIUS] * min_coordinate[1][RADIUS])
					+ (min_coordinate[1][X_COORDINATE] * min_coordinate[1][X_COORDINATE]) - (min_coordinate[0][X_COORDINATE] * min_coordinate[0][X_COORDINATE]))
					/ (2);
		}

		float x_coordinate, y_coordinate;

		//Three Linear To Find One Point
		//If Y Coefficient Both Are Equal, Then Do Exception Process
		//
		if((min_coordinate[0][Y_COORDINATE] != min_coordinate[1][Y_COORDINATE]) && (min_coordinate[1][Y_COORDINATE] != min_coordinate[2][Y_COORDINATE])
				&& (min_coordinate[0][Y_COORDINATE] != min_coordinate[2][Y_COORDINATE]))
		{
			//X = (K2 - K1) / (M1 - M2), Y = M1 * X + K1
			x_coordinate = (axis2[2] - axis1[2]) / (axis1[0] - axis2[0]);
			y_coordinate = (axis1[0] * x_coordinate) + axis1[2];
		}
		else
		{
			//X = ((-1 * K1) / M1), Y = M2 * X + K2
			x_coordinate = ((-1 * axis1[2]) / axis1[0]);
			y_coordinate = (axis2[0] * x_coordinate) + axis2[2];
		}

		for (int i = 0; i < MAX_COORDINATE_COUNT; i++)
		{
			Log.e("coordinate", i + x_coordinate + " " + y_coordinate);
			Log.e("xy", axis2[0] + " " + axis2[2]);
		}

		if(Float.isNaN(x_coordinate) || Float.isNaN(y_coordinate) || Float.isInfinite(x_coordinate) || Float.isInfinite(y_coordinate))	return;

		tvTraingle.setText("目前定位座標為：(" + String.format("%.5f, %.5f", x_coordinate, y_coordinate) + ")");

		down_count = 0;
	}

	//
	ArrayList<HashMap<String, int[]>> beacon = new ArrayList<HashMap<String, int[]>>();
	int [][] beacon_rssi	  = new int [MAX_DEVICE_COUNT][MAX_RSSI_COUNT];		//[device_MaxCount][rssi_MaxCount]
	int [] beacon_rssi_index  = new int [MAX_DEVICE_COUNT];							//儲存的值為該裝置的rssi儲存到哪個位置了

	int count = MAX_RSSI_COUNT;
	private void StoreRssi(BluetoothDevice device, int rssi) {
		HashMap<String, int[]> rssi_filter = new HashMap<String, int[]>();
		int rssi_index = deviceMacList.indexOf(device.getAddress());

		if(rssi_index == -1)
		{
			// a new one
			beacon.add(rssi_filter);
			//deviceMacList.add(device.getAddress());
		}
		else
		{
			/*if(count != 0)
			{
				count--;
				beacon_rssi[rssi_index][beacon_rssi_index[rssi_index]] = rssi;

				if (beacon_rssi_index[rssi_index] == MAX_RSSI_COUNT - 1)
					beacon_rssi_index[rssi_index] = 0;
				else
					beacon_rssi_index[rssi_index]++;

				rssi_filter.put(device.getAddress(), beacon_rssi[rssi_index]);
			}
			else
			{
				for(int i = 0; i < MAX_DEVICE_COUNT; i++)
				{
					beacon_rssi_index[i] = 0;
					for(int j = 0; j < MAX_RSSI_COUNT; j++)
						beacon_rssi[i][j] = 0;
				}

				count = MAX_RSSI_COUNT;
			}*/

			beacon_rssi[rssi_index][beacon_rssi_index[rssi_index]] = rssi;

			if (beacon_rssi_index[rssi_index] == MAX_RSSI_COUNT - 1)
				beacon_rssi_index[rssi_index] = 0;
			else
				beacon_rssi_index[rssi_index]++;

			rssi_filter.put(device.getAddress(), beacon_rssi[rssi_index]);

			// replace old one !
			beacon.remove(rssi_index);
			beacon.add(rssi_index, rssi_filter);
		}
	}

	private int MeanFilter(BluetoothDevice device, int a) {
		int rssi_index = deviceMacList.indexOf(device.getAddress());

		if(rssi_index == -1)
			return 0;

		int [] rssi = beacon.get(rssi_index).get(device.getAddress());

		//for(int i = 0; i < 15; i++)
			//sum += rssi[i];
			//sum += beacon_rssi[rssi_index][i];

		Arrays.sort(rssi);
		/*for(int i = rssi.length - 2; i >= 0; i--)
		{
			for (int j = 0; j <= i; j++)
			{
				if (rssi[j] < rssi[j + 1])
				{
					int temp = rssi[j];
					rssi[j] = rssi[j + 1];
					rssi[j + 1] = temp;
				}
			}
		}*/

		int mean, middle, gaussian;

		mean = 0;
		for(int i = 0; i < MAX_RSSI_COUNT; i++)
		{
			Log.w("rssi", rssi[i] + " " + i);
			mean += rssi[i];
		}

		mean = mean / MAX_RSSI_COUNT;
		middle = (rssi[MAX_RSSI_COUNT / 2 - 1] + rssi[MAX_RSSI_COUNT / 2]) / 2;
		gaussian = GaussianFilter(rssi);

		Log.e("gaussian", gaussian + "");

		if(a == 1)
			return gaussian;
		else
			return (mean + middle + gaussian) / 3;
	}

	private int GaussianFilter1(int[] rssi) {
		double standard_deviation = 0.0, variance = 0.0;
		int gaussian = 0, count = 0;

		//Calculate Standard Deviation
		for(int i = 0; i < MAX_RSSI_COUNT; i++)
			standard_deviation += rssi[i];

		standard_deviation /= MAX_RSSI_COUNT;

		//Calculate Variance
		for(int i = 0; i < MAX_RSSI_COUNT; i++)
			variance += Math.pow((rssi[i] - standard_deviation), 2);

		variance = Math.pow((variance / MAX_RSSI_COUNT), 0.5);

		//Calculate Interval
		for(int i = 0; i < MAX_RSSI_COUNT; i++)
		{
			if((rssi[i] > (standard_deviation - variance)) && (rssi[i] < (standard_deviation + variance)))
			{
				gaussian += rssi[i];
				count++;
			}
		}

		//Exception Process
		count = (count == 0)  ? 1 : count;

		Log.e("gaussian", "" + gaussian / count);

		return gaussian / count;
	}

	private int GaussianFilter(int[] rssi) {
		int count = 0, gaussian = 0;

		int [] gaussianArrayRSSI = new int [MAX_GAUSSIAN_COUNT]; //Sotre [RSSI]
		int [] gaussianArrayTIME = new int [MAX_GAUSSIAN_COUNT]; //Sotre [TIME]

		//for (int times = (int)(MAX_RSSI_COUNT * 0.2); times < MAX_RSSI_COUNT - (int)(MAX_RSSI_COUNT * 0.2) - 1; times++)

		//Move The Different RSSI
		for (int times = 0; times < MAX_RSSI_COUNT; times += count)
		{
			count = 0;

			//Find The Same
			for(int i = 0; i < MAX_RSSI_COUNT; i++)
			{
				if(rssi[times] == rssi[i])
					count++;
			}

			Log.e("times", rssi[times] + " " + times + " " + count);

			//Find Top 20 MAX Count
			for(int index = 0; index < MAX_GAUSSIAN_COUNT; index++)
			{
				if(count > gaussianArrayTIME[index])
				{
					//Move Right
					for(int move = MAX_GAUSSIAN_COUNT - 2; move >= index; move--)
					{
						gaussianArrayRSSI[move + 1] = gaussianArrayRSSI[move];
						gaussianArrayTIME[move + 1] = gaussianArrayTIME[move];
					}

					gaussianArrayRSSI[index] = rssi[times];
					gaussianArrayTIME[index] = count;

					break;
				}
			}
		}

		//Top 20 Calculate Mean
		for(int i = 0; i < MAX_GAUSSIAN_COUNT; i++)
			gaussian += gaussianArrayRSSI[i];

		Log.e("gaussian", "" + gaussian / MAX_GAUSSIAN_COUNT);

		return gaussian / MAX_GAUSSIAN_COUNT;
	}

	private String getDistance(int txPower, double rssi) {
		double distance;

		if(rssi == 0)
			return "-1.0";

		/*double ratio = rssi * 1.0 / txPower;
		if(ratio < 1.0)
			distance = Math.pow(ratio, 10);
		else
			distance = (0.89976) * Math.pow(ratio, 7.7095) + 0.111;*/

		distance  =  Math.pow(10,(txPower - rssi) / 30 );

		Log.e("distance", distance + " ");

		return String.format("%.5f", distance);
	}

	private ScanCallback mScanCallback = new ScanCallback() {
		public void onScanResult(int callbackType, ScanResult result) {
			Log.i("callbackType", String.valueOf(callbackType));
			Log.i("result", result.toString());
			BluetoothDevice btDevice = result.getDevice();

			//Cancel MI and MI Band Connection
			if(!btDevice.getAddress().equals("88:0F:10:34:9A:62") && !btDevice.getAddress().equals("D5:3F:BA:96:96:50")  && !btDevice.getAddress().equals("EB:40:15:9D:32:82"))
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

	// Lab 103-1
	private void cancelScanBluetoothDevices() {
		if (bDoScanning) {
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

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		String btMAC = deviceMacList.get(position);
		BluetoothDevice device = btAdapter.getRemoteDevice(btMAC);
		// BLE GATT Serve GattCallback
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

		@SuppressLint("LongLogTag")
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


		//BluetoothGattDescriptor
		// characteristic.getDescriptor(K.).setValue(K);
		// mBluetoothGatt.writeDescriptor(descriptor);

		@Override
		public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {

		};

	};

}
