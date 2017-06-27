package tw.idv.jameschen.loginapp600sp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity implements OnClickListener {
	private Button btnDial, btnGoWeb, btnExit;
	private MediaPlayer mp;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(tw.idv.jameschen.loginapp600sp.R.layout.main);
		//
		btnDial = (Button)findViewById(tw.idv.jameschen.loginapp600sp.R.id.btnDial);
		btnGoWeb = (Button)findViewById(tw.idv.jameschen.loginapp600sp.R.id.btnGoWeb);
		btnExit = (Button)findViewById(tw.idv.jameschen.loginapp600sp.R.id.btnExit);
		//
		btnDial.setOnClickListener(this);
		btnGoWeb.setOnClickListener(this);
		btnExit.setOnClickListener(this);
		//
		mp = MediaPlayer.create( this, tw.idv.jameschen.loginapp600sp.R.raw.dingdong);
	}
	//
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
			case tw.idv.jameschen.loginapp600sp.R.id.btnDial:
				Uri phone = Uri.parse("tel://04-123456");
				Intent it1 = new Intent(Intent.ACTION_CALL, phone);
				startActivity(it1);
				break;
			case tw.idv.jameschen.loginapp600sp.R.id.btnGoWeb:
				Uri web = Uri.parse("http://www.ctas.tc.edu.tw");
				Intent it2 = new Intent(Intent.ACTION_VIEW, web);
				startActivity(it2);
				break;
			case tw.idv.jameschen.loginapp600sp.R.id.btnExit:
				finish();
				break;
		}
	}

	// 新增MP3播放控制
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		//
		mp.pause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		//
		mp.start();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		//
		mp.stop();
		mp.release();
	}

	// 加入選單控制
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inf;
		inf = getMenuInflater();
		inf.inflate(tw.idv.jameschen.loginapp600sp.R.menu.main_menu, menu);
		//
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
			case tw.idv.jameschen.loginapp600sp.R.id.miAuthor:
				new AlertDialog.Builder(this)
						.setTitle("作者資訊")
						.setMessage("作者：James Chen")
						.setNeutralButton( "OK", null)
						.show();
				break;
			case tw.idv.jameschen.loginapp600sp.R.id.miVersion:
				new AlertDialog.Builder(this)
						.setTitle("版本資訊")
						.setMessage("Version: 1.0")
						.setNeutralButton("OK", null)
						.show();
				break;
		}
		//
		return super.onOptionsItemSelected(item);
	}

}