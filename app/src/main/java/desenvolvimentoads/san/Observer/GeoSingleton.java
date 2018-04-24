package desenvolvimentoads.san.Observer;

import com.firebase.geofire.GeoQuery;

public class GeoSingleton {

    private static GeoSingleton instance;


    GeoQuery geoQuery;


    private GeoSingleton(){


    }

public static GeoSingleton getInstance(){
   if(instance == null){
       instance = new GeoSingleton();
   }

        return instance;
}

    public GeoQuery getGeoQuery() {
        return geoQuery;
    }

    public void setGeoQuery(GeoQuery geoQuery) {
        this.geoQuery = geoQuery;
    }
}
