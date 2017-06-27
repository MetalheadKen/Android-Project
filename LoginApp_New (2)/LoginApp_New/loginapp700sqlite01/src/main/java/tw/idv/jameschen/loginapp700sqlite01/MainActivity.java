package tw.idv.jameschen.loginapp700sqlite01;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {
	Button btnQryEnroll, btnGoWeb, btnExit,btnLogout ;
	String acc;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		//
		Bundle data = getIntent().getExtras();
		acc = data.getString("account");
		int    age = data.getInt("age");
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
				Toast.makeText(this, "Not yet implemented!", Toast.LENGTH_LONG).show();
				break;
			case R.id.btnGoWeb:
				//
				Uri web = Uri.parse("http://www.edu.tw");
				Intent it2 = new Intent(Intent.ACTION_VIEW, web);
				startActivity(it2);
				break;
			case R.id.btnLogout:
				//
				Intent it3 = new Intent();
				Bundle data = new Bundle();
				data.putInt("code", 1);
				it3.putExtras(data);
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

