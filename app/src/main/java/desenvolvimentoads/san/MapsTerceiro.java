package desenvolvimentoads.san;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
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

import java.util.HashMap;
import java.util.Map;

import desenvolvimentoads.san.DAO.ConfigFireBase;
import desenvolvimentoads.san.Marker.MarkerDialog;
import desenvolvimentoads.san.Marker.MarkerTag;
import desenvolvimentoads.san.Observer.Action;
import desenvolvimentoads.san.Observer.ActionObserver;
import desenvolvimentoads.san.Observer.SharedContext;

public class MapsTerceiro extends SupportMapFragment implements OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, ActionObserver {

    private GoogleMap mMap;
    int RQS_GooglePlayServices = 0;
    private static final int MY_PERMISSION_REQUEST_CODE = 2508;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 1991;
    private static final String TAG = "MapsTerceiro";

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    private static int UPDATE_INTERVAL = 40000;
    private static int FATEST_INTERVAL = 20000;
    private static int DISPLACEMENT = 0;

    private FirebaseAuth mAuth = com.google.firebase.auth.FirebaseAuth.getInstance();
    private FirebaseUser currentUser = mAuth.getCurrentUser();
    //private String userId = currentUser.getUid();

    MenuInicial menuInicial = new MenuInicial();
    String userId = menuInicial.getUsers();


    private DatabaseReference mDatabaseReference;
    private FirebaseDatabase firebaseDatabase;

    GeoFire geoFire;
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

    public static HashMap<String, String> keyFirebaseHashMap = new HashMap<>();
    public static HashMap<String, String> keyAppHashMap = new HashMap<>();
    public static HashMap<String, Marker> markerHashMap = new HashMap<>();
    public static HashMap<String, GeoQueryEventListener> notificationHashMap = new HashMap<>();
    public static final int REQUEST_PERMISSION_LOCATION = 10;

    GeoQueryEventListener notificationListener;
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getMapAsync(this);

        mContext = getContext();
        mDatabaseReference = ConfigFireBase.getFirebase();
        firebaseDatabase = ConfigFireBase.getFirebaseDatabase();
        geoFire = new GeoFire(firebaseDatabase.getReferenceFromUrl("https://websan-46271.firebaseio.com/MyLocation/"));
        setUpLocation();

        /*usando o prefers para popular the last location from user..*/
        myPreferences = getContext().getSharedPreferences("Location", Context.MODE_PRIVATE);
        latString = myPreferences.getString("lat", null);
        lngString = myPreferences.getString("lng", null);
        if (latString != null && lngString != null) {
            latPrefers = Double.valueOf(latString);
            lngPrefers = Double.valueOf(lngString);
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
       /*isso tudo esta sendo ignorado por que esta indo para o menu inicial no super..*/
        Log.i("teste", "permission terceiro called ");
        switch (requestCode) {

            case MY_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    buildGoogleApiClient();
                    createLocationRequest();
                    buildLocationSettingsRequest();
                    if (checkPlayService()) {
                        displayLocation();
                    }
                }

                break;
            default:
                Log.i("teste", "Permission neged!!!");
        }
    }

    /*0*/
    private void setUpLocation() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            //Request runtime permission
            ActivityCompat.requestPermissions(getActivity(), new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, MY_PERMISSION_REQUEST_CODE);


        } else {
            buildGoogleApiClient();
            createLocationRequest();
            buildLocationSettingsRequest();
            if (checkPlayService()) {


                displayLocation();
                checkLocationSettings();
            }

        }
    }


    /*1*/
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();


    }

    /*2*/
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);


    }

    /* 3 - Construindo o location Settings request*/
    protected void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    /*3.5*/
    private boolean checkPlayService() {

        /*Se npegar a instancia do googleservice o fused não funciona -.-*/
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int resultCode = googleAPI.isGooglePlayServicesAvailable(getActivity());
        if (resultCode == ConnectionResult.SUCCESS) {
            mGoogleApiClient.connect();
            return true;
        } else {
            googleAPI.getErrorDialog(getActivity(), resultCode, RQS_GooglePlayServices);

            return false;

        }
    }

    /* 4 - criando o method que irá checar o status do locationSettings */
    protected void checkLocationSettings() {
        Log.i("teste", "CHECKLOCATION ");


        recreateGoogleRefers();
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(
                        mGoogleApiClient,
                        mLocationSettingsRequest
                );

        Log.i("teste", "location pronto 1?");
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {

            /* 5 Aqui que controla caso o location não estiver pronto*/
            @Override
            public void onResult(LocationSettingsResult locationSettingsResult) {

                Log.i("teste", "location pronto2 ?");
                final Status status = locationSettingsResult.getStatus();
                switch (status.getStatusCode()) {
                    /*Pronto.. as outras opções são opcionais a tirando a required ali.*/
                    case LocationSettingsStatusCodes.SUCCESS:


                        //Toast.makeText(getContext(), "Location is already on.", Toast.LENGTH_SHORT).show();
                        mapConfig();
                        startLocationUpdates();
                        Log.i("teste", "location pronto3 ?");
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:

                        //Toast.makeText(getContext(), "Location dialog will be open", Toast.LENGTH_SHORT).show();

                        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            Log.i("teste", "location pronto4 ?");

                            /*Aqui só é chamado quando o user nega a primeira vez, nessa segunda vez o android deixa lançar uma mensagem para o user.*/
                            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                                Log.i("teste", "location pronto5 ?");
                                //     try {
                                // Show the dialog by calling startResolutionForResult(), and check the result
                                // in onActivityResult().

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

                                Log.i("teste", "location pronto7 ?");
                                ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION_LOCATION);


                            }


                        } else {
                            Log.i("teste", "location pronto8 ?");

                        }


                        //move to step 6 in onActivityResult to check what action user has taken on settings dialog
                        //    status.startResolutionForResult(getActivity(), REQUEST_CHECK_SETTINGS);
                        //   } catch (IntentSender.SendIntentException e) {

                        //    }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.i("teste", "location pronto9 ?");
                        break;
                }
            }
        });
    }

    /*Aqui que vai ser o callback do status resolution do step 4/5 quando o user faz uma ação ali no settings do android para mudar o location aqui q verifica mas nem sempre funciona o ok*/
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.i("Teste", "User agreed to make required location settings changes.");
                        mapConfig();
                        startLocationUpdates();
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i("Teste", "User chose not to make required location settings changes.");
                        /*Geralmente ele cai aqui mesmo qdo mudamos o location para ok, ae ele faz uma volta danada e verifica tudo dnovo. para dar certo no final*/
                        checkLocationSettings();
                        break;
                }
                break;
        }
    }

    protected void startLocationUpdates() {

        if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION_LOCATION);
            Log.i("teste", "location pronto331 ?");
        } else {

            Log.i("teste", "location pronto332 ?");
            goAndDetectLocation();
        }

    }


    public void goAndDetectLocation() {

        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION_LOCATION);

            Log.i("teste", "location pronto33222 ?");
        } else {

           /* LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            Toast.makeText(getContext(),""+LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient),
                    Toast.LENGTH_SHORT).show();
*/
            Log.i("teste", "location pronto33333 ?");
            recreateGoogleRefers();
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient,
                    mLocationRequest, this
            ).setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(Status status) {
                /* usa isso para saber se esta habilitado o location updates*/
                    mRequestingLocationUpdates = true;
                }
            });
            started = true;


        }

    }

    /*é bom para colocar qdo o app entrar em resumo para parar o fused..*/
    protected void stopLocationUpdates() {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.
        recreateGoogleRefers();
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient,
                this
        ).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
/* usa isso para saber se esta habilitado o location updates*/
                mRequestingLocationUpdates = false;
                //   setButtonsEnabledState();
            }
        });
    }

    /*4*/
    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            //Request runtime permission
            ActivityCompat.requestPermissions(getActivity(), new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, MY_PERMISSION_REQUEST_CODE);
        } else {

            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            Log.i("teste", "fused  " + mLastLocation);

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
                        //    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 16.0f));


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
                checkGpsPermission();
                checkPermission();
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
        mMap.setOnMapClickListener(this);

        mapConfig();

        if (mapRebuildOk) {
            if (latString != null && lngString != null) {


                getRaioFirebase(latPrefers, lngPrefers, 5.00);
                Log.i("teste", "prefers getRaio inicial ok...");

            }

        }

        //   mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-23.6202800, -45.4130600), 15.0f));
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


        Log.i("teste", "fim rebuilding...");


    }

    @Override
    public void onMapClick(LatLng latLng) {
        checkGpsPermission();
        checkPermission();
        Log.i("teste", "Map clicked");
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
        Log.i("teste", "Location changed markerhashmap size : " + markerHashMap.size());

        mLastLocation = location;
        if (mCurrent != null) {
            mCurrent.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_my_location));
        }

        if (mLastLocation != null) {
            getRaioFirebase(mLastLocation.getLatitude(), mLastLocation.getLongitude(), 5.0);
            displayLocation();

            myLat = location.getLatitude();
            myLong = location.getLongitude();

            sharedContext.createPrefers("Location", "lat", String.valueOf(mLastLocation.getLatitude()));
            sharedContext.createPrefers("Location", "lng", String.valueOf(mLastLocation.getLongitude()));


        }


        rebuildGeoQuery2();


    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // displayLocation();
        Log.i("teste", "OnConnected !!!Q");
        if (started) {
            startLocationsUpdates();
        }


    }

    /*Em teoria se voce da stop seria aqui que daria o start, é bem melhor que o outro metodo que esta tem quase o mesmo nome sem o S*/
    private void startLocationsUpdates() {

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            /*Aqui só é chamado quando o user nega a primeira vez, nessa segunda vez o android deixa lançar uma mensagem para o user.*/
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                Log.i("teste", "checkGpsPermission Rationale");
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

                Log.i("teste", "checkGpsPermission normal");
                ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION_LOCATION);


            }

        } else {
            Log.i("teste", "permission track This one has called 2");

            recreateGoogleRefers();
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);


        }

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
        if (true) {
            getServerTime();
            Log.i("teste", "getraiofirebase ");
            mDatabaseReference = ConfigFireBase.getFirebase();
            firebaseDatabase = ConfigFireBase.getFirebaseDatabase();
            GeoFire geoFire2 = new GeoFire(firebaseDatabase.getReferenceFromUrl("https://websan-46271.firebaseio.com/marker_location/"));

            final GeoQuery geoQuery = geoFire2.queryAtLocation(new GeoLocation(lat, lng), radius);
            if (mLastLocation != null) {
                geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                    @Override
                    public void onKeyEntered(final String key, GeoLocation location) {
                        keyFirebaseHashMap.clear();
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

    public boolean hashMapClear() {
        HashMap<String, Marker> markerHashMapTemp = markerHashMap;
        for (Map.Entry<String, Marker> markerTemp : markerHashMapTemp.entrySet()) {
            MarkerTag markerTag = (MarkerTag) markerHashMapTemp.get(markerTemp.getKey()).getTag();
            markerTag.getCircle().remove();
            markerTemp.getValue().remove();
            markerHashMapTemp.get(markerTemp.getKey()).remove();
        }
        markerHashMap.clear();
        if (mCurrent != null) {
            mCurrent.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_my_location_fine));
        }

        return true;

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
        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.i("teste", "checkGpsPermission ?");

            /*Aqui só é chamado quando o user nega a primeira vez, nessa segunda vez o android deixa lançar uma mensagem para o user.*/
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                Log.i("teste", "checkGpsPermission Rationale");
                //     try {
                // Show the dialog by calling startResolutionForResult(), and check the result
                // in onActivityResult().

                //
                // User selected the Never Ask Again Option Change settings in app settings manually
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


                if (sharedContext.isFirstTimeAskingPermission("Location")) {
                    Log.i("teste", "checkGpsPermission normal");
                    sharedContext.firstTimeAskingPermission("Location", false);
                    ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION_LOCATION);

                } else {

                    /*never asked selecionado...*/
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                    alertDialogBuilder.setTitle("Permissão necessaria");
                    alertDialogBuilder
                            .setMessage("" +
                                    "\n O App San utiliza de dados da localização para seu funcionamento." + "\n Ao selecionar para nunca mais ser requisitado permissão e negar o aplicativo deixa de funcionar."
                                    + "\n Para que ele funcione agora será necessario mudar manualmente a permission do location nas configurações do App")
                            .setCancelable(false)
                            .setPositiveButton("Configurações", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    Uri uri = Uri.fromParts("package", getContext().getPackageName(), null);
                                    intent.setData(uri);
                                    startActivity(intent);// step 6

                                }
                            });

                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();

                }


            }

/*Se o user mudar durante o uso o location e botar never ask again e voltar o location para true, ae essa parte faz a checagem novamente...*/
        } else if (mapRebuildOk == false) {
            mapConfig();
            setUpLocation();
            MenuInicial.permissionOk = false;

        }
    }


    public void checkPermission() {
        displayLocation();

        if (MenuInicial.permissionOk) {
            mapConfig();
            setUpLocation();
            Log.i("teste", "permission ok and checked");
            MenuInicial.permissionOk = false;
        } else {

            Log.i("teste", "permission ok and checked  ??");


        }

    }


    public void mapConfig() {

        /*Checkando a permissão dos acessos, vulgo frescura do Android..*/
        if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) ==
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

            mapRebuildOk = true;


        } else {
            //Request runtime permission
            ActivityCompat.requestPermissions(getActivity(), new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, MY_PERMISSION_REQUEST_CODE);
        }
    }

    public void recreateGoogleRefers() {

        if (mGoogleApiClient == null) {
            buildGoogleApiClient();
            checkPlayService();
        }

        if (mLocationSettingsRequest == null) {
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


}
