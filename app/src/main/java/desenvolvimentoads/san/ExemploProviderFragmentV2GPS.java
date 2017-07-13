package desenvolvimentoads.san;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

public class ExemploProviderFragmentV2GPS extends SupportMapFragment implements OnMapReadyCallback, GoogleMap.OnMapClickListener, LocationListener{

    private GoogleMap mMap;

    Marker test = null;

    private LocationManager locationManager;

    private static final String TAG = "ExemploProFragmentV2";


    private static final int MY_PERMISSIONS_ACCESS_COARSE_LOCATION = 0;

    private AlertDialog alerta;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getMapAsync(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        //Ativa o GPS
        try {
            locationManager = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,0,this);
        }catch (SecurityException ex){
            Log.e(TAG, "Erro", ex);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        locationManager = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);
        locationManager.removeUpdates(this);

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
    public void onMapReady(final GoogleMap googleMap) {

        try {

            locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

            mMap = googleMap;

            mMap.setOnMapClickListener(this);

            mMap.getUiSettings().setZoomControlsEnabled(true);



            googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {

                @Override
                public void onMapLongClick(LatLng arg0) {
                    // TODO Auto-generated method stub
                    dialogAdd();

                        MarkerOptions markerOption = new MarkerOptions();
                        markerOption.position(arg0).icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_maker_amarelo));
                        test = googleMap.addMarker(markerOption);
                        LatLng position = test.getPosition();
                        test.setDraggable(true);

                }
            });


            if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                            PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);

                if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                        android.Manifest.permission.ACCESS_COARSE_LOCATION)) {

                    // Show an expanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.

                } else {

                    // No explanation needed, we can request the permission.

                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                            MY_PERMISSIONS_ACCESS_COARSE_LOCATION);

                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }
            }
        }catch (SecurityException ex){
            Log.e(TAG, "Erro", ex);
        }

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

    @Override
    public void onLocationChanged(Location location) {
        Toast.makeText(getActivity(), "Posição Alterada", Toast.LENGTH_LONG).show();

        newMarker(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Toast.makeText(getActivity(), "O Status do Provider foi Alterado", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(getActivity(), "Provider Habilitado", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(getActivity(), "Provider Desabilitado", Toast.LENGTH_LONG).show();
    }

    private void newMarker(Location location){

        LatLng newPosition = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions marker = new MarkerOptions();

        //Classe que fornece a localização da cidade
        Geocoder geocoder = new Geocoder(this.getContext());
        List myLocation = null;

        try {
            //Obtendo os dados do endereço
            myLocation = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Log.d("My Location", myLocation.toString());
        if ( myLocation != null && myLocation.size() > 0) {
            Address address = (Address)myLocation.get(0);
            //Pega nome da cidade
            String city = address.getLocality();
            //Pega nome da rua
            String street = address.getAddressLine(0);

            marker.position(newPosition);
            marker.title(street);
            marker.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_maker_amarelo_star));

            mMap.addMarker(marker);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
        } else {
            Log.d("geolocation", "endereço não localizado");
        }
    }

    private void dialogAdd() {
        //LayoutInflater é utilizado para inflar nosso layout em uma view.
        //-pegamos nossa instancia da classe
        LayoutInflater li = LayoutInflater.from(getContext());

        //inflamos o layout alerta.xml na view
        View view = li.inflate(R.layout.dialogadd, null);
        //definimos para o botão do layout um clickListener
        view.findViewById(R.id.btConfirm).setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                alerta.dismiss();
            }
        });

        view.findViewById(R.id.btCancel).setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                alerta.dismiss();
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Titulo");
        builder.setView(view);
        alerta = builder.create();
        alerta.show();
    }
}
