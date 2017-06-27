package tw.idv.jameschen.loginapp701sqlite02;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity implements OnClickListener {
	Button btnQryEnroll, btnGoWeb, btnExit,btnLogout ;
	String acc;
	int    sid;
	String dbFilePath="";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		//
		Bundle data = getIntent().getExtras();
		acc = data.getString("account");
		sid = data.getInt("SID");
		//
		dbFilePath = data.getString("DBFILE");
		//
		TextView title = (TextView) findViewById(R.id.textView1);
		String pre = getResources().getString(R.string.welcome);
		//title.setText("歡迎" + acc);
		title.setText( pre + " " + acc);
		//

		//
		btnQryEnroll = (Button)findViewById(R.id.btnQryEnroll);
		btnGoWeb = (Button)findViewById(R.id.btnGoWeb);
		btnLogout = (Button)findViewById(R.id.btnLogout);
		btnExit = (Button)findViewById(R.id.btnExit);
		//
		btnQryEnroll.setOnClickListener( this);
		btnGoWeb.setOnClickListener( this);
		btnLogout.setOnClickListener( this);
		btnExit.setOnClickListener( this);
	}

	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch( v.getId() ) {
			case R.id.btnQryEnroll:
				// Lab02
				Intent it = new Intent(getApplicationContext(),
						Enrollment.class);
				//
				Bundle data = new Bundle();
				data.putString("account", acc);
				data.putInt("SID", sid);
				data.putString("DBFILE", dbFilePath);
				it.putExtras(data);
				//
				startActivity(it);
				break;
			case R.id.btnGoWeb:
				//
				Uri web = Uri.parse("http://www.ncut.edu.tw");
				Intent it2 = new Intent(Intent.ACTION_VIEW, web);
				startActivity(it2);
				break;
			case R.id.btnLogout:
				//
				Intent it3 = new Intent();
				Bundle bundle = new Bundle();
				bundle.putInt("code", 1);
				it3.putExtras(bundle);
				//
				setResult( 1, it3);

				finish();
				break;
			case R.id.btnExit:
				//
				Intent it4 = new Intent();
				Bundle data2 = new Bundle();
				data2.putInt("code", 2);
				it4.putExtras(data2);
				//
				setResult( 2, it4);
				//
				finish();
				break;
		}
	}
}

