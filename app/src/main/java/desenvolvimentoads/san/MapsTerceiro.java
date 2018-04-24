package desenvolvimentoads.san;


import android.Manifest;
import android.app.Activity;
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
import com.google.android.gms.maps.model.Circle;
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
import desenvolvimentoads.san.Observer.GeoSingleton;
import desenvolvimentoads.san.notification.NotificationApp;

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
    GeoSingleton geoSingleton = GeoSingleton.getInstance();
    Context mContext;


    GeoQuery geoQuery2;
    Geocoder geocoder2;
    Marker mCurrent;
    Marker marcador = null;
    LatLng newLatLng;
    double myLat = 0;
    double myLong = 0;

    private long timestamp;

    private boolean buttomAddMarkerVisivel;
    private String itemId;
    Action action = Action.getInstance();

    MarkerTag myCurrentLocationTag;
    /* Classe com os metodos dos markers */
    MarkerDialog markerDialog = new MarkerDialog();

    public static HashMap<String, Marker> markerHashMap = new HashMap<>();
    public static HashMap<String, GeoQueryEventListener> notificationHashMap = new HashMap<>();
    public static final int REQUEST_PERMISSION_LOCATION = 10;

    GeoQueryEventListener notificationListener;
    public static HashMap<String, String> alertHashMap = new HashMap<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getMapAsync(this);

        mContext = getContext();
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
                        //displayLocation();
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
        if (false) {

        }
        if (ActivityCompat.checkSelfPermission((Activity) mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission((Activity) mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            //Request runtime permission
            ActivityCompat.requestPermissions(getActivity(), new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, MY_PERMISSION_REQUEST_CODE);
        } else {

            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);


            if (mLastLocation != null) {
                final double latitude = mLastLocation.getLatitude();
                final double longitude = mLastLocation.getLongitude();


                if (myCurrentLocationTag == null) {
                    myCurrentLocationTag = new MarkerTag();
                    myCurrentLocationTag.setId("MyCurrentTag");
                    myCurrentLocationTag.setValidate(true);
                    myCurrentLocationTag.setLatitude(mLastLocation.getLatitude());
                    myCurrentLocationTag.setLongitude(mLastLocation.getLongitude());
                } else {
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


                        if (alertHashMap != null) {

                            if (alertHashMap.size() == 0) {
                                if (mCurrent != null) {
                                    mCurrent.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_my_location_fine));
                                }
                            } else {
                                if (mCurrent != null) {
                                    mCurrent.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_my_location_danger));
                                }


                            }
                        }

                    }
                });
                Log.d(TAG, String.format("Sua Localização mudou: %f/%f", latitude, longitude));
            } else {
                Log.d(TAG, "Não foi possível obter a ultima localização");
            }


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
                                Log.d(TAG, "Action ID: " + itemId);


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
                    if (mLastLocation != null) {
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude())));

                    }


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
        mMap.clear();
        rebuildMap();

    }

    public void rebuildGeoQuery2() {

        if (alertHashMap != null) {
            alertHashMap.clear();

            if (mCurrent != null) {
                mCurrent.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_my_location));
            }

            if (mLastLocation != null) {


                for (String key : markerHashMap.keySet()) {
                    Log.i("teste", "rebuildGeoQuery2 marker hash key " + key);
                    LatLng latLng = new LatLng(markerHashMap.get(key).getPosition().latitude, markerHashMap.get(key).getPosition().longitude);
                    if (markerDialog.closeToMeToHash(newLatLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), latLng, alertHashMap, key, 0.001)) {
                        if (mCurrent != null) {
                            mCurrent.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_my_location_danger));
                        }

                    }

                }

            }


            if (alertHashMap.size() == 0) {
                if (mCurrent != null) {
                    mCurrent.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_my_location_fine));
                }
            } else {
                if (mCurrent != null) {
                    mCurrent.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_my_location_danger));
                }

            }


        }

    }


    public void rebuildMap() {
        Log.i("teste", "inicio rebuilding...");

        rebuildGeoQuery2();

        Marker marker1;
        MarkerOptions markerOption;
        if (markerHashMap.size() >= 1) {

            for (Map.Entry<String, Marker> markTemp : markerHashMap.entrySet()) {
                Log.i("teste", "tag...");

                Circle circle;
                markerOption = new MarkerOptions();
                MarkerTag tagTemp = (MarkerTag) markTemp.getValue().getTag();
                LatLng latLng = tagTemp.getPosition();

                //     Log.i("restart","tag..."+tag.getPosition());
                markerOption.position(latLng).icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_maker_vermelho_star));

                circle = mMap.addCircle(new CircleOptions()
                        .center(tagTemp.getPosition())
                        .radius(500)
                        .strokeWidth(10)
                        .strokeColor(Color.argb(128, 173, 216, 230))
                        .fillColor(Color.argb(24, 30, 144, 255))
                        .clickable(true));

                mMap.setOnCircleClickListener(new GoogleMap.OnCircleClickListener() {

                    @Override
                    public void onCircleClick(Circle circle) {
                        // Flip the r, g and b components of the circle's
                        // stroke color.
                        int strokeColor = circle.getStrokeColor() ^ 0x00ffffff;
                        circle.setStrokeColor(strokeColor);
                    }
                });
                MarkerTag tag = new MarkerTag(tagTemp.getPosition().latitude, tagTemp.getPosition().longitude, circle, tagTemp.getValidate());
                tag.setId(tagTemp.getId());
                if (tag.getValidate()) {
                    tag.getCircle().setStrokeColor(Color.argb(128, 2, 158, 90));
                } else {
                    tag.getCircle().setStrokeColor(Color.argb(128, 224, 158, 90));
                }
                marker1 = markTemp.getValue();
                marker1.remove();

                marker1 = mMap.addMarker(markerOption);
                marker1.setTag(tag);
                markerHashMap.put(tag.getId(), marker1);

            }

            Log.i("teste", "foi restaurado.." + markerHashMap.size());


        }

        markerOption = new MarkerOptions();
        //   MarkerTag tag =( MarkerTag) markTemp.getValue().getTag();
        //     Log.i("restart","tag..."+tag.getPosition());
        markerOption.position(new LatLng(21.22, 22)).icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_maker_vermelho_star));

        marker1 = mMap.addMarker(markerOption);

        Log.i("teste", "fim rebuilding...");


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
        Log.i("teste", "markerhashmap size : " + markerHashMap.size());

        mLastLocation = location;

        if (mLastLocation != null) {
            getRaioFirebase(mLastLocation.getLatitude(), mLastLocation.getLongitude(), 5.0);
            displayLocation();

            myLat = location.getLatitude();
            myLong = location.getLongitude();

            if (mCurrent != null) {
                mCurrent.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_my_location));
            }


        }


        rebuildGeoQuery2();


    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // displayLocation();
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

    // public void sendNotification(String title, String content, Context context) {
    public void sendNotification() {

        /*Notificação*/
        //  NotificationApp notificationApp = new NotificationApp(getView());
        //  notificationApp.notification();

     /*

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

    */
    }


    @Override
    public void notificaticarInteressados(Action action) {
        buttomAddMarkerVisivel = action.getButtomAddMakerClickado();

        if (action.getItemId() != null) {

            if (itemId != null) {

                if (mLastLocation != null) {

                    if (!itemId.equals(action.getItemId())) {
                        itemId = action.getItemId();
                        Log.i("teste", "itemid status :" + itemId);

                        if (markerHashMap.containsKey(itemId)) {
                            Log.i("teste", "itemid status contains :" + itemId);
                            LatLng newLatLangTemp = markerHashMap.get(itemId).getPosition();

                            if (markerDialog.closeToMeToHash(newLatLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), newLatLangTemp, alertHashMap, itemId, 0.001)) {
                                if (mCurrent != null) {
                                    mCurrent.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_my_location_danger));
                                }

                            }


                        }
                    }

                }


            } else {
                itemId = action.getItemId();
                Log.i("teste", "itemid status :" + itemId);

                if (mLastLocation != null) {
                    if (markerHashMap.containsKey(itemId)) {
                        Log.i("teste", "itemid status contains :" + itemId);
                        LatLng newLatLangTemp = markerHashMap.get(itemId).getPosition();


                        if (markerDialog.closeToMeToHash(newLatLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), newLatLangTemp, alertHashMap, itemId, 0.001)) {
                            if (mCurrent != null) {
                                mCurrent.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_my_location_danger));
                            }

                        }

                    }

                }


            }


        }


    }


    public void getRaioFirebase(Double lat, Double lng, Double radius) {
        hashMapClear();
        getServerTime();

        mDatabaseReference = ConfigFireBase.getFirebase();
        firebaseDatabase = ConfigFireBase.getFirebaseDatabase();
        GeoFire geoFire2 = new GeoFire(firebaseDatabase.getReferenceFromUrl("https://websan-46271.firebaseio.com/marker_location/"));

        final GeoQuery geoQuery = geoFire2.queryAtLocation(new GeoLocation(lat, lng), radius);
        if (mLastLocation != null) {
            geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                @Override
                public void onKeyEntered(final String key, GeoLocation location) {
                    mDatabaseReference = ConfigFireBase.getFirebase();
                    mDatabaseReference.child("Marker").child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot != null && dataSnapshot.getValue() != null) {
                                double latitude = (double) dataSnapshot.child("latitude").getValue();
                                double longitude = (double) dataSnapshot.child("longitude").getValue();
                                LatLng latLng = new LatLng(latitude, longitude);
                                if (dataSnapshot.child("fim").getValue() != null) {
                                    Log.i(TAG, "Servertime: " + getServerTime());
                                    Log.i(TAG, "MarkerTime: " + dataSnapshot.child("fim").getValue());
                                    Log.i(TAG, "Condição TEMPO: " + (getServerTime() < (Long) dataSnapshot.child("fim").getValue()));
                                    Log.i(TAG, "Condição DENUNCIA: " + (!dataSnapshot.child("Denunciar").child(userId).exists()));
                                    Log.i(TAG, "Condição VALIDOU: " + (dataSnapshot.child("Validar").child(userId).exists()));
                                    if (getServerTime() < (Long) dataSnapshot.child("fim").getValue() && !dataSnapshot.child("Denunciar").child(userId).exists()) {
                                        Log.i(TAG, "onDataChange: NÃO EXISTE DENUNCIA");
                                        if (markerHashMap.get(key) == null) {
                                            Log.i(TAG, "Entrou Criar: " + key);
                                            markerDialog.addDataArrayFirebase(latLng, getContext(), mMap, geocoder2, markerHashMap, key, dataSnapshot.child("Validar").child(userId).exists());
                                            if (markerDialog.closeToMeToHash(newLatLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), latLng, alertHashMap, key, 0.001)) {
                                                if (mCurrent != null) {
                                                    mCurrent.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_my_location_danger));
                                                }
                                            }
                                        }
                                    } else {
                                        if (markerHashMap.get(key) != null) {
                                            Log.i(TAG, "Entrou Remover: " + key);
                                            MarkerDialog.deleteDataArrayFirebase(markerHashMap, key);
                                            Log.d(TAG, "Notifications: " + notificationHashMap.size());


                                            Log.i("teste", "key : " + key);

                                            if (alertHashMap.containsKey(key)) {
                                                alertHashMap.remove(key);
                                                if (alertHashMap.size() == 0) {
                                                    if (mCurrent != null) {
                                                        mCurrent.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_my_location_fine));
                                                    }
                                                }

                                            }

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

    }

    public void createListenerNotification(String idMarker, double latitude, double longitude) {
        //Add GeoQuery
        //  geoQuery = geoFire.queryAtLocation(new GeoLocation(latitude, longitude), 0.5);
        Log.i("teste", "createListenerNotification Started ");
        notificationListener = new GeoQueryEventListener() {
            public void onKeyEntered(String key, GeoLocation location) {
                if (mCurrent != null) {
                    mCurrent.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_my_location_danger));
                }
                // sendNotification("SAN", String.format("%s Existe um ponto de alagamento próximo", key),getContext());
                sendNotification();
                Log.d("ENTROU", "DENTRO");

            }

            @Override
            public void onKeyExited(String key) {
                if (mCurrent != null) {
                    mCurrent.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_my_location_fine));
                }
                //   sendNotification("SAN", String.format("%s Fora do ponto de alagamento", key), getContext());
                sendNotification();
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
        //  geoQuery2.addGeoQueryEventListener(notificationListener);


        //   Log.i("teste"," Geoquery2 is String  inserted in "+     geoQuery2.toString()+ " idmaker "+idMarker);


        Log.d(TAG, "idMarker Listener added : " + idMarker);


        Log.i("teste", "hashmap notification size  " + notificationHashMap.size());
        Log.i("teste", "hashmap notification  keys list has started ");
        for (String testekey : notificationHashMap.keySet()) {
            Log.i("teste", "key -> " + testekey);
        }


        Log.i("teste", "createListenerNotification has finished ");
    }


    public void removeListenerNotification(final String idMarker) {
        Log.i("teste", "Remove listener started " + notificationHashMap.size());
        if (notificationHashMap != null) {
            Log.i("teste", "hashmap notification " + notificationHashMap.size());
            //Log.i("teste","idMarker a reser removido -> "+idMarker+ " memory listener "+notificationHashMap.get(idMarker).toString());

            for (String testekey : notificationHashMap.keySet()) {
                Log.i("teste", "key dentro do hash -> " + testekey);
            }

            // Log.i("teste","geoquery string remove listener"+notificationHashMap.get(idMarker).toString());

            if (geoQuery2 == null) {
                Log.i("teste", " Geoquery2 is null");


            } else {
                Log.i("teste", " Geoquery2 is not null String " + geoQuery2.toString());
                Log.i("teste", " Geoquery2 is String to remove " + geoQuery2.toString() + " idmaker " + idMarker);

            }

            if (notificationHashMap.get(idMarker) != null) {
                Log.i("teste", "hashmap notification is not a null key ");


            }
            //   geoQuery2.removeGeoQueryEventListener(notificationHashMap.get(idMarker));
            notificationHashMap.remove(idMarker);
            Log.i("teste", "Remove listener finished " + notificationHashMap.size());

        }

    }

    public void hashMapClear() {
        HashMap<String, Marker> markerHashMapTemp = markerHashMap;
        for (Map.Entry<String, Marker> markerTemp : markerHashMapTemp.entrySet()) {
            MarkerTag markerTag = (MarkerTag) markerHashMapTemp.get(markerTemp.getKey()).getTag();
            markerTag.getCircle().remove();
            markerTemp.getValue().remove();
            markerHashMapTemp.get(markerTemp.getKey()).remove();
        }
        markerHashMap.clear();
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
