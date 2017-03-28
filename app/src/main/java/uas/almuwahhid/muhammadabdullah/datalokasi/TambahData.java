package uas.almuwahhid.muhammadabdullah.datalokasi;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CancellationException;

import uas.almuwahhid.muhammadabdullah.datalokasi.adapter.LokasiAdapter;
import uas.almuwahhid.muhammadabdullah.datalokasi.controller.GPSTracker;
import uas.almuwahhid.muhammadabdullah.datalokasi.controller.JSONParser;
import uas.almuwahhid.muhammadabdullah.datalokasi.controller.SessionManager;
import uas.almuwahhid.muhammadabdullah.datalokasi.model.DataLokasi;

public class TambahData extends AppCompatActivity implements LocationListener {
    private static ProgressBar progressBar;
    TextView edit1, edit2, txtProgress;
    SessionManager session;
    private LocationManager locationManager;
    Location location;
    private String provider;
    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;
    ImageView gambarLokasi;
    FloatingActionButton fab;
    private static final int CAMERA_REQUEST = 1888;
    File finalFile;
    private Boolean status = false;
    String nomhs="", lat, longi, decodedImage, nama;
    EditText nama_txt;
    Bitmap bitmap;
    private simpanLokasi request;
    Button simpan;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tambah_data);
        edit1 = (TextView) findViewById(R.id.lat_edit);
        edit2 = (TextView) findViewById(R.id.long_edit);
        gambarLokasi = (ImageView)findViewById(R.id.gambarLokasi);
        fab = (FloatingActionButton) findViewById(R.id.foto_fab);
        progressBar = (ProgressBar) findViewById(R.id.progressBar_upload);
        txtProgress = (TextView) findViewById(R.id.txtPercentage);
        nama_txt = (EditText) findViewById(R.id.nama_edit);
        simpan = (Button) findViewById(R.id.button_tambah);


        initToolbar();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        session = new SessionManager(getApplicationContext());
        session.checkLogin();
        edit2.setText("0.00");
        edit1.setText("0.00");
        lat = edit1.getText().toString();
        longi = edit2.getText().toString();
        HashMap<String, String> user = session.getUserDetails();
        nomhs = user.get(SessionManager.KEY_NAME);
        /**fab on click **/
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isDeviceSupportCamera()){
                    Intent cameraIntent = new
                            Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, CAMERA_REQUEST);
                }else{
                    Toast.makeText(getBaseContext(), "Perangkat Anda tidak mendukung kamera", Toast.LENGTH_SHORT).show();
                }
            }
        });

        /** simpan on click **/
        simpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nama = nama_txt.getText().toString();
                if(!nama.isEmpty()&&status){
                    request = new simpanLokasi();
                    progressBar.setVisibility(View.VISIBLE);
                    new Thread(new Runnable() {
                        public void run() {
                            try {
                                request.execute();
                            } catch (CancellationException e) {
                                Log.d("request ", "run: "+e);
                                request.cancel(true);
                            }
                        }
                    }).start();
                }else{
                    Toast.makeText(getBaseContext(), "Data Belum Lengkap", Toast.LENGTH_SHORT).show();
                }
            }
        });

        /** Check session **/

        Picasso.with(this)
                .load(R.drawable.image_default)
                .placeholder(R.drawable.image_default)
                .error(R.drawable.image_default)
                .noFade()
                .into(gambarLokasi);

        /** lokasi **/
        locationManager = (LocationManager)
                getSystemService(this.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        location = locationManager.getLastKnownLocation(provider);
        isGPSEnabled = locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnabled = locationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (location != null) {
            onLocationChanged(location);
        }else{
            showSettingsAlert();
        }
        /**end of lokasi **/
    }

    /** Camera **/
    private boolean isDeviceSupportCamera() {
        if (getApplicationContext().getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA)) {
            return true;
        } else {
            return false;
        }
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent
            data) {
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            Bitmap mphoto = (Bitmap) data.getExtras().get("data");
            //String a = data.getStringExtra("data");
            Uri tempUri = getImageUri(getApplicationContext(), mphoto);
            // CALL THIS METHOD TO GET THE ACTUAL PATH
            finalFile = new File(getRealPathFromURI(tempUri));
            Log.d("req", "onActivityResult: "+finalFile.toString());
            //gambarLokasi.setImageBitmap(mphoto);
            status = true;
            Picasso.with(this)
                    .load(finalFile)
                    .placeholder(R.drawable.image_default)
                    .error(R.drawable.image_default)
                    .noFade()
                    .into(gambarLokasi);
        }
    }
    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public String getRealPathFromURI(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        return cursor.getString(idx);
    }
    /**Camera**/

    /** Access Location **/
    @Override
    public void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (location != null) {
            locationManager.requestLocationUpdates(provider, 400, 1, this);
        }else{
            showSettingsAlert();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        double latitude = (location.getLatitude());
        double longitude = (location.getLongitude());
        edit1.setText(""+latitude);
        edit2.setText(""+longitude);
        lat = edit1.getText().toString();
        longi = edit2.getText().toString();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
    public void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        // Setting Dialog Title
        alertDialog.setTitle("Pengaturan GPS");

        // Setting Dialog Message
        alertDialog.setMessage("GPS tidak aktif, aktifkan GPS ?");

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        alertDialog.show();
    }
    /** End of access Location **/
    class simpanLokasi extends AsyncTask<String, String, JSONObject> {

        public JSONParser jsonParser = new JSONParser();
        private static final String LOGIN_URL = "http://datalokasi.esy.es/simpanLokasi.php";
        //private static final String LOGIN_URL = "http://hmjti.akakom.ac.id/datalokasi_web/simpanLokasi.php";
        private static final String TAG_SUCCESS = "sukses";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected JSONObject doInBackground(String... data) {
            try {
                HashMap<String, String> params = new HashMap<>();
                BitmapFactory.Options options = null;
                options = new BitmapFactory.Options();
                options.inSampleSize = 3;
                bitmap = BitmapFactory.decodeFile(finalFile.toString(),
                        options);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                // Must compress the Image to reduce image size to make upload easy
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream);
                byte[] byte_arr = stream.toByteArray();
                decodedImage = Base64.encodeToString(byte_arr, 0);
                Log.d("req", "doInBackground: "+decodedImage);
                Log.d("req", "doInBackground: "+nomhs);
                Log.d("req", "doInBackground: "+nama);

                params.put("nomhs", nomhs);
                params.put("nama", nama);
                params.put("lattitude", lat);
                params.put("longitude", longi);
                params.put("image", decodedImage);
                JSONObject json = jsonParser.makeHttpRequest(
                        LOGIN_URL, "POST", params);
                if (json != null) {
                    return json;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(JSONObject json) {
            int success=2;
            if (json != null) {
                try {
                    success = json.getInt(TAG_SUCCESS);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if (success == 1) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(TambahData.this, "Penyimpanan data Berhasil",
                        Toast.LENGTH_LONG).show();
                Intent i = new Intent(getApplicationContext(),
                        MainActivity.class);
                startActivity(i);
                finish();
            }else if(success==0){
                progressBar.setVisibility(View.GONE);
                Toast.makeText(TambahData.this, "Penyimpanan data Gagal",
                        Toast.LENGTH_LONG).show();
            }else{
                progressBar.setVisibility(View.GONE);
                Toast.makeText(TambahData.this, "Gagal, Upload data error",
                        Toast.LENGTH_LONG).show();
            }
        }
    }
    private void initToolbar() {
        ActionBar actionbar = getSupportActionBar ();
        if (actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true);
            actionbar.setTitle("Tambah Lokasi");
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return (super.onOptionsItemSelected(item));
    }

}
