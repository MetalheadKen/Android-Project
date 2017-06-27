package tw.idv.jameschen.recyclerviewdemo;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.AlertDialog;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

public class NewsRcViewAdapter extends RecyclerView.Adapter<NewsRcViewAdapter.MyViewHolder>{
	//
	private Context context;
	private ArrayList<HashMap<String,String>> news;
	private LayoutInflater mLayoutInflater;
	//
	public NewsRcViewAdapter( Context context, LayoutInflater inflator, ArrayList<HashMap<String,String>> news) {
		this.context = context;
		this.news = news;
		this.mLayoutInflater = inflator;
	}

	@Override
	public int getItemCount() {
		return (news==null) ? 0 : news.size();
	}

	@Override
	public void onBindViewHolder(MyViewHolder holder, int position) {
		//
		if (getItemCount() == 0 ) return;
		//
		HashMap<String,String> item = news.get(position);
		String title = item.get("title");
		String desc = item.get("detail");
		//
		holder.tvTitle.setText(title);
		holder.tvDetail.setText(desc);
		// (1) 附加資料 for 事件處理
		holder.tvTitle.setTag(position);
	}

	@Override
	public MyViewHolder onCreateViewHolder(	ViewGroup parent, int viewType) {
		// TODO Auto-generated method stub
		return new MyViewHolder(mLayoutInflater.inflate(R.layout.myitem2, parent, false));
	}

	//
	public class MyViewHolder extends ViewHolder {
		//
		private TextView tvTitle, tvDetail;
		//
		public MyViewHolder(View itemView) {
			super(itemView);
			//
			tvTitle = (TextView) itemView.findViewById(R.id.tvTitle);
			tvDetail = (TextView) itemView.findViewById(R.id.tvDetail);
			//
			// (2) Item 事件處理: setOnClickListener...
			itemView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// (2-1) 取得項目編號
					int pos = (Integer) tvTitle.getTag();
					// (2-2) 以對話框顯示結果
					new AlertDialog.Builder(context)
							.setTitle("OnClick(...)")
							.setMessage("click: "+pos)
							.setNeutralButton("OK", null)
							.show();
				}
			});
		}
	}

	public void changeNews(ArrayList<HashMap<String,String>> news) {
		this.news = news;
		this.notifyDataSetChanged();
	}

}
