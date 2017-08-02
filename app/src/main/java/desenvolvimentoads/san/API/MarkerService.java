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
    public static final String BASE_URL = "https://jeanfelipebrock.000webhostapp.com/index.php/";

    @GET("getAllMarkers")
    Call<List<MarkerBD>> getAllMarker();

    @GET("getMarker/{id}")
    Call<MarkerBD> getMarker(@Path("id") String id);

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
