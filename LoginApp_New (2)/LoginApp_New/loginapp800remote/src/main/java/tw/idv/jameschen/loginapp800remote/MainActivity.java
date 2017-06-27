package tw.idv.jameschen.loginapp800remote;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity implements OnClickListener {
	Button btnGetTodoList, btnShowTodoList, btnExit,btnLogout ;
	String acc;
	int    sid;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		//
		Bundle data = getIntent().getExtras();
		acc = data.getString("account");
		sid = data.getInt("sid");
		TextView title = (TextView) findViewById(R.id.textView1);
		String pre = getResources().getString(R.string.welcome);
		//title.setText("歡迎" + acc);
		title.setText( pre + " " + acc);
		//

		//
		btnGetTodoList = (Button)findViewById(R.id.btnGetTodoList);
		btnShowTodoList = (Button)findViewById(R.id.btnShowTodoList);
		btnLogout = (Button)findViewById(R.id.btnLogout);
		btnExit = (Button)findViewById(R.id.btnExit);
		//
		btnGetTodoList.setOnClickListener( this);
		btnShowTodoList.setOnClickListener( this);
		btnLogout.setOnClickListener( this);
		btnExit.setOnClickListener( this);
	}

	public void onClick(View v) {
		Intent it = new Intent();
		Bundle data = new Bundle();
		//
		switch( v.getId() ) {
			case R.id.btnGetTodoList:
				//

				break;
			case R.id.btnShowTodoList:
				//

				break;
			case R.id.btnLogout:
				//
				data.putInt("code", 1);
				it.putExtras(data);
				setResult( 1, it);
				finish();
				break;
			case R.id.btnExit:
				//
				data.putInt("code", 2);
				it.putExtras(data);
				setResult( 2, it);
				finish();
				break;
		}
	}
}

