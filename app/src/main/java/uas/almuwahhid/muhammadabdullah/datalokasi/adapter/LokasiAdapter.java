package uas.almuwahhid.muhammadabdullah.datalokasi.adapter;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.StringTokenizer;

import uas.almuwahhid.muhammadabdullah.datalokasi.R;
import uas.almuwahhid.muhammadabdullah.datalokasi.model.DataLokasi;

public class LokasiAdapter extends ArrayAdapter<DataLokasi> {
    private final Context context;
    private ArrayList<DataLokasi> items;
    private LayoutInflater inflater;

    public LokasiAdapter(Context context, ArrayList<DataLokasi> data) {
        super(context, R.layout.activity_lokasi_adapter, data);
        this.context = context;
        this.items = data;
    }
    @Override
    public long getItemId(int position) {
        return items.size();
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        ViewHolder viewHolder = new ViewHolder();
        if (convertView == null) {
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.activity_lokasi_adapter, parent, false);
        } else {
            view = convertView;
        }
        viewHolder.imageView = (ImageView) view.findViewById(R.id.gbr);
        viewHolder.txt_nama= (TextView) view.findViewById(R.id.judul_txt);
        viewHolder.txt_lat= (TextView) view.findViewById(R.id.lat_txt);
        viewHolder.txt_long= (TextView) view.findViewById(R.id.long_txt);
        int id = items.get(position).getId();
        String nama = items.get(position).getNama();
        String latitude = items.get(position).getLattitude();
        String longitude = items.get(position).getLongitude();
        viewHolder.txt_nama.setText(nama);
        viewHolder.txt_lat.setText(latitude);
        viewHolder.txt_long.setText(longitude);
        if (String.valueOf(id) != null) {
            Picasso.with(context)
                    .load("http://datalokasi.esy.es/images/"+id+".jpg")
                    //.load("http://hmjti.akakom.ac.id/datalokasi_web/images/"+id+".jpg")
                    .placeholder(R.drawable.image_default)
                    .error(R.drawable.image_default)
                    .noFade()
                    .into(viewHolder.imageView);
        }
        return view;
    }
    public class ViewHolder {
        TextView txt_lat, txt_long, txt_nama;
        ImageView imageView;
    }
}