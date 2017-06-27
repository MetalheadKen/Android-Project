package tw.idv.jameschen.loginapp801json;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class TodoListActivity extends Activity {
	String dbFilePath = "";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.todolist);
		// ** 取得參數 ( 資料庫檔案路徑、學號 )
		dbFilePath = getIntent().getStringExtra("DBFILE");
		int sid = getIntent().getIntExtra("SID", -1);
		// (0) 連結 ListView
		ListView lvTodoList = (ListView) findViewById(R.id.lvTodoList);
		// (1) 建立資料庫物件
		SQLiteDatabase db = SQLiteDatabase.openDatabase(dbFilePath, null, 0);
		// (2) SQL statement
		String sql = "select tid as _id, * from todolist where sid=" + sid;
		Cursor cursor = db.rawQuery(sql, null);
		// (3) 利用 SimpleCursorAdapter 串接資料
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(
				this, R.layout.todo_item, cursor,
				new String[]{"todo", "due"},
				new int[] {R.id.tvTodo, R.id.tvDue} );
		// (4) 建立 ListView & Adapter 的關係
		lvTodoList.setAdapter(adapter);

		// (5) 事件處理  if need !
	}
}
