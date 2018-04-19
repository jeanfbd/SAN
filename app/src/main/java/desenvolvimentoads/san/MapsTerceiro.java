package desenvolvimentoads.san;


import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import desenvolvimentoads.san.DAO.ConfigFireBase;
import desenvolvimentoads.san.Marker.MarkerDialog;
import desenvolvimentoads.san.Marker.MarkerTag;
import desenvolvimentoads.san.Observer.Action;
import desenvolvimentoads.san.Observer.ActionObserver;

public class MapsTerceiro extends SupportMapFragment implements OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, ActionObserver {

    private GoogleMap mMap;

    private static final int MY_PERMISSION_REQUEST_CODE = 2508;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 1991;
    private static final String TAG = "MapsTerceiro";

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    private static int UPDATE_INTERVAL = 5000;
    private static int FATEST_INTERVAL = 3000;
    private static int DISPLACEMENT = 10;

    private FirebaseAuth mAuth = com.google.firebase.auth.FirebaseAuth.getInstance();
    private FirebaseUser currentUser = mAuth.getCurrentUser();
   // private String userId = currentUser.getUid();
    String userId = "123";
    private DatabaseReference mDatabaseReference;
    private FirebaseDatabase firebaseDatabase;

    GeoFire geoFire;
    GeoQuery geoQuery;
    GeoQuery geoQuery2;
    Geocoder geocoder2;
    Marker mCurrent;
    Marker marcador = null;
    LatLng newLatLng;

    private long timestamp;

    private boolean buttomAddMarkerVisivel;
    private String itemId;
    Action action = Action.getInstance();

    MarkerTag myCurrentLocationTag;
    /* Classe com os metodos dos markers */
    MarkerDialog markerDialog = new MarkerDialog();

    public static HashMap<String, Marker> markerHashMap = new HashMap<>();
    public static HashMap<String, GeoQueryEventListener> notificationHashMap = new HashMap<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getMapAsync(this);

        mDatabaseReference = ConfigFireBase.getFirebase();
        firebaseDatabase = ConfigFireBase.getFirebaseDatabase();
        geoFire = new GeoFire(firebaseDatabase.getReferenceFromUrl("https://websan-46271.firebaseio.com/MyLocation/"));
        setUpLocation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (checkPlayService()) {
                        buildGoogleApiClient();
                        createLocationRequest();
                        displayLocation();
                    }
                }
                break;
        }
    }

    private void setUpLocation() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            //Request runtime permission
            ActivityCompat.requestPermissions(getActivity(), new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, MY_PERMISSION_REQUEST_CODE);
        } else {
            if (checkPlayService()) {
                buildGoogleApiClient();
                createLocationRequest();
                displayLocation();
            }
        }
    }

    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);


        if (mLastLocation != null) {
            final double latitude = mLastLocation.getLatitude();
            final double longitude = mLastLocation.getLongitude();



            if(myCurrentLocationTag == null){
                myCurrentLocationTag = new MarkerTag();
                myCurrentLocationTag.setId("MyCurrentTag");
                myCurrentLocationTag.setValidate(true);
                myCurrentLocationTag.setLatitude(mLastLocation.getLatitude());
                myCurrentLocationTag.setLongitude(mLastLocation.getLongitude());
            }else{
                myCurrentLocationTag.setLatitude(mLastLocation.getLatitude());
                myCurrentLocationTag.setLongitude(mLastLocation.getLongitude());
            }

            //Update to Firebase
            geoFire.setLocation(userId, new GeoLocation(latitude, longitude), new GeoFire.CompletionListener() {
                @Override
                public void onComplete(String key, DatabaseError error) {
                    //addMarker
                    if (mCurrent != null) {
                        mCurrent.setPosition(new LatLng(latitude, longitude));
                    } else {
                        mCurrent = mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(latitude, longitude))
                                .title("Você")
                                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_my_location)));
                    }
                    //Move Camera to this Position
                    mCurrent.setTag(myCurrentLocationTag);
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 16.0f));

                }
            });
            Log.d(TAG, String.format("Sua Localização mudou: %f/%f", latitude, longitude));
        } else {
            Log.d(TAG, "Não foi possível obter a ultima localização");
        }
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);

    }

    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();

    }

    private boolean checkPlayService() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getContext());
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, getActivity(), PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(getContext(), "Dispositivo não suportado", Toast.LENGTH_SHORT).show();

            }
            return false;
        }
        return true;
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
                //.makeText(getContext(), "Clickou", Toast.LENGTH_LONG).show();
            }
        });

        /*Criando o listener do click longo*/
        googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {

            @Override
            public void onMapLongClick(LatLng arg0) {
                // TODO Auto-generated method stub

                if (mLastLocation != null) {
                    newLatLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                    if (action.isReportNotSelected()) {
                            /* Verifico a proximidade do user com o local que ele vai por o marcador*/
                        if (markerDialog.closeToMe(newLatLng, arg0)) {
                                  /* Verifico se existe algum marcador proximo */
                            if (!markerDialog.hasNearby(markerHashMap, arg0)) {
                                markerDialog.dialogAdd2(arg0, getContext(), mMap, geocoder2, markerHashMap);
//                                itemId = action.getItemId();
                                Log.d(TAG, "Action ID: "+itemId);


                            } else {
                                Toast.makeText(getContext(), "TEM MARCADOR AQUI PERTO!!!", Toast.LENGTH_LONG).show();
                            }

                        } else {
                            Toast.makeText(getContext(), "Você precisa estar proximo do ponto a ser marcado", Toast.LENGTH_LONG).show();
                        }
                    }
                } else {
                    Toast.makeText(getContext(), " Não foram encontrados os dados da ultima localização", Toast.LENGTH_LONG).show();
                }
            }
        });

        /*Criando o listener click on marker*/
        googleMap = markerDialog.setMarkerClick(googleMap, getContext(), markerHashMap);


        /*Criando o listener do drag*/
        googleMap = markerDialog.setListenerDragDiag(googleMap, marcador, getContext(), getView());

        mMap = googleMap;

        /*Checkando a permissão dos acessos, vulgo frescura do Android..*/
        if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.setOnMapClickListener(this);
            mMap.getUiSettings().setMapToolbarEnabled(false);
            mMap.getUiSettings().setZoomControlsEnabled(true);

            getRaioFirebase(-23.6202800, -45.4130600, 5.00);

            /*Listener responsavel adicionar o botão da localização*/
            mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                @Override
                public boolean onMyLocationButtonClick() {
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude())));
                    return false;
                }
            });

        } else {
            //Request runtime permission
            ActivityCompat.requestPermissions(getActivity(), new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, MY_PERMISSION_REQUEST_CODE);
        }


        LatLng dangerous_area = new LatLng(-23.6202800, -45.4130600);
        mMap.addCircle(new CircleOptions()
                .center(dangerous_area)
                .radius(500)
                .strokeColor(Color.BLUE)
                .fillColor(0x22000FF)
                .strokeWidth(5.0f)

        );

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-23.6202800, -45.4130600), 15.0f));
    }

    @Override
    public void onMapClick(LatLng latLng) {
        if (mLastLocation != null) {
            newLatLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            //Toast.makeText(getContext(), "Presta atençao " + latLng.toString(), Toast.LENGTH_LONG).show();
            getRaioFirebase(mLastLocation.getLatitude(), mLastLocation.getLongitude(), 5.0);


                    /*Aqui é normal*/


            if (!buttomAddMarkerVisivel) {


              /* Verifico a proximidade do user com o local que ele vai por o marcador*/
                if (markerDialog.closeToMe(newLatLng, latLng)) {
                                  /* Verifico se existe algum marcador proximo */
                    if (!markerDialog.hasNearby(markerHashMap, latLng)) {
                        markerDialog.dialogAdd2(latLng, this.getContext(), mMap, geocoder2, markerHashMap);
                        Log.i(TAG, "onMapClick: LAT: " + latLng.latitude + " LOG: " + latLng.longitude);

                    } else {
                        Toast.makeText(getContext(), "TEM MARCADOR AQUI PERTO!!!", Toast.LENGTH_LONG).show();

                    }

                } else {
                    Toast.makeText(getContext(), " É necessario estar proximo ao local a ser marcado", Toast.LENGTH_LONG).show();

                }

            } else {


                Toast.makeText(getContext(), "Coordenadas: " + latLng.toString(), Toast.LENGTH_LONG).show();
            }


        } else {
            Toast.makeText(getContext(), " Não foram encontrados os dados da ultima localização", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        getRaioFirebase(mLastLocation.getLatitude(), mLastLocation.getLongitude(), 5.0);
        displayLocation();


        if(geoQuery2 != null){
            geoQuery2.removeAllListeners();

        }

        notificationHashMap.clear();
        notificationHashMap = new HashMap<>();
        geoQuery2 = null;
        geoQuery2 = geoFire.queryAtLocation(new GeoLocation(location.getLatitude(),location.getLongitude() ), 0.5);
        Log.i("teste","geoqeury mudou?");


    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationsUpdates();

    }

    private void startLocationsUpdates() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public void sendNotification(String title, String content, Context context) {
        Notification.Builder builder = new Notification.Builder(context)
                .setSmallIcon(R.mipmap.ic_maker_vermelho)
                .setContentTitle(title)
                .setContentText(content);
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = new Intent(context, MapsQuarto.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        builder.setContentIntent(contentIntent);
        Notification notification = builder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.defaults |= Notification.DEFAULT_SOUND;

        manager.notify(new Random().nextInt(), notification);
    }

    @Override
    public void notificaticarInteressados(Action action) {
        buttomAddMarkerVisivel = action.getButtomAddMakerClickado();

        if(action.getItemId() != null){

            if(itemId != null){

                if(!itemId.equals(action.getItemId())){
                    itemId = action.getItemId();
                    Log.i("teste","itemid status :"+itemId);
                    LatLng newLatLangTemp =  markerHashMap.get(itemId).getPosition();
                    createListenerNotification(itemId, newLatLangTemp.latitude, newLatLangTemp.longitude);


                }


            }else{
                itemId = action.getItemId();
                Log.i("teste","itemid status :"+itemId);
                LatLng newLatLangTemp =  markerHashMap.get(itemId).getPosition();
                createListenerNotification(itemId, newLatLangTemp.latitude, newLatLangTemp.longitude);



            }




        }




    }

    public void getRaioFirebase(Double lat, Double lng, Double radius) {
        getServerTime();
        if (geoQuery != null){
            geoQuery.removeAllListeners();
        }
        mDatabaseReference = ConfigFireBase.getFirebase();
        firebaseDatabase = ConfigFireBase.getFirebaseDatabase();
        GeoFire geoFire2 = new GeoFire(firebaseDatabase.getReferenceFromUrl("https://websan-46271.firebaseio.com/marker_location/"));

        final GeoQuery geoQuery = geoFire2.queryAtLocation(new GeoLocation(lat, lng), radius);

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(final String key, GeoLocation location) {
                mDatabaseReference = ConfigFireBase.getFirebase();
                mDatabaseReference.child("Marker").child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot != null && dataSnapshot.getValue() != null) {
                            double latitude = (double) dataSnapshot.child("position").child("latitude").getValue();
                            double longitude = (double) dataSnapshot.child("position").child("longitude").getValue();
                            LatLng latLng = new LatLng(latitude, longitude);
                            if (dataSnapshot.child("fim").getValue() != null) {
                                Log.i(TAG, "Servertime: "+getServerTime());
                                Log.i(TAG, "MarkerTime: "+ dataSnapshot.child("fim").getValue());
                                Log.i(TAG, "Condição: "+(getServerTime() < (Long) dataSnapshot.child("fim").getValue()));
                                if (getServerTime() < (Long) dataSnapshot.child("fim").getValue()) {
                                    Log.i(TAG, "onDataChange: NÃO EXISTE DENUNCIA");
                                    if (markerHashMap.get(key) == null) {
                                        Log.i(TAG, "Entrou Criar: " + key);
                                        markerDialog.addDataArrayFirebase(latLng, getContext(), mMap, geocoder2, markerHashMap, key, dataSnapshot.child("Validar").child(userId).exists());
                                        createListenerNotification(key, latitude, longitude);
                                    }
                                } else {
                                    if (markerHashMap.get(key) != null) {
                                        Log.i(TAG, "Entrou Remover: " + key);
                                        MarkerDialog.deleteDataArrayFirebase(markerHashMap, key);
                                        Log.d(TAG, "Notifications: "+notificationHashMap.size());
                                        Log.i("teste" ,"key : " + key);
                                        removeListenerNotification(key);
                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    public void createListenerNotification(String idMarker, double latitude, double longitude) {
        //Add GeoQuery
       //  geoQuery = geoFire.queryAtLocation(new GeoLocation(latitude, longitude), 0.5);

        GeoQueryEventListener notificationListener = new GeoQueryEventListener() {
            public void onKeyEntered(String key, GeoLocation location) {
                if (mCurrent != null) {
                    mCurrent.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_my_location_danger));
                }
                sendNotification("SAN", String.format("%s Existe um ponto de alagamento próximo", key), getContext());
                Log.d("ENTROU", "DENTRO");

            }

            @Override
            public void onKeyExited(String key) {
                if (mCurrent != null) {
                    mCurrent.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_my_location_fine));
                }
                sendNotification("SAN", String.format("%s Fora do ponto de alagamento", key), getContext());
                Log.d("SAIU", "FORA");
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                Log.e("Error", "" + error);
            }
        };
        notificationHashMap.put(idMarker, notificationListener);
        geoQuery2.addGeoQueryEventListener(notificationListener);
              Log.d(TAG, "idMarker Listener: "+idMarker);


        Log.i("teste","hashmap notification populado ? "+notificationHashMap.size());

        Log.i("teste","geoquery string add "+geoQuery2.toString());
        Log.i("teste","geoquery string add listener"+notificationListener.toString());
        for (String testekey : notificationHashMap.keySet()) {
            Log.i("teste","key -> "+testekey);
        }

    }


    public void removeListenerNotification(final String idMarker) {
        if (notificationHashMap != null){
            Log.i("teste","idMarker -> "+idMarker);
            Log.i("teste","hashmap notification "+notificationHashMap.size());
            for (String testekey : notificationHashMap.keySet()) {
                Log.i("teste","key -> "+testekey);
            }

            Log.i("teste","geoquery string remover "+geoQuery2.toString());
            Log.i("teste","geoquery string remove listener"+notificationHashMap.get(idMarker).toString());
            geoQuery2.removeGeoQueryEventListener(notificationHashMap.get(idMarker));


        }

    }

    public Long getServerTime() {
        mDatabaseReference = ConfigFireBase.getFirebase();
        final Long[] timestampServer = new Long[1];
        mDatabaseReference.child("current_timestamp").setValue(ServerValue.TIMESTAMP);
        mDatabaseReference.child("current_timestamp").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                timestampServer[0] = (Long) dataSnapshot.getValue();
                timestamp = timestampServer[0];
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return timestamp;
    }
}
