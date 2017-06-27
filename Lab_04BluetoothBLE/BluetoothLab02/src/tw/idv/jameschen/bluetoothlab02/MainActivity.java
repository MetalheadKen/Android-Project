package tw.idv.jameschen.bluetoothlab02;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
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

public class MainActivity extends Activity implements OnClickListener {
	private static final boolean DEBUG = true;
	private static final String TAG = "BluetoothLab";
	//
	// -------------------------------------------------------------
	public static final int MODE_SERVER = 2;
	public static final int MODE_CLIENT = 1;
	// -------------------------------------------------------------
	public static final UUID SPP_UUID = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");
	public static final int SOCKET_CLOSED = 0;
	public static final int SERVER_SOCKET_CLOSED = 10;
	public static final int SOCKET_CONNECTED = 1;
	public static final int SOCKET_READ_OK = 2;
	public static final int SOCKET_WRITE_OK = 3;
	public static final int SOCKET_FAIL_READ = -2;
	public static final int SOCKET_FAIL_WRITE = -3;
	public static final int CLIENT_FAIL_CONNECT = -100;
	public static final int CLIENT_CONNECT_OK = 100;
	public static final int SERVER_LISTENING = 200;
	public static final int SERVER_ACCEPT_OK = 201;
	public static final int SERVER_FAIL_LISTEN = -200;
	public static final int SERVER_FAIL_ACCEPT = -201;
	public static final int BLUETOOTH_DISABLED = -300;
	public static final int SHOW_TOAST = 900;
	public static final String MESSAGE_TOAST = "TOAST";
	//
	private Button btnConnectServer, btnDisconnect, btnSend;
	private EditText etServerMacAddress;
	private Button btnCancel;
	private Button btnAsServer;
	private EditText etMessageToSend;
	private EditText etReceivedMessage;
	private TextView tvStatus;
	//
	private LinearLayout layoutModePanel, layoutProcessPanel;
	//
	private BluetoothDevice remoteBtDevice;
	private BluetoothAdapter btAdapter;
	private BluetoothSocket btSocket;
	private BluetoothServerSocket btServerSocket;
	//
	private boolean bAsServer = false;
	//
	public static final int MODE_IDLE = 0;
	public static final int MODE_WAITING = 1;
	public static final int MODE_CONNECTING = 2;
	public static final int MODE_CONNECTED = 3;
	public static final int MODE_DISCONNECTED = 4;
	public static final int MODE_CANCEL = 5;
	private int currentMode = MODE_IDLE;
	//
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//
		btnConnectServer = (Button) findViewById(R.id.btnConnectServer);
		btnDisconnect = (Button) findViewById(R.id.btnDisconnect);
		//
		btnAsServer = (Button) findViewById(R.id.btnAsServer);
		btnSend = (Button) findViewById(R.id.btnSendMsg);
		//
		layoutModePanel = (LinearLayout) findViewById(R.id.layoutModePanel);
		layoutProcessPanel = (LinearLayout) findViewById(R.id.layoutProcessPanel);
		//
		etServerMacAddress = (EditText)findViewById(R.id.etServerMacAddress);
		etMessageToSend = (EditText) findViewById(R.id.etMessageToSend);
		etReceivedMessage = (EditText) findViewById(R.id.etReceivedMessage);
		etReceivedMessage.setEnabled(false);
		etReceivedMessage.setBackgroundColor(Color.LTGRAY);
		tvStatus = (TextView) findViewById(R.id.tvStatus);
		tvStatus.setBackgroundColor(Color.GRAY);
		//
		btnConnectServer.setOnClickListener(this);
		btnDisconnect.setOnClickListener(this);
		btnSend.setOnClickListener(this);
		btnAsServer.setOnClickListener(this);
		btnCancel.setOnClickListener(this);
		
		// Lab 201-1. Initialize BT
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null){
        	showDialogMessage("���~", "�S���Ū޳]��? �нT�{!!!");
            return;
        }
        
        // Lab 201-2. �ˬd�ñҰ��Ūޥ\�� ?
        if (!btAdapter.isEnabled()){
        	Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        	startActivityForResult(intent, 1999);
        }
        // 
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
		// disconnect BT connection if necessary
		disconnectBtConnection();
	}

	//
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnConnectServer:
			bAsServer = false;
			tvStatus.setText("[As Client] �إ߳s�u��...");
			// Lab 202. connect to BT Server (MAC Accress)
		    String mac = etServerMacAddress.getText().toString();
		    BluetoothDevice device = btAdapter.getRemoteDevice(mac);
			try {
				btSocket = device.createRfcommSocketToServiceRecord(SPP_UUID);
			    btSocket.connect();
			    //
			    InputStream is = btSocket.getInputStream();
			    OutputStream os = btSocket.getOutputStream();
			    // ���ѫ�....
			    
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    
			break;
		case R.id.btnDisconnect:
			// disconnect BT connection if necessary
			disconnectBtConnection();
			break;
		case R.id.btnSendMsg:
			String msg = etMessageToSend.getText().toString() + "\n";
			// send msg to peer
			break;
		case R.id.btnAsServer:
			bAsServer = true;
			tvStatus.setText("[As Server] ���ݳs�u��...");
			// waiting for remote BT connection request (client)
			// Lab 203. Server -- ���ݳs�u !!
			try {
				btServerSocket = btAdapter.listenUsingRfcommWithServiceRecord("JamesChen", SPP_UUID);
			    btSocket = btServerSocket.accept();
			    // Tx, Rx
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			break;
		}
	}

	private void disconnectBtConnection() {
		//
		if (btSocket != null) {
			try {
				btSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//
		if (bAsServer) {
		    try {
				btServerSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	// The Handler that gets information back from the BluetoothService
	private final Handler mBtHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case CLIENT_CONNECT_OK:
				showToast("Client ���A: �s�u���\ !");
				refreshPanelMode(MODE_CONNECTED);
				break;
			case BLUETOOTH_DISABLED:
				showDialogMessage("Bluetooth ���Ұ�?", "�бҰ��Ūޥ\��A�A��������{���C");
				refreshPanelMode(MODE_IDLE);
				break;
			case CLIENT_FAIL_CONNECT:
				showDialogMessage("BluetoothSocket", "connect ����!\n�Э��s�Ұ��Ūޥ\��C");
				refreshPanelMode(MODE_DISCONNECTED);
				break;
			case SERVER_FAIL_ACCEPT:
				showDialogMessage("BluetoothServerSocket", "Accept ����!\n�Э��s�Ұ��Ūޥ\��C");
				refreshPanelMode(MODE_DISCONNECTED);
				break;
			case SERVER_ACCEPT_OK:
				showToast("Server ���A: Accept OK !");
				refreshPanelMode(MODE_CONNECTED);
				break;
			case SERVER_FAIL_LISTEN:
				showDialogMessage("BluetoothServerSocket", "Listen ����!\n�Э��s�Ұ��Ūޥ\��C");
				refreshPanelMode(MODE_DISCONNECTED);
				break;
			case SERVER_LISTENING:
				showToast("Server ���A: Listening !");
				refreshPanelMode(MODE_WAITING);
				break;
			case SOCKET_CONNECTED:
				refreshPanelMode(MODE_CONNECTED);
				showToast("Socket ���A: �s�u���\ !");
				break;
			case SERVER_SOCKET_CLOSED:
				showToast("ServerSocket ���A: ���� !");
				refreshPanelMode(MODE_DISCONNECTED);
				break;
			case SOCKET_CLOSED:
				showToast("Socket ���A: ���� !");
				refreshPanelMode(MODE_DISCONNECTED);
				break;
			case SOCKET_FAIL_WRITE:
				showDialogMessage("BluetoothSocket", "�ǰe��Ƶo�Ͱ��D!!!");
				break;
			case SOCKET_WRITE_OK:
				showToast("�ǰe���: ���\!");
				break;
			case SOCKET_FAIL_READ:
				showDialogMessage("BluetoothSocket", "������Ƶo�Ͱ��D!!!");
				break;
			case SOCKET_READ_OK:
				byte[] readBuf = (byte[]) msg.obj;
				String recvData = getCurrentTimString() + ":"  + new String(readBuf, 0, msg.arg1);
				etReceivedMessage.setText(  recvData + "\n" + etReceivedMessage.getText().toString());
				showToast("������!");
				break;
			case SHOW_TOAST:
				String data = msg.getData().getString(MESSAGE_TOAST);
				showToast(data);
				break;
			}
		}
	};

	// 
	public void refreshPanelMode(int mode) {
		 // update the UI for current mode
		  switch (mode) {
		  case MODE_IDLE:
		  case MODE_DISCONNECTED: 
		  case MODE_CANCEL:
			  layoutModePanel.setVisibility(View.VISIBLE);
				layoutProcessPanel.setVisibility(View.GONE);
				btnAsServer.setVisibility(View.VISIBLE);
				btnConnectServer.setVisibility(View.VISIBLE);
				btnCancel.setVisibility(View.GONE);
				tvStatus.setText("Status: N/A");
			  break;
		  case MODE_WAITING:
		  case MODE_CONNECTING:
				//layoutModePanel.setVisibility(View.VISIBLE);
				//layoutProcessPanel.setVisibility(View.GONE);
				btnAsServer.setVisibility(View.GONE);
				btnConnectServer.setVisibility(View.GONE);
				btnCancel.setVisibility(View.VISIBLE);
			 break;
		  case MODE_CONNECTED: 
				layoutModePanel.setVisibility(View.GONE);
				layoutProcessPanel.setVisibility(View.VISIBLE);
			  break;
		  }
		  
		  // Show something for wrong state-switching !!
		 switch (currentMode) {
		 case MODE_IDLE:
			 break;
		 case MODE_WAITING:
			 if (mode == MODE_DISCONNECTED) {
				 showDialogMessage("���~", "Server�s�u����!");
				 mode = MODE_IDLE;
			 }
			 break;
		 case MODE_CONNECTING:
			 if (mode == MODE_DISCONNECTED) {
				 showDialogMessage("���~", "Client�s�u����!");
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
