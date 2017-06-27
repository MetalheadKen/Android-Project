package tw.idv.jameschen.loginapp700sqlite01;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends Activity {
	private EditText edAccount, edPassword;
	private Button btnLogin, btnClear;
	//
	private SharedPreferences sp;

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
				// (1)
				// if (acc.equals("jameschen") && pw.equals("123456")) {
				if (isLegalAccount( acc, pw)==true) {
					Toast.makeText(getApplicationContext(), acc + ": 登入成功。",
							Toast.LENGTH_LONG).show();
					Intent it = new Intent(getApplicationContext(),
							MainActivity.class);
					//
					Bundle data = new Bundle();
					data.putString("account", acc);
					data.putInt("age", 25);
					it.putExtras(data);
					//
					// (1) 儲存到 SharedPreferences : Account+Password
					sp = getSharedPreferences( "account", MODE_PRIVATE);
					sp.edit().putString("account", acc).commit();
					sp.edit().putString("password", pw).commit();

					//
					// startActivity( it );
					//
					startActivityForResult(it, 1001);
				} else {
					Toast.makeText(getApplicationContext(), acc + ": 登入失敗!!",
							Toast.LENGTH_LONG).show();
				}
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
		//
	}

	protected boolean isLegalAccount(String acc, String pw) {
		// 1. 開啟資料庫連線 (Schema, username, password, encoding...)

		// 2. 查詢帳號、密碼

		// 3. 確認結果

		// 4. 關閉必要連線、釋放資源.

		// 5. 回傳
		return false;
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
