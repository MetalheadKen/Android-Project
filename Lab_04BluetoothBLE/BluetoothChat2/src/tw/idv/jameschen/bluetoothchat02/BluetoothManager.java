package tw.idv.jameschen.bluetoothchat02;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class BluetoothManager {
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
	private String errorMessage = "";
	// -------------------------------------------------------------
	private Context context;
	private Handler mHandler;
	private int mMode;
	// -------------------------------------------------------------
	private BluetoothAdapter mBluetoothAdapter;
	private ServerThread mBluetoothServerThread;
	private ClientThread mBluetoothClientThread;
	private SocketHandlingThread mSocketHandlingThread;
	private boolean bRelease = false;

	//
	private String receivedMessage="";
	
	// ------------------------------------------------------------------
	public BluetoothManager(Context context, Handler handler, int mode) {
		this.context = context;
		this.mHandler = handler;
		this.mMode = mode;
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	}

	public void enableBluetooth(Context context) {
		BluetoothAdapter mBtAdapter = BluetoothAdapter.getDefaultAdapter();
		if ( mBtAdapter.isEnabled() == false) {
			Intent enableIntent = new Intent( BluetoothAdapter.ACTION_REQUEST_ENABLE);
            context.startActivity(enableIntent);
		}	
	}
	
	public void enableBluetoothDiscoverable(Context context) {
		BluetoothAdapter mBtAdapter = BluetoothAdapter.getDefaultAdapter();
		Intent discoverableBtIntent = 
		                      new Intent( BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE );
		discoverableBtIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
		context.startActivity(discoverableBtIntent );
	}
	
	public static BluetoothManager actAsClient(Context context,
			Handler handler, String peerDeviceAddress) {
		BluetoothDevice btDevice = getRemoteDevice(peerDeviceAddress);
		if (btDevice == null ) return null;
		//
		BluetoothManager mgr = new BluetoothManager(context, handler, MODE_CLIENT);
		mgr.mBluetoothClientThread = mgr.new ClientThread(  btDevice, SPP_UUID);
		mgr.mBluetoothClientThread.start();
		return mgr;
	}

	public static BluetoothManager actAsClient(Context context,
			Handler handler, BluetoothDevice peerDevice) {
		BluetoothManager mgr = new BluetoothManager(context, handler,
				MODE_CLIENT);
		mgr.mBluetoothClientThread = mgr.new ClientThread(peerDevice, SPP_UUID);
		mgr.mBluetoothClientThread.start();
		return mgr;
	}

	public static BluetoothManager actAsServer(Context context,	final Handler handler, final String nickname) {
		final BluetoothManager mgr = new BluetoothManager(context, handler, MODE_SERVER);
		// mgr.enableBluetooth(context);
		mgr.enableBluetoothDiscoverable(context);
		new Thread() {
			long beginTime = System.currentTimeMillis();
			public void run() {
				// waiting for enabling the BT or exceeding 30 seconds the max limitation.
				while (BluetoothAdapter.getDefaultAdapter().isEnabled()==false &&
						    beginTime+30*1000 < System.currentTimeMillis());
				// the max timeout is 30 seconds.
				if (BluetoothAdapter.getDefaultAdapter().isEnabled()) {
				   mgr.mBluetoothServerThread = mgr.new ServerThread(nickname, SPP_UUID);
				   mgr.mBluetoothServerThread.start();
				}
				else {
					handler.sendEmptyMessage(BLUETOOTH_DISABLED);
				}
			}
		}.start();
		return mgr;
	}

	//
	public void release() {
		// marker for avoiding Error message
		bRelease  = true;
		// close the BluetoothSocket Handling Thread
		if (mSocketHandlingThread != null) {
			mSocketHandlingThread.cancel();
			mSocketHandlingThread = null;
		}
		// stop Server Thread !!
		if (mBluetoothServerThread != null) {
			mBluetoothServerThread.cancel();
			mBluetoothServerThread = null;
		}
		// stop Client Thread !!
		if (mBluetoothClientThread != null) {
			mBluetoothClientThread.cancel();
			mBluetoothClientThread = null;
		}
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		this.release();
	}

	public void handleSocket(BluetoothSocket socket) {
		// 同一時間，只會有單一個 Thread 在處理單一BluetoothSocket連線的封包!
		// cancel the old one !!
		if (mSocketHandlingThread != null) {
			mSocketHandlingThread.cancel();
			mSocketHandlingThread = null;
		}
		// issue a new Thread
		mSocketHandlingThread = new SocketHandlingThread(socket);
		mSocketHandlingThread.start();
	}

	//
	public void write(byte[] data) {
		if (mSocketHandlingThread != null) {
			mSocketHandlingThread.write(data);
		}
	}

	//
	public void cancel() {
		if (mSocketHandlingThread != null) {
			mSocketHandlingThread.cancel();
		}
	}

	//
	public static BluetoothDevice getRemoteDevice(String mac_address) {
		return BluetoothAdapter.getDefaultAdapter()
				.getRemoteDevice(mac_address);
	}

	//
	public String getErrorMessage() {
		return errorMessage;
	}

	//
	// -------------------------------------------------------------------------------------------------
	// Should initialize a new ServerThread for another BT connection
	// -------------------------------------------------------------------------------------------------
	private class ServerThread extends Thread {
		private final BluetoothServerSocket mmServerSocket;

		public ServerThread(String myName, UUID myUUID) {
			// Use a temporary object that is later assigned to mmServerSocket,
			// because mmServerSocket is final
			BluetoothServerSocket tmp = null;
			try {
				// MY_UUID is the app's UUID string, also used by the client
				// code
				tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(
						myName, myUUID);
				mHandler.sendMessage(mHandler.obtainMessage(SERVER_LISTENING));
			} catch (IOException e) {
				mHandler.sendMessage(mHandler.obtainMessage(SERVER_FAIL_LISTEN));
				errorMessage = e.getMessage();
				Log.e("BluetoothManager",
						"Error in listenUsingRfcommWithServiceRecord():"
								+ e.getMessage());
			}
			mmServerSocket = tmp;
		}

		public void run() {
			BluetoothSocket socket = null;
			// Keep listening until exception occurs or a socket is returned
			while (true) {
				try {
					socket = mmServerSocket.accept();
					mHandler.sendMessage(mHandler.obtainMessage(SERVER_ACCEPT_OK,
							socket.getRemoteDevice()));
				} catch (IOException e) {
					if (bRelease==true) break;
					//
					mHandler.sendMessage(mHandler.obtainMessage(SERVER_FAIL_ACCEPT));
					errorMessage = e.getMessage();
					Log.e("BluetoothManager",
							"Close Thread for error happened in mmServerSocket.accept():\n"
									+ e.getMessage());
					break;
				}
				// If a connection was accepted
				if (socket != null) {
					// Do work to manage the connection (in a separate thread)
					handleSocket(socket);
					
					// For the BT connection belongs to one-to-one model, so we
					// MUST STOP listening now!!
					// cancel();
					break;
				}
			}
		}

		/** Will cancel the listening socket, and cause the thread to finish */
		public void cancel() {
			try {
				mmServerSocket.close();
			} catch (IOException e) {
				// Log.i("BluetoothManager",
				// "Error happened in mmServerSocket.close():\n"+e.getMessage());
			} finally {
				mHandler.sendMessage(mHandler.obtainMessage(SOCKET_CLOSED));
			}
		}
	}

	// ---------------------------------------------
	private class ClientThread extends Thread {
		private final BluetoothSocket mmSocket;
		private final BluetoothDevice mmDevice;

		public ClientThread(BluetoothDevice peerDevice, UUID myUUID) {
			// Use a temporary object that is later assigned to mmSocket,
			// because mmSocket is final
			BluetoothSocket tmp = null;
			mmDevice = peerDevice;

			// Get a BluetoothSocket to connect with the given BluetoothDevice
			try {
				// MY_UUID is the app's UUID string, also used by the server
				// code
				tmp = peerDevice.createRfcommSocketToServiceRecord(myUUID);
			} catch (IOException e) {
				errorMessage = e.getMessage();
				Log.e("BluetoothManager",
						"Error in createRfcommSocketToServiceRecord():"
								+ e.getMessage());
			}
			mmSocket = tmp;
		}

		public void run() {
			// Cancel discovery because it will slow down the connection
			mBluetoothAdapter.cancelDiscovery();

			try {
				// Connect the device through the socket. This will block
				// until it succeeds or throws an exception
				mmSocket.connect();
				mHandler.sendMessage(mHandler.obtainMessage(CLIENT_CONNECT_OK,
						mmSocket.getRemoteDevice()));
			} catch (IOException connectException) {
				// Unable to connect; close the socket and get out
				errorMessage = connectException.getMessage();
				try {
					mmSocket.close();
				} catch (IOException e) {
					// Log.i("BluetoothManager",
					// "Error happened in mmSocket.close():\n"+e.getMessage());
				} finally {
					mHandler.sendMessage(mHandler.obtainMessage(SOCKET_CLOSED));
				}
				return;
			}

			// Do work to manage the connection (in a separate thread)
			handleSocket(mmSocket);
		}

		/** Will cancel an in-progress connection, and close the socket */
		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
				// Log.i("BluetoothManager",
				// "Error happened in mmSocket.close():\n"+e.getMessage());
			} finally {
				mHandler.sendMessage(mHandler.obtainMessage(SOCKET_CLOSED));
			}
		}
	}

	// ---------------------------------------------------------------------
	private class SocketHandlingThread extends Thread {
		private final BluetoothSocket mmSocket;
		private final InputStream mmInStream;
		private final OutputStream mmOutStream;

		public SocketHandlingThread(BluetoothSocket socket) {
			mmSocket = socket;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;

			// Get the input and output streams, using temp objects because
			// member streams are final
			try {
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			} catch (IOException e) {
				errorMessage = e.getMessage();
				Log.i("BluetoothManager",
						"Error happened in socket.getInputStream/getOutputStream():\n"
								+ e.getMessage());
			}

			mmInStream = tmpIn;
			mmOutStream = tmpOut;
		}

		public void run() {
			int bytes; // bytes returned from read()

			// Keep listening to the InputStream until an exception occurs
			while (true) {
				try {
					byte[] buffer = new byte[1024]; // buffer store for the stream
					// Read from the InputStream
					bytes = mmInStream.read(buffer);
					receivedMessage += new String(buffer, 0, bytes);
					if (receivedMessage.endsWith("\n")) {
					   // Send the obtained bytes to the UI activity
					   buffer = receivedMessage.getBytes();
					   Message msg=mHandler.obtainMessage(SOCKET_READ_OK, buffer.length, -1,buffer);
					   mHandler.sendMessage(msg);
					   receivedMessage = "";
					}
				} catch (IOException e) {
					// when releasing, the error should be ignored!
					if (bRelease) break;
					// otherwise, send something !
					Log.i("BluetoothManager",
							"Error happened in mmInputStream.read(...):\n"
									+ e.getMessage());
					errorMessage = e.getMessage();
					mHandler.sendMessage(mHandler.obtainMessage(SOCKET_FAIL_READ));
					break;
				}
			}
		}

		/* Call this from the main activity to send data to the remote device */
		public void write(byte[] bytes) {
			try {
				mmOutStream.write(bytes);
				mHandler.sendMessage(mHandler.obtainMessage(SOCKET_WRITE_OK));
			} catch (IOException e) {
				mHandler.sendMessage(mHandler.obtainMessage(SOCKET_FAIL_WRITE));
				errorMessage = e.getMessage();
				Log.i("BluetoothManager",
						"Error happened in mmOutputStream.write(...):\n"
								+ e.getMessage());
			}
		}

		/* Call this from the main activity to shutdown the connection */
		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
				// Log.i("BluetoothManager",
				// "Error happened in mmScoket.close():\n"+e.getMessage());
			} finally {
				mHandler.sendMessage(mHandler.obtainMessage(SOCKET_CLOSED));
			}
		}
	}
	// ---------------------------------------------------------------------------
}
