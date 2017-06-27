package tw.edu.ncut.csie.qr_code;

import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

//http://tomkuo139.blogspot.tw/2016/06/android-navigation-drawer-sliding-menu.html
public class MaturityActivity extends AppCompatActivity {
    /** 側邊欄 Layout */
    private DrawerLayout slideMenuLayout;
    /** 側邊欄 ListView */
    private ExpandableListView slideMenuList;
    private WebView wv_menu;
    private List<String> groupData;
    private List<List<String>> childrenData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maturity);

        wv_menu = (WebView) findViewById(R.id.wv_menu);
        slideMenuLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        slideMenuList = (ExpandableListView)findViewById(R.id.left_drawer);

        // Sidebar 寬度
        DisplayMetrics dm = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int gScreenWidth  = dm.widthPixels;
        slideMenuList.getLayoutParams().width = (int)(gScreenWidth*0.3f);

        // Sidebar 背景
        slideMenuList.setBackgroundResource(R.drawable.slideview_background);
        // 設定側邊欄的資料來源

        initDrawerList();
        ExpandableListView expandableListView = (ExpandableListView)findViewById(R.id.left_drawer);
        expandableListView.setAdapter(new ExpandableAdapter());
        expandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View clickedView, int groupPosition, long groupId) {
                return false;//返回true表示此事件在此被處理了
            }
        });
        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandablelistview, View clickedView, int groupPosition, int childPosition, long childId) {
                Log.e("XXXX","XXX");
                return false;//返回true表示此事件在此被處理了
            }
        });
        // group合攏監聽
        expandableListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
            @Override
            public void onGroupCollapse(int groupPosition) {
            }
        });
        expandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 這是 <Menu> KeyCode
        if( keyCode == 82 ) {
            // 若有開啟 SlideMenu, 則關閉之
            if( slideMenuLayout.isDrawerOpen( slideMenuList ) ) {
                slideMenuLayout.closeDrawer( slideMenuList );
            }
            // 若沒開啟 SlideMenu, 則開啟之
            else {
                slideMenuLayout.openDrawer( slideMenuList );
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        // 若有開啟 SlideMenu, 則關閉之
        if( slideMenuLayout.isDrawerOpen( slideMenuList ) ) {
            slideMenuLayout.closeDrawer( slideMenuList );
            return;
        }
        super.onBackPressed();
    }
    private void initDrawerList(){
        groupData = new ArrayList<String>();
        groupData.add("【水果類 Fruit】");
        groupData.add("【蔬菜類 Vegetables】");
        groupData.add("【肉類 Meat】");
        groupData.add("【海鮮類 Seafood】");

        childrenData = new ArrayList<List<String>>();
        List<String> fruit = new ArrayList<String>();
        fruit.add("　  火龍果");
        fruit.add("　  芒果");
        fruit.add("　  奇異果");
        childrenData.add(fruit);

        List<String> vegetables = new ArrayList<String>();
        vegetables.add("　  高麗菜");
        vegetables.add("　  絲瓜");
        vegetables.add("　  白蘿蔔");
        childrenData.add(vegetables);

        List<String> meat = new ArrayList<String>();
        meat.add("　  豬肉挑選");
        meat.add("　  牛肉挑選");
        meat.add("　  羊肉挑選");
        childrenData.add(meat);

        List<String> seafoot = new ArrayList<String>();
        seafoot.add("　  魚");
        seafoot.add("　  蝦");
        seafoot.add("　  貝");
        childrenData.add(seafoot);

    }
    private class ExpandableAdapter extends BaseExpandableListAdapter {
        @Override
        public Object getChild(int groupPosition, int childPosition) {
            Log.e("getChild","hello i get");
            return childrenData.get(groupPosition).get(childPosition);
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return 0;
        }

        @Override
        public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            TextView text = null;
            if (convertView != null) {
                text = (TextView)convertView;
                text.setText(childrenData.get(groupPosition).get(childPosition));
            } else {
                text = createView(childrenData.get(groupPosition).get(childPosition));
            }

            text.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (groupPosition){
                        case 0:
                            switch (childPosition){
                                case 0:
                                    wv_menu.loadUrl("http://192.168.200.11/fruit.php?fruit=火龍果&ref=http://life.ettoday.net/article/454162.htm");
                                    break;
                                case 1:
                                    wv_menu.loadUrl("http://192.168.200.11/fruit.php?fruit=芒果&ref=http://life.ettoday.net/article/454162.htm");
                            }
                    }

                }
            });

            return text;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return childrenData.get(groupPosition).size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return groupData.get(groupPosition);
        }

        @Override
        public int getGroupCount() {
            return groupData.size();
        }

        @Override
        public long getGroupId(int groupPosition) {
            return 0;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            TextView text = null;
            if (convertView != null) {
                text = (TextView)convertView;
                text.setText(groupData.get(groupPosition));
            } else {
                text = createView(groupData.get(groupPosition));
            }
            return text;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
        }

        private TextView createView(String content) {
            AbsListView.LayoutParams layoutParams = new AbsListView.LayoutParams(
                    ViewGroup.LayoutParams.FILL_PARENT, 100);
            TextView text = new TextView(MaturityActivity.this);
            text.setLayoutParams(layoutParams);
            text.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
            text.setPadding(100, 0, 0, 0);
            text.setText(content);
            return text;
        }

    }
}
