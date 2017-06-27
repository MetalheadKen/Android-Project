package tw.idv.james;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;

public class MainActivity extends Activity implements OnClickListener {
	EditText edServer, edPort, edMessage;
	Button btnConnect, btnDisconnect, btnSend;
	TextView tvDisplay;
	private Socket s;
	private int guess_count;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		//
		edServer = (EditText) findViewById(R.id.edServer);
		edPort = (EditText) findViewById(R.id.edPort);
		edMessage = (EditText) findViewById(R.id.edMessage);
		btnConnect = (Button) findViewById(R.id.btnConnect);
		btnDisconnect = (Button) findViewById(R.id.btnDisconnect);
		btnSend = (Button) findViewById(R.id.btnSend);
		tvDisplay = (TextView) findViewById(R.id.tvDisplay);
		//
		btnConnect.setOnClickListener(this);
		btnDisconnect.setOnClickListener(this);
		btnSend.setOnClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if ( v == btnConnect ) {
			new Thread(){
				/* run 為 Thread 的本體程式 */
				@Override
				public void run() {
					doConnect();
				}
			}.start();
		}
		else if ( v == btnDisconnect ) {
			try {
				/* 防呆機制 */
				if (s == null) return;

				s.close();
				s = null;
				tvDisplay.append("離線成功\n");
			} catch (IOException e) {
				e.printStackTrace();
				/* 印出錯誤訊息 */
				Log.e("ERROR", e.getMessage());
			}
		} else if (v == btnSend) {
			new Thread() {
				@Override
				public void run() {
					/* android.os.NetworkOnMainThreadException 表示不能在 MainThread 做網路相關動作 */
					sendMessage();
				}
			}.start();
		}
	}

	private void sendMessage() {
		String msg = edMessage.getText().toString();

		try {
			if (s == null) {
				showToast("請先建立連線");
				return;
			}

			OutputStream os = s.getOutputStream();
			/* 型態轉換，把位元組轉乘字元 */
			Writer wr = new OutputStreamWriter(os);
			/* BufferedWriter 需等 Buffer 滿了才送出 */
			BufferedWriter bwr = new BufferedWriter(wr);
			bwr.write(msg + "\n");
			/* 需寫 flush 才會送出，否則會 Queue 在那 */
			bwr.flush();
			//
			readResponse();
		} catch (IOException e) {
			e.printStackTrace();
			Log.e("ERROR", e.getMessage());
		}

	}

	private void readResponse() {
		/* 若 Socket 未連線，則必不可能收到 InputStream 和 OutputStream */
		if (s == null) return;

		try {
			InputStream is = s.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			/* 轉成 BufferedReader */
			BufferedReader br = new BufferedReader(isr);

			String recv = br.readLine();
			appendMessage(edMessage.getText().toString() + ":" + recv + "\n");

			/* 計算猜錯幾次 */
			guess_count++;
			if (recv.equals("4A0B")) {
				showToast("您答對了，總共猜了 " + guess_count + " 次");
				guess_count = 0;
			} else if (recv.equals("-1")) {
				showToast("數字不能重覆");
			} else if (recv.equals("-2")) {
				showToast("只能輸入四個數字(0~9)");
			}

		} catch (IOException e) {
			e.printStackTrace();
			Log.e("ERROR", e.getMessage());
		}
	}

	private void doConnect() {
		String host = edServer.getText().toString();
		int port = Integer.parseInt(edPort.getText().toString());

		try {
			s = new Socket( host, port );
			/* 若要改變畫面上的東西，需用 runOnUiThread ，不是 UiThread 的不能修改畫面 */
			appendMessage("連線成功\n");
			//tvDisplay.append("\n連線成功!!");
		} catch (IOException e) {
			e.printStackTrace();
			/* 印出錯誤訊息 */
			Log.e("ERROR", e.getMessage());
		}
	}

	private void appendMessage(final String ch) {
		runOnUiThread(new Runnable(){
			@Override
			public void run() {
				tvDisplay.append(ch+"");
			}});
	}

	private void appendMessage(final char ch) {
		runOnUiThread(new Runnable(){
            @Override
			public void run() {
			    tvDisplay.append(ch+"");	
            }});
		
	}

	private void showToast(final String string) {
		runOnUiThread(new Runnable(){

			@Override
			public void run() {
				Toast.makeText(MainActivity.this, string, Toast.LENGTH_LONG).show();
			}});


	}


}





