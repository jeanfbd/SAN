package desenvolvimentoads.san;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by jeanf on 14/08/2017.
 */

public class BroadcastThread extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        intent = new Intent(context,ServiceThread.class);
        context.startService(intent);


    }
}
