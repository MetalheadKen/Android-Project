package tw.idv.jameschen.loginapp801json;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

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
				// (1B)
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

	// Lab800 - 定義 AsyncTask
	class LoginAsyncTask extends AsyncTask<String, Integer, Boolean> {
		//
		String acc;
		int sid = -1;
		//
		private ProgressDialog pDialog;
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			// 顯示登入中的訊息
			pDialog = new ProgressDialog(LoginActivity.this);
			pDialog.setMax(100);
			pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			pDialog.setMessage("登入檢查中...");
			pDialog.setCanceledOnTouchOutside(false);
			pDialog.setCancelable(true);
			pDialog.setButton(
					ProgressDialog.BUTTON_NEGATIVE,
					"Cancel",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							cancel(true);
						}
					});
			pDialog.show();
			//
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			//
			if (result == true) {
				Toast.makeText(getApplicationContext(), acc + ": 登入成功。",
						Toast.LENGTH_LONG).show();
				Intent it = new Intent(getApplicationContext(),	MainActivity.class);
				//
				Bundle data = new Bundle();
				data.putString("account", acc);
				data.putInt("sid", sid);
				it.putExtras(data);
				//
				startActivity(it);
			}
			else {
				Toast.makeText(getApplicationContext(), acc + ": 登入失敗!!",
						Toast.LENGTH_LONG).show();
			}

			// 讓畫面消失
			pDialog.dismiss();
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			// 讓畫面消失
			pDialog.dismiss();
		}

		@Override
		protected Boolean doInBackground(String... params) {
			acc = params[0];
			String pw = params[1];

			// for demo ONLY : 5  秒
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//
			String ccc =
					String.format(
							"http://163.17.83.53/demo01/checkAccount.php?name=%s&pw=%s",
							acc, pw);
			//
			try {
				URL url = new URL( ccc );
				URLConnection conn = url.openConnection();
				//
				String encoding = conn.getContentEncoding();
				// String result = (String)conn.getContent();
				InputStream is = (InputStream)conn.getContent();
				BufferedReader br = new BufferedReader( new InputStreamReader(is));
				String result = br.readLine();
				//
				Log.i("URL_TEST", result);
				// -1 : fail; otherwise, result = sid
				if (result.contains("-1")) {
					sid = -1;
					return false;
				}
				else  {
					sid = Integer.parseInt(result);
					return true;
				}
			}
			catch(Exception ex) {
				Log.i("URL_TEST", ex.getMessage());
				ex.printStackTrace();
				return false;
			}
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
