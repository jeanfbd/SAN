package desenvolvimentoads.san;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;

/**
 * Created by jeanf on 10/07/2017.
 */

public class TelaInicial extends Activity {

    protected static final int TIME_RUNTIME = 10000;

    protected boolean pbActive;

    protected ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tela_inicial);

        progressBar = (ProgressBar)findViewById(R.id.progressBar);

        Thread timerThread = new Thread(){
            @Override
            public void run(){
                try {
                    sleep(TIME_RUNTIME);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }finally {
                    Intent intent = new Intent(TelaInicial.this, MenuInicial.class);
                    startActivity(intent);
                }

            }
        };

        final Thread timerBar = new Thread(){
            @Override
            public void run() {
                pbActive = true;
                try {
                    int waited = 0;
                    while (pbActive && (waited < TIME_RUNTIME)){
                        sleep(200);
                        if (pbActive){
                            waited+=200;
                            updateProgressBar(waited);
                        }
                    }
                }catch (InterruptedException e){
                    e.printStackTrace();
                }finally {
                    onContinue();
                }
            }
        };

        timerThread.start();
        timerBar.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    public void updateProgressBar(final int timePassed){
        if (null != progressBar){
            final int progress = progressBar.getMax() * timePassed /TIME_RUNTIME;
            progressBar.setProgress(progress);
        }
    }

    public void onContinue(){
        Log.d("mensagemFinal", "Sua barra concluiu");
    }
}
