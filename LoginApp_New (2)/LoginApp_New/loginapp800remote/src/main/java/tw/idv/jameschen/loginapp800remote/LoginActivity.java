package tw.idv.jameschen.loginapp800remote;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginActivity extends Activity {
	private EditText edAccount, edPassword;
	private Button btnLogin, btnClear;
	//
	private SharedPreferences sp;

	//
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		//
		edAccount = (EditText) findViewById(R.id.edAccount);
		edPassword = (EditText) findViewById(R.id.edPassword);
		btnLogin = (Button) findViewById(R.id.btnLogin);
		btnClear = (Button) findViewById(R.id.btnClear);
		//
		btnLogin.setOnClickListener(new Button.OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				String acc = edAccount.getText().toString();
				String pw = edPassword.getText().toString();

				// Lab800 - 1. 呼叫認證帳密的方法
				checkLegalAccount(acc, pw);
				//
			}
		});
		btnClear.setOnClickListener(new Button.OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				edAccount.setText("");
				edPassword.setText("");
			}
		});

		// (2) pre-load Account/password
		sp = getSharedPreferences( "account", MODE_PRIVATE);
		String acc = sp.getString("account", "");
		String pw = sp.getString("password", "");
		edAccount.setText(acc);
		edPassword.setText(pw);
	}

	protected void checkLegalAccount(String acc, String pw) {
		LoginAsyncTask task = new LoginAsyncTask();
		task.execute(acc, pw);
	}

	// Lab800 - 2. 定義 AsyncTask
	class LoginAsyncTask extends AsyncTask<String, Integer, Boolean> {
		//
		String acc;
		int sid = -1;
		//
		private ProgressDialog pDialog;
		//
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			// (*) 可以利用 ProgressDialog 顯示登入中的訊息
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			//
			// (*) 讓 ProgressDialog 畫面消失
			if (result) {
				Intent it = new Intent( LoginActivity.this, MainActivity.class);
				it.putExtra("sid", sid);
				it.putExtra("account", acc);
				startActivity(it);
			}
			else {
				Toast.makeText( LoginActivity.this, "登入失敗", Toast.LENGTH_LONG).show();
			}
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			// 讓 ProgressDialog 畫面消失
		}

		@Override
		protected Boolean doInBackground(String... params) {
			acc = params[0];
			String pw = params[1];

			// for demo ONLY : 5  秒
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// (*) 以 HttpURLConnection 進行HTTP連線以檢查帳號與密碼
			String httpStr = String.format("http://163.17.83.53/demo01/checkAccount.php?name=%s&pw=%s", acc, pw);
			try {
				URL url = new URL( httpStr );
				HttpURLConnection httpClient = (HttpURLConnection) url.openConnection();
				//
				httpClient.connect();
				int statusCode = httpClient.getResponseCode();
				if (statusCode == 200) {
					InputStream input = (InputStream) httpClient.getContent();
					BufferedReader br = new BufferedReader( new InputStreamReader(input));
					String result = br.readLine();
					httpClient.disconnect();
					//
					if (result.equals("-1")) {
						sid = -1;
						return false;
					}
					else {
						sid = Integer.parseInt(result);
						return true;
					}
				}
				else {
					httpClient.disconnect();
					sid = -1;
					return false;
				}
				//

			}
			catch( Exception ex) {
				return false;
			}
			finally {
			}
			//
			// return false;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_login, menu);
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if (requestCode == 1001) {
			if (resultCode == 1) {
				edAccount.setText("");
				edPassword.setText("");
			} else {
				finish();
			}
		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}
}
