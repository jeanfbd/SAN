package desenvolvimentoads.san.API;

import java.util.List;

import desenvolvimentoads.san.Model.MarkerBD;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

/**
 * Created by jeanf on 28/07/2017.
 */

public interface MarkerService {
    //Webservice Online
    //public static final String BASE_URL = "https://jeanfelipebrock.000webhostapp.com/index.php/";

    //Localhost substituir 10.92.40.176 pelo ip da maquina
    public static final String BASE_URL = "http://10.92.40.176/Webservice/";


    @GET("getAllMarkers")
    Call<List<MarkerBD>> getAllMarker();

    @GET("getMarker/{id}")
    Call<MarkerBD> getMarker(@Path("id") String id);

    @GET("getRaio/{lat}/{lng}/{km}")
    Call<List<MarkerBD>> getRaio(@Path("lat") String lat,@Path("lng") String lng, @Path("km") String km );

    @POST("insert")
    Call<Void> insertMarker(@Body MarkerBD markerBD);

    @POST("update/{id}")
    Call<Void> updateMarker(@Path("id") String id, @Body MarkerBD markerBD);

    @GET("delete/{id}")
    Call<Void> deleteMarker(@Path("id") String id);



    public static final Retrofit RETROFIT = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();

}
