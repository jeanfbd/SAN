package desenvolvimentoads.san;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsFragment extends SupportMapFragment implements OnMapReadyCallback, GoogleMap.OnMapClickListener{

    private GoogleMap mMap;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMapClickListener(this);

        mMap.getUiSettings().setZoomControlsEnabled(true);
        //Metodo carrega após o mapa estiver pronto usar o timeready da tela inicial
        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);

        MarkerOptions marker = new MarkerOptions();
        marker.position(sydney);
        marker.title("Marker Sidney");
        marker.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_maker_amarelo));

        mMap.addMarker(marker);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
    }

    @Override
    public void onMapClick(LatLng latLng) {
        Toast.makeText(getContext(), "Coordenadas: "+latLng.toString(), Toast.LENGTH_LONG).show();
    }
}
