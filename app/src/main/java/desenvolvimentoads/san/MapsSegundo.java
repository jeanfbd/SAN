package desenvolvimentoads.san;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import desenvolvimentoads.san.DAO.ConfigFireBase;
import desenvolvimentoads.san.Marker.MarkerTag;
import desenvolvimentoads.san.Observer.SharedContext;

public class MapsSegundo extends SupportMapFragment implements OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;

    private static final String TAG = "MapsSegundo";

    private FirebaseAuth mAuth = com.google.firebase.auth.FirebaseAuth.getInstance();
    private FirebaseUser currentUser = mAuth.getCurrentUser();
    private String userId = currentUser.getUid();
    //String userId = "123";
    private DatabaseReference mDatabaseReference;
    private FirebaseDatabase firebaseDatabase;
    public static HashMap<Marker, MarkerTag> markerHashMap = new HashMap<>();

    /*SharedPrefers*/
    SharedPreferences myPreferences;
    SharedPreferences.Editor edit;
    double latPrefers;
    double lngPrefers;
    String latString;
    String lngString;

    private long timestamp;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getMapAsync(this);

       /*usando o prefers para popular the last location from user..*/
        myPreferences = getContext().getSharedPreferences("Location", Context.MODE_PRIVATE);
        latString = myPreferences.getString("lat", null);
        lngString = myPreferences.getString("lng", null);
        Log.d(TAG, "SharedPreferences: " + lngString);
        if (latString != null && lngString != null) {
            latPrefers = Double.valueOf(latString);
            lngPrefers = Double.valueOf(lngString);
            Log.d(TAG, "onCreate: LAT: " + latPrefers);
            Log.d(TAG, "onCreate: LNG: " + lngString);
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {
        getRaioOldFirebase(latLng.latitude, latLng.longitude, 50.0);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mapConfig();

        mMap.setInfoWindowAdapter(new InfoWindowAdapter() {

            // Use default InfoWindow frame
            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            // Defines the contents of the InfoWindow
            @Override
            public View getInfoContents(Marker arg0) {
                LayoutInflater li = LayoutInflater.from(getContext());
                View v = li.inflate(R.layout.info_window_layout, null);

                if (markerHashMap != null) {
                    MarkerTag markerTag = markerHashMap.get(arg0);

                    TextView street = (TextView) v.findViewById(R.id.street);

                    TextView location = (TextView) v.findViewById(R.id.location);

                    TextView datetime = (TextView) v.findViewById(R.id.datetime);

                    ImageView markerimage = (ImageView) v.findViewById(R.id.markerimage);

                    markerimage.setImageResource(R.mipmap.ic_maker_cinza);

                    street.setText(markerTag.getStreet());

                    location.setText("Latitude: " + markerTag.getPosition().latitude + "\nLongitude: " + markerTag.getPosition().longitude);

                    datetime.setText(convertTime(Long.parseLong(markerTag.getId())));
                }
                return v;

            }
        });

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                marker.hideInfoWindow();
            }
        });
    }


    public void getRaioOldFirebase(Double lat, Double lng, Double radius) {
        getServerTime();
//        hashMapClear();

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
                            MarkerTag markerTag = dataSnapshot.getValue(MarkerTag.class);
                            markerTag.setId("" + dataSnapshot.child("fim").getValue());
                            if (dataSnapshot.child("fim").getValue() != null) {
                                MarkerOptions markerOption = new MarkerOptions();
                                markerOption.position(markerTag.getPosition());
                                markerOption.title((String) dataSnapshot.child("street").getValue());
                                if (getServerTime() > (Long) dataSnapshot.child("fim").getValue() && !dataSnapshot.child("Denunciar").child(userId).exists()) {
                                    if (dataSnapshot.child("idUser").getValue().equals(userId)) {
                                        markerOption.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_maker_cinza));
                                        Marker marker = mMap.addMarker(markerOption);
                                        markerHashMap.put(marker, markerTag);
                                    }
                                    if (dataSnapshot.child("Validar").child(userId).exists()) {
                                        markerOption.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_maker_cinza_star));
                                        Marker marker = mMap.addMarker(markerOption);
                                        markerHashMap.put(marker, markerTag);
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


    public void mapConfig() {
        if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.getUiSettings().setMapToolbarEnabled(false);
            mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.setOnMarkerClickListener(this);
            mMap.setOnMapClickListener(this);

            if (latString != null && lngString != null) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latPrefers, lngPrefers), 16.0f));
                getRaioOldFirebase(latPrefers, lngPrefers, 50.0);
            }

        /*Listener responsavel adicionar o botão da localização*/
            mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                @Override
                public boolean onMyLocationButtonClick() {
                    if (latString != null && lngString != null) {
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(latPrefers, lngPrefers)));
                    }
                    return false;
                }
            });
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.d(TAG, "onMarkerClick: Clicou: " + marker.getTitle());

        return false;
    }

    public String convertTime(long time) {
        Date date = new Date(time);
        Format format = new SimpleDateFormat("dd/MM/yyyy-HH:mm:ss");

        return format.format(date);
    }

//    public void hashMapClear() {
//        HashMap<String, Marker> markerHashMapTemp = markerHashMap;
//        for (Map.Entry<String, Marker> markerTemp : markerHashMapTemp.entrySet()) {
//            MarkerTag markerTag = (MarkerTag) markerHashMapTemp.get(markerTemp.getKey()).getTag();
//            markerTemp.getValue().remove();
//            markerHashMapTemp.get(markerTemp.getKey()).remove();
//        }
//    }
}
