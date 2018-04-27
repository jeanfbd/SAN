package desenvolvimentoads.san;


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
import java.util.List;
import java.util.Map;

import desenvolvimentoads.san.DAO.ConfigFireBase;
import desenvolvimentoads.san.Marker.MarkerTag;

public class MapsSegundo extends SupportMapFragment implements OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;

    private static final String TAG = "MapsSegundo";

    private FirebaseAuth mAuth = com.google.firebase.auth.FirebaseAuth.getInstance();
    private FirebaseUser currentUser = mAuth.getCurrentUser();
    // private String userId = currentUser.getUid();
    String userId = "123";
    private DatabaseReference mDatabaseReference;
    private FirebaseDatabase firebaseDatabase;
    public static HashMap<Marker, MarkerTag> markerHashMap = new HashMap<>();

    private long timestamp;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getMapAsync(this);
    }

    @Override
    public void onMapClick(LatLng latLng) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setInfoWindowAdapter(new InfoWindowAdapter() {

            // Use default InfoWindow frame
            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            // Defines the contents of the InfoWindow
            @Override
            public View getInfoContents(Marker arg0) {
                MarkerTag markerTag = markerHashMap.get(arg0);

                LayoutInflater li = LayoutInflater.from(getContext());
                View v = li.inflate(R.layout.info_window_layout, null);

                TextView street = (TextView) v.findViewById(R.id.street);

                TextView location = (TextView) v.findViewById(R.id.location);

                TextView datetime = (TextView) v.findViewById(R.id.datetime);

                ImageView markerimage = (ImageView) v.findViewById(R.id.markerimage);

                markerimage.setImageResource(R.mipmap.ic_maker_cinza);

                street.setText(markerTag.getStreet());

                location.setText("Latitude: "+markerTag.getPosition().latitude+"\nLongitude: "+markerTag.getPosition().longitude);

                datetime.setText(convertTime(Long.parseLong(markerTag.getId())));

                return v;

            }
        });

        mapConfig();
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-23.61989710864952, -45.41503362357616), 16.0f));
        getRaioOldFirebase(-23.61989710864952, -45.41503362357616, 50.0);
    }


    public void getRaioOldFirebase(Double lat, Double lng, Double radius) {
        getServerTime();
//        hashMapClear();
        Log.d(TAG, "getRaioOldFirebase: ");

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
                                markerTag.setId(""+dataSnapshot.child("fim").getValue());
                                Log.d(TAG, "MarkerTag: "+markerTag.getId());
                                Log.d(TAG, "MarkerTag: "+markerTag.getStreet());
                                if (dataSnapshot.child("fim").getValue() != null) {
                                    MarkerOptions markerOption = new MarkerOptions();
                                    markerOption.position(markerTag.getPosition());
                                    markerOption.title((String) dataSnapshot.child("street").getValue());
                                    Log.d(TAG, "onDataChange: Achou Datasnapshot");
                                    if (getServerTime() > (Long) dataSnapshot.child("fim").getValue() && !dataSnapshot.child("Denunciar").child(userId).exists()) {
                                        Log.d(TAG, "onDataChange: Achou Marker pra inserir");
                                        if (dataSnapshot.child("idUser").getValue().equals(userId)) {
                                            markerOption.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_maker_cinza));
                                        } else {
                                            if (dataSnapshot.child("Validar").child(userId).exists()) {
                                                markerOption.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_maker_cinza_star));
                                            }
                                        }
                                        Marker marker = mMap.addMarker(markerOption);
                                        markerHashMap.put(marker, markerTag);
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
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.d(TAG, "onMarkerClick: Clicou: "+marker.getTitle());

        return false;
    }

    public String convertTime(long time){
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
