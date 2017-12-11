package desenvolvimentoads.san.DAO;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import desenvolvimentoads.san.Helper.PersistenceHelper;
import desenvolvimentoads.san.Model.MarkerBD;

/**
 * Created by jeanf on 17/07/2017.
 */

public class MarkerDAO {
    /**
     * Variaveis estaticas que armazenam no nome e campos da tabela marker no banco de dados
     */
    public static final String TABLE_NAME = "Marker";
    public static final String ID = "id";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String CREATIONDATE = "creation";

    /**
     * Variavel estatica que armazenam a query de criacao da tabela marker no banco de dados
     */
    public static final String CREATE_TABLE_MARKER = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "("
            + ID + " VARCHAR NOT NULL, "
            + LATITUDE + " DOUBLE NOT NULL, "
            + LONGITUDE + " DOUBLE NOT NULL, "
            + CREATIONDATE + " LONG NOT NULL"
            + ");";

    /**
     * Variavel estatica que armazenam a query de exclusao da tabela marker no banco de dados
     */
    public static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

    private static SQLiteDatabase dataBase = null;

    private static MarkerDAO instance;

    public static MarkerDAO getInstance(Context context) {
        if (instance == null)
            instance = new MarkerDAO(context);
        return instance;
    }


    private MarkerDAO(Context context) {
        PersistenceHelper persistenceHelper = PersistenceHelper.getInstance(context);
        dataBase = persistenceHelper.getWritableDatabase();
    }

    public void saveMarker(MarkerBD markerBD) {
        ContentValues values = contentValuesMarker(markerBD);
        Log.d(TABLE_NAME, CREATE_TABLE_MARKER);
        dataBase.insert(TABLE_NAME, null, values);
    }

    public List<MarkerBD> getAllMarkers() {
        String queryReturnAll = "SELECT * FROM " + TABLE_NAME;
        Cursor cursor = dataBase.rawQuery(queryReturnAll, null);
        List<MarkerBD> markerBDs = markerCreateCursor(cursor);
        return markerBDs;
    }

    public List<MarkerBD> getAllMarkersActive(String query) {
        String queryReturnAllActive = "SELECT * FROM " + TABLE_NAME + " WHERE " + query;
        Cursor cursor = dataBase.rawQuery(queryReturnAllActive, null);
        List<MarkerBD> markerBDs = markerCreateCursor(cursor);
        return markerBDs;
    }

    public List<MarkerBD> getPerLatLng(double latitude, double longitude) {
        String queryReturnPerLatLng = "SELECT * FROM " + TABLE_NAME + " WHERE " + LATITUDE + " = " + latitude + " AND "+ LONGITUDE + " = " + longitude;
        Cursor cursor = dataBase.rawQuery(queryReturnPerLatLng, null);
        List<MarkerBD> markerBDs = markerCreateCursor(cursor);
        return markerBDs;
    }

    public List<MarkerBD> getPerCreationDate(String date) {
        String queryReturnPerCreationDate = "SELECT * FROM " + TABLE_NAME + " WHERE " + CREATIONDATE + " = " + date;
        Cursor cursor = dataBase.rawQuery(queryReturnPerCreationDate, null);
        List<MarkerBD> markerBDs = markerCreateCursor(cursor);
        return markerBDs;
    }

    public MarkerBD getPerMarker(String idMarker) {
        String queryReturnPerMarker = "SELECT * FROM " + TABLE_NAME + " WHERE " + ID + " = '"+idMarker+"'";
        Cursor cursor = dataBase.rawQuery(queryReturnPerMarker, null);
        List<MarkerBD> markerBDs = markerCreateCursor(cursor);
        Log.d("Query: ",queryReturnPerMarker);
        for (MarkerBD m: markerBDs){
           Log.d("idClasse", String.valueOf(m.getId()));
        }
        return markerBDs.get(0);
    }

    public MarkerBD getPerIdClass(int id) {
        String queryReturnPerIdClass = "SELECT * FROM " + TABLE_NAME + " WHERE " + ID + " = '"+id+"'";
        Cursor cursor = dataBase.rawQuery(queryReturnPerIdClass, null);
        List<MarkerBD> markerBDs = markerCreateCursor(cursor);
        Log.d("Query: ",queryReturnPerIdClass);
        return markerBDs.get(0);
    }

    public void delete(MarkerBD markerBD) {
        ContentValues values = contentValuesMarker(markerBD);
        String[] replaceValues = {
                String.valueOf(markerBD.getId())
        };
        dataBase.update(TABLE_NAME, values, ID + " = ?", replaceValues);
    }

    public void update(final MarkerBD markerBD) {
        ContentValues values = contentValuesMarker(markerBD);
        String[] replaceValues = {
                String.valueOf(markerBD.getId())
        };
        Log.d("ID", String.valueOf(markerBD.getId()));
        dataBase.update(TABLE_NAME, values, ID + " = ?", replaceValues);
    }

    public void updateQuery(MarkerBD markerBD){
        String query = "UPDATE "+TABLE_NAME+" SET "
                +LATITUDE+" = "+ markerBD.getLatitude()+", "
                +LONGITUDE+" = "+ markerBD.getLongitude()+", "
                +CREATIONDATE+" = '"+ markerBD.getCreationDate()+
                " WHERE "+ID+" = "+ markerBD.getId()+";";

        Log.d("Query", query);
        dataBase.execSQL(query);
    }

    public static int lastQueryId(){
        int lastId=-1;
        try{
            Cursor cursor=dataBase.rawQuery("SELECT MAX(id) FROM "+TABLE_NAME, new String [] {});
            if (cursor != null)
                if(cursor.moveToFirst())
                {
                    lastId= cursor.getInt(0);
                }
           Log.d("", "LASTID: "+lastId);
           return lastId;
        }
        catch(Exception e){
            return -1;
        }
    }

    public static int QueryGetTimeLife(final int id){
        int timeLife=-1;
        try{
            Cursor cursor=dataBase.rawQuery("SELECT liveTime FROM "+TABLE_NAME+" WHERE "+ID+" = "+id, new String [] {});
            if (cursor != null)
                if(cursor.moveToFirst())
                {
                    timeLife= cursor.getInt(0);
                }
            Log.d("", "TIMELIFE: "+timeLife);
            return timeLife;
        }
        catch(Exception e){
            return timeLife;
        }
    }

    public void closeConection() {
        if (dataBase != null && dataBase.isOpen())
            dataBase.close();
    }

    private List<MarkerBD> markerCreateCursor(Cursor cursor) {
        List<MarkerBD> markerBDs = new ArrayList<>();
        if (cursor == null)
            return markerBDs;

        try {
            if (cursor.moveToFirst()) {
                do {
                    int indexID = cursor.getColumnIndex(ID);
                    int indexLatitude = cursor.getColumnIndex(LATITUDE);
                    int indexLongitude = cursor.getColumnIndex(LONGITUDE);
                    int indexCreationDate = cursor.getColumnIndex(CREATIONDATE);

                    String id = cursor.getString(indexID);
                    double latitude = cursor.getDouble(indexLatitude);
                    double longitude = cursor.getDouble(indexLongitude);
                    Long creationDate = cursor.getLong(indexCreationDate);

                    MarkerBD markerBD = new MarkerBD(id, latitude, longitude, creationDate);

                    markerBDs.add(markerBD);

                }while(cursor.moveToNext());
            }
        }finally {
            cursor.close();
        }
        return markerBDs;
    }

    private ContentValues contentValuesMarker(MarkerBD markerBD){
        ContentValues values = new ContentValues();
        values.put(ID, markerBD.getId());
        values.put(LATITUDE, markerBD.getLatitude());
        values.put(LONGITUDE, markerBD.getLongitude());
        values.put(CREATIONDATE, markerBD.getCreationDate());
        return values;
    }


}
