package tw.idv.jameschen.bluetoothlab04BLE;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class BleUuidsActivity extends Activity implements OnItemClickListener {
	ArrayList<String> uuids;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_uuid);
		//
		uuids = getIntent().getStringArrayListExtra("uuids");
		//
		ListView lv = (ListView) findViewById(R.id.lvUuid);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, uuids);
		lv.setAdapter(adapter);
		//
		lv.setOnItemClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		String uuid = uuids.get(position);
		Intent it = new Intent( BleUuidsActivity.this, BleCharacteristicUuidsActivity.class);
		it.putStringArrayListExtra("char_uuids", getIntent().getStringArrayListExtra(uuid));
		startActivity(it);
	}
}
