package desenvolvimentoads.san.Observer;

import android.util.Log;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by master on 03/09/2017.
 */

public class Action {
    private static Action instance;
    private Set<ActionObserver> interessados = new HashSet<>();
    private boolean buttomAddMakerVisivel = true;
    private boolean reportNotSelected = true;

    private Action() {


    }


    public static Action getInstance() {

        if (instance == null) {
            instance = new Action();

        }


        return instance;
    }

    public boolean isReportNotSelected() {
        return reportNotSelected;
    }

    public void setReportNotSelected(boolean reportNotSelected) {
        this.reportNotSelected = reportNotSelected;
        reportNotSelectedStateChanged();
    }

    private void reportNotSelectedStateChanged() {

        for (ActionObserver interessado : interessados) {
            interessado.notificaticarInteressados(this);


        }

    }

    public boolean getButtomAddMakerClickado() {
        return buttomAddMakerVisivel;
    }

    public void setButtomAddMakerClickado(boolean buttomAddMakerClickado) {

        this.buttomAddMakerVisivel = buttomAddMakerClickado;
        Log.d("Mudei aqui agora olha",String.valueOf(this.buttomAddMakerVisivel));
        buttonAddMarkerStateChanged();
    }

    public void registraInteressados(ActionObserver interessado) {
        this.interessados.add(interessado);

    }

    public void cancelaInteressados(ActionObserver interessado) {
        this.interessados.remove(interessado);

    }

     private void buttonAddMarkerStateChanged() {

        for (ActionObserver interessado : interessados) {
            interessado.notificaticarInteressados(this);


        }

    }

}
