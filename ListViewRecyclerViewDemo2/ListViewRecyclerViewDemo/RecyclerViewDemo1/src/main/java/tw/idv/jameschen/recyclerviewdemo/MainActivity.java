package tw.idv.jameschen.recyclerviewdemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends Activity implements OnItemClickListener {
	// 0.
	// ListView lv;
	RecyclerView rcView;
	// 1. Data Source
	String[] news = {"N1",
			"N2",
			"N3",
			"N4",
			"N5",
			"N6",
			"N7",
			"N8",
			"N9",
			"N10",
			"N11",
			"N12"};
	String[] detail = {"N1111111111111",
			"N2222222222222",
			"N3333333333333",
			"N4444444444444",
			"N5555555555555",
			"N6666666666666",
			"N7777777777777",
			"N8888888888888",
			"N9999999999999",
			"Naaaaaaaaaaaaa",
			"Nbbbbbbbbbbbbb",
			"Nccccccccccccc"};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// 1 將資料組裝為 List --
		ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String,String>>();
		for (int i=0; i<news.length; i++) {
			// Map: 產生單項的內容!!
			HashMap<String, String> item = new HashMap<String, String>();
			// (A) 置入資料
			item.put("title", news[i]);
			item.put("detail", detail[i]);
			// 將 項目(item) 加入容器 list
			list.add(item);
		}

		// 2 Adapter -- RecyclerView
		// (B) 產生 NewsRcViewAdapter
		NewsRcViewAdapter adapter = new NewsRcViewAdapter( this, getLayoutInflater(), list);

		// 3. 找到 RecyclerView, 設定 LayoutManager
		// lv = (ListView) findViewById(R.id.listView1);
		rcView = (RecyclerView) findViewById(R.id.rvItemList);
		LinearLayoutManager layoutManager1 = new LinearLayoutManager( this, LinearLayoutManager.VERTICAL, false);
		rcView.setLayoutManager(layoutManager1);
		// 4. 關聯 RecyclerView + Adapter
		//lv.setAdapter(adapter);
		rcView.setAdapter(adapter);
		// 5. 事件處理 -- 無

	}


	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
							long id) {
		// TODO Auto-generated method stub
		//
		new AlertDialog.Builder(this)
				.setTitle("選擇是..")
				.setMessage( "ch" + news[position])
				.setNeutralButton("OK", null)
				.show();
	}

}
