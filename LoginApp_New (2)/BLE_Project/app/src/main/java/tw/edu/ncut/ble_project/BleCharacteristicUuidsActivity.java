package tw.edu.ncut.ble_project;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class BleCharacteristicUuidsActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_characteristic_uuid);
		//
		ArrayList<String> uuids = getIntent().getStringArrayListExtra("char_uuids");
		//
		ListView lv = (ListView) findViewById(R.id.lvCharacteristicUuid);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, uuids);
		lv.setAdapter(adapter);
	}
}


