package tw.idv.jameschen.listviewdemo;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

public class MainActivity extends Activity implements AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener {
    // (1)
	ListView lv;
	Spinner sp;
	//
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	    // (2)
		lv = (ListView) findViewById(R.id.listView1);
		sp = (Spinner) findViewById(R.id.spinner);
		//
		lv.setOnItemClickListener(this);
		sp.setOnItemSelectedListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Toast.makeText(this, "你選了:" + position, Toast.LENGTH_LONG).show();
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		String[] ff = getResources().getStringArray(R.array.fruits);
		Toast.makeText(this, "你選了:" + ff[position], Toast.LENGTH_LONG).show();
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {

	}
}
