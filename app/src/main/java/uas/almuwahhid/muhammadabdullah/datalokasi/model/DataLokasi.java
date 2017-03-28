package uas.almuwahhid.muhammadabdullah.datalokasi.model;

/**
 * Created by gueone on 7/3/2016.
 */
public class DataLokasi {
    String lattitude;
    String longitude;
    String nama;
    int id;

    public DataLokasi(int id, String nama, String lattitude, String longitude) {
        this.lattitude = lattitude;
        this.longitude = longitude;
        this.nama = nama;
        this.id = id;
    }

    public String getNama() {
        return nama;
    }

    public String getLattitude() {
        return lattitude;

    }

    public String getLongitude() {
        return longitude;
    }

    public int getId() {
        return id;
    }
}
