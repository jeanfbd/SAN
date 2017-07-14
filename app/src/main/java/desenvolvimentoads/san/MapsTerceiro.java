package desenvolvimentoads.san;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Icon;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Image;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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

import java.io.IOException;
import java.util.List;

public class MapsTerceiro extends SupportMapFragment implements OnMapReadyCallback, GoogleMap.OnMapClickListener, LocationListener{

    private GoogleMap mMap;
    private GoogleMap googleMapFinal;
    Marker marcador = null;

    private LocationManager locationManager;

    private static final String TAG = "ExemploProFragmentV2";


    private static final int MY_PERMISSIONS_ACCESS_COARSE_LOCATION = 0;

    private AlertDialog alerta;
    private int image = R.mipmap.ic_maker_amarelo_star;

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
            googleMapFinal = googleMap;
            mMap = googleMap;

            mMap.setOnMapClickListener(this);

            mMap.getUiSettings().setZoomControlsEnabled(true);

            mMap.setMinZoomPreference(10);

//            mMap.setMapStyle();



            googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {

                @Override
                public void onMapLongClick(LatLng arg0) {
                    // TODO Auto-generated method stub
                    dialogAdd(arg0);

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
        LatLng caragua = new LatLng(-23.6202800, -45.4130600);

        MarkerOptions marker = new MarkerOptions();
        marker.position(caragua);
        Geocoder geocoder = new Geocoder(this.getContext());
        try {
            List myLocation = geocoder.getFromLocation(caragua.latitude, caragua.longitude, 1);
            Address address = (Address)myLocation.get(0);
            marker.title(address.getLocality());
            marker.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_maker_amarelo));
        } catch (IOException e) {
            e.printStackTrace();
        }



        mMap.addMarker(marker);
        zoomMarker(marker.getPosition(), mMap);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        Toast.makeText(getContext(), "Coordenadas: "+latLng.toString(), Toast.LENGTH_LONG).show();

    }

    @Override
    public void onLocationChanged(Location location) {
        Toast.makeText(getActivity(), "Posição Alterada", Toast.LENGTH_LONG).show();
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


        private void dialogAdd(final LatLng latLng) {

            //LayoutInflater é utilizado para inflar nosso layout em uma view.
            //-pegamos nossa instancia da classe
            LayoutInflater li = LayoutInflater.from(getContext());

            //inflamos o layout alerta.xml na view
            final View view = li.inflate(R.layout.dialogadd, null);

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Criar Marcador");
            final Button confirm = (Button)  view.findViewById(R.id.btConfirm);

            Button cancel = (Button)  view.findViewById(R.id.btCancel);

            confirm.setOnClickListener(new View.OnClickListener() {
                public void onClick(View arg0) {
                    alerta.dismiss();


                    MarkerOptions markerOption = new MarkerOptions();
                    markerOption.position(latLng).icon(BitmapDescriptorFactory.fromResource(image));
                    marcador = googleMapFinal.addMarker(markerOption);
                    marcador.setDraggable(true);
                    marcador.setTitle(getStreet(latLng));
                    zoomMarker(latLng, googleMapFinal);


                }
            });

            cancel.setOnClickListener(new View.OnClickListener() {
                public void onClick(View arg0) {

                    alerta.dismiss();



                }
            });

            confirm.setVisibility(View.INVISIBLE);


            ImageView amarelo = (ImageView) view.findViewById(R.id.yellow_star);

            amarelo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    image = R.mipmap.ic_maker_amarelo_star;
                    confirm.setVisibility(View.VISIBLE);
                }
            });


            ImageView laranja = (ImageView) view.findViewById(R.id.orange_star);

            laranja.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    image = R.mipmap.ic_maker_laranja_star;
                    confirm.setVisibility(View.VISIBLE);

                }
            });

            ImageView vermelho = (ImageView) view.findViewById(R.id.red_star);

            vermelho.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    image = R.mipmap.ic_maker_vermelho_star;
                    confirm.setVisibility(View.VISIBLE);

                }
            });

            builder.setView(view);

            alerta = builder.create();

            alerta.show();







        }


    public void zoomMarker(LatLng arg0, GoogleMap googleMap){
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(arg0)               // Sets the center of the map
                .zoom(17)                   // Sets the zoom
                .bearing(90)                // Sets the orientation of the camera to east
                .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    public String getStreet(LatLng location) {
        String street = "";
        //Classe que fornece a localização da cidade
        Geocoder geocoder = new Geocoder(this.getContext());
        List myLocation = null;

        try {
            //Obtendo os dados do endereço
            myLocation = geocoder.getFromLocation(location.latitude, location.longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Log.d("My Location", myLocation.toString());
        if (myLocation != null && myLocation.size() > 0) {
            Address address = (Address) myLocation.get(0);
            //Pega nome da cidade
            String city = address.getLocality();
            //Pega nome da rua
            street = address.getAddressLine(0);
        }else{
            street = "Endereço não encontrado";
        }

        return street;
    }
}
