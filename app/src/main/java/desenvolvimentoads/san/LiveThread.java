package desenvolvimentoads.san;

import android.app.Activity;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;

/**
 * Created by jeanf on 15/07/2017.
 */

public class LiveThread {
    private int waited = 0;
    public void liveMarkerCount(final int timeSeconds, final Marker marker, final Activity activity){//
        Thread thread = new Thread() {
            public void run() {
                while (waited <= timeSeconds) {
                    try {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                marker.setSnippet("Tempo de Duração: "+(timeSeconds - waited));
                                marker.showInfoWindow();
                                if (waited == timeSeconds){
                                    marker.setSnippet(null);
                                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_maker_cinza_star));
                                }
                            }
                        });
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    waited++;
                }
            }
        };
        thread.start();
    }

}
