package tw.idv.jameschen.recyclerviewdemo;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by user on 2016/12/27.
 */
public class MyRcViewAdapter extends RecyclerView.Adapter<MyRcViewAdapter.MyViewHolder> {

    private final Activity activity;
    private final ArrayList<HashMap<String, String>> data;

    //加入建構子
    public MyRcViewAdapter(Activity activity, ArrayList<HashMap<String, String>> data) {
        //記住變數數值
        this.activity = activity;
        this.data = data;
    }

    @Override
    //可依 viewType 不同，吹出不同的佈局檔
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //吹出一個View
        View view = activity.getLayoutInflater().inflate(R.layout.myitem2, null, false);
        MyViewHolder holder = new MyViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        //更新佈局
        HashMap<String, String> item = data.get(position);
        holder.tvTitle.setText( item.get("title") );
        holder.tvDetail.setText( item.get("detail") );
        //
        //可用 Tag 記住它是第幾筆資料
        holder.tvTitle.setTag(position); //原需為物件，需用 new Integer(position)
    }

    @Override
    public int getItemCount() {
        //assert
        if(data == null)
            return 0;
        else
            return data.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView imv;
        TextView tvTitle, tvDetail;
        //
        public MyViewHolder(View itemView) {
            super(itemView);
            //
            imv = (ImageView) itemView.findViewById(R.id.imageView1);
            tvTitle = (TextView) itemView.findViewById(R.id.tvTitle);
            tvDetail = (TextView) itemView.findViewById(R.id.tvDetail);
            // Listener建議放於此...
            //...


        }
    }
}
