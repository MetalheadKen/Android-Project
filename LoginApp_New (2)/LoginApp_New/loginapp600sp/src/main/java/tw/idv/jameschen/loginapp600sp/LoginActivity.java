package tw.idv.jameschen.loginapp600sp;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class LoginActivity extends Activity {
	private EditText edAccount, edPassword;
	private Button btnLogin, btnClear;
	private ImageView imageView1;

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
		imageView1 = (ImageView) findViewById(R.id.imageView1);
		// imageView1.setOnCreateContextMenuListener(this);
		registerForContextMenu(imageView1);
		//
		btnLogin.setOnClickListener(new Button.OnClickListener() {

			public void onClick(View v) {

				String acc = edAccount.getText().toString();
				String pw = edPassword.getText().toString();
				//
				if (acc.equals("3A317032") && pw.equals("123456")) {
					Toast.makeText(getApplicationContext(), acc + ": 登入成功。",
							Toast.LENGTH_LONG).show();
					Intent it = new Intent(getApplicationContext(),
							MainActivity.class);
					// (1)
					saveInfoToXML(acc, pw);
					//
					startActivity(it);
				} else {
					Toast.makeText(getApplicationContext(), acc + ": 登入失敗!!",
							Toast.LENGTH_LONG).show();
				}
			}
		});
		btnClear.setOnClickListener(new Button.OnClickListener() {

			public void onClick(View v) {

				edAccount.setText("");
				edPassword.setText("");
			}
		});

		// (2)
		loadInfoFromXML();
	}

	private void loadInfoFromXML() {
		//Get From account.xml
		SharedPreferences sp = getSharedPreferences("account", MODE_PRIVATE);
		String acc = sp.getString("account", "");
		String pw = sp.getString("password", "");

		edAccount.setText( acc );
		edPassword.setText( pw );
	}

	private void saveInfoToXML(String acc, String pw) {
		//Store in account.xml
		SharedPreferences sp = getSharedPreferences("account", MODE_PRIVATE);
		//取得編輯模式的控制物件
		SharedPreferences.Editor sped = sp.edit();
		sped.putString("account", acc);
		sped.putString("password", pw);
		//減少I/O存取次數，以利程式執行速度
		sped.commit();
	}


	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
									ContextMenuInfo menuInfo) {
		if (v == imageView1) {
			menu.add(100, 1001, 1, "ITEM1");
			menu.add(100, 1002, 2, "ITEM2");
		}
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case 1001:
				Toast.makeText(this, "Item1 clicked", Toast.LENGTH_LONG).show();
				break;
			case 1002:
				Toast.makeText(this, "Item2 clicked", Toast.LENGTH_LONG).show();
				break;
			default:
				return super.onContextItemSelected(item);
		}
		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_login, menu);
		return true;
	}

	//
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_settings) {
			Toast.makeText(LoginActivity.this, "選單被點了!", Toast.LENGTH_LONG)
					.show();
			return true;
		} else
			return super.onOptionsItemSelected(item);
	}
}
