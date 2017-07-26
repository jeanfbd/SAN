package desenvolvimentoads.san.Helper;

import org.json.JSONException;
import org.json.JSONObject;

import desenvolvimentoads.san.Model.MarkerBD;

/**
 * Created by jeanf on 26/07/2017.
 */

public class JsonHelper {
    public static String generateJSON(MarkerBD markerBD){
        JSONObject jo = new JSONObject();

        try{
            jo.put("id", markerBD.getId());
            jo.put("idUser", markerBD.getIdUser());
            jo.put("idMarker", markerBD.getIdMarker());
            jo.put("latitude", markerBD.getLatitude());
            jo.put("longitude", markerBD.getLongitude());
            jo.put("title", markerBD.getTitle());
            jo.put("lifetime", markerBD.getLifeTime());
            jo.put("image", markerBD.getImage());
            jo.put("creationdate", markerBD.getCreationDate());
            jo.put("draggable", markerBD.isDraggable());
            jo.put("status", markerBD.isStatus());
        }
        catch(JSONException e){ e.printStackTrace(); }

        return(jo.toString());
    }


    public static MarkerBD degenerateJSON(String data){
        MarkerBD markerBD = new MarkerBD();

        try{
            JSONObject jo = new JSONObject(data);

            markerBD.setId(jo.getInt("id"));
            markerBD.setIdUser(jo.getInt("idUser"));
            markerBD.setIdMarker(jo.getString("idMarker"));
            markerBD.setLatitude(jo.getDouble("latitude"));
            markerBD.setLongitude(jo.getDouble("longitude"));
            markerBD.setTitle(jo.getString("title"));
            markerBD.setLifeTime(jo.getInt("lifetime"));
            markerBD.setImage(jo.getInt("image"));
            markerBD.setCreationDate(jo.getString("creationdate"));
            markerBD.setDraggable(jo.getBoolean("draggable"));
            markerBD.setStatus(jo.getBoolean("status"));
        }
        catch(JSONException e){ e.printStackTrace(); }

        return(markerBD);
    }
}
