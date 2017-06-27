package tw.idv.jameschen.loginapp702;

import android.app.Fragment;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class DetailFragment extends Fragment {
	EnrollmentRcViewAdapter adapter;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.detail, null);
		RecyclerView rvDetail = (RecyclerView) view.findViewById(R.id.rvItemList);
		GridLayoutManager layout1 = new GridLayoutManager( 
				                   getActivity(), 
				                   1,
				                   GridLayoutManager.VERTICAL, 
				                   false);
		adapter = new EnrollmentRcViewAdapter( 
				                   getActivity(), inflater, null);
		rvDetail.setLayoutManager(layout1);
		rvDetail.setAdapter(adapter);
		//
		return view;
	}
	
	public void switchEnrollmentBySid(int sid) {
		Cursor cursor = ((MainActivity)getActivity()).getEntrollmentBySid(sid);
		adapter.changeCursor(cursor);
	}

}
