package desenvolvimentoads.san;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Icon;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Image;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import desenvolvimentoads.san.DAO.MarkerDAO;
import desenvolvimentoads.san.Helper.DateHelper;

public class MapsTerceiro extends SupportMapFragment implements OnMapReadyCallback, GoogleMap.OnMapClickListener, LocationListener{

    private GoogleMap mMap;
    private GoogleMap googleMapFinal;
    private static Circle circle;
    private LatLng inicial = null;

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
            loadMarkers();

            //Estilos de mapas
            //mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            //mMap.setMapStyle();



            googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {

                @Override
                public void onMapLongClick(LatLng arg0) {
                    // TODO Auto-generated method stub
                    inicial = arg0;
                    dialogAdd(arg0);

                }
            });

            googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                @Override
                public View getInfoWindow(Marker marker) {
                    return null;
                }

                @Override
                public View getInfoContents(Marker marker) {
                    LinearLayout info = new LinearLayout(getContext());
                    info.setOrientation(LinearLayout.VERTICAL);

                    ImageView imageView = new ImageView(getContext());
                    imageView.setImageResource(image);

                    TextView title = new TextView(getContext());
                    title.setTextColor(Color.BLACK);
                    title.setGravity(Gravity.CENTER);
                    title.setTypeface(null, Typeface.BOLD);
                    title.setText(marker.getTitle());

                    TextView snippet = new TextView(getContext());
                    snippet.setTextColor(Color.GRAY);
                    snippet.setText(marker.getSnippet());

                    info.addView(imageView);
                    info.addView(title);
                    info.addView(snippet);

                    return info;
                }
            });

            googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {
                    marker.showInfoWindow();
                }
            });

            googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                @Override
                public void onMarkerDragStart(Marker marker) {
                    Snackbar.make(getView(), "Arraste o marcador sobre o mapa para melhor precisão", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }

                @Override
                public void onMarkerDrag(Marker marker) {
                    Snackbar.make(getView(), "Confirme a posição do marcador", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }

                @Override
                public void onMarkerDragEnd(Marker marker) {
                    dialogDrag(marker);
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
        LatLng caragua = new LatLng(-23.6202800, -45.4130600);
        zoomMarker(caragua,googleMapFinal);
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


    private void dialogDrag(final Marker marker) {
        //LayoutInflater é utilizado para inflar nosso layout em uma view.
        //-pegamos nossa instancia da classe
        LayoutInflater li = LayoutInflater.from(getContext());

        //inflamos o layout alerta.xml na view
        final View view = li.inflate(R.layout.confirm_drag, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Alterar Posição");
        final Button confirm = (Button)  view.findViewById(R.id.btConfirm);

        Button cancel = (Button)  view.findViewById(R.id.btCancel);

        confirm.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                alerta.dismiss();
                MarkerDAO markerDAO = MarkerDAO.getInstance(getContext());
                List<desenvolvimentoads.san.Model.Marker> markers = markerDAO.getPerMarker(marker.getId());
                desenvolvimentoads.san.Model.Marker markerClass = markers.get(0);

                markerClass.setDraggable(false);
                markerClass.setLatitude(marker.getPosition().latitude);
                markerClass.setLongitude(marker.getPosition().longitude);
                markerClass.setTitle(getStreet(marker.getPosition()));
                markerDAO.update(markerClass);

                Toast.makeText(getActivity(), "Draggable: "+markerClass.isDraggable(), Toast.LENGTH_LONG).show();
                marker.setDraggable(markerClass.isDraggable());
                marker.setTitle(markerClass.getTitle());


                removeCircle();
                CreateCircle(marker);
                Snackbar.make(getView(), "Confirmado posição!", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                alerta.dismiss();
            }
        });
        builder.setView(view);
        alerta = builder.create();
        alerta.show();
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

                desenvolvimentoads.san.Model.Marker markerClass = new desenvolvimentoads.san.Model.Marker(1,latLng.latitude, latLng.longitude,getStreet(latLng), getTimeLive(image), image);
                MarkerDAO markerDAO = MarkerDAO.getInstance(getContext());


                Marker marker = googleMapFinal.addMarker(new MarkerOptions()
                        .position(new LatLng(markerClass.getLatitude(), markerClass.getLongitude()))
                        .title(markerClass.getTitle())
                        .draggable(markerClass.isDraggable())
                        .icon(BitmapDescriptorFactory.fromResource(markerClass.getImage()))
                        .visible(markerClass.isStatus())
                );
                markerClass.setIdMarker(marker.getId());
                markerDAO.saveMarker(markerClass);
                marker.setDraggable(markerClass.isDraggable());
                CreateCircle(marker);
                zoomMarker(marker.getPosition(), googleMapFinal);
                new LiveThread().liveMarkerCount( marker, markerClass, getActivity());
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

    public void CreateCircle (Marker marker){
        circle = mMap.addCircle(new CircleOptions()
                .center(new LatLng(marker.getPosition().latitude, marker.getPosition().longitude))
                .radius(10)
                .strokeWidth(10)
                .strokeColor(Color.argb(128, 173,216,230))
                .fillColor(Color.argb(24, 30,144,255))
                .clickable(true));

        googleMapFinal.setOnCircleClickListener(new GoogleMap.OnCircleClickListener() {

            @Override
            public void onCircleClick(Circle circle) {
                // Flip the r, g and b components of the circle's
                // stroke color.
                int strokeColor = circle.getStrokeColor() ^ 0x00ffffff;
                circle.setStrokeColor(strokeColor);
            }
        });
    }

    public static void removeCircle(){
        if (circle != null)
            circle.remove();
    }

    public void loadMarkers(){
        MarkerDAO markerDAO = MarkerDAO.getInstance(getContext());
        List<desenvolvimentoads.san.Model.Marker> markers = markerDAO.getAllMarkersActive();
        List<Marker> listMarkers = new ArrayList<Marker>();
        int count = 0;
        for (desenvolvimentoads.san.Model.Marker m : markers ){
            Marker marker = googleMapFinal.addMarker(new MarkerOptions()
                    .position(new LatLng(m.getLatitude(), m.getLongitude()))
                    .title(m.getTitle())
                    .draggable(m.isDraggable())
                    .icon(BitmapDescriptorFactory.fromResource(m.getImage()))
                    .visible(m.isStatus())
            );
            listMarkers.add(marker);
        }
        for (Marker m: listMarkers){
            new LiveThread().liveMarkerCount(m, markers.get(count), getActivity());
            count++;
        }
    }

    public int getTimeLive(int idImage){
        int timeLife = 0;
        switch (idImage){
            case R.mipmap.ic_maker_amarelo_star:
                timeLife = 10;
                break;
            case R.mipmap.ic_maker_laranja_star:
                timeLife = 20;
                break;
            case R.mipmap.ic_maker_vermelho_star:
                timeLife = 30;
                break;
        }

        return timeLife;
    }
}
