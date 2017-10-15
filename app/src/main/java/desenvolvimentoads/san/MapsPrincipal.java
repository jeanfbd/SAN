package desenvolvimentoads.san;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.List;

import desenvolvimentoads.san.Marker.MarkerDialog;
import desenvolvimentoads.san.Observer.Action;
import desenvolvimentoads.san.Observer.ActionObserver;
import desenvolvimentoads.san.notification.NotificationApp;


public class MapsPrincipal extends SupportMapFragment implements OnMapReadyCallback, GoogleMap.OnMapClickListener,ActionObserver{

    private GoogleMap mMap;
    Marker marcador = null;
    private LocationManager locationManager;
    private boolean buttomAddMarkerVisivel;
    Action action = Action.getInstance();
    List<Marker> marcadores = new ArrayList<>();

    /* Classe com os metodos dos markers */
    MarkerDialog markerDialog = new MarkerDialog();

    Geocoder geocoder2;

    /*Iniciando dados estaticos de posição*/
    private double longitude = -45.4130600;
    private double latitude = -23.6202800;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getMapAsync(this);
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        action.registraInteressados(this);
        buttomAddMarkerVisivel = action.getButtomAddMakerClickado();
        geocoder2 = new Geocoder(getContext());

        /*Adiciona o listener no infoWindows(tag) do marker*/
        googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
              //  marker.showInfoWindow();
                Toast.makeText(getContext(), "Clickou", Toast.LENGTH_LONG).show();
            }
        });

        /*Criando o listener do click longo*/
        googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {

            @Override
            public void onMapLongClick(LatLng arg0) {
                // TODO Auto-generated method stub
                if(MenuInicial.vDenunciar){
                   if(markerDialog.hasNearby(marcadores,arg0)){
                       Toast.makeText(getContext(), "TEM MARCADOR AQUI PERTO!!!", Toast.LENGTH_LONG).show();

                   }else{

                       Toast.makeText(getContext(), "NÃO TEM NADA NÃO!!!", Toast.LENGTH_LONG).show();
                       markerDialog.dialogAdd2(arg0,getContext(), mMap, geocoder2, marcadores);

                   }



                }

            }
        });


        /*Criando o listener click on marker*/
        googleMap = markerDialog.setMarkerClick(googleMap , getContext());


        /*Criando o listener do drag*/
        googleMap = markerDialog.setListenerDragDiag(googleMap, marcador, getContext(), getView());

        mMap = googleMap;

        /*Checkando a permissão dos acessos, vulgo frescura do Android..*/
        if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED) {

            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);



        }

        mMap.setOnMapClickListener(this);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(true);


        /*Listener responsavel adicionar o botão da localização*/
        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener(){
            @Override
            public boolean onMyLocationButtonClick()
            {   LatLng hue = new LatLng(latitude, longitude);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(hue));
                return false;
            }
        });

        /*Iniciando o serviço de localização*/
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

         final Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);


        /*Criando o listener de localização*/
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {


            }

            public void onStatusChanged(String provider, int status, Bundle extras) {

                longitude = location.getLongitude();
                latitude = location.getLatitude();

            }

            public void onProviderEnabled(String provider) {
                longitude = location.getLongitude();
                latitude = location.getLatitude();
            }

            public void onProviderDisabled(String provider) {}
        };



        /*As frescuras de teste de implementação*/


        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);


        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        Location loc = locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER);

        NotificationApp notificationApp = new NotificationApp(getView());
        notificationApp.notification();


       /* Intent intent = new Intent(getContext(),ServiceThread.class);
        getContext().startService(intent);
*/

    }






    /*Override do metodo onMapClick..*/
    @Override
    public void onMapClick(LatLng latLng) {

        if(!buttomAddMarkerVisivel){

            if(!markerDialog.hasNearby(marcadores,latLng)) {
              markerDialog.dialogAdd2(latLng, this.getContext(), mMap, geocoder2, marcadores);
            }

            else{
                Toast.makeText(getContext(), "TEM MARCADOR AQUI PERTO!!!", Toast.LENGTH_LONG).show();


            }
        }

        else{


            Toast.makeText(getContext(), "Coordenadas: "+latLng.toString(), Toast.LENGTH_LONG).show();
        }

    }


    @Override
    public void notificaticarInteressados(Action action) {
        buttomAddMarkerVisivel = action.getButtomAddMakerClickado();
    }
}
