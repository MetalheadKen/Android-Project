package tw.edu.ncut.csie.qr_code;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import static tw.edu.ncut.csie.qr_code.visitflag.user_email;

public class BargainActivity extends AppCompatActivity {
    private String get_onsale_url="http://192.168.200.126/cheap.php";
    private String result;
    private ListView lv_onsale;
    private ArrayList<String> onsale_list;
    private ArrayAdapter<String> listAdapter ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bargain);
        lv_onsale = (ListView) findViewById(R.id.lv_onsale);

        onsale_list = new ArrayList<String>();
        listAdapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,onsale_list);
        lv_onsale.setAdapter(listAdapter);

        new Thread(onsalerunnable).start();
    }
    Runnable onsalerunnable = new Runnable() {
        @Override
        public void run() {
            try {
                onsale_list.clear();

                HttpClient client = new DefaultHttpClient();
                HttpPost post = new HttpPost(get_onsale_url);

                JSONObject json = new JSONObject();
                post.setEntity(new ByteArrayEntity(json.toString().getBytes("UTF8")));
                post.setHeader("json",json.toString());

                Log.e("JSONDATA", String.valueOf(json));

                HttpResponse response = client.execute(post);
                HttpEntity entity = response.getEntity();
                result = EntityUtils.toString(entity);

                Log.e("get_onsale", String.valueOf(response));
                Log.e("get_onsale_entity", String.valueOf(entity));
                Log.e("result",result);

                JSONArray onsale_JsonData = null;
                try {
                    onsale_JsonData = new JSONArray(result);
                    if (onsale_JsonData != null) {
                        for (int i=0;i<onsale_JsonData.length();i++){
                            onsale_list.add(onsale_JsonData.get(i).toString());
                            Log.e("record_list", onsale_JsonData.get(i).toString());
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                listAdapter.notifyDataSetChanged();
                //post.setHeader("json",json.toString());
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };
}
