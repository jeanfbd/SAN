package desenvolvimentoads.san;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jeanf on 14/08/2017.
 */

public class ServiceThread extends Service {

    public List<Worker> threads = new ArrayList<Worker>();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d("Service teste bind"," esta funcioando???");

        return null;
    }

    @Override
    public void onCreate(){
        super.onCreate();
        Log.d("Service teste"," esta funcioando???");
        Log.i("Script", "onCreate()");
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId){


        Log.d("Service teste 0"," esta funcioando???");
        new Worker(){}.start();

        return(START_STICKY);
        // START_NOT_STICKY
        // START_STICKY
        // START_REDELIVER_INTENT
    }


    class Worker extends Thread{
        public int count = 0;

        public void run(){
            while(true){
                try {
                    Thread.sleep(1000);
                    if (count == 2){
                        Log.d("Service teste 1"," esta funcioando???");
                        Log.i("Script", "Tempo de Requisição");
                        count = 0;
                        MapsTerceiro.getRaio("-23.6202800","-45.4130600","10");

                    }
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                count++;
                Log.i("Script", "COUNT: "+count);
            }
        }
    }


    @Override
    public void onDestroy(){

        Log.d("Service teste morreu"," esta funcioando???");
        super.onDestroy();
    }
}

