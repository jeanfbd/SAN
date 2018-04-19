package desenvolvimentoads.san;

/**
 * Created by jeanf on 31/10/2017.
 */

import android.*;
import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Geocoder;

import android.location.Location;

import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.firebase.geofire.core.GeoHash;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import desenvolvimentoads.san.DAO.ConfigFireBase;
import desenvolvimentoads.san.Marker.MarkerDialog;
import desenvolvimentoads.san.Marker.MarkerTag;
import desenvolvimentoads.san.Observer.Action;
import desenvolvimentoads.san.Observer.ActionObserver;
import desenvolvimentoads.san.notification.NotificationApp;


import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import static android.content.ContentValues.TAG;


public class MapsQuarto extends SupportMapFragment implements LocationListener, OnMapReadyCallback, GoogleMap.OnMapClickListener, ActionObserver, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private DatabaseReference mDatabase;
    private GeoFire geoFire2;
    private FirebaseDatabase firebaseDatabase;
 //   private FirebaseAuth mAuth = com.google.firebase.auth.FirebaseAuth.getInstance();
   // private FirebaseUser currentUser = mAuth.getCurrentUser();
 //   private String userId = currentUser.getUid();
    String userId = "123";
    public static HashMap<Marker, String> mHashMap = new HashMap<Marker, String>();
    public static HashMap<String, Marker> markerHashMap = new HashMap<>();
    private static Long timestamp;

    private static final String TAG = "MapsQuarto";

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;

    private static int UPDATE_INTERVAL = 5000;
    private static int FATEST_INTERVAL = 3000;
    private static int DISPLACEMENT = 10;

    MarkerTag myCurrentLocationTag;
    Marker mCurrLocation;
    LatLng newLatLng;


    /*Um int randomico para o callback do metodo de request permission, só vai utilizado caso formos tratar alguma coisa com ele */
    public static final int REQUEST_PERMISSION_LOCATION = 10;

    /*Não sei pq tem que ser essa constante mas é para ser assim para funcionar*/
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;

    Boolean permissionOK = false;

    private GoogleMap mMap;
    Marker marcador = null;
    private boolean buttomAddMarkerVisivel;
    Action action = Action.getInstance();
    /* Esse hashmap é responsavel por controlar o add e remove dos markers pelo Latlng para os identificar*/
    HashMap<LatLng, Marker> marcadores = new HashMap<>();

    /* Classe com os metodos dos markers */
    MarkerDialog markerDialog = new MarkerDialog();

    Geocoder geocoder2;

    /*Iniciando dados estaticos de posição*/
    private double longitude = -45.4130600;
    private double latitude = -23.6202800;

    protected LocationSettingsRequest mLocationSettingsRequest;

    protected Location mCurrentLocation;

    int RQS_GooglePlayServices = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getMapAsync(this);
    }


    public void checkPermission() {

        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this.getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {


            } else {


                ActivityCompat.requestPermissions(this.getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION_LOCATION);


            }


        } else {


        }


    }

    /*Se fosse necessario mudar algo apos as permissões*/
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    permissionOK = true;


                } else {
                    permissionOK = false;

                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    /* Sem isso aqui o frango azéda*/
    public void checkPlayService() {


         /*Se npegar a instancia do googleservice o fused não funciona -.-*/
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int resultCode = googleAPI.isGooglePlayServicesAvailable(getActivity());
        if (resultCode == ConnectionResult.SUCCESS) {
            mGoogleApiClient.connect();
        } else {
            googleAPI.getErrorDialog(getActivity(), resultCode, RQS_GooglePlayServices);
        }

    }

    /* 1 - Construindo a googleApiClient*/
    protected synchronized void buildGoogleApiClient() {
        //Toast.makeText(getContext(), "buildGoogleApiClient", Toast.LENGTH_SHORT).show();
        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    /* 2 - Criando as configurações do locationRequest*/
    protected void createLocationRequest() {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000); //5 seconds
        mLocationRequest.setFastestInterval(3000); //3 seconds
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        //mLocationRequest.setSmallestDisplacement(0.1F); //1/10 meter


    }

    /* 3 - Construindo o location Settings request*/
    protected void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    /* 4 - criando o method que irá checar o status do locationSettings */
    protected void checkLocationSettings() {
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(
                        mGoogleApiClient,
                        mLocationSettingsRequest
                );

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {

            /* 5 Aqui que controla caso o location não estiver pronto*/
            @Override
            public void onResult(LocationSettingsResult locationSettingsResult) {


                final Status status = locationSettingsResult.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:


                        //Toast.makeText(getContext(), "Location is already on.", Toast.LENGTH_SHORT).show();
                        startLocationUpdates();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:

                        //Toast.makeText(getContext(), "Location dialog will be open", Toast.LENGTH_SHORT).show();

                        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {


                            /*Aqui só é chamado quando o user nega a primeira vez, nessa segunda vez o android deixa lançar uma mensagem para o user.*/
                            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                                    Manifest.permission.ACCESS_FINE_LOCATION)) {


                                //     try {
                                // Show the dialog by calling startResolutionForResult(), and check the result
                                // in onActivityResult().

                                //
                                // User selected the Never Ask Again Option Change settings in app settings manually
                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                                alertDialogBuilder.setTitle("Change Permissions in Settings");
                                alertDialogBuilder
                                        .setMessage("" +
                                                "\nClick SETTINGS to Manually Set\n" + "Permissions to use Location")
                                        .setCancelable(false)
                                        .setPositiveButton("SETTINGS", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                                Uri uri = Uri.fromParts("package", getContext().getPackageName(), null);
                                                intent.setData(uri);
                                                startActivityForResult(intent, REQUEST_CHECK_SETTINGS);     // step 6
                                            }
                                        });

                                AlertDialog alertDialog = alertDialogBuilder.create();
                                alertDialog.show();


                            } else {


                                ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION_LOCATION);


                            }


                        } else {


                        }


                        //move to step 6 in onActivityResult to check what action user has taken on settings dialog
                        //    status.startResolutionForResult(getActivity(), REQUEST_CHECK_SETTINGS);
                        //   } catch (IntentSender.SendIntentException e) {

                        //    }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:

                        break;
                }
            }
        });
    }

    /*Aqui que vai ser o callback do status resolution do step 4/5*/
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.i("Teste", "User agreed to make required location settings changes.");
                        startLocationUpdates();
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i("Teste", "User chose not to make required location settings changes.");
                        checkLocationSettings();
                        break;
                }
                break;
        }
    }


    protected void startLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION_LOCATION);

        } else {


            goAndDetectLocation();
        }

    }


    public void goAndDetectLocation() {

        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION_LOCATION);


        } else {

           /* LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            Toast.makeText(getContext(),""+LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient),
                    Toast.LENGTH_SHORT).show();
*/
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient,
                    mLocationRequest, this
            ).setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(Status status) {

                }
            });


        }

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        action.registraInteressados(this);
        buttomAddMarkerVisivel = action.getButtomAddMakerClickado();
        geocoder2 = new Geocoder(getContext());

        buildGoogleApiClient();
        createLocationRequest();
        buildLocationSettingsRequest();
        checkLocationSettings();
        checkPlayService();

        /*               ....       */
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

                if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION_LOCATION);
                } else {
                    Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                            mGoogleApiClient);
                    if (mLastLocation != null) {
                        newLatLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());


                        //Toast.makeText(getContext(), "Presta atençao " + newLatLng.toString(), Toast.LENGTH_LONG).show();


                        if (action.isReportNotSelected()) {
                            /* Verifico a proximidade do user com o local que ele vai por o marcador*/
                            if (markerDialog.closeToMe(newLatLng, arg0)) {
                                  /* Verifico se existe algum marcador proximo */
                                if (!markerDialog.hasNearby(markerHashMap, arg0)) {
                                    markerDialog.dialogAdd2(arg0, getContext(), mMap, geocoder2, markerHashMap);


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


            }
        });


        /*Criando o listener click on marker*/
        googleMap = markerDialog.setMarkerClick(googleMap, getContext(), markerHashMap);


        /*Criando o listener do drag*/
        googleMap = markerDialog.setListenerDragDiag(googleMap, marcador, getContext(), getView());


        mMap = googleMap;
        //  mMap.setMinZoomPreference(10);

        /*Checkando a permissão dos acessos, vulgo frescura do Android..*/
        if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {

            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.setOnMapClickListener(this);
            mMap.getUiSettings().setMapToolbarEnabled(false);
            mMap.getUiSettings().setZoomControlsEnabled(true);

            getRaioFirebase(-23.6202800, -45.4130600, 5.00);
            //getAllFirebase();
            markerDialog.zoomMarker(new LatLng(-23.6202800, -45.4130600), mMap);

            //Aqui atualiza a busca de acordo com a LATLNG não roda o currentLocation
//            if (mCurrentLocation != null){
//                markerDialog.zoomMarker(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()),mMap);
//                getRaioFirebase(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), 5.00);
//            }


        /*Listener responsavel adicionar o botão da localização*/
            mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                @Override
                public boolean onMyLocationButtonClick() {
                    LatLng hue = new LatLng(latitude, longitude);
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(hue));
                    return false;
                }
            });


        } else {

            ActivityCompat.requestPermissions(this.getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION_LOCATION);
        }


        /*Notificação*/
        NotificationApp notificationApp = new NotificationApp(getView());
        notificationApp.notification();


        /*Teste do serviço de thread*/
       /* Intent intent = new Intent(getContext(),ServiceThread.class);
        getContext().startService(intent);*/
    }


    /*Override do metodo onMapClick..*/
    @Override
    public void onMapClick(LatLng latLng) {
        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this.getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION_LOCATION);
        } else {

            /*AS coisas estavam dando errado aqui, mas agora funciona.. apenas para teste..*/
            Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);

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

    }


    @Override
    public void notificaticarInteressados(Action action) {
        buttomAddMarkerVisivel = action.getButtomAddMakerClickado();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {

            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);


        } else {

            ActivityCompat.requestPermissions(this.getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION_LOCATION);

        }


    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }


    @Override
    public void onLocationChanged(Location location) {

        /*Armazenando a ultima posição*/
        mCurrentLocation = location;
        newLatLng = new LatLng(location.getLatitude(),location.getLongitude()) ;


        if(myCurrentLocationTag == null){
            myCurrentLocationTag = new MarkerTag();
            myCurrentLocationTag.setId("MyCurrentTag");
            myCurrentLocationTag.setValidate(true);
            myCurrentLocationTag.setLatitude(location.getLatitude());
            myCurrentLocationTag.setLongitude(location.getLongitude());
        }else{
            myCurrentLocationTag.setLatitude(location.getLatitude());
            myCurrentLocationTag.setLongitude(location.getLongitude());
        }

        //Update to Firebase
        GeoFire geoFire2 = new GeoFire(mDatabase.child("MyLocation"));
        geoFire2.setLocation("Você", new GeoLocation(location.getLatitude(), location.getLongitude()),
                new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error) {
                        //addMarker
                        if (mCurrLocation != null) {
                            mCurrLocation.remove();
                        }
                        mCurrLocation = mMap.addMarker(new MarkerOptions()
                                .position(newLatLng)
                                .title("Você")
                                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_my_location))
                        );
                        mCurrLocation.setTag(myCurrentLocationTag);
                //        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(newLatLng, 12.0f));
                    }
                });

        getRaioFirebase(location.getLatitude(), location.getLongitude(), 5.0);

    }

    public void getRaioFirebase(Double lat, Double lng, Double radius) {
        getServerTime();
        mDatabase = ConfigFireBase.getFirebase();
//        GeoFire geoFire = new GeoFire(mDatabase.child("marker_location"));
        firebaseDatabase = ConfigFireBase.getFirebaseDatabase();
        GeoFire geoFire = new GeoFire(firebaseDatabase.getReferenceFromUrl("https://websan-46271.firebaseio.com/marker_location/"));

        final GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(lat, lng), radius);

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(final String key, GeoLocation location) {
                mDatabase = ConfigFireBase.getFirebase();
                mDatabase.child("Marker").child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot != null && dataSnapshot.getValue() != null) {
                            double latitude = (double) dataSnapshot.child("position").child("latitude").getValue();
                            double longitude = (double) dataSnapshot.child("position").child("longitude").getValue();
                            LatLng latLng = new LatLng(latitude, longitude);
                            if (dataSnapshot.child("fim").getValue() != null) {
                                if (getServerTime() < (Long) dataSnapshot.child("fim").getValue()) {
                                    if (!dataSnapshot.child("Denunciar").child(userId).exists()) {
                                        Log.i(TAG, "onDataChange: NÃO EXISTE DENUNCIA");
                                        if (markerHashMap.get(key) == null) {
                                            Log.i(TAG, "Entrou Criar: " + key);
                                            markerDialog.addDataArrayFirebase(latLng, getContext(), mMap, geocoder2, markerHashMap, key, dataSnapshot.child("Validar").child(userId).exists());

//                                                MarkerOptions markerOption = new MarkerOptions();
//                                                markerOption.position(latLng);
//                                                mMap.addMarker(markerOption);
                                        }
                                    } else {
                                        Log.i(TAG, "onDataChange: EXISTE DENUNCIA");
                                    }
                                } else {
                                    if (markerHashMap.get(key) != null) {
                                        Log.i(TAG, "Entrou Remover: " + key);
                                        MarkerDialog.deleteDataArrayFirebase(markerHashMap, key);
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

    public Long getServerTime() {
        mDatabase = ConfigFireBase.getFirebase();
        final Long[] timestampServer = new Long[1];
        mDatabase.child("current_timestamp").setValue(ServerValue.TIMESTAMP);
        mDatabase.child("current_timestamp").addListenerForSingleValueEvent(new ValueEventListener() {
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

    public void getAllFirebase() {
        final List<MarkerTag> markerList = new ArrayList<>();

        mDatabase = ConfigFireBase.getFirebase();
        mDatabase.child("Marker").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                markerList.clear();
                if (dataSnapshot != null) {
                    for (DataSnapshot objSnapShot : dataSnapshot.getChildren()) {
                        Log.i(TAG, "onDataChange: " + objSnapShot);
                        MarkerTag markerBD = objSnapShot.getValue(MarkerTag.class);
                        markerList.add(markerBD);
                    }
                    Log.i(TAG, "getAllFirebase: Size: " + markerList);
                    if (markerList != null) {
                        for (int i = 0; i < markerList.size(); i++) {
                            Marker marker = mMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(markerList.get(i).getPosition().latitude, markerList.get(i).getPosition().longitude))
                            );
                            mHashMap.put(marker, markerList.get(i).getId());
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}

