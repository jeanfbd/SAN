package desenvolvimentoads.san;

import android.app.Activity;
import android.util.Log;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;

import desenvolvimentoads.san.DAO.MarkerDAO;
import desenvolvimentoads.san.Helper.DateHelper;
import desenvolvimentoads.san.Model.MarkerBD;

/**
 * Created by jeanf on 15/07/2017.
 */

public class LiveThread {
    int timeLife = 0;

    public void liveMarkerCount(final Marker marker, final MarkerBD markerBDClass, final Activity activity) {//
        final MarkerDAO markerDAO = MarkerDAO.getInstance(activity);
        new Thread() {
            public void run() {
                while (getTimeLife(markerBDClass) != 0) {
                    try {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                markerBDClass.setLifeTime(getTimeLife(markerBDClass) - 1);
                                markerDAO.update(markerBDClass);//
                                marker.setSnippet("Data Registro: " + (DateHelper.dateBRFormat(markerBDClass.getCreationDate())) + "\n" +
                                        "Tempo de Duração: " + (markerBDClass.getLifeTime()));
                                if (getTimeLife(markerBDClass) == 0) {
                                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_maker_cinza_star));
                                    markerBDClass.setStatus(false);
                                    markerBDClass.setDraggable(false);
                                    markerDAO.update(markerBDClass);
                                    marker.setVisible(markerBDClass.isStatus());
                                    marker.setDraggable(markerBDClass.isDraggable());
                                 //   MapsTerceiro.removeCircle();
                                }
                            }
                        });
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    public int getTimeLife(MarkerBD markerBD){
        Log.d("LifeTime", String.valueOf(timeLife));
        return timeLife = MarkerDAO.QueryGetTimeLife(markerBD.getId());
    }
}
