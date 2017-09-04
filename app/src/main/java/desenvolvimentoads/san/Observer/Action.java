package desenvolvimentoads.san.Observer;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by master on 03/09/2017.
 */

public class Action {
    private static Action instance;
    private Set<ActionObserver> interessados = new HashSet<>();
    private boolean buttomAddMaker = true;

    private Action() {


    }


    public static Action getInstance() {

        if (instance == null) {
            instance = new Action();

        }


        return instance;
    }

    public boolean getButtomAddMaker() {
        return buttomAddMaker;
    }

    public void setButtomAddMaker(boolean buttomAddMaker) {

        this.buttomAddMaker = buttomAddMaker;
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
