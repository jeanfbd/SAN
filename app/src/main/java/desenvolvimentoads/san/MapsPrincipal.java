package desenvolvimentoads.san;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;



public class MapsPrincipal extends SupportMapFragment implements OnMapReadyCallback, GoogleMap.OnMapClickListener{

    private GoogleMap mMap;
    static Boolean marcadorON = false;
    Marker marcador = null;
    private LocationManager locationManager;

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

        geocoder2 = new Geocoder(getContext());

        /*Adiciona o listener no infoWindows(tag) do marker*/
        googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                marker.showInfoWindow();
            }
        });

        /*Criando o listener do click longo*/
        googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {

            @Override
            public void onMapLongClick(LatLng arg0) {
                // TODO Auto-generated method stub

                markerDialog.dialogAdd(arg0,getContext(), mMap, geocoder2);

            }
        });


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
      //  LatLng sydney = new LatLng(loc.getLatitude(),loc.getLongitude());
        LatLng sydney = new LatLng(latitude, longitude);



        MarkerOptions marker = new MarkerOptions();
        marker.position(sydney);
        marker.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_maker_amarelo));
        mMap.addMarker(marker);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));




    }






    /*Override do metodo onMapClick..*/
    @Override
    public void onMapClick(LatLng latLng) {
        if(MenuInicial.fab2.isShown()){
            markerDialog.dialogAdd(latLng,this.getContext(), mMap, geocoder2);

        }

        else{
            Toast.makeText(getContext(), "Coordenadas: "+latLng.toString(), Toast.LENGTH_LONG).show();
        }

    }






}
