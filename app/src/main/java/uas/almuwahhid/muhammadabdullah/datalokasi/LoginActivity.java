package uas.almuwahhid.muhammadabdullah.datalokasi;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import uas.almuwahhid.muhammadabdullah.datalokasi.controller.JSONParser;
import uas.almuwahhid.muhammadabdullah.datalokasi.controller.SessionManager;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity{

    EditText nim;
    Button btn;
    private Autentifikasi request;
    private static ProgressBar progressBar;
    SessionManager session;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //mLoginFormView = findViewById(R.id.email_login_form);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        nim = (EditText)findViewById(R.id.input_text);
        btn = (Button)findViewById(R.id.btn_sign);
        session = new SessionManager(getApplicationContext());

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(nim.getText().toString().isEmpty()){
                    Toast.makeText(LoginActivity.this, "Isi NIM terlebih dahulu",
                            Toast.LENGTH_LONG).show();
                }else{
                    request = new Autentifikasi();
                    progressBar.setVisibility(View.VISIBLE);
                    new Thread(new Runnable() {
                        public void run() {
                            try {
                                request.execute(nim.getText().toString());
                            } catch (CancellationException e) {
                                Log.d("request ", "run: "+e);
                                request.cancel(true);
                            }
                        }
                    }).start();
                }
            }
        });
    }

    class Autentifikasi extends AsyncTask<String, String, JSONObject> {
        public JSONParser jsonParser = new JSONParser();
        private static final String LOGIN_URL = "http://datalokasi.esy.es/autentifikasi.php";
        //private static final String LOGIN_URL = "http://hmjti.akakom.ac.id/datalokasi_web/autentifikasi.php";
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
                        LOGIN_URL, "POST", params);
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
            /**if (pDialog != null && pDialog.isShowing()) {
                pDialog.dismiss();
            }**/
            if (json != null) {
                try {
                    success = json.getInt(TAG_SUCCESS);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            if (success == 1) {
                progressBar.setVisibility(View.GONE);
                session.createLoginSession("135410025");
                Intent i = new Intent(getApplicationContext(),
                        MainActivity.class);
                i.putExtra("nomhs", success);
                startActivity(i);
                finish();
            }else{
                Toast.makeText(LoginActivity.this, "Login Gagal, Koneksi Bermasalah",
                        Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);
            }
            Log.d("req", "onPostExecute: "+success);
        }
    }
}


