package uas.almuwahhid.muhammadabdullah.datalokasi;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CancellationException;

import uas.almuwahhid.muhammadabdullah.datalokasi.adapter.LokasiAdapter;
import uas.almuwahhid.muhammadabdullah.datalokasi.controller.JSONParser;
import uas.almuwahhid.muhammadabdullah.datalokasi.controller.SessionManager;
import uas.almuwahhid.muhammadabdullah.datalokasi.model.DataLokasi;

public class MainActivity extends AppCompatActivity {
    SessionManager session;
    private Ambildata request;
    private static ProgressBar progressBar;
    private ArrayList<DataLokasi> items;
    private LokasiAdapter adapter;
    String name;
    ListView lv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        progressBar = (ProgressBar) findViewById(R.id.progressBar_main);
        lv = (ListView) findViewById(R.id.listView);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        session = new SessionManager(getApplicationContext());
        session.checkLogin();

        request = new Ambildata();
        progressBar.setVisibility(View.VISIBLE);
        HashMap<String, String> user = session.getUserDetails();
        name = user.get(SessionManager.KEY_NAME);
        new Thread(new Runnable() {
            public void run() {
                try {
                    request.execute(name);
                } catch (CancellationException e) {
                    Log.d("request ", "run: "+e);
                    request.cancel(true);
                }
            }
        }).start();
        Log.d("request", "onCreate: "+name);



        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /**Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();**/
                Intent intent = new Intent(getApplicationContext(),
                        TambahData.class);
                startActivity(intent);
            }
        });
    }

    class Ambildata extends AsyncTask<String, String, JSONObject> {
        public JSONParser jsonParser = new JSONParser();
        private static final String LOGIN_URL = "http://datalokasi.esy.es/tampilData.php";
        //private static final String LOGIN_URL = "http://hmjti.akakom.ac.id/datalokasi_web/tampilData.php";
        private static final String TAG_SUCCESS = "sukses";
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected JSONObject doInBackground(String... args) {
            try {
                HashMap<String, String> params = new HashMap<>();
                params.put("nomhs", args[0]);
                Log.d("req", "doInBackground: "+params);
                JSONObject json = jsonParser.makeHttpRequest(
                        LOGIN_URL, "GET", params);
                Log.d("req", "doInBackground: "+json.getInt("sukses"));
                if (json != null) {
                    return json;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(JSONObject json) {
            int success=0;
            if (json != null) {
                try {
                    success = json.getInt(TAG_SUCCESS);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if (success == 1) {
                progressBar.setVisibility(View.GONE);
                try {
                    JSONArray datanya = json.getJSONArray("data");
                    if(datanya.length()==0){
                        Toast.makeText(MainActivity.this, "Tidak ada data",
                                Toast.LENGTH_LONG).show();
                    }
                    items = new ArrayList<>();
                    for (int i = 0; i < datanya.length(); i++) {
                        JSONObject object = datanya.getJSONObject(i);
                        DataLokasi dataLokasi = new DataLokasi(object.getInt("id"), object.getString("nama"), object.getString("lat"), object.getString("long"));
                        items.add(dataLokasi);
                    }
                    adapter = new LokasiAdapter(MainActivity.this, items);
                    lv.setAdapter(adapter);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }else{
                progressBar.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "Pengambilan data gagal",
                        Toast.LENGTH_LONG).show();
            }
            Log.d("req", "onPostExecute: "+success);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            request = new Ambildata();
            progressBar.setVisibility(View.VISIBLE);
            HashMap<String, String> user = session.getUserDetails();
            name = user.get(SessionManager.KEY_NAME);
            new Thread(new Runnable() {
                public void run() {
                    try {
                        request.execute(name);
                    } catch (CancellationException e) {
                        Log.d("request ", "run: "+e);
                        request.cancel(true);
                    }
                }
            }).start();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onPause(){
        Log.d("request", "onPause: "+name);
        super.onPause();
    }
    @Override
    public void onResume(){
        Log.d("request", "onResume: "+name);
        super.onResume();
    }
}
