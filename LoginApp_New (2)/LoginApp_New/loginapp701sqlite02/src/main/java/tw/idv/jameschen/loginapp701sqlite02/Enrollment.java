package tw.idv.jameschen.loginapp701sqlite02;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class Enrollment extends Activity {
	String dbFilePath = "";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.enrollment);
		// ** 取得參數 ( 資料庫檔案路徑、學號 )
		dbFilePath = getIntent().getStringExtra("DBFILE");
		int sid = getIntent().getIntExtra("SID", -1);

		// Lab701 - 2. 關聯 ListView 和 Adapter

		// (0) 連結 ListView
		ListView lv = (ListView) findViewById(R.id.listView1);

		// (1) 建立資料庫物件
		SQLiteDatabase db = SQLiteDatabase.openDatabase(dbFilePath, null, MODE_PRIVATE);

		// (2) SQL statement
		String sql = String.format(
				"select course.cid as _id, course.* from course, enroll, account "
						+ "where enroll.sid = account.id "
						+ "and enroll.cid = course.cid "
						+ "and account.id = %d", sid);
		Cursor cursor = db.rawQuery(sql, null);

		// (3) 利用 SimpleCursorAdapter 串接資料
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(
				this,
				R.layout.course,
				cursor,
				new String[]{ "cname", "c_desc" },
				new int[]   { R.id.tvCouseName, R.id.tvCourseDesc });

		// (4) 建立 ListView & Adapter 的關係
		lv.setAdapter(adapter);

		// (5) 事件處理  if need !
	}
}
