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
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import desenvolvimentoads.san.DAO.ConfigFireBase;
import desenvolvimentoads.san.Marker.MarkerTag;
import desenvolvimentoads.san.Observer.SharedContext;

public class MapsSegundo extends SupportMapFragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;

    private static final String TAG = "MapsSegundo";

    private FirebaseAuth mAuth = com.google.firebase.auth.FirebaseAuth.getInstance();
    private FirebaseUser currentUser = mAuth.getCurrentUser();
    private String userId = currentUser.getUid();

//    MenuInicial menuInicial = new MenuInicial();
//    String userId = menuInicial.getUsers();

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
        markerHashMap.clear();

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

                    if (markerTag.getIdUser().equals(userId)){
                        markerimage.setImageResource(R.mipmap.ic_maker_cinza);
                    }else{
                        markerimage.setImageResource(R.mipmap.ic_maker_cinza_star);
                    }



                    street.setText(markerTag.getStreet());

                    location.setText("Latitude: " + markerTag.getPosition().latitude + "\nLongitude: " + markerTag.getPosition().longitude);

                    datetime.setText(convertTime(markerTag.getFim()));
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


    public void getOldFirebase() {
        getServerTime();
        final AtomicInteger count = new AtomicInteger();

        mDatabaseReference = ConfigFireBase.getFirebase();

        mDatabaseReference = ConfigFireBase.getFirebase();
        mDatabaseReference.child("Marker").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (snapshot != null && snapshot.getValue() != null) {
                        Log.d(TAG, "onDataChange: "+snapshot.getValue());
                        MarkerTag markerTag = new MarkerTag();
                        markerTag.setIdUser((String)snapshot.child("id").getValue());
                        markerTag.setId((String)snapshot.child("id").getValue());
                        markerTag.setStreet((String)snapshot.child("street").getValue());
                        markerTag.setFim((Long) snapshot.child("fim").getValue());
                        markerTag.setLatitude((Double) snapshot.child("latitude").getValue());
                        markerTag.setLongitude((Double) snapshot.child("longitude").getValue());

                        if (snapshot.child("fim").getValue() != null) {
                            MarkerOptions markerOption = new MarkerOptions();
                            markerOption.position(new LatLng(((Double) snapshot.child("latitude").getValue()) ,((Double) snapshot.child("longitude").getValue())));
                            markerOption.title((String) snapshot.child("street").getValue());
                            if (getServerTime() > (Long) snapshot.child("fim").getValue() && !snapshot.child("Denunciar").child(userId).exists()) {
                                Log.d(TAG, "onDataChange: add "+snapshot.child("id").getValue());
                                if (snapshot.child("idUser").getValue().equals(userId)) {
                                    markerOption.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_maker_cinza));
                                    Marker marker = mMap.addMarker(markerOption);
                                    markerHashMap.put(marker, markerTag);
                                }
                                if (snapshot.child("Validar").child(userId).exists()) {
                                    markerOption.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_maker_cinza_star));
                                    Marker marker = mMap.addMarker(markerOption);
                                    markerHashMap.put(marker, markerTag);
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

            if (latString != null && lngString != null) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latPrefers, lngPrefers), 16.0f));
                getOldFirebase();
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
}
