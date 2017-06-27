package tw.idv.jameschen.loginapp702;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		//
		// (0) for testing ONLY !!
		copySqliteDB2PrivatePath("loginapp.sqlite", true);
	}

	protected void onDestroy() {
		super.onDestroy();
		// 釋放資源
		releaseDB();
	};

	//
	String dbFilePath = "";
	private boolean copySqliteDB2PrivatePath(String dbName, boolean bOverwrite) {
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

	public void switchEnrollmentBySid(int sid) {
		// (*) 利用學號切換選課清單
		DetailFragment fragDetail = (DetailFragment) getFragmentManager().findFragmentById(R.id.fragmentDetail);
		fragDetail.switchEnrollmentBySid(sid);
	}

	// ===================================
	private SQLiteDatabase db;
	//
	public void initialDB() {
		// 1. 開啟資料庫連線 (Schema, username, password, encoding...)
		db = SQLiteDatabase.openDatabase(dbFilePath, null, SQLiteDatabase.OPEN_READWRITE);
	}
	public void releaseDB() {
		if (db != null) {
			db.close();
			db = null;
		}
	}
	//
	protected ArrayList<HashMap<String, String>> getAccountInfo() {
		// 1. 開啟資料庫連線 (Schema, username, password, encoding...)
		if (db == null)
			initialDB();

		// 2. 取得帳號資訊
		String sql = String.format("select id, name from account ");
		Cursor cursor = db.rawQuery(sql, null);

		// 3. 儲存結果
		ArrayList<HashMap<String,String>> results = new ArrayList<HashMap<String,String>>();
		if (cursor == null) return null;
		//
		cursor.moveToFirst();
		for (int i=0; i<cursor.getCount(); i++) {
			HashMap<String,String> item = new HashMap<String, String>();
			item.put("sid", cursor.getInt(0)+"");
			item.put("name", cursor.getString(1));
			//
			results.add(item);
			//
			cursor.moveToNext();
		}

		// 4. 關閉、釋放資源.
		cursor.close();

		// 5. 回傳
		return results;
	}

	//
	protected Cursor getEntrollmentBySid(int sid) {
		// 1. 開啟資料庫連線 (Schema, username, password, encoding...)
		if (db == null)
			initialDB();

		// 2. 取得選課資訊
		String sql = String.format(
				"select course.cid as _id, course.* from course, enroll, account "
						+ "where enroll.sid = account.id "
						+ "and enroll.cid = course.cid "
						+ "and account.id = %d", sid);
		Cursor cursor = db.rawQuery(sql, null);
		//
		return cursor;
	}
}

