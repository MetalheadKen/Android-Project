package tw.idv.jameschen.loginapp801json;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {
	Button btnGetTodoList, btnShowTodoList, btnExit,btnLogout ;
	String acc;
	int sid = -1;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		//
		Bundle data = getIntent().getExtras();
		acc = data.getString("account");
		sid = data.getInt("sid");
		//
		TextView title = (TextView) findViewById(R.id.textView1);
		String pre = getResources().getString(R.string.welcome);
		//title.setText("歡迎" + acc);
		title.setText( pre + " " + acc);
		//

		//
		btnGetTodoList = (Button)findViewById(R.id.btnGetTodoList);
		btnShowTodoList = (Button)findViewById(R.id.btnShowTodoList);
		btnLogout = (Button)findViewById(R.id.btnLogout);
		btnExit = (Button)findViewById(R.id.btnExit);
		//
		btnGetTodoList.setOnClickListener( this);
		btnShowTodoList.setOnClickListener( this);
		btnLogout.setOnClickListener( this);
		btnExit.setOnClickListener( this);
		//
		initialDB();
		//
	}
	//
	protected void onDestroy() {
		super.onDestroy();
		//
		releaseDB();
	};
	// ===================================
	private SQLiteDatabase db;
	//
	public void initialDB() {
		// (0) for testing ONLY !!
		copySqliteDB2PrivatePath("loginapp.sqlite", false); // false: 不覆寫!!

		// 1. 開啟資料庫連線 (Schema, username, password, encoding...)
		db = SQLiteDatabase.openDatabase(dbFilePath, null, SQLiteDatabase.OPEN_READWRITE);
	}
	//
	public void releaseDB() {
		if (db != null) {
			db.close();
			db = null;
		}
	}
	//
	public void onClick(View v) {
		Intent it;
		Bundle data = new Bundle();
		switch( v.getId() ) {
			case R.id.btnGetTodoList:
				// Lab 801 - 3. 執行非同步下載程序
				GetTodoAsyncTask myTask = new GetTodoAsyncTask();
				myTask.execute();
				//
				break;
			case R.id.btnShowTodoList:
				// Lab 801 - 4. 顯示待辦事項清單畫面
				it = new Intent(this, TodoListActivity.class);
				data.putString("DBFILE", dbFilePath);
				data.putInt("SID", sid);
				it.putExtras(data);
				//
				startActivity(it);
				break;
			case R.id.btnLogout:
				//
				it = new Intent();
				data.putInt("code", 1);
				it.putExtras(data);
				setResult( 1, it);
				finish();
				break;
			case R.id.btnExit:
				//
				it = new Intent();
				data.putInt("code", 2);
				it.putExtras(data);
				setResult( 2, it);
				finish();
				break;
		}
	}
	//
	// Lab801 - 定義 GetTodoAsyncTask
	class GetTodoAsyncTask extends AsyncTask<String, Integer, Boolean> {
		//
		String resultMsg="";
		//
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			// Log.i("","");
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			// Lab 801 -- 3. 顯示結果
			String title = "";
			if (result == true) {
				title = "下載 Todo成功";
			}
			else {
				title = "下載 Todo失敗";
			}
			// 顯示結果 !!
			new AlertDialog.Builder(MainActivity.this)
					.setTitle(title)
					.setMessage(resultMsg)
					.setNeutralButton("OK", null)
					.show();
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
		}

		@Override
		protected void onCancelled() {
			// TODO Auto-generated method stub
			super.onCancelled();
		}

		@Override
		protected Boolean doInBackground(String... params) {
			// Lab801 - 1. 利用 HTTP 下載 JSON 資料
			String url = "http://163.17.83.53/demo01/getTodoList.php?sid="+sid;
			String jsonString = downloadData(url);
			if (jsonString == null || jsonString.length() == 0 || jsonString.contains("error")) {
				// 下載失敗
				resultMsg = "todolist: Sync/Download Error; result:"+jsonString;
				//pDialog.setMessage(downloadMsg+downloadLog);
				Log.e("KSI_mPOS", resultMsg);
				//
				return false;
			}
			// Lab801 - 2. 成功後，就下載後的 JSON 資料，新增到資料表內!
			int numOfRecords = insertData( jsonString,  "todolist" );
			resultMsg = "todolist: Sync/Download OK, #(records):"+numOfRecords;
			//pDialog.setMessage(downloadMsg+downloadLog);
			Log.i("KSI_mPOS", resultMsg);
			return true;
		}
		//
	}

	// Lab801 - (2-1). 利用  HttpClient 下載遠端資料
	public String downloadData(String url) {
		//
		HttpClient client = new DefaultHttpClient();
		try {
			HttpGet get = new HttpGet(url);
			HttpResponse response = client.execute(get);
			//
			if (response.getStatusLine().getStatusCode() == 200) {
				HttpEntity entity = response.getEntity();
				String result = EntityUtils.toString(entity);
				//
				Log.i("KSI_mPOS", result);
				//
				return result;
			}
			else {
				Log.e("KSI_mPOS", "HTTP Response Code: "+response.getStatusLine().getStatusCode());
				return null;
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//
		return null;
	}

	// Lab801 - (2-2). 將 JSON 資料新增到指定的資料表內
	// 功能：新增資料到指定的資料表
	// 要求： jsonString 是 JSON 格式 (JSONArray)。
	public int insertData(String jsonString, String tablename) {
		JSONArray newRecords;
		int numOfRecords = 0;
		try {
			newRecords = new JSONArray(jsonString);
			numOfRecords = newRecords.length();
			Log.i("KSI_mPOS", tablename + "; #(records): " + numOfRecords);
			//
			ContentValues cv = new ContentValues();
			JSONObject rec;
			int successfulCounter = 0;
			for (int i = 0; i < numOfRecords; i++) {
				rec = newRecords.getJSONObject(i);
				Iterator<String> itr = rec.keys();
				// Insert into Database
				cv.clear();
				for (int j = 0; j < rec.length(); j++) {
					String key = itr.next();
					cv.put(key, rec.getString(key));
				}
				try {
					long rowid = db.insertOrThrow(tablename, "", cv);
					if (rowid != -1) {
						successfulCounter++;
						Log.i("KSI_mPOS", tablename + ":inserted record ID: "
								+ rowid);
					} else {
						Log.e("KSI_mPOS", tablename
								+ ":Fail to insert record!!");
					}
				} catch (Exception ex) {
					Log.e("KSI_mPOS",
							tablename + ":Fail to insert record; Exception: "
									+ ex.getMessage());
				}
				//
			}
			return successfulCounter;
		} catch (JSONException e) {
			Log.e("KSI_mPOS", "Error: " + e.getMessage());
			e.printStackTrace();
			return 0;
		}
	}

	// -------------------------------
	String dbFilePath = "";
	private boolean copySqliteDB2PrivatePath(String dbName, boolean bOverwrite) {
		//
		try {
			// (A) /data/data/... 私有路徑
			//String PRIVATE_DB_PATH = "/data/data/"
			//		+ this.getApplicationContext().getPackageName()
			//		+ "/databases";
			// (B) For DEBUG
			String PRIVATE_DB_PATH = Environment.getExternalStorageDirectory()
					.getAbsolutePath() + "/mydb";
			//String PRIVATE_DB_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
			//		.getAbsolutePath() + "/../mydb";
			//
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

}

