package tw.edu.ncut.csie.qr_code;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import static tw.edu.ncut.csie.qr_code.visitflag.user_email;

public class recordActivity extends AppCompatActivity {
    private Spinner sp_year ,sp_month;
    private String record_url = "http://140.128.88.166:8080/month_list.php";
    private String year,month,record_result;
    //----
    private ListView lv_main;
    private ArrayList<String> record_list;
    private ArrayAdapter<String> listAdapter ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        sp_year = (Spinner) findViewById(R.id.sp_year);
        sp_month = (Spinner)findViewById(R.id.sp_month);
        lv_main = (ListView)findViewById(R.id.lv_main);
        record_list = new ArrayList<String>();
        listAdapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,record_list);
        lv_main.setAdapter(listAdapter);

        ArrayAdapter<CharSequence> yearList = ArrayAdapter.createFromResource(recordActivity.this,
                R.array.years,
                android.R.layout.simple_spinner_dropdown_item);
        sp_year.setAdapter(yearList);

        ArrayAdapter<CharSequence> monthList = ArrayAdapter.createFromResource(recordActivity.this,
                R.array.month,
                android.R.layout.simple_spinner_dropdown_item);
        sp_month.setAdapter(monthList);


        sp_year.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                year = sp_year.getSelectedItem().toString();
                Log.e("select year",year);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        sp_month.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String month_ch = sp_month.getSelectedItem().toString();
                Log.e("select month_ch",month_ch);

                month = String.valueOf(month_ch.charAt(0));
                Log.e("select month",month);

                new Thread(ym_runnable).start();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });




    }

    Runnable ym_runnable = new Runnable() {
        @Override
        public void run() {
            try {
                record_list.clear();

                HttpClient client = new DefaultHttpClient();
                HttpPost post = new HttpPost(record_url);

                List<NameValuePair> vars=new ArrayList< NameValuePair>();
                vars.add(new BasicNameValuePair("email",user_email));
                vars.add(new BasicNameValuePair("year",year));
                vars.add(new BasicNameValuePair("month",month));
                post.setEntity(new UrlEncodedFormEntity(vars, HTTP.UTF_8));
                Log.e("email & year & month", String.valueOf(vars));

                HttpResponse response = client.execute(post);
                final HttpEntity entity = response.getEntity();
                Log.e("entity", String.valueOf(entity));

                record_result = EntityUtils.toString(entity);
                //Log.e("record",record_result);


                JSONArray record_JSA = new JSONArray(record_result);
                if (record_JSA != null) {
                    int len = record_JSA.length();
                    for (int i=0;i<len;i++){
                        record_list.add(record_JSA.get(i).toString());
                        Log.e("record_list", record_JSA.get(i).toString());
                    }
                }

                Log.e("record_list", String.valueOf(record_list));

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //Toast.makeText(recordActivity.this, record_result,Toast.LENGTH_SHORT).show();
                        listAdapter.notifyDataSetChanged();
                    }
                });

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };


}
