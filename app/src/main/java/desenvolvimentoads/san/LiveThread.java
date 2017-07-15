package desenvolvimentoads.san;

import android.app.Activity;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by jeanf on 15/07/2017.
 */

public class LiveThread {
    private int waited = 0;

    public void liveMarkerCount(final int timeSeconds, final Marker marker, final Activity activity) {//
        final String systemDate = getSystemDate();
        Thread thread = new Thread() {
            public void run() {
                while (waited <= timeSeconds) {
                    try {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                marker.setSnippet("Data Registro: "+(systemDate)+"\n"+
                                                  "Tempo de Duração: "+(timeSeconds - waited));
                                marker.showInfoWindow();
                                if (waited == timeSeconds) {
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

    public String getSystemDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy-HH:mm:ss");
        // OU
        SimpleDateFormat dateFormat_hora = new SimpleDateFormat("HH:mm:ss");

        Date data = new Date();

        Calendar cal = Calendar.getInstance();
        cal.setTime(data);
        Date data_atual = cal.getTime();

        String data_completa = dateFormat.format(data_atual);

        String hora_atual = dateFormat_hora.format(data_atual);

        return data_completa;
    }

}
