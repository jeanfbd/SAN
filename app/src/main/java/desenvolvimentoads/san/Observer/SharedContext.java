package desenvolvimentoads.san.Observer;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class SharedContext {
    private static SharedContext instance;
    Context context;

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    private SharedContext() {


    }

    public static SharedContext getInstance() {
        if (instance == null) {
            instance = new SharedContext();
        }

        return instance;
    }

    public void createPrefers(String packName, String name, String value){

        if(context != null){
            SharedPreferences myPreferences = context.getApplicationContext().getSharedPreferences(packName,Context.MODE_PRIVATE);
            SharedPreferences.Editor edit = myPreferences.edit();
            edit.putString(name,value);
            edit.commit();
            Log.i("teste", "SharedContext is working !!");

        }
    }


}