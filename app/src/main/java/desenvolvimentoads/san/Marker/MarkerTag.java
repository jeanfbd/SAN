package desenvolvimentoads.san.Marker;


import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

import java.util.Map;

/**
 * Created by master on 22/07/2017.
 */

public class MarkerTag {

    private String id;
    private String idUser;
    private double latitude;
    private double longitude;
    private String street;
    private Long fim;
    private Map<String, String> inicio;

    @Exclude
    private boolean validate = false;

    @Exclude
    private Circle circle;

    private com.google.android.gms.maps.model.LatLng mapsLatLng;

    public MarkerTag() {
    }

    public MarkerTag(double latitude, double longitude, Circle circle, boolean validate) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.circle = circle;
        this.validate = validate;
        this.inicio = ServerValue.TIMESTAMP;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getIdUser() {
        return idUser;
    }

    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }

    public Map<String, String> getInicio() {
        return inicio;
    }

    public void setInicio(Map<String, String> inicio) {
        this.inicio = inicio;
    }

    public void setFim(Long fim) {
        this.fim = fim;
    }

    public Long getFim() {
        return fim;
    }

    @Exclude
    public LatLng getPosition() {
        return new LatLng(latitude, longitude);
    }

    @Exclude
    public Circle getCircle() {
        return circle;
    }

    public void setCircle(Circle circle) {
        this.circle = circle;
    }

    @Exclude
    public Boolean getValidate() {
        return validate;
    }

    public void setValidate(Boolean validate) {
        this.validate = validate;
    }
}
