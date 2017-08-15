package desenvolvimentoads.san;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by jeanf on 14/08/2017.
 */

public class BroadcastThread extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("Script", "onReceive()");
        intent = new Intent("SERVICO_TEST");
        context.startService(intent);
    }
}
