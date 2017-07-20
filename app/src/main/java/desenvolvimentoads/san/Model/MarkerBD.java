package desenvolvimentoads.san.Model;

import android.util.Log;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import desenvolvimentoads.san.DAO.MarkerDAO;
import desenvolvimentoads.san.Helper.DateHelper;

/**
 * Created by jeanf on 17/07/2017.
 */

public class MarkerBD {

    private int id;
    private int idUser;
    private String idMarker;
    private double latitude;
    private double longitude;
    private String title;
    private int lifeTime;
    private int image;
    private String creationDate;
    private boolean draggable;
    private boolean status;

    public MarkerBD(){

    }

    /**
     * @param idUser Responsavel por identificacao do usuario que criou o marcador
     * @param latitude Responsavel por armazenar a latitude do marcador
     * @param longitude Responsavel por armazenar a longitude do marcador
     * @param title Responsavel por armazenar o titulo do marcador
     * @param lifeTime Responsavel por armazenar o tempo de vida do marcador
     * @param image Responsavel por armazenar a referencia da imagem do marcador
     */
    public MarkerBD(int id, int idUser, String idMarker, double latitude, double longitude, String title, int lifeTime, int image, boolean draggable, boolean status) {
        this.id = id;
        this.idUser = idUser;
        this.idMarker = idMarker;
        this.latitude = latitude;
        this.longitude = longitude;
        this.title = title;
        this.lifeTime = lifeTime;
        this.image = image;
        this.creationDate = DateHelper.getSystemDate();
        this.draggable = draggable;
        this.status = status;
    }

    public MarkerBD(int id, int idUser, double latitude, double longitude, String title, int lifeTime, int image) {
        this.id = id;
        Log.d("Criação ID:", String.valueOf(id));
        this.idUser = idUser;
        this.latitude = latitude;
        this.longitude = longitude;
        this.title = title;
        this.lifeTime = lifeTime;
        this.image = image;
        this.creationDate = DateHelper.getSystemDate();
        this.draggable = true;
        this.status = true;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdUser() {
        return idUser;
    }

    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }

    public String getIdMarker() {
        return idMarker;
    }

    public void setIdMarker(String idMarker) {
        this.idMarker = idMarker;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getLifeTime() {
        return lifeTime;
    }

    public void setLifeTime(int lifeTime) {
        this.lifeTime = lifeTime;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public boolean isDraggable() {
        return draggable;
    }

    public void setDraggable(boolean draggable) {
        this.draggable = draggable;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
}
