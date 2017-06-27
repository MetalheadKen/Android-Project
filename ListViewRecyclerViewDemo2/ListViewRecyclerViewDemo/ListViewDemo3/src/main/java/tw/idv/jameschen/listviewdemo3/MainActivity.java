package tw.idv.jameschen.listviewdemo3;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends Activity implements OnItemClickListener {

	ListView lv;
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

		// 1-1 將資料組裝為 List --
		List<Map<String, String>> list = new ArrayList<Map<String,String>>();
		for (int i=0; i<news.length; i++) {
			// Map: 產生單項的內容!!
			Map<String, String> item = new HashMap<String, String>();
			// (A) 置入資料

			// 將 項目(item) 加入容器 list
			list.add(item);
		}

		// 2+3. Adapter -- MyAdapter
		// (B) 產生 MyAdapter

		//
		lv = (ListView) findViewById(R.id.listView1);

		// 4.
		//lv.setAdapter(adapter);

		// 5.
		lv.setOnItemClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
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
