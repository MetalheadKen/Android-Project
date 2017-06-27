package tw.idv.jameschen.bluetoothchat02;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class ChatMainActivity extends Activity implements OnClickListener {
	private static final boolean DEBUG = true;
	private static final String TAG = "BluetoothChat2";
	//
	private ProgressBar progressBar1;
	private Button btnPickDevice, btnDisconnect, btnSend;
	private Button btnCancel;
	private Button btnAsServer;
	private EditText etMessageToSend;
	private EditText etReceivedMessage;
	private TextView tvStatus;
	//
	private LinearLayout layoutModePanel, layoutProcessPanel;
	//
	private BluetoothDevice remoteBtDevice;
	private BluetoothManager btManager;
	//
	private boolean bAsServer = false;
	public static final int MODE_IDLE = 0;
	public static final int MODE_WAITING = 1;
	public static final int MODE_CONNECTING = 2;
	public static final int MODE_CONNECTED = 3;
	public static final int MODE_DISCONNECTED = 4;
	public static final int MODE_CANCEL = 5;
	private int                   currentMode = MODE_IDLE;
	//
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//
		progressBar1 = (ProgressBar) findViewById(R.id.progressBar1);
		btnPickDevice = (Button) findViewById(R.id.btnPickDevice);
		btnDisconnect = (Button) findViewById(R.id.btnDisconnect);
		btnCancel = (Button) findViewById(R.id.btnCancel);
		//
		btnAsServer = (Button) findViewById(R.id.btnAsServer);
		btnSend = (Button) findViewById(R.id.btnSendMsg);
		//
		layoutModePanel = (LinearLayout) findViewById(R.id.layoutModePanel);
		layoutProcessPanel = (LinearLayout) findViewById(R.id.layoutProcessPanel);
		//
		etMessageToSend = (EditText) findViewById(R.id.etMessageToSend);
		etReceivedMessage = (EditText) findViewById(R.id.etReceivedMessage);
		etReceivedMessage.setEnabled(false);
		etReceivedMessage.setBackgroundColor(Color.LTGRAY);
		tvStatus = (TextView) findViewById(R.id.tvStatus);
		tvStatus.setBackgroundColor(Color.GRAY);
		//
		btnPickDevice.setOnClickListener(this);
		btnDisconnect.setOnClickListener(this);
		btnSend.setOnClickListener(this);
		btnAsServer.setOnClickListener(this);
		btnCancel.setOnClickListener(this);
		//
		refreshPanelMode(MODE_IDLE);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		//
		if (btManager != null) {
			btManager.release();
		}
	}

	//
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnPickDevice:
			Intent it = new Intent(this, DevicePickerActivity.class);
			startActivityForResult(it, 999);
			break;
		case R.id.btnDisconnect:
			if (btManager != null) {
				btManager.release();
			}
			refreshPanelMode(MODE_DISCONNECTED);	
			break;
		case R.id.btnSendMsg:
			String msg = etMessageToSend.getText().toString() + "\n";
			btManager.write(msg.getBytes());
			break;
		case R.id.btnAsServer:
			bAsServer = true;
			btManager = BluetoothManager.actAsServer(ChatMainActivity.this, mBtHandler, "ChatServer");
			tvStatus.setText("[As a Server] 等待連線中...");
			refreshPanelMode(MODE_WAITING);
			break;
		case R.id.btnCancel:
			btManager.release();
			refreshPanelMode(MODE_CANCEL);
			break;
		}
	}

	//
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 999) {
			String prefixStr = getResources().getString(R.string.selected);
			if (resultCode == RESULT_OK) {
				String name = data
						.getStringExtra(DevicePickerActivity.EXTRA_DEVICE_NAME);
				String mac_address = data
						.getStringExtra(DevicePickerActivity.EXTRA_DEVICE_ADDRESS);
				String bt_info = String.format(
						"Device name: %s\nMac address: %s", name, mac_address);
				// Toast.makeText( this, bt_info, Toast.LENGTH_LONG).show();
				Log.i(TAG, bt_info);
				tvStatus.setText(prefixStr + bt_info);

				// After picking, we need make the connection with the selected
				// device.
				btManager = BluetoothManager.actAsClient(this, mBtHandler,
						mac_address);
				//
				refreshPanelMode(MODE_CONNECTING);
			} else {
				// Toast.makeText( this, "Nothing selected!",
				// Toast.LENGTH_LONG).show();
				Log.i("BluetoothChat2", "Nothng selected!");
				tvStatus.setText(prefixStr + "N/A");
			}
		} else
			super.onActivityResult(requestCode, resultCode, data);
	}

	// The Handler that gets information back from the BluetoothService
	private final Handler mBtHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case BluetoothManager.CLIENT_CONNECT_OK:
				showToast("Client 狀態: 連線成功 !");
				refreshPanelMode(MODE_CONNECTED);
				break;
			case BluetoothManager.BLUETOOTH_DISABLED:
				showDialogMessage("Bluetooth 未啟動?", "請啟動藍芽功能，再執行相關程式。");
				refreshPanelMode(MODE_IDLE);
				break;
			case BluetoothManager.CLIENT_FAIL_CONNECT:
				showDialogMessage("BluetoothSocket", "connect 失敗!\n請重新啟動藍芽功能。");
				refreshPanelMode(MODE_DISCONNECTED);
				break;
			case BluetoothManager.SERVER_FAIL_ACCEPT:
				showDialogMessage("BluetoothServerSocket", "Accept 失敗!\n請重新啟動藍芽功能。");
				refreshPanelMode(MODE_DISCONNECTED);
				break;
			case BluetoothManager.SERVER_ACCEPT_OK:
				showToast("Server 狀態: Accept OK !");
				refreshPanelMode(MODE_CONNECTED);
				break;
			case BluetoothManager.SERVER_FAIL_LISTEN:
				showDialogMessage("BluetoothServerSocket", "Listen 失敗!\n請重新啟動藍芽功能。");
				refreshPanelMode(MODE_DISCONNECTED);
				break;
			case BluetoothManager.SERVER_LISTENING:
				showToast("Server 狀態: Listening !");
				refreshPanelMode(MODE_WAITING);
				break;
			case BluetoothManager.SOCKET_CONNECTED:
				refreshPanelMode(MODE_CONNECTED);
				showToast("Socket 狀態: 連線成功 !");
				break;
			case BluetoothManager.SERVER_SOCKET_CLOSED:
				showToast("ServerSocket 狀態: 關閉 !");
				refreshPanelMode(MODE_DISCONNECTED);
				break;
			case BluetoothManager.SOCKET_CLOSED:
				showToast("Socket 狀態: 關閉 !");
				refreshPanelMode(MODE_DISCONNECTED);
				break;
			case BluetoothManager.SOCKET_FAIL_WRITE:
				showDialogMessage("BluetoothSocket", "傳送資料發生問題!!!");
				break;
			case BluetoothManager.SOCKET_WRITE_OK:
				showToast("傳送資料: 成功!");
				break;
			case BluetoothManager.SOCKET_FAIL_READ:
				showDialogMessage("BluetoothSocket", "接收資料發生問題!!!");
				break;
			case BluetoothManager.SOCKET_READ_OK:
				byte[] readBuf = (byte[]) msg.obj;
				String recvData = getCurrentTimString() + ":"  + new String(readBuf, 0, msg.arg1);
				etReceivedMessage.setText(  recvData + "\n" + etReceivedMessage.getText().toString());
				showToast("收到資料!");
				break;
			case BluetoothManager.SHOW_TOAST:
				String data = msg.getData().getString(
						BluetoothManager.MESSAGE_TOAST);
				showToast(data);
				break;
			}
		}
	};

	// int mode 
	// 0 : initial mode
	// 1 : waiting connection
	// 2 : ready to send/receive message
	public void refreshPanelMode(int mode) {
		 // update the UI for current mode
		  switch (mode) {
		  case MODE_IDLE:
		  case MODE_DISCONNECTED: 
		  case MODE_CANCEL:
			  layoutModePanel.setVisibility(View.VISIBLE);
				layoutProcessPanel.setVisibility(View.GONE);
				btnAsServer.setVisibility(View.VISIBLE);
				btnPickDevice.setVisibility(View.VISIBLE);
				btnCancel.setVisibility(View.GONE);
				progressBar1.setVisibility(View.GONE);
				tvStatus.setText("Status: N/A");
			  break;
		  case MODE_WAITING:
		  case MODE_CONNECTING:
				//layoutModePanel.setVisibility(View.VISIBLE);
				//layoutProcessPanel.setVisibility(View.GONE);
				btnAsServer.setVisibility(View.GONE);
				btnPickDevice.setVisibility(View.GONE);
				btnCancel.setVisibility(View.VISIBLE);
				progressBar1.setVisibility(View.VISIBLE);
			 break;
		  case MODE_CONNECTED: 
				layoutModePanel.setVisibility(View.GONE);
				layoutProcessPanel.setVisibility(View.VISIBLE);
				progressBar1.setVisibility(View.GONE);
			  break;
		  }
		  
		  // Show something for wrong state-switching !!
		 switch (currentMode) {
		 case MODE_IDLE:
			 break;
		 case MODE_WAITING:
			 if (mode == MODE_DISCONNECTED) {
				 showDialogMessage("Server連線失敗",btManager.getErrorMessage());
				 mode = MODE_IDLE;
			 }
			 break;
		 case MODE_CONNECTING:
			 if (mode == MODE_DISCONNECTED) {
				 showDialogMessage("Client連線失敗",btManager.getErrorMessage());
				 mode = MODE_IDLE;
			 }
			 break;
		 case MODE_CONNECTED:
			 break;
		 case MODE_DISCONNECTED:
			 break;
		 case MODE_CANCEL:
			 break;
		 }
		 //
		 currentMode = (mode== MODE_DISCONNECTED)? MODE_IDLE : mode;
	}
	
	public void showToast(String message) {
		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT)
				.show();
	}

	public void showDialogMessage(String title, String message) {
		new AlertDialog.Builder(this).setTitle(title).setMessage(message)
				.setNeutralButton("OK", null).show();
	}

	public String getCurrentTimString() {
		SimpleDateFormat ff = new SimpleDateFormat("HH:mm:ss");
		return ff.format(new Date());
	}
}
