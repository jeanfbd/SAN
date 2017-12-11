package desenvolvimentoads.san.Model;

import android.util.Log;

import java.sql.Timestamp;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import desenvolvimentoads.san.DAO.MarkerDAO;
import desenvolvimentoads.san.Helper.DateHelper;

/**
 * Created by jeanf on 17/07/2017.
 */

public class MarkerBD {

    private String id;
    private String idUser;
    private double latitude;
    private double longitude;
    private Long creationDate;

    public MarkerBD(){

    }

    public MarkerBD(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public MarkerBD(String id, double latitude, double longitude, Long creationDate) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.creationDate = creationDate;
    }

    public MarkerBD(String id, String idUser, double latitude, double longitude, Long creationDate) {
        this.id = id;
        this.idUser = idUser;
        this.latitude = latitude;
        this.longitude = longitude;
        this.creationDate = creationDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdUser() {
        return idUser;
    }

    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public Long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Long creationDate) {
        this.creationDate = creationDate;
    }
}
