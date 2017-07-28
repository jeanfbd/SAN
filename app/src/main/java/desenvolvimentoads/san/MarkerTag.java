package desenvolvimentoads.san;

import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by master on 22/07/2017.
 */

public class MarkerTag {

Circle circle;
LatLng position;
int nivel;


    public MarkerTag(Circle circle, LatLng position, int nivel) {
        this.circle = circle;
        this.position = position;
        this.nivel = nivel;
    }

    public int getNivel() {
        return nivel;
    }

    public void setNivel(int nivel) {
        this.nivel = nivel;
    }

    public Circle getCircle() {
        return circle;
    }

    public void setCircle(Circle circle) {
        this.circle = circle;
    }

    public LatLng getPosition() {
        return position;
    }

    public void setPosition(LatLng position) {

        this.position = position;

    }
}
