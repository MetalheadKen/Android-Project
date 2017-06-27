package tw.idv.jameschen.loginapp702;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class EnrollmentRcViewAdapter extends RecyclerView.Adapter<EnrollmentRcViewAdapter.MyViewHolder>{
	//
	private Context context;
	private Cursor mCursor;
	private LayoutInflater mLayoutInflater;
	//
	public EnrollmentRcViewAdapter( Context context, LayoutInflater inflator, Cursor cursor) {
		this.context = context;
		this.mCursor = cursor;
		this.mLayoutInflater = inflator;
	}
    
	@Override
	public int getItemCount() {
		return (mCursor==null || mCursor.isClosed() ? 0 : mCursor.getCount());
	}

	@Override
	public void onBindViewHolder(MyViewHolder holder, int position) {
		//
    	if (mCursor == null || mCursor.isClosed()) return;	
    	//
    	mCursor.moveToPosition(position);
    	String cname = mCursor.getString(mCursor.getColumnIndex("cname"));
    	String cdesc = mCursor.getString(mCursor.getColumnIndex("c_desc"));
    	//    	
        holder.tvCourseName.setText(cname);
        holder.tvCourseDesc.setText(cdesc);

	}

	@Override
	public MyViewHolder onCreateViewHolder(	ViewGroup parent, int viewType) {
		// TODO Auto-generated method stub
		return new MyViewHolder(mLayoutInflater.inflate(R.layout.course_item, parent, false));
	}

	//
	public class MyViewHolder extends ViewHolder {
		//
		private TextView tvCourseName, tvCourseDesc;
		//
		public MyViewHolder(View itemView) {
			super(itemView);
			//
			tvCourseName = (TextView) itemView.findViewById(R.id.tvCouseName);
			tvCourseDesc = (TextView) itemView.findViewById(R.id.tvCourseDesc);
			// 
			// setOnClickListener...
		}
	}
	
	// EXTRA -- for change Cursor
    public void changeCursor(Cursor newCursor) {
    	mCursor = newCursor;
    	if (mCursor != null && mCursor.getCount()>0) 
    		mCursor.moveToFirst();
    	//
    	this.notifyDataSetChanged();
    }

}
