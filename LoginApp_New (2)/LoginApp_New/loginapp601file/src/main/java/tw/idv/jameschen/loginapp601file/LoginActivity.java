package tw.idv.jameschen.loginapp601file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
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
					// saveInfoToXML(acc, pw);
					// (1A)
					saveInfoToFile(acc, pw);
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
		// loadInfoFromXML();
		// (2A)
		loadInfoFromFile();
	}

	private void loadInfoFromFile() {
		try {
			FileInputStream is = openFileInput("account.txt");
			//轉成文字模式
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader( isr );
			String acc = br.readLine(); //一直讀，直到換行
			String pw = br.readLine();
			//
			edAccount.setText( acc );
			edPassword.setText( pw );
			//
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void saveInfoToFile(String acc, String pw) {
		try {
			FileOutputStream os = openFileOutput("account.txt", MODE_PRIVATE);
			OutputStreamWriter osw = new OutputStreamWriter(os);
			osw.write(acc+"\n");
			osw.write(pw+"\n");
			osw.flush();
			osw.close();
		}
		catch (Exception ex) {
			ex.printStackTrace();
			Log.e("XXXXXX", ex.getMessage());
		}


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
