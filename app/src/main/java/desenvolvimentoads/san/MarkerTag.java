package desenvolvimentoads.san;

import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by master on 22/07/2017.
 */

public class MarkerTag {

Circle circle;

    public MarkerTag(Circle circle, LatLng position) {
        this.circle = circle;
        this.position = position;
    }

    LatLng position;

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
