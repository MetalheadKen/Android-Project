package tw.idv.jameschen.loginapp702;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;

public class MasterFragment extends Fragment implements OnItemClickListener {
	ListView lvAccount;
	private ArrayList<HashMap<String, String>> accounts;
   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
		Bundle savedInstanceState) {

	    View view = inflater.inflate( R.layout.master, null);
	    lvAccount = (ListView) view.findViewById(R.id.lvAccount);
	    lvAccount.setOnItemClickListener(this);
	    //
	    return view;
   }

   @Override
   public void onActivityCreated(Bundle savedInstanceState) {
	   // TODO Auto-generated method stub
	   super.onActivityCreated(savedInstanceState);
	   //
	   accounts = ((MainActivity)getActivity()).getAccountInfo();
	   ArrayList<String> nameList = new ArrayList<String>();
	   for (int i=0; i<accounts.size(); i++) {
		   HashMap<String, String> item = accounts.get(i);
		   nameList.add( item.get("name"));
	   }
	   //
	   ArrayAdapter<String> adapter = new ArrayAdapter<String>( 
			              getActivity(), 
			              android.R.layout.simple_list_item_1, 
			              nameList);
	   //
	   lvAccount.setAdapter(adapter);
   }
   
   public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
	   HashMap<String, String> item = accounts.get(position);
	   int sid = Integer.parseInt( item.get("sid") );
	   //
	   ((MainActivity)getActivity()).switchEnrollmentBySid(sid);
   }
}
