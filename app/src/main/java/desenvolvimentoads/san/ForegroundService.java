package desenvolvimentoads.san;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
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
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import desenvolvimentoads.san.DAO.ConfigFireBase;
import desenvolvimentoads.san.notification.NotificationID;

public class ForegroundService extends Service implements GoogleApiClient.ConnectionCallbacks{
    public static boolean IS_SERVICE_RUNNING = false;
    public static String STOP = "STOP";
    public static String START = "PLAY";
    public static int ID_FOREGROUND_SERVICE = 11;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private static int UPDATE_INTERVAL = 40000;
    private static int FATEST_INTERVAL = 20000;
    private static int DISPLACEMENT = 0;
    protected LocationSettingsRequest mLocationSettingsRequest;
    private Location mLastLocation;
    Context context = this;
    boolean started = false;
    private long timestamp;
    private DatabaseReference mDatabaseReference;
    private FirebaseDatabase firebaseDatabase;
    GeoFire geoFire;
    String userId ="123";
    HashMap<String, String> foregroundHashMap = new HashMap<>();

    @Override
    public void onCreate() {
        super.onCreate();


        mDatabaseReference = ConfigFireBase.getFirebase();
        firebaseDatabase = ConfigFireBase.getFirebaseDatabase();
        geoFire = new GeoFire(firebaseDatabase.getReferenceFromUrl("https://websan-46271.firebaseio.com/marker_location/"));
        if(MapsTerceiro.alertHashMap != null && MapsTerceiro.alertHashMap.size()>0){
            foregroundHashMap = MapsTerceiro.alertHashMap;

        }
        else{

            foregroundHashMap = new HashMap<>();
        }


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("teste", "intent");
        if (intent.getAction().equals(START)) {
            Log.i("teste", "Received Start Foreground Intent ");
            IS_SERVICE_RUNNING = true;
            showNotification();


            //  Toast.makeText(this, "Service Started!", Toast.LENGTH_SHORT).show();
        } else if (intent.getAction().equals(STOP)) {
            Log.i("teste", "Received Stop Foreground Intent");
            if(started){
                Log.i("teste", "started");
                stopLocationUpdates();
                IS_SERVICE_RUNNING = false;
                stopForeground(true);
                stopSelf();
            }

        }
        return START_STICKY;
    }

    private void showNotification() {



        Intent stopIntent = new Intent(this, ForegroundService.class);
        stopIntent.setAction(STOP);
        PendingIntent pStopIntent = PendingIntent.getService(this, 0,
                stopIntent, 0);


        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("San ForegroundService")
                .setTicker("San")
                .setContentText("Serviço de alerta SAN")
                .setSmallIcon(R.mipmap.ic_maker_vermelho)
                .setOngoing(true)
                .addAction(android.R.drawable.ic_media_play, "Click aqui para Desativar",
                        pStopIntent).build();
        startForeground(ID_FOREGROUND_SERVICE,
                notification);

        setUpLocation();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("teste", "In onDestroy");
        Toast.makeText(this, "Service Detroyed!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Used only in case if services are bound (Bound Services).
        return null;
    }

    public void checkGpsPermission() {
        Log.i("teste", "checkGpsPermission >.< ");
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {


        } else {
            Log.i("teste", "checkGpsPermission2  ");
        }


    }

    /*0*/
    private void setUpLocation() {
        Log.i("teste", "iniciou  ");
        checkGpsPermission();
        buildGoogleApiClient();
        createLocationRequest();
        buildLocationSettingsRequest();
        if (checkPlayService()) {


            displayLocation();
            checkLocationSettings();
            started = true;

        }

    }

    /*1*/

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
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
        Log.i("teste","checkpaly");
        /*Se npegar a instancia do googleservice o fused não funciona -.-*/
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int resultCode = googleAPI.isGooglePlayServicesAvailable(this);
        if (resultCode == ConnectionResult.SUCCESS) {
            mGoogleApiClient.connect();
            Log.i("teste","playservice connected");

            return true;
        } else {
            Log.i("teste","playservice not connected");
            return false;

        }
    }

    /* 4 - criando o method que irá checar o status do locationSettings */
    protected void checkLocationSettings() {


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


                        startLocationUpdates();
                        Log.i("teste", "location pronto3 ?");
                        break;

                }
            }
        });
    }

    protected void startLocationUpdates() {
        Log.i("teste","start");
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {


        } else {


            goAndDetectLocation();
        }

    }

    LocationListener locationListenerGPS = new LocationListener() {
        @Override
        public void onLocationChanged(android.location.Location location) {


            Log.i("teste","location ok esta mudando..");
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
             getRaioFirebase(latitude,longitude,5.0);


        }

    };

        public void notification(){





            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.mipmap.ic_maker_vermelho)
                            .setContentTitle("Alerta")
                            .setTicker("Foram encontrados alertas proximos da sua localidade")
                            .setContentText("Foram encontrados alertas proximos da sua localidade")
                            .setSubText("Alerta!");







            mBuilder.setAutoCancel(true);
            NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            android.app.Notification n = mBuilder.build();
            //Frescura de vibrar.
             n.vibrate = new long[]{150,300,150,600};



            mNotificationManager.notify(NotificationID.getID(),n);


        }

    public void goAndDetectLocation() {
        Log.i("teste","goanddetect");
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

        } else {


            // recreateGoogleRefers();
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                    mLocationRequest, locationListenerGPS);


        }

    }

    /*é bom para colocar qdo o app entrar em resumo para parar o fused..*/
    protected void stopLocationUpdates() {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.
        recreateGoogleRefers();
        if(LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient,
                locationListenerGPS) !=null) {


        }
    }

    /*4*/
    @SuppressLint("MissingPermission")
    private void displayLocation() {
        Log.i("teste","display");
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            if (mLastLocation != null) {
                final double latitude = mLastLocation.getLatitude();
                final double longitude = mLastLocation.getLongitude();


            }

    }

    public void recreateGoogleRefers(){
        Log.i("teste","recreate");
        if(mGoogleApiClient == null){
            buildGoogleApiClient();
            checkPlayService();
        }

        if(mLocationSettingsRequest == null){
            createLocationRequest();
            buildLocationSettingsRequest();
        }

    }





    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    public void getRaioFirebase(Double lat, Double lng, Double radius) {

        getServerTime();

        mDatabaseReference = ConfigFireBase.getFirebase();
        firebaseDatabase = ConfigFireBase.getFirebaseDatabase();

        final GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(lat, lng), radius);
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

                                    if (getServerTime() < (Long) dataSnapshot.child("fim").getValue() && !dataSnapshot.child("Denunciar").child(userId).exists()) {


                                        Log.i("teste","Marker found");
                                        notification();
                                        if(foregroundHashMap.get(key) == null){

                                            foregroundHashMap.put(key,key);


                                        }



                                    } else {

                                        if(foregroundHashMap.get(key) != null){

                                            foregroundHashMap.remove(key);


                                        }

                                        Log.i("teste","Marker removed");
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

