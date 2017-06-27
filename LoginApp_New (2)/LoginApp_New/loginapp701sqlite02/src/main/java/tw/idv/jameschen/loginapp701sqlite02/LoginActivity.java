package tw.idv.jameschen.loginapp701sqlite02;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
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

	//
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

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
				//
				// if (acc.equals("jameschen") && pw.equals("123456")) {
				if (isLegalAccount(acc, pw)==true) {
					Toast.makeText(getApplicationContext(), acc + ": 登入成功。",
							Toast.LENGTH_LONG).show();
					Intent it = new Intent(getApplicationContext(),
							MainActivity.class);

					// Lab701 - 1 Pass parameters to Enrollment
					Bundle bundle = new Bundle();
					bundle.putString("account", acc);
					bundle.putString("DBFILE", dbFilePath);
					bundle.putInt("SID", sid);
					it.putExtras( bundle );
					//
					// 儲存到 SharedPreferences : Account+Password
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

		// pre-load Account/password
		sp = getSharedPreferences( "account", MODE_PRIVATE);
		String acc = sp.getString("account", "");
		String pw = sp.getString("password", "");
		edAccount.setText(acc);
		edPassword.setText(pw);

		// (3) for testing ONLY !!
		copySqliteDB2PrivatePath("loginapp.sqlite", false);
	}

	String dbFilePath = "";
	private boolean copySqliteDB2PrivatePath(String dbName, boolean bOverwrite) {
		//
		//
		try {
			// (A) /data/data/... 私有路徑
			String PRIVATE_DB_PATH = "/data/data/"
					+ this.getApplicationContext().getPackageName()
					+ "/databases";
			// (B) For DEBUG
			//PRIVATE_DB_PATH = Environment.getExternalStorageDirectory()
			//		.getAbsolutePath() + "/mydb";
			File dbPath = new File(PRIVATE_DB_PATH);
			// 檢查路徑是否存在?
			if (dbPath.exists() == false) {
				dbPath.mkdirs();
			}
			//
			dbFilePath = dbPath + "/" + dbName;
			File dbFile = new File(dbFilePath);
			// 檔案是否存在?? 或者需要覆寫舊的DB File(開發測試用)
			if (dbFile.exists() == false || bOverwrite) {
				// (A) /assets/loginapp.sqlite
				InputStream is = this.getAssets().open(dbName);
				// (B) /res/raw/loginapp.sqlite
				// InputStream is =
				// getResources().openRawResource(R.raw.loginapp);
				// 輸出檔案
				FileOutputStream fos = new FileOutputStream(dbFilePath);
				//
				byte[] buf = new byte[4096];
				int cc = 0;
				while ((cc = is.read(buf)) > 0) {
					fos.write(buf, 0, cc);
				}
				is.close();
				fos.flush();
				fos.close();
				//
				Toast.makeText(this, "Copy DB file Successfully!", 1).show();
				Log.i("KSI_mPOS", "[DB_COPY] : Successfully copy the DB file!");
				Log.i("KSI_mPOS", dbFilePath.toString());
				return true;
			} else {
				Toast.makeText(this, "DB file existed, Skipped!!", 1).show();
				Log.i("KSI_mPOS", "[DB_COPY] : DB file existed, skipping!");
				return true;
			}
		} catch (Exception e) {
			Log.i("KSI_mPOS", "[DB_COPY], Error: " + e.getMessage());
			Toast.makeText(this, "Fail to copy DB file? msg:" + e.getMessage(), 1).show();
			return false;
		}
	}

	//
	int sid = -1;
	protected boolean isLegalAccount(String acc, String pw) {
		// 1. 開啟資料庫連線 (Schema, username, password, encoding...)
		SQLiteDatabase db = SQLiteDatabase.openDatabase(dbFilePath, null, SQLiteDatabase.OPEN_READWRITE);
		// 2. 查詢帳號、密碼
		String sql = String.format("select * from account "
						+ " where name = '%s' and pw = '%s'"
				, acc, pw);
		Cursor cursor = db.rawQuery(sql, null);
		// 3. 確認結果
		boolean result = false;
		if (cursor.getCount() >= 1) {
			cursor.moveToFirst();
			sid = cursor.getInt(0);  // field index: 0, 1, 2, ...
			result = true;
		}
		// 4. 關閉必要連線、釋放資源.
		cursor.close();
		db.close();

		// 5. 回傳
		return result;
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
