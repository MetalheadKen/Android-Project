package tw.idv.jameschen.listviewdemo1;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.widget.ListView;

public class MainActivity extends Activity  {

	ListView lv;
	// 1. Data Source
	String[] news = {"N1", "N2", "N3", "N4",  "N5",  "N6",
			"N7", "N8", "N9", "N10", "N11", "N12"};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// 1. 生出資料清單

		// 2+3. Adapter -- ArrayAdapter

		// 3.

		// 4.

		// 5.

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}


}
