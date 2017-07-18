package desenvolvimentoads.san;

import android.app.Activity;
import android.util.Log;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import desenvolvimentoads.san.DAO.MarkerDAO;
import desenvolvimentoads.san.Helper.DateHelper;
/**
 * Created by jeanf on 15/07/2017.
 */

public class LiveThread {
    public void liveMarkerCount(final Marker marker, final desenvolvimentoads.san.Model.Marker markerClass, final Activity activity) {//
        final MarkerDAO markerDAO = MarkerDAO.getInstance(activity);
        if (markerClass.getLifeTime() != 0 && markerClass.isStatus()){
            new Thread() {
                public void run() {
                    while (markerClass.getLifeTime() != 0) {
                        try {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    markerClass.setLifeTime(markerClass.getLifeTime() - 1);
                                    markerDAO.update(markerClass);
//                                marker.setSnippet("Data Registro: "+(markerClass.getCreationDate())+"\n"+
//                                                  "Tempo de Duração: "+(markerClass.getLifeTime() - waited));
                                    marker.setSnippet("Data Registro: "+(DateHelper.dateBRFormat(markerClass.getCreationDate()))+"\n"+
                                            "Tempo de Duração: "+(markerClass.getLifeTime()));
                                    if (markerClass.getLifeTime() == 0) {
                                        marker.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_maker_cinza_star));
                                        markerClass.setStatus(false);
                                        markerDAO.update(markerClass);
                                        marker.setVisible(markerClass.isStatus());
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
    }
}
