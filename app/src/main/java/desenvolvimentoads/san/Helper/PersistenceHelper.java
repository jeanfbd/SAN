package desenvolvimentoads.san.Helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import desenvolvimentoads.san.DAO.MarkerDAO;

/**
 * Created by jeanf on 17/07/2017.
 */

public class PersistenceHelper extends SQLiteOpenHelper {

    public static final String DB_NAME =  "SAN";
    public static final int VERSION =  1;

    private static PersistenceHelper instance;

    private PersistenceHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    public static PersistenceHelper getInstance(Context context) {
        if(instance == null)
            instance = new PersistenceHelper(context);

        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(MarkerDAO.CREATE_TABLE_MARKER);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(MarkerDAO.DROP_TABLE);
        onCreate(db);
    }
}
