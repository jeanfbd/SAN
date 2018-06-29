package desenvolvimentoads.san;


import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationListener;
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
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import desenvolvimentoads.san.DAO.ConfigFireBase;
import desenvolvimentoads.san.Marker.MarkerDialog;
import desenvolvimentoads.san.Marker.MarkerTag;
import desenvolvimentoads.san.Observer.Action;
import desenvolvimentoads.san.Observer.ActionObserver;
import desenvolvimentoads.san.Observer.SharedContext;

public class MapsTerceiro extends SupportMapFragment implements OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ActionObserver {

    private GoogleMap mMap;
    int RQS_GooglePlayServices = 0;
    private static final int MY_PERMISSION_REQUEST_CODE = 2508;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 1991;
    private static final String TAG = "MapsTerceiro";

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    private static int UPDATE_INTERVAL = 4000;
    private static int FATEST_INTERVAL = 2000;
    private static int DISPLACEMENT = 0;

    //Login Firebase Auth
    private FirebaseAuth mAuth = com.google.firebase.auth.FirebaseAuth.getInstance();
    private FirebaseUser currentUser = mAuth.getCurrentUser();
    private String userId = currentUser.getUid();

    //Banco de Dados Firebase
    private DatabaseReference mDatabaseReference;
    private FirebaseDatabase firebaseDatabase;

    //Raio Geofire
    private GeoQuery geoQueryRaio;
    private GeoFire geoFireRaio;

    //HashMap dos markers
    public static HashMap<String, Marker> markerHashMap = new HashMap<>();
    public static HashMap<String, Marker> tempMarkerHashMap = new HashMap<>();
    public static HashMap<String, String> keyMarkerMap = new HashMap<>();

    Context mContext;

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

    public static HashMap<String, String> keyFirebaseHashMap = new HashMap<>();

    public static final int REQUEST_PERMISSION_LOCATION = 10;
    Double getRaioFirebaseRadius = 1.0;
    /*Value in Meters*/
    Double closeToMeRadius = 1.0;
    Double circleRadius = 12.5;

    public static HashMap<String, String> alertHashMap = new HashMap<>();
    /*SharedPrefers*/
    SharedPreferences myPreferences;
    SharedPreferences.Editor edit;
    double latPrefers;
    double lngPrefers;
    String latString;
    String lngString;
    SharedContext sharedContext = SharedContext.getInstance();


    protected LocationSettingsRequest mLocationSettingsRequest;
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    /*da para usar como controle para verificar se deixamos ligado ou não o update*/
    boolean mRequestingLocationUpdates;
    boolean mapRebuildOk = false;
    boolean started = false;
    boolean zoomActive = false;

    LocationListener locationListenerGPS;
    Context pContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pContext = getActivity().getBaseContext();
        getMapAsync(this);

        mContext = getContext();
        mDatabaseReference = ConfigFireBase.getFirebase();
        firebaseDatabase = ConfigFireBase.getFirebaseDatabase();
        setUpLocation();


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
       /*isso tudo esta sendo ignorado por que esta indo para o menu inicial no super..*/
        Log.i("teste", "permission map terceiro called ");
        switch (requestCode) {

            case MY_PERMISSION_REQUEST_CODE:
                Log.i("teste", "onRequestPermissionsResult Permission MY_PERMISSION_REQUEST_CODE");
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i("teste", "onRequestPermissionsResult Permission MY_PERMISSION_REQUEST_CODE    OK!!!!!!!");

                    buildGoogleApiClient();
                    createLocationRequest();
                    buildLocationSettingsRequest();
                    if (checkPlayService()) {

                        checkLocationSettings();
                        displayLocation();

                    }
                }

                break;
            default:
                Log.i("teste", "onRequestPermissionsResult Permission neged!!!");
        }
    }

    /*0*/
    private void setUpLocation() {
        Log.i("teste", "------------------- METHOD 0 setUpLocation ---------------------");
        if (ActivityCompat.checkSelfPermission(pContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            //Request runtime permission
            ActivityCompat.requestPermissions(getActivity(), new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, MY_PERMISSION_REQUEST_CODE);


        } else {
            buildGoogleApiClient();
            createLocationRequest();
            buildLocationSettingsRequest();
            if (checkPlayService()) {

                createLocationListener();
                checkLocationSettings();
                displayLocation();

            }

        }
    }


    /*1*/
    protected synchronized void buildGoogleApiClient() {

        Log.i("teste", "------------------- METHOD 1 buildGoogleApiClient ---------------------");
        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();


    }

    /*2*/
    private void createLocationRequest() {
        Log.i("teste", "------------------- METHOD 2 createLocationRequest ---------------------");
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);


    }

    /* 3 - Construindo o location Settings request*/
    protected void buildLocationSettingsRequest() {
        Log.i("teste", "------------------- METHOD 3 buildLocationSettingsRequest ---------------------");
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    /*3.5*/
    private boolean checkPlayService() {
        Log.i("teste", "------------------- METHOD 3.5 checkPlayService ---------------------");
        /*Se npegar a instancia do googleservice o fused não funciona -.-*/
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int resultCode = googleAPI.isGooglePlayServicesAvailable(getActivity());
        if (resultCode == ConnectionResult.SUCCESS) {
            mGoogleApiClient.connect();
            Log.i("teste", "-------------------  checkPlayService true---------------------");

            return true;
        } else {
            googleAPI.getErrorDialog(getActivity(), resultCode, RQS_GooglePlayServices);
            Log.i("teste", "-------------------  checkPlayService false---------------------");

            return false;

        }
    }

    /* 4 - criando o method que irá checar o status do locationSettings */
    protected void checkLocationSettings() {

        Log.i("teste", "------------------- METHOD 4 checkLocationSettings ---------------------");

        recreateGoogleRefers();
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(
                        mGoogleApiClient,
                        mLocationSettingsRequest
                );

        Log.i("teste", "LocationSettingsResult was created ");
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {

            /* 5 Aqui que controla caso o location não estiver pronto*/
            @Override
            public void onResult(LocationSettingsResult locationSettingsResult) {

                Log.i("teste", "LocationSettingsResult result.. ?");
                final Status status = locationSettingsResult.getStatus();
                Log.i("teste", "LocationSettingsResult CODE STATUS.. " + status);
                switch (status.getStatusCode()) {
                    /*Pronto.. as outras opções são opcionais a tirando a required ali.*/
                    case LocationSettingsStatusCodes.SUCCESS:

                        startLocationsUpdates();
                        Log.i("teste", "LocationSettingsResult sucess ");
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:


                        if (Build.VERSION.SDK_INT <= 22) {
                            // never asked selecionado
                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                            alertDialogBuilder.setTitle("Serviço de localização.");
                            alertDialogBuilder
                                    .setMessage("" +
                                            "\nO App San utiliza de dados de 'alta precisão' de localização para seu funcionamento." + "\nAtualmente não esta sendo encontrado o serviço de localização ou a alta precisão não esta ativada, possivelmente esteja desativado ou configurado a localização apenas para o dispositivo."
                                            + "\nPor favor verifique as Configurações e o habilite a localização e selecione o modo ALTA PRECISÃO, caso já esteja habilitado desabilite e habilite novamente.")
                                    .setCancelable(false)
                                    .setPositiveButton("Configurações.", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                            startActivityForResult(intent, 1);// step 6

                                        }
                                    });

                            AlertDialog alertDialog = alertDialogBuilder.create();
                            alertDialog.show();


                        } else {
                            if (ActivityCompat.checkSelfPermission(pContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                Log.i("teste", "LocationSettingsResult permission not granted");

                                /*Aqui só é chamado quando o user nega a primeira vez, nessa segunda vez o android deixa lançar uma mensagem para o user.*/
                                if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                                        Manifest.permission.ACCESS_FINE_LOCATION)) {

                                    Log.i("teste", "LocationSettingsResult permission rationale");
                                    //     try {
                                    // Show the dialog by calling startResolutionForResult(), and check the result
                                    // in onActivityResult().

                                    // User selected the Never Ask Again Option Change settings in app settings manually
                                    final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                                    alertDialogBuilder.setTitle("   Olá.    ");
                                    alertDialogBuilder
                                            .setMessage("" +
                                                    "\nO App San utiliza de dados da localização para seu funcionamento, ao negar a permissão o aplicativo deixa de funcionar." + "" +
                                                    "\nPor isso pedimos para que aceite que ele utilize da permissão do Location.")
                                            .setCancelable(false)
                                            .setPositiveButton("Ok.", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION_LOCATION);

                                                }
                                            });

                                    AlertDialog alertDialog = alertDialogBuilder.create();
                                    alertDialog.show();

                                } else {

                                    Log.i("teste", "LocationSettingsResult permission rationale not needed to ask only the normal one");
                                    ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION_LOCATION);


                                }


                            } else {
                                Log.i("teste", "LocationSettingsResult permission was checked and not needed");
                                startLocationsUpdates();

                            }

                        }
                        //move to step 6 in onActivityResult to check what action user has taken on settings dialog
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.i("teste", "LocationSettingsResult  SETTINGS_CHANGE_UNAVAILABLE");
                        break;
                }
            }
        });
    }


    /*é bom para colocar qdo o app entrar em resumo para parar o fused..*/
    protected void stopLocationUpdates() {
        Log.i("teste", "------------------- METHOD stopLocationUpdates ---------------------");
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient,
                locationListenerGPS
        ).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
/* usa isso para saber se esta habilitado o location updates*/
                mRequestingLocationUpdates = false;
                //   setButtonsEnabledState();
                Log.i("teste", "stopLocationUpdates called and stoped..");

            }
        });
    }

    /*5*/
    private void displayLocation() {
        Log.i("teste", "------------------- METHOD 5 displayLocation ---------------------");
        if (ActivityCompat.checkSelfPermission(pContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(pContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            //Request runtime permission
            ActivityCompat.requestPermissions(getActivity(), new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, MY_PERMISSION_REQUEST_CODE);
        } else {
            Log.i("teste", "displayLocation fused  " + mLastLocation);

            if (mLastLocation == null) {

                if (latString != null && lngString != null) {
                    mLastLocation = new Location("");
                    mLastLocation.setLatitude(latPrefers);
                    mLastLocation.setLongitude(lngPrefers);
                    Log.i("teste", "displayLocation new one " + mLastLocation);
                }

            }

            if (mLastLocation != null) {


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


                if (mCurrent != null) {
                    mCurrent.setPosition(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
                } else {

                    mCurrent = mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()))
                            .title("Você"));


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
                    } else {
                        mCurrent.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_my_location));

                    }

                    //Move Camera to this Position
                    mCurrent.setTag(myCurrentLocationTag);
                    if (!zoomActive) {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), 16.0f));
                    } else {
                        zoomActive = true;
                    }


                }
                ;

            }


        }


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        Log.i("teste", "---------- onMapReady ---------------");

        action.registraInteressados(this);
        buttomAddMarkerVisivel = action.getButtomAddMakerClickado();
        geocoder2 = new Geocoder(getContext());

         /*Adiciona o listener no infoWindows(tag) do marker*/
        googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
            }
        });

        /*Criando o listener do click longo*/
        googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {


            @Override
            public void onMapLongClick(LatLng arg0) {
                // TODO Auto-generated method stub
                checkGpsPermission();
                checkPermission();
                if (mLastLocation != null) {
                    newLatLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                    getRaio(arg0.latitude, arg0.longitude, getRaioFirebaseRadius, false);
                    if (action.isReportNotSelected()) {
                            /* Verifico a proximidade do user com o local que ele vai por o marcador*/
                        if (markerDialog.closeToMe(newLatLng, arg0)) {
                                  /* Verifico se existe algum marcador proximo */
                            if (!markerDialog.hasNearby(markerHashMap, arg0)) {
                                alertDialog(arg0, getContext(), mMap, geocoder2, markerHashMap);
                                Log.d(TAG, "Action ID: " + itemId);


                            } else {
                                Toast.makeText(getContext(), "Já existe um marcador na proximidade.", Toast.LENGTH_SHORT).show();
                            }

                        } else {
                            Toast.makeText(getContext(), "Você precisa estar próximo do ponto a ser marcado.", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Toast.makeText(getContext(), " Não foram encontrados os dados da ultima localização.", Toast.LENGTH_LONG).show();
                }
            }
        });

        /*Criando o listener click on marker*/
        googleMap = markerDialog.setMarkerClick(googleMap, getContext(), markerHashMap);

        /*Criando o listener do drag*/
        googleMap = markerDialog.setListenerDragDiag(googleMap, marcador, getContext(), getView());

        mMap = googleMap;
        mMap.setOnMapClickListener(this);

        mMap.setOnCircleClickListener(new GoogleMap.OnCircleClickListener() {

            @Override
            public void onCircleClick(Circle circle) {
                // Flip the r, g and b components of the circle's
                // stroke color.
                int strokeColor = circle.getStrokeColor() ^ 0x00ffffff;
                circle.setStrokeColor(strokeColor);
            }

        });
        Log.i("teste", "---------- onMapReady listeners were created with success ! ---------------");

        /*usando o prefers para popular the last location from user..*/
        myPreferences = getContext().getSharedPreferences("Location", Context.MODE_PRIVATE);
        latString = myPreferences.getString("lat", null);
        lngString = myPreferences.getString("lng", null);
        if (latString != null && lngString != null) {
            latPrefers = Double.valueOf(latString);
            lngPrefers = Double.valueOf(lngString);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latPrefers, lngPrefers), 16.0f));
            Log.i("teste", "Shared teste");
        }

        mapConfig();


        if (mapRebuildOk) {
            if (latString != null && lngString != null) {

                Log.i("teste1", "->>>> map create " + keyFirebaseHashMap.size());
                //getRaioFirebase(latPrefers, lngPrefers, getRaioFirebaseRadius);
                getRaio(latPrefers, lngPrefers, getRaioFirebaseRadius, true);
                Log.i("teste", " setOnMapLongClickListener prefers getRaio inicial ok...");

            }

        }
        mMap.clear();
        rebuildMap();

        Log.i("teste", "---------- onMapReady has finished with success ! ---------------");


    }

    public void rebuildGeoQuery2() {
        Log.i("teste", "rebuildGeoQuery2 ");

        if (alertHashMap != null) {
            alertHashMap = new HashMap<>();

            if (mLastLocation != null) {
                Log.i("teste", "rebuildGeoQuery2 mLastLocation not null ");


                for (String key : markerHashMap.keySet()) {
                    Log.i("teste", "rebuildGeoQuery2 marker hash key " + key);
                    LatLng latLng = new LatLng(markerHashMap.get(key).getPosition().latitude, markerHashMap.get(key).getPosition().longitude);
                    if (markerDialog.closeToMeToHash(newLatLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), latLng, alertHashMap, key, closeToMeRadius)) {
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

            Log.i("teste", "rebuildGeoQuery2 fim ");


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

                if (tagTemp.getIdUser().equals(userId)) {
                    markerOption.position(latLng).icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_maker_vermelho_star));
                } else {
                    markerOption.position(latLng).icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_maker_vermelho));
                }


                circle = mMap.addCircle(new CircleOptions()
                        .center(tagTemp.getPosition())
                        .radius(circleRadius)
                        .strokeWidth(10)
                        .strokeColor(Color.argb(128, 173, 216, 230))
                        .fillColor(Color.argb(24, 30, 144, 255))
                        .clickable(true));


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


        Log.i("teste", "fim rebuilding...");


    }

    @Override
    public void onMapClick(LatLng latLng) {
        checkGpsPermission();
        checkPermission();
        checkLocationSettings();
        Log.i("teste", "Map clicked");
        if (mLastLocation != null) {
            newLatLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            //Toast.makeText(getContext(), "Presta atençao " + latLng.toString(), Toast.LENGTH_LONG).show();
            Log.i("teste1", "->>>> map click " + keyFirebaseHashMap.size());
            //getRaioFirebase(latLng.latitude, latLng.longitude, getRaioFirebaseRadius);
            hashMapClear(tempMarkerHashMap);
            getRaio(latLng.latitude, latLng.longitude, getRaioFirebaseRadius, false);


                    /*Aqui é normal*/


            if (!buttomAddMarkerVisivel) {
                Log.i("teste", "marker not visible");

              /* Verifico a proximidade do user com o local que ele vai por o marcador*/
                if (markerDialog.closeToMe(newLatLng, latLng)) {
                    Log.i("teste", "marker not closed to me");
                                  /* Verifico se existe algum marcador proximo */
                    Log.i("teste", " before hasneraby RAIO " + markerHashMap.size());
                    if (!markerDialog.hasNearby(markerHashMap, latLng)) {
                        Log.i("teste", "marker not nearby");
                        alertDialog(latLng, this.getContext(), mMap, geocoder2, markerHashMap);
                    } else {
                        Toast.makeText(getContext(), "Já existe um marcador na proximidade.", Toast.LENGTH_SHORT).show();

                    }

                } else {
                    Toast.makeText(getContext(), "É necessario estar próximo ao local a ser marcado.", Toast.LENGTH_SHORT).show();

                }

            } else {


            }


        } else {
            Toast.makeText(getContext(), "Não foram encontrados os dados da ultima localização.", Toast.LENGTH_LONG).show();
        }
    }

    /*3.7*/
    public void createLocationListener() {
        Log.i("teste", "------------------- METHOD 3.7 createLocationListener ---------------------");
        locationListenerGPS = new LocationListener() {
            @Override
            public void onLocationChanged(android.location.Location location) {
                Log.i("teste", "Location changed markerhashmap size : " + markerHashMap.size());

                mLastLocation = location;

                if (mLastLocation != null) {
                    Log.i("teste1", "->>>> Location Change " + keyFirebaseHashMap.size());
                    displayLocation();
                    getRaio(mLastLocation.getLatitude(), mLastLocation.getLongitude(), getRaioFirebaseRadius, true);
                    Log.d("GEOFIREKEYHASHMAP", "Size: " + keyMarkerMap.size());
                    Log.d("GEOFIREHASHMAP", "Size: " + markerHashMap.size());

                    HashMap<String, Marker> tempHashMap = (HashMap) markerHashMap.clone();
                    for (Map.Entry<String, Marker> markerTemp : tempHashMap.entrySet()) {
                        Log.d("GEOFIRECONDIÇÃO", "COMTEM: " + keyMarkerMap.containsKey(markerTemp.getKey()));
                        Log.d("GEOFIRECONDIÇÃO", "KEY A: " + markerTemp.getKey());
                        Log.d("GEOFIRECONDIÇÃO", "KEY B: " + keyMarkerMap.get(markerTemp.getKey()));
                        if (!keyMarkerMap.containsKey(markerTemp.getKey())) {
                            Log.d("GEOFIREHASHMAP", "removeu a key: " + markerTemp.getKey());
                            MarkerTag markerTag = (MarkerTag) markerHashMap.get(markerTemp.getKey()).getTag();
                            markerTag.getCircle().remove();
                            markerTemp.getValue().remove();
                            markerHashMap.remove(markerTemp.getKey());
                        }
                    }
                    keyMarkerMap.clear();

                    myLat = location.getLatitude();
                    myLong = location.getLongitude();

                    sharedContext.createPrefers("Location", "lat", String.valueOf(mLastLocation.getLatitude()));
                    sharedContext.createPrefers("Location", "lng", String.valueOf(mLastLocation.getLongitude()));


                }


                rebuildGeoQuery2();


            }

        };


    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i("teste", "OnConnected !!!");
        if (started) {
            Log.i("teste", "OnConnected WAS STARTED startLocationsUpdates");
            startLocationsUpdates();
        }


    }

    /*Em teoria se voce da stop seria aqui que daria o start, é bem melhor que o outro metodo que esta tem quase o mesmo nome sem o S*/
    private void startLocationsUpdates() {
        Log.i("teste", "startLocationsUpdates ");
        if (ActivityCompat.checkSelfPermission(pContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.i("teste", "startLocationsUpdates PERMISSION NEEDED");
            /*Aqui só é chamado quando o user nega a primeira vez, nessa segunda vez o android deixa lançar uma mensagem para o user.*/
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                Log.i("teste", "startLocationsUpdates checkGpsPermission Rationale");
                //     try {
                // Show the dialog by calling startResolutionForResult(), and check the result
                // in onActivityResult().

                //
                // User selected the Never Ask Again Option Change settings in app settings manually
                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                alertDialogBuilder.setTitle("   Olá    ");
                alertDialogBuilder
                        .setMessage("" +
                                "\n O App San utiliza de dados da localização para seu funcionamento, ao negar a permissão o aplicativo deixa de funcionar." + "" +
                                "\n Por isso pedimos para que aceite que ele utilize da permissão do Location.")
                        .setCancelable(false)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION_LOCATION);

                            }
                        });

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();


            } else {

                Log.i("teste", "startLocationsUpdates normal permission NEEDED and called");
                ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION_LOCATION);


            }

        } else {
            Log.i("teste", "startLocationsUpdates permission NOT NEEDED");

            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient,
                    mLocationRequest, locationListenerGPS
            ).setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(Status status) {
                    /* usa isso para saber se esta habilitado o location updates*/
                    mRequestingLocationUpdates = true;
                    Log.i("teste", "startLocationsUpdates setResultCallback true");
                }
            });
            started = true;

        }

    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
        Log.i("teste", "onConnectionSuspended called");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i("teste", "onConnectionFailed called");
    }


    @Override
    public void notificaticarInteressados(Action action) {
        buttomAddMarkerVisivel = action.getButtomAddMakerClickado();
        Log.i("teste", "notificationinterassados called");
        if (action.getItemId() != null) {

            if (itemId != null) {

                if (mLastLocation != null) {

                    if (!itemId.equals(action.getItemId())) {
                        itemId = action.getItemId();
                        Log.i("teste", "itemid status :" + itemId);

                        if (markerHashMap.containsKey(itemId)) {
                            Log.i("teste", "itemid status contains :" + itemId);
                            LatLng newLatLangTemp = markerHashMap.get(itemId).getPosition();

                            if (markerDialog.closeToMeToHash(newLatLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), newLatLangTemp, alertHashMap, itemId, closeToMeRadius)) {
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


                        if (markerDialog.closeToMeToHash(newLatLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), newLatLangTemp, alertHashMap, itemId, closeToMeRadius)) {
                            if (mCurrent != null) {
                                mCurrent.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_my_location_danger));
                            }

                        }

                    }

                }


            }


        }


    }

    public void hashMapClear(HashMap<String, Marker> HashMap) {
        HashMap<String, Marker> markerHashMapTemp = (HashMap) HashMap.clone();
        for (Map.Entry<String, Marker> markerTemp : markerHashMapTemp.entrySet()) {
            MarkerTag markerTag = (MarkerTag) markerHashMapTemp.get(markerTemp.getKey()).getTag();
            markerTag.getCircle().remove();
            markerTemp.getValue().remove();
            markerHashMapTemp.get(markerTemp.getKey()).remove();
        }
        HashMap.clear();
    }


    public void getRaio(Double lat, Double lng, Double radius, boolean method) {
        if (geoQueryRaio != null) {
            geoQueryRaio.removeAllListeners();
        }
        getRaioListener(lat, lng, radius, method);
    }

    public void getRaioListener(Double lat, Double lng, Double radius, final boolean method) {
        getServerTime();

        mDatabaseReference = ConfigFireBase.getFirebase();
        firebaseDatabase = ConfigFireBase.getFirebaseDatabase();
        geoFireRaio = new GeoFire(firebaseDatabase.getReferenceFromUrl("https://websan-46271.firebaseio.com/marker_location/"));

        geoQueryRaio = geoFireRaio.queryAtLocation(new GeoLocation(lat, lng), radius);
        if (mLastLocation != null) {
            geoQueryRaio.addGeoQueryEventListener(new GeoQueryEventListener() {
                @Override
                public void onKeyEntered(String key, GeoLocation location) {
                    String tempKey = key;
                    if (!method) {
                        if (!markerHashMap.containsKey(tempKey)) {
                            fetchData(tempKey, tempMarkerHashMap);
                        }
                    } else {
                        fetchData(tempKey, markerHashMap);
                        Log.d("GEOFIREKEYHASHMAP", "add: " + tempKey);
                    }
                }

                @Override
                public void onKeyExited(String key) {
                    Log.d("GEOFIRE", "onKeyExited: " + key);
                    if (markerHashMap.get(key) != null) {
                        MarkerTag markerTag = (MarkerTag) markerHashMap.get(key).getTag();
                        markerTag.getCircle().remove();
                        markerHashMap.get(key).remove();
                        markerHashMap.remove(key);
                    }
                }

                @Override
                public void onKeyMoved(String key, GeoLocation location) {
                    Log.d("GEOFIRE", "onKeyMoved: " + key);
                }

                @Override
                public void onGeoQueryReady() {
                    Log.d("GEOFIRE", "onGeoQueryReady: Carregou a query");
                }

                @Override
                public void onGeoQueryError(DatabaseError error) {

                }
            });
        }
    }

    public void fetchData(final String key, final HashMap<String, Marker> HashMap) {
        mDatabaseReference = ConfigFireBase.getFirebase();
        mDatabaseReference.child("Marker").child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null && dataSnapshot.getValue() != null) {
                    double latitude = (double) dataSnapshot.child("latitude").getValue();
                    double longitude = (double) dataSnapshot.child("longitude").getValue();
                    LatLng latLng = new LatLng(latitude, longitude);
                    if (dataSnapshot.child("fim").getValue() != null) {
                        if (getServerTime() < (Long) dataSnapshot.child("fim").getValue()) {
                            if (!dataSnapshot.child("Denunciar").child(userId).exists()) {
                                if (HashMap.get(key) == null) {
                                    Log.i(TAG, "Entrou Criar: " + key);
                                    String idUser = (String) dataSnapshot.child("idUser").getValue();
                                    Long inicio = (Long) dataSnapshot.child("inicio").getValue();
                                    Log.d(TAG, "onDataChange: " + inicio);
                                    Long validaram = dataSnapshot.child("Validar").getChildrenCount();
                                    markerDialog.addDataArrayFirebase(latLng, getContext(), mMap, geocoder2, HashMap, key, dataSnapshot.child("Validar").child(userId).exists(), idUser, inicio, validaram);
                                    keyMarkerMap.put(key, key);
                                    if (markerDialog.closeToMeToHash(newLatLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), latLng, alertHashMap, key, closeToMeRadius)) {
                                        if (mCurrent != null) {
                                            mCurrent.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_my_location_danger));
                                        }
                                    }
                                }
                            } else {
                                if (HashMap.get(key) != null) {
                                    Log.i(TAG, "Entrou Remover: " + key);
                                    MarkerDialog.deleteDataArrayFirebase(HashMap, key);
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
                        } else {
                            geoFireRaio.removeLocation(key, new GeoFire.CompletionListener() {
                                @Override
                                public void onComplete(String key, DatabaseError error) {
                                    Log.d("GEOFIRE", "REMOVEU LISTENER: " + key);
                                    if (HashMap.get(key) != null) {
                                        Log.i(TAG, "Entrou Remover: " + key);
                                        MarkerDialog.deleteDataArrayFirebase(HashMap, key);
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
                            });
                        }
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

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

    public void checkGpsPermission() {
        Log.i("teste", "checkGpsPermission >.< ");
        if (ActivityCompat.checkSelfPermission(pContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.i("teste", "checkGpsPermission permission needed");

            /*Aqui só é chamado quando o user nega a primeira vez, nessa segunda vez o android deixa lançar uma mensagem para o user.*/
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                Log.i("teste", "checkGpsPermission Rationale permission called");
                //     try {
                // Show the dialog by calling startResolutionForResult(), and check the result
                // in onActivityResult().

                //
                // User selected the Never Ask Again Option Change settings in app settings manually
                // User selected the Never Ask Again Option Change settings in app settings manually
                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                alertDialogBuilder.setTitle("   Olá.    ");
                alertDialogBuilder
                        .setMessage("" +
                                "\nO App San utiliza de dados da localização para seu funcionamento, ao negar a permissão o aplicativo deixa de funcionar." + "" +
                                "\nPor isso pedimos para que aceite que ele utilize da permissão do Location.")
                        .setCancelable(false)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION_LOCATION);

                            }
                        });

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();


            } else {


                if (sharedContext.isFirstTimeAskingPermission("Location")) {
                    Log.i("teste", "checkGpsPermission permission needed and normal one was called");
                    sharedContext.firstTimeAskingPermission("Location", false);
                    ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION_LOCATION);

                } else {
                    Log.i("teste", "checkGpsPermission permission needed and 'never ask' one was called");


                }


            }

/*Se o user mudar durante o uso o location e botar never ask again e voltar o location para true, ae essa parte faz a checagem novamente...*/
        } else if (mapRebuildOk == false) {
            Log.i("teste", "checkGpsPermission permission not needed and rebuil = false");
            mapConfig();
            setUpLocation();
            MenuInicial.permissionOk = false;

        } else {
            Log.i("teste", "checkGpsPermission permission not needed and rebuil = true");
        }
    }


    public void checkPermission() {

        if (Build.VERSION.SDK_INT >= 23) {

            if (MenuInicial.permissionOk) {
                mapConfig();
                setUpLocation();
                Log.i("teste", "checkPermission MenuInicial.permissionOk checked");
                MenuInicial.permissionOk = false;
            } else {

                Log.i("teste", "checkPermission MenuInicial.permissionOk false and not checked");


            }
        }


    }


    public void mapConfig() {
        Log.i("teste", " MAP CONFIG ");
        /*Checkando a permissão dos acessos, vulgo frescura do Android..*/
        if (ContextCompat.checkSelfPermission(pContext, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);

            mMap.getUiSettings().setMapToolbarEnabled(false);
            mMap.getUiSettings().setZoomControlsEnabled(true);



            /*Listener responsavel adicionar o botão da localização*/
            mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                @Override
                public boolean onMyLocationButtonClick() {


                    if (mLastLocation != null) {
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude())));

                    } else {

                        if (latString != null && lngString != null) {

                            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(latPrefers, lngPrefers)));
                            Log.i("teste", "prefers camera ok...");

                        }


                    }


                    return false;
                }
            });
            if (!mapRebuildOk) {

                if (mLastLocation != null) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude())));

                } else {

                    if (latString != null && lngString != null) {

                        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(latPrefers, lngPrefers)));
                        Log.i("teste", "prefers camera ok...");
                    }


                }

            }


            mapRebuildOk = true;


        } else {
            //Request runtime permission
            ActivityCompat.requestPermissions(getActivity(), new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, MY_PERMISSION_REQUEST_CODE);
        }

        Log.i("teste", " MAP CONFIG END");
    }

    public void recreateGoogleRefers() {
        Log.i("teste", "recreateGoogleRefers");
        if (mGoogleApiClient == null) {
            Log.i("teste", "recreateGoogleRefers mGoogleApiClient null");
            buildGoogleApiClient();
            checkPlayService();
        }

        if (mLocationSettingsRequest == null) {
            Log.i("teste", "recreateGoogleRefers mLocationSettingsRequest null");
            createLocationRequest();
            buildLocationSettingsRequest();
        }

    }

    @Override
    public void onStop() {
        Log.i("teste", "STOP TERCEIRO  ??");
        super.onStop();
        if (started) {
            Log.i("teste", "STOPLOCATION CALLED");
            stopLocationUpdates();
        }


    }

    @Override
    public void onResume() {
        Log.i("teste", "RESUME TERCEIRO ??");
        super.onResume();
        if (started) {
            startLocationsUpdates();
        }


    }

    public void onDestroy() {
        super.onDestroy();
        Log.i("teste", "In onDestroy mapTerceiro");

    }

    final double RADIUS = 12.5;
    private int image;
    boolean alertOn = false;
    long markerDuration = 1200000;

    public void alertDialog(final LatLng latLng, final Context c, final GoogleMap googleMapFinal, final Geocoder g, final HashMap<String, Marker> m) {
        if (!alertOn) {
            alertOn = true;


            LayoutInflater li = LayoutInflater.from(getContext());
            final View view = li.inflate(R.layout.dialogadd2, null);

            AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
            builder.setTitle("Criar Marcador");
            builder.setCancelable(false);
            final Button confirm = (Button) view.findViewById(R.id.btConfirm);

            final Button cancel = (Button) view.findViewById(R.id.btCancel);
            builder.setView(view);


            final String streetText = getStreet(latLng, c, g);

            final TextView street = (TextView) view.findViewById(R.id.street);
            street.setText(streetText);

            final ImageView vermelho = (ImageView) view.findViewById(R.id.markerimage);
            vermelho.setImageResource(R.mipmap.ic_maker_vermelho_star);

            final TextView location = (TextView) view.findViewById(R.id.location);
            location.setText("Latitude: " + latLng.latitude + "\nLongitude: " + latLng.longitude);


            final AlertDialog alerta = builder.create();

            confirm.setOnClickListener(new View.OnClickListener() {
                public void onClick(View arg0) {
                    alerta.dismiss();
                    markerCreate(latLng, c, googleMapFinal, m, streetText);
                    alertOn = false;
                }

            });

            cancel.setOnClickListener(new View.OnClickListener()

            {
                public void onClick(View arg0) {
                    alerta.dismiss();
                    if (!Action.getInstance().getButtomAddMakerClickado()) {

                        Action.getInstance().setButtomAddMakerClickado(true);
                    }

                    alertOn = false;
                }
            });


            alerta.show();
            Vibrar();
        }


    }

    public void markerCreate(final LatLng latLng, final Context c, final GoogleMap googleMapFinal, final HashMap<String, Marker> m, String street) {
        image = R.mipmap.ic_maker_vermelho_star;


        MarkerOptions markerOption = new MarkerOptions();
        markerOption.position(latLng).icon(BitmapDescriptorFactory.fromResource(image));
        Marker marcadorCreated = googleMapFinal.addMarker(markerOption);
        //marcador.setTitle(getStreet(latLng, c, g));


        MarkerTag tag = new MarkerTag(marcadorCreated.getPosition().latitude, marcadorCreated.getPosition().longitude, createCircle(latLng, googleMapFinal), false);
        tag.setStreet(street);

        //Pega Referencia do Firebase
        DatabaseReference mDatabase = ConfigFireBase.getFirebase();
        //Persiste uma key no banco do firebase
        final String itemIdMarker = mDatabase.child("Marker").push().getKey();
        //Faz referencia da key na tag do marcador
        tag.setId(itemIdMarker);
        tag.setValidate(true);
        marcadorCreated.setTag(tag);
        marcadorCreated.setDraggable(false);

        final MarkerTag markerTag = (MarkerTag) marcadorCreated.getTag();


        action.setButtomAddMakerClickado(true);
        String teste1 = "";
        teste1 = itemIdMarker;
        m.put(teste1, marcadorCreated);


        String teste3 = "";
        teste3 = itemIdMarker;
        action.setItemId(teste3);


        geoFireNewMarker(mDatabase, itemIdMarker, markerTag, marcadorCreated);
        Toast.makeText(getContext(), "Marcador criado com sucesso.", Toast.LENGTH_SHORT).show();


    }

    public void geoFireNewMarker(final DatabaseReference mDatabase, final String itemIdMarker, final MarkerTag markerTag, final Marker marker) {

        GeoFire geoFire = new GeoFire(mDatabase.child("marker_location"));
        geoFire.setLocation(itemIdMarker, new GeoLocation(marker.getPosition().latitude, marker.getPosition().longitude), new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {
                if (error != null) {
                    Log.e("GEOFIRE", "There was an error saving the location to GeoFire: " + error);
                } else {
                    Log.d("GEOFIRE", "Location saved on server successfully!");

                    //Persiste os dados sobre a chave itemIdMarker
                    mDatabase.child("Marker").child(itemIdMarker).setValue(markerTag);

                    //Persiste a key do usuario no campo IdUser
                    mDatabase.child("Marker").child(itemIdMarker).child("idUser").setValue(userId);

                    //Persiste o tempo fim para o marker baseado no inicio
                    mDatabase.child("Marker").child(itemIdMarker).child("inicio").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot != null) {
                                long inicio = (long) dataSnapshot.getValue();
                                Log.d("TIMESTAMP", "" + inicio);
                                mDatabase.child("Marker").child(itemIdMarker).child("fim").setValue(inicio + markerDuration);
                                mDatabase.child("Marker").child(itemIdMarker).child("Validar").child(userId).setValue(userId);

                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }
        });


    }

    public Circle createCircle(LatLng latLng, GoogleMap googleMap) {
        Circle circle = googleMap.addCircle(new CircleOptions()
                .center(latLng)
                .radius(RADIUS)
                .strokeWidth(10)
                .strokeColor(Color.argb(128, 173, 216, 230))
                .fillColor(Color.argb(24, 30, 144, 255))
                .clickable(true));


        return circle;
    }

    public String getStreet(LatLng location, Context c, Geocoder g) {
        String street = "";
        //Classe que fornece a localização da cidade

        // Geocoder geocoder = new Geocoder(c);
        Geocoder geocoder = g;
        List myLocation = null;

        try {
            //Obtendo os dados do endereço
            myLocation = geocoder.getFromLocation(location.latitude, location.longitude, 1);


        } catch (IOException e) {
            e.printStackTrace();
        }
        // Log.d("My Location", myLocation.toString());
        if (myLocation != null && myLocation.size() > 0) {
            Address address = (Address) myLocation.get(0);
            //Pega nome da cidade
            String city = address.getLocality();
            //Pega nome da rua
            street = address.getAddressLine(0);
        } else {
            street = "Endereço não encontrado";
        }

        return street;
    }

    private void Vibrar() {
        Vibrator rr = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
        long milliseconds = 200;
        rr.vibrate(milliseconds);
    }
}
