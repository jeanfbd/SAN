package desenvolvimentoads.san.DAO;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import desenvolvimentoads.san.Helper.DateHelper;
import desenvolvimentoads.san.Helper.PersistenceHelper;
import desenvolvimentoads.san.Model.Marker;

/**
 * Created by jeanf on 17/07/2017.
 */

public class MarkerDAO {
    /**
     * Variaveis estaticas que armazenam no nome e campos da tabela marker no banco de dados
     */
    public static final String TABLE_NAME = "Marker";
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

    public void saveMarker(Marker marker) {
        ContentValues values = contentValuesMarker(marker);
        Log.d(TABLE_NAME, CREATE_TABLE_MARKER);
        dataBase.insert(TABLE_NAME, null, values);
    }

    public List<Marker> getAllMarkers() {
        String queryReturnAll = "SELECT * FROM " + TABLE_NAME;
        Cursor cursor = dataBase.rawQuery(queryReturnAll, null);
        List<Marker> markers = markerCreateCursor(cursor);
        return markers;
    }

    public List<Marker> getAllMarkersActive() {
        String queryReturnAllActive = "SELECT * FROM " + TABLE_NAME + " WHERE " + STATUS + " = 1";
        Cursor cursor = dataBase.rawQuery(queryReturnAllActive, null);
        List<Marker> markers = markerCreateCursor(cursor);
        return markers;
    }

    public List<Marker> getPerLatLng(double latitude, double longitude) {
        String queryReturnPerLatLng = "SELECT * FROM " + TABLE_NAME + " WHERE " + LATITUDE + " = " + latitude + " AND "+ LONGITUDE + " = " + longitude;
        Cursor cursor = dataBase.rawQuery(queryReturnPerLatLng, null);
        List<Marker> markers = markerCreateCursor(cursor);
        return markers;
    }

    public List<Marker> getPerCreationDate(String date) {
        String queryReturnPerCreationDate = "SELECT * FROM " + TABLE_NAME + " WHERE " + CREATIONDATE + " = " + date;
        Cursor cursor = dataBase.rawQuery(queryReturnPerCreationDate, null);
        List<Marker> markers = markerCreateCursor(cursor);
        return markers;
    }

    public List<Marker> getPerMarker(String idMarker) {
        String queryReturnPerMarker = "SELECT * FROM " + TABLE_NAME + " WHERE " + IDMARKER + " = '" +idMarker+"'";
        Cursor cursor = dataBase.rawQuery(queryReturnPerMarker, null);
        List<Marker> markers = markerCreateCursor(cursor);
        return markers;
    }

    public void delete(Marker marker) {
        ContentValues values = contentValuesMarker(marker);
        String[] replaceValues = {
                String.valueOf(marker.getIdMarker())
        };
        dataBase.update(TABLE_NAME, values, IDMARKER + " = ?", replaceValues);
    }

    public void update(Marker marker) {
        ContentValues values = contentValuesMarker(marker);
        String[] replaceValues = {
                String.valueOf(marker.getId())
        };
        Log.d("ID", "update: "+marker.getId());
        dataBase.update(TABLE_NAME, values, ID + " = ?", replaceValues);
    }

    public void updateQuery(Marker marker){
        String query = "UPDATE "+TABLE_NAME+" SET "
                +LATITUDE+" = "+marker.getLatitude()+", "
                +LONGITUDE+" = "+marker.getLongitude()+", "
                +TITLE+" = '"+marker.getTitle()+"', "
                +LIFETIME+" = "+marker.getLifeTime()+", "
                +IMAGE+" = "+marker.getImage()+", "
                +CREATIONDATE+" = '"+marker.getCreationDate()+"', "
                +DRAGGABLE+" = "+(marker.isDraggable() ? 1 : 0)+", "
                +STATUS+" = "+(marker.isStatus() ? 1 : 0)+
                " WHERE "+IDMARKER+" = "+marker.getIdMarker()+";";

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

    private List<Marker> markerCreateCursor(Cursor cursor) {
        List<Marker> markers = new ArrayList<>();
        if (cursor == null)
            return markers;

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

                    Marker marker = new Marker(idUser, idMarker, latitude, longitude, title, lifeTime, image, draggable, status);

                    markers.add(marker);

                }while(cursor.moveToNext());
            }
        }finally {
            cursor.close();
        }
        return markers;
    }

    private ContentValues contentValuesMarker(Marker marker){
        ContentValues values = new ContentValues();
        values.put(ID, marker.getId());
        values.put(IDUSER, marker.getIdUser());
        values.put(IDMARKER, marker.getIdMarker());
        values.put(LATITUDE, marker.getLatitude());
        values.put(LONGITUDE, marker.getLongitude());
        values.put(TITLE, marker.getTitle());
        values.put(LIFETIME, marker.getLifeTime());
        values.put(IMAGE, marker.getImage());
        values.put(CREATIONDATE, marker.getCreationDate());
        values.put(DRAGGABLE, (marker.isDraggable() ? 1 : 0));
        values.put(STATUS, (marker.isStatus() ? 1 : 0));

        return values;
    }


}
