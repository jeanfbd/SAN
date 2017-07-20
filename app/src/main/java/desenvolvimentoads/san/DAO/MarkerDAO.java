package desenvolvimentoads.san.DAO;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

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
    public static final String TABLE_NAME = "MarkerBD";
    public static final String ID = "id";
    public static final String IDUSER = "idUser";
    public static final String IDMARKER = "idMarker";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String TITLE = "title";
    public static final String LIFETIME = "liveTime";
    public static final String IMAGE = "image";
    public static final String CREATIONDATE = "creation";
    public static final String DRAGGABLE = "draggable";
    public static final String STATUS = "status";

    /**
     * Variavel estatica que armazenam a query de criacao da tabela marker no banco de dados
     */
    public static final String CREATE_TABLE_MARKER = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "("
            + ID + " INTEGER PRIMARY KEY NOT NULL, "
            + IDUSER + " INTEGER NOT NULL, "
            + IDMARKER + " INTEGER NULL, "
            + LATITUDE + " DOUBLE NOT NULL, "
            + LONGITUDE + " DOUBLE NOT NULL, "
            + TITLE + " TEXT NOT NULL, "
            + LIFETIME + " INTEGER NOT NULL, "
            + IMAGE + " INTEGER NOT NULL, "
            + CREATIONDATE + " DATETIME NOT NULL, "
            + DRAGGABLE + " BOOLEAN NOT NULL DEFAULT 1, "
            + STATUS + " BOOLEAN NOT NULL DEFAULT 1"
            + ");";

    /**
     * Variavel estatica que armazenam a query de exclusao da tabela marker no banco de dados
     */
    public static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;


    private SQLiteDatabase dataBase = null;

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

    public List<MarkerBD> getAllMarkersActive() {
        String queryReturnAllActive = "SELECT * FROM " + TABLE_NAME + " WHERE " + STATUS + " = 1";
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

    public List<MarkerBD> getPerMarker(String idMarker) {
        String queryReturnPerMarker = "SELECT * FROM " + TABLE_NAME + " WHERE " + IDMARKER + " = '" +idMarker+"'";
        Cursor cursor = dataBase.rawQuery(queryReturnPerMarker, null);
        List<MarkerBD> markerBDs = markerCreateCursor(cursor);
        return markerBDs;
    }

    public void delete(MarkerBD markerBD) {
        ContentValues values = contentValuesMarker(markerBD);
        String[] replaceValues = {
                String.valueOf(markerBD.getIdMarker())
        };
        dataBase.update(TABLE_NAME, values, IDMARKER + " = ?", replaceValues);
    }

    public void update(MarkerBD markerBD) {
        ContentValues values = contentValuesMarker(markerBD);
        String[] replaceValues = {
                String.valueOf(markerBD.getId())
        };
        Log.d("ID", "update: "+ markerBD.getId());
        dataBase.update(TABLE_NAME, values, ID + " = ?", replaceValues);
    }

    public void updateQuery(MarkerBD markerBD){
        String query = "UPDATE "+TABLE_NAME+" SET "
                +LATITUDE+" = "+ markerBD.getLatitude()+", "
                +LONGITUDE+" = "+ markerBD.getLongitude()+", "
                +TITLE+" = '"+ markerBD.getTitle()+"', "
                +LIFETIME+" = "+ markerBD.getLifeTime()+", "
                +IMAGE+" = "+ markerBD.getImage()+", "
                +CREATIONDATE+" = '"+ markerBD.getCreationDate()+"', "
                +DRAGGABLE+" = "+(markerBD.isDraggable() ? 1 : 0)+", "
                +STATUS+" = "+(markerBD.isStatus() ? 1 : 0)+
                " WHERE "+IDMARKER+" = "+ markerBD.getIdMarker()+";";

        Log.d("Query", query);
        dataBase.execSQL(query);
    }

    public int lastQueryId(){
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
                    int indexIDUser = cursor.getColumnIndex(IDUSER);
                    int indexIDMarker = cursor.getColumnIndex(IDMARKER);
                    int indexLatitude = cursor.getColumnIndex(LATITUDE);
                    int indexLongitude = cursor.getColumnIndex(LONGITUDE);
                    int indexTitle = cursor.getColumnIndex(TITLE);
                    int indexLifeTime = cursor.getColumnIndex(LIFETIME);
                    int indexImage = cursor.getColumnIndex(IMAGE);
                    int indexCreationDate = cursor.getColumnIndex(CREATIONDATE);
                    int indexDraggable = cursor.getColumnIndex(DRAGGABLE);
                    int indexStatus = cursor.getColumnIndex(STATUS);

                    int id = cursor.getInt(indexID);
                    int idUser = cursor.getInt(indexIDUser);
                    String idMarker = cursor.getString(indexIDMarker);
                    double latitude = cursor.getDouble(indexLatitude);
                    double longitude = cursor.getDouble(indexLongitude);
                    String title = cursor.getString(indexTitle);
                    int lifeTime = cursor.getInt(indexLifeTime);
                    int image = cursor.getInt(indexImage);
                    String creationDate = cursor.getString(indexCreationDate);

                    boolean draggable = (cursor.getInt(indexDraggable) == 1);
                    boolean status = (cursor.getInt(indexStatus) == 1);

                    MarkerBD markerBD = new MarkerBD(idUser, idMarker, latitude, longitude, title, lifeTime, image, draggable, status);

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
        values.put(IDUSER, markerBD.getIdUser());
        values.put(IDMARKER, markerBD.getIdMarker());
        values.put(LATITUDE, markerBD.getLatitude());
        values.put(LONGITUDE, markerBD.getLongitude());
        values.put(TITLE, markerBD.getTitle());
        values.put(LIFETIME, markerBD.getLifeTime());
        values.put(IMAGE, markerBD.getImage());
        values.put(CREATIONDATE, markerBD.getCreationDate());
        values.put(DRAGGABLE, (markerBD.isDraggable() ? 1 : 0));
        values.put(STATUS, (markerBD.isStatus() ? 1 : 0));

        return values;
    }


}
