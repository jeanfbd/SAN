package desenvolvimentoads.san;


import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
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
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Random;

public class MapsTerceiro extends SupportMapFragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleMap mMap;

    private static final int MY_PERMISSION_REQUEST_CODE = 2508;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 1991;
    private static final String TAG = "MapsTerceiro";

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLasLocation;

    private static int UPDATE_INTERVAL = 5000;
    private static int FATEST_INTERVAL = 3000;
    private static int DISPLACEMENT = 10;

    DatabaseReference mDatabaseReference;
    GeoFire geoFire;
    Marker mCurrent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getMapAsync(this);

        mDatabaseReference = FirebaseDatabase.getInstance().getReference("MyLocation");
        geoFire = new GeoFire(mDatabaseReference);
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

            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.getUiSettings().setMapToolbarEnabled(false);
            mMap.getUiSettings().setZoomControlsEnabled(true);

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
        mLasLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLasLocation != null) {
            final double latitude = mLasLocation.getLatitude();
            final double longitude = mLasLocation.getLongitude();

            //Update to Firebase
            geoFire.setLocation("Você", new GeoLocation(latitude, longitude), new GeoFire.CompletionListener() {
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
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 12.0f));

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
        mMap = googleMap;

        LatLng dangerous_area = new LatLng(-23.6202800, -45.4130600);
        mMap.addCircle(new CircleOptions()
                .center(dangerous_area)
                .radius(500)
                .strokeColor(Color.BLUE)
                .fillColor(0x22000FF)
                .strokeWidth(5.0f)

        );

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-23.6202800, -45.4130600), 15.0f));

        //Add GeoQuery
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(dangerous_area.latitude, dangerous_area.longitude), 0.5);

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
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
                Log.d("MOVE", String.format("Movendo-se pela area de alagamento [%f/%f]", key, location.latitude, location.longitude));
            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                Log.e("Error", "" + error);
            }
        });
    }

    @Override
    public void onLocationChanged(Location location) {
        mLasLocation = location;
        displayLocation();
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

}
