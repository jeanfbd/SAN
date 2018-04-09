package desenvolvimentoads.san;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.firebase.geofire.core.GeoHash;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import desenvolvimentoads.san.DAO.ConfigFireBase;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MapsTerceiro {//extends SupportMapFragment implements OnMapReadyCallback, GoogleMap.OnMapClickListener, LocationListener {
//
//    private DatabaseReference mDatabase;
//    private FirebaseDatabase firebaseDatabase;
//    private GoogleMap mMap;
//    public static GoogleMap googleMapFinal;
//    private static Circle circle;
//    private static Long timestamp;
//    private LatLng inicial = null;
//    public static HashMap<Marker, String> mHashMap = new HashMap<Marker, String>();
//    public static HashMap<String, Marker> markerHashMap = new HashMap<>();
//    private List<Marker> listMarkers = new ArrayList<Marker>();
//    private List<MarkerBD> markerBDList = new ArrayList<MarkerBD>();
//    private Long creationDate;
//
//    private LayoutInflater mInflater;
//
//    private LocationManager locationManager;
//
//    private static final String TAG = "ExemploProFragmentV2";
//
//
//    private static final int MY_PERMISSIONS_ACCESS_COARSE_LOCATION = 0;
//
//    private AlertDialog alerta;
//    private boolean validation = true;
//    private int image = R.mipmap.ic_maker_amarelo_star;
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        getMapAsync(this);
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        try {
//            locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
//            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
//        } catch (SecurityException ex) {
//            Log.e(TAG, "Erro", ex);
//        }
//    }
//
//    @Override
//    public void onPause() {
//        super.onPause();
//        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
//        locationManager.removeUpdates(this);
//    }
//
//    /**
//     * Manipulates the map once available.
//     * This callback is triggered when the map is ready to be used.
//     * This is where we can add markers or lines, add listeners or move the camera. In this case,
//     * we just add a marker near Sydney, Australia.
//     * If Google Play services is not installed on the device, the user will be prompted to install
//     * it inside the SupportMapFragment. This method will only be triggered once the user has
//     * installed Google Play services and returned to the app.
//     */
//
//
//    @Override
//    public void onMapReady(final GoogleMap googleMap) {
//        try {
//            locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
//            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//            if (location != null) {
//                Log.i(TAG, "Minha Localização: Lat: " + location.getLatitude() + "  Lng: " + location.getLongitude());
//            }
//            googleMapFinal = googleMap;
//            mMap = googleMap;
//            mMap.setOnMapClickListener(this);
//            mMap.getUiSettings().setZoomControlsEnabled(true);
//            mMap.setMinZoomPreference(10);
//            //loadMarkers();
////            getAllMarkers();
//
////            getRaio("-23.6202800","-45.4130600","1.5");
////            getActivity().startService(new Intent(getActivity(),ServiceThread.class));
////            getAllFirebase();
//            getRaioFirebase(-23.6202800, -45.4130600, 5.00);
////            getTimestampFirebase();
//
//
//            //Estilos de mapas
//            //mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
//            //mMap.setMapStyle();
//
//
//            googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
//
//                @Override
//                public void onMapLongClick(LatLng arg0) {
//                    // TODO Auto-generated method stub
//                    inicial = arg0;
//                    dialogAdd(arg0);
//
//                }
//            });
//
//            googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
//                @Override
//                public boolean onMarkerClick(Marker marker) {
//                    final String id = mHashMap.get(marker);
//                    Log.e("Real Marker ID", id + "");
//                    return false;
//                }
//            });
//
//            googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
//                @Override
//                public View getInfoWindow(Marker marker) {
//                    return null;
//                }
//
//                @Override
//                public View getInfoContents(Marker marker) {
////                    final MarkerDAO markerDAO = MarkerDAO.getInstance(getContext());
////                    markerDAO.getPerMarker(marker.getId());
////                    List<MarkerBD> markerBDs = markerDAO.getPerMarker(marker.getId());
////                    final MarkerBD markerBDClass = markerBDs.get(0);
//
//                    LayoutInflater li = LayoutInflater.from(getContext());
//
//                    //inflamos o layout alerta.xml na view
//                    final View view = li.inflate(R.layout.dialog_validar, null);
//
//                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
//                    builder.setTitle("Validar Marcador");
//
//                    LinearLayout info = new LinearLayout(getContext());
//                    info.setOrientation(LinearLayout.VERTICAL);
//
////                    ImageView imageView = new ImageView(getContext());
////                    imageView.setImageResource(markerBDClass.getImage());
//
//                    TextView title = new TextView(getContext());
//                    title.setTextColor(Color.BLACK);
//                    title.setGravity(Gravity.CENTER);
//                    title.setTypeface(null, Typeface.BOLD);
//                    title.setText(marker.getTitle());
//
//                    TextView snippet = new TextView(getContext());
//                    snippet.setTextColor(Color.GRAY);
//                    snippet.setText(marker.getSnippet());
//
//                    Button btnLike = (Button) view.findViewById(R.id.btLike);
//
//                    Button btnDislike = (Button) view.findViewById(R.id.btDislike);
//
////                    btnLike.setOnClickListener(new View.OnClickListener() {
////                        @Override
////                        public void onClick(View v) {
////                            markerBDClass.setLifeTime(markerBDClass.getLifeTime() + 10);
////                            markerDAO.update(markerBDClass);
//////                            validation = false;
////                            alerta.dismiss();
////                        }
////                    });
////
////                    btnDislike.setOnClickListener(new View.OnClickListener() {
////                        @Override
////                        public void onClick(View v) {
////                            markerBDClass.setLifeTime(markerBDClass.getLifeTime() - 10);
////                            markerDAO.update(markerBDClass);
////                            alerta.dismiss();
////                        }
////                    });
//
////                    info.addView(imageView);
//                    info.addView(title);
//                    info.addView(snippet);
//
//                    builder.setView(view);
//                    if (validation) {
//                        alerta = builder.create();
//                        alerta.show();
//                    }
//
//
//                    return info;
//                }
//            });
//
//            googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
//                @Override
//                public void onInfoWindowClick(Marker marker) {
//                    marker.showInfoWindow();
//                }
//            });
//
//            googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
//                @Override
//                public void onMarkerDragStart(Marker marker) {
//                    Snackbar.make(getView(), "Arraste o marcador sobre o mapa para melhor precisão", Snackbar.LENGTH_LONG)
//                            .setAction("Action", null).show();
//                }
//
//                @Override
//                public void onMarkerDrag(Marker marker) {
//                    Snackbar.make(getView(), "Confirme a posição do marcador", Snackbar.LENGTH_LONG)
//                            .setAction("Action", null).show();
//                }
//
//                @Override
//                public void onMarkerDragEnd(Marker marker) {
//                    final String id = mHashMap.get(marker);
//                    dialogDrag(marker, id);
//                }
//            });
//
//
//            if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) ==
//                    PackageManager.PERMISSION_GRANTED &&
//                    ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
//                            PackageManager.PERMISSION_GRANTED) {
//                mMap.setMyLocationEnabled(true);
//                mMap.getUiSettings().setMyLocationButtonEnabled(true);
//
//                if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
//                        android.Manifest.permission.ACCESS_COARSE_LOCATION)) {
//
//                    // Show an expanation to the user *asynchronously* -- don't block
//                    // this thread waiting for the user's response! After the user
//                    // sees the explanation, try again to request the permission.
//
//                } else {
//
//                    // No explanation needed, we can request the permission.
//
//                    ActivityCompat.requestPermissions(getActivity(),
//                            new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
//                            MY_PERMISSIONS_ACCESS_COARSE_LOCATION);
//
//                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
//                    // app-defined int constant. The callback method gets the
//                    // result of the request.
//                }
//            }
//        } catch (SecurityException ex) {
//            Log.e(TAG, "Erro", ex);
//        }
//        LatLng caragua = new LatLng(-23.6202800, -45.4130600);
//        zoomMarker(caragua, googleMapFinal);
//    }
//
//
//    @Override
//    public void onMapClick(LatLng latLng) {
//        Toast.makeText(getContext(), "Coordenadas: " + latLng.toString(), Toast.LENGTH_LONG).show();
////        getRaio(String.valueOf(latLng.latitude), String.valueOf(latLng.longitude), "10");
//        getRaioFirebase(latLng.latitude, latLng.longitude, 5.0);
////        getTimestampFirebase();
//
//    }
//
//    @Override
//    public void onLocationChanged(Location location) {
//        Toast.makeText(getActivity(), "Posição Alterada", Toast.LENGTH_LONG).show();
//        getRaioFirebase(location.getLatitude(), location.getLongitude(), 5.00);
//    }
//
//    @Override
//    public void onStatusChanged(String provider, int status, Bundle extras) {
//        Toast.makeText(getActivity(), "O Status do Provider foi Alterado", Toast.LENGTH_LONG).show();
//    }
//
//    @Override
//    public void onProviderEnabled(String provider) {
//        Toast.makeText(getActivity(), "Provider Habilitado", Toast.LENGTH_LONG).show();
//    }
//
//    @Override
//    public void onProviderDisabled(String provider) {
//        Toast.makeText(getActivity(), "Provider Desabilitado", Toast.LENGTH_LONG).show();
//    }
//
//
//    private void dialogDrag(final Marker marker, final String id) {
//        //LayoutInflater é utilizado para inflar nosso layout em uma view.
//        //-pegamos nossa instancia da classe
//        LayoutInflater li = LayoutInflater.from(getContext());
//
//        //inflamos o layout alerta.xml na view
//        final View view = li.inflate(R.layout.confirm_drag, null);
//
//        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
//        builder.setTitle("Alterar Posição");
//        final Button confirm = (Button) view.findViewById(R.id.btConfirm);
//
//        Button cancel = (Button) view.findViewById(R.id.btCancel);
//
//        confirm.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View arg0) {
//                alerta.dismiss();
//                MarkerDAO markerDAO = MarkerDAO.getInstance(getContext());
//                final MarkerBD markerBD = markerDAO.getPerMarker(id);
//                markerBD.setLatitude(marker.getPosition().latitude);
//                markerBD.setLongitude(marker.getPosition().longitude);
//                markerDAO.update(markerBD);
//
//                removeCircle();
//                CreateCircle(marker);
//                Snackbar.make(getView(), "Confirmado posição!", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
//
//        cancel.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View arg0) {
//                alerta.dismiss();
//            }
//        });
//        builder.setView(view);
//        alerta = builder.create();
//        alerta.show();
//    }
//
//    private void dialogAdd(final LatLng latLng) {
//
//        //LayoutInflater é utilizado para inflar nosso layout em uma view.
//        //-pegamos nossa instancia da classe
//        LayoutInflater li = LayoutInflater.from(getContext());
//
//        //inflamos o layout alerta.xml na view
//        final View view = li.inflate(R.layout.dialogadd, null);
//
//        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
//        builder.setTitle("Criar Marcador");
//        final Button confirm = (Button) view.findViewById(R.id.btConfirm);
//
//        Button cancel = (Button) view.findViewById(R.id.btCancel);
//
//        confirm.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View arg0) {
//                alerta.dismiss();
//
////                //markerInsertLocal(latLng);
////                markerInsertServer(latLng);
//                insertFirebase(latLng);
//
//            }
//        });
//
//        cancel.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View arg0) {
//                alerta.dismiss();
//            }
//        });
//
//        confirm.setVisibility(View.INVISIBLE);
//
//
//        ImageView amarelo = (ImageView) view.findViewById(R.id.yellow_star);
//
//        amarelo.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                image = R.mipmap.ic_maker_amarelo_star;
//                confirm.setVisibility(View.VISIBLE);
//            }
//        });
//
//
//        ImageView laranja = (ImageView) view.findViewById(R.id.orange_star);
//
//        laranja.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                image = R.mipmap.ic_maker_laranja_star;
//                confirm.setVisibility(View.VISIBLE);
//
//            }
//        });
//
//        ImageView vermelho = (ImageView) view.findViewById(R.id.red_star);
//
//        vermelho.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                image = R.mipmap.ic_maker_vermelho_star;
//                confirm.setVisibility(View.VISIBLE);
//
//            }
//        });
//
//        builder.setView(view);
//
//        alerta = builder.create();
//
//        alerta.show();
//    }
//
//    public void markerInsertLocal(MarkerBD markerBD) {
//        MarkerDAO markerDAO = MarkerDAO.getInstance(getContext());
//        markerDAO.saveMarker(markerBD);
//
//        DateHelper dateHelper = new DateHelper();
//        Toast.makeText(getContext(), "Convertido timestamp: " + dateHelper.getTimestamp(markerBD.getCreationDate()), Toast.LENGTH_SHORT).show();
//    }
//
////    public void markerInsertServer(LatLng latLng) {
////        MarkerBD markerBD = new MarkerBD(0, 1, latLng.latitude, latLng.longitude, getStreet(latLng), getTimeLive(image), image);
////        insertMarker(markerBD);
////        insertFirebase(latLng);
//////        getAllMarkers();
////    }
//
//    public void zoomMarker(LatLng arg0, GoogleMap googleMap) {
//        CameraPosition cameraPosition = new CameraPosition.Builder()
//                .target(arg0)               // Sets the center of the map
//                .zoom(17)                   // Sets the zoom
//                .bearing(90)                // Sets the orientation of the camera to east
//                .tilt(30)                   // Sets the tilt of the camera to 30 degrees
//                .build();                   // Creates a CameraPosition from the builder
//        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
//    }
//
//    public String getStreet(LatLng location) {
//        String street = "";
//        //Classe que fornece a localização da cidade
//        Geocoder geocoder = new Geocoder(this.getContext());
//        List myLocation = null;
//        try {
//            //Obtendo os dados do endereço
//            myLocation = geocoder.getFromLocation(location.latitude, location.longitude, 1);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        if (myLocation != null && myLocation.size() > 0) {
//            Address address = (Address) myLocation.get(0);
//            //Pega nome da cidade
//            String city = address.getLocality();
//            //Pega nome da rua
//            street = address.getAddressLine(0);
//        } else {
//            street = "Endereço não encontrado";
//        }
//
//        return street;
//    }
//
//    public void CreateCircle(Marker marker) {
//        circle = googleMapFinal.addCircle(new CircleOptions()
//                .center(new LatLng(marker.getPosition().latitude, marker.getPosition().longitude))
//                .radius(10)
//                .strokeWidth(10)
//                .strokeColor(Color.argb(128, 173, 216, 230))
//                .fillColor(Color.argb(24, 30, 144, 255))
//                .clickable(true));
//
//        googleMapFinal.setOnCircleClickListener(new GoogleMap.OnCircleClickListener() {
//
//            @Override
//            public void onCircleClick(Circle circle) {
//                // Flip the r, g and b components of the circle's
//                // stroke color.
//                int strokeColor = circle.getStrokeColor() ^ 0x00ffffff;
//                circle.setStrokeColor(strokeColor);
//            }
//        });
//    }
//
//    public static void removeCircle() {
//        if (circle != null)
//            circle.remove();
//    }
//
////    public void loadMarkers() {
////        MarkerDAO markerDAO = MarkerDAO.getInstance(getContext());
////        final List<MarkerBD> markerBDs = markerDAO.getAllMarkersActive();
////        int count = 0;
////
////        for (int i = 0; i < markerBDs.size(); i++) {
////            final Marker marker = googleMapFinal.addMarker(new MarkerOptions()
////                    .position(new LatLng(markerBDs.get(i).getLatitude(), markerBDs.get(i).getLongitude()))
////            );
////            listMarkers.add(marker);
////            mHashMap.put(marker, markerBDs.get(i).getId());
////
////
////        }
////        for (Marker m : listMarkers) {
////            //new LiveThread().liveMarkerCount(m, markerBDs.get(count), getActivity());
////            count++;
////        }
////    }
//
//    public int getTimeLive(int idImage) {
//        int timeLife = 0;
//        switch (idImage) {
//            case R.mipmap.ic_maker_amarelo_star:
//                timeLife = 10;
//                break;
//            case R.mipmap.ic_maker_laranja_star:
//                timeLife = 20;
//                break;
//            case R.mipmap.ic_maker_vermelho_star:
//                timeLife = 30;
//                break;
//        }
//
//        return timeLife;
//    }
//
////    public void getMarkerWebService() {
////        AcessRestHelper acessRestHelper = new AcessRestHelper();
////
////        //URL DO WEBSERVICE CASO LOCALHOST TROCAR POR IP DA MAQUINA
////        String callWS = "http://10.92.40.176:8080/SAN-WebService/webresources/SAN/Marker/getAllActive";
////        String result = acessRestHelper.Get(callWS);
////
////        Log.i("JSON", result);
////
////        try {
////            Gson g = new Gson();
////            Type listType = new TypeToken<ArrayList<MarkerBD>>() {
////            }.getType();
////
////            List<MarkerBD> markerBDs = g.fromJson(result, listType);
////
////            for (int i = 0; i < markerBDs.size(); i++) {
////                Marker webMarker = googleMapFinal.addMarker(new MarkerOptions()
////                        .position(new LatLng(markerBDs.get(i).getLatitude(), markerBDs.get(i).getLongitude()))
////                        .title(markerBDs.get(i).getTitle())
////                        .draggable(markerBDs.get(i).isDraggable())
////                        .icon(BitmapDescriptorFactory.fromResource(markerBDs.get(i).getImage()))
////                        .visible(markerBDs.get(i).isStatus())
////                );
////                listMarkers.add(webMarker);
////                mHashMap.put(webMarker, markerBDs.get(i).getId());
////            }
////
////        } catch (Exception e) {
////            e.printStackTrace();
////        }
////    }
//
////    @SuppressLint("NewApi")
////    private void callServer(final String complementURL, final String method, final String data) {
////        new Thread() {
////            public void run() {
////                String answer = HttpConnection.getSetDataWeb(complementURL, method, data);
////
////                Log.i("Script", "ANSWER: " + answer);
////
////                if (data.isEmpty()) {
////                    MarkerBD markerBD = JsonHelper.degenerateJSON(answer);
////                }
////            }
////        }.start();
////    }
//
////    public void getAllMarkersWebService() {
////        requestQueue = Volley.newRequestQueue(getContext());
////
////        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
////                showUrl, new Response.Listener<JSONObject>() {
////            @Override
////            public void onResponse(JSONObject response) {
////                Log.d("Response: ",response.toString());
////                try {
////                    JSONArray markers = response.getJSONArray("marker");
////                    for (int i = 0; i < markers.length(); i++) {
////                        JSONObject markerJson = markers.getJSONObject(i);
////                        Gson g = new Gson();
////                        Type listType = new TypeToken<ArrayList<MarkerBD>>() {
////                        }.getType();
////
////                        MarkerBD markerBD = g.fromJson(String.valueOf(markerJson), listType);
////
////                        Marker webMarker = googleMapFinal.addMarker(new MarkerOptions()
////                                .position(new LatLng(markerBD.getLatitude(), markerBD.getLongitude()))
////                                .title(markerBD.getTitle())
////                                .draggable(markerBD.isDraggable())
////                                .icon(BitmapDescriptorFactory.fromResource(markerBD.getImage()))
////                                .visible(markerBD.isStatus())
////                        );
////                        listMarkers.add(webMarker);
////                        mHashMap.put(webMarker, markerBD.getId());
////
////                    }
////                } catch (JSONException e) {
////                    e.printStackTrace();
////                }
////
////            }
////        }, new Response.ErrorListener() {
////            @Override
////            public void onErrorResponse(VolleyError error) {
////                Log.d("Error: ",error.getMessage());
////
////            }
////        });
////        requestQueue.add(jsonObjectRequest);
////    }
////
////
////    StringRequest request = new StringRequest(Request.Method.POST, insertUrl, new Response.Listener<String>() {
////        @Override
////        public void onResponse(String response) {
////
////            System.out.println(response.toString());
////        }
////    }, new Response.ErrorListener() {
////        @Override
////        public void onErrorResponse(VolleyError error) {
////
////        }
////    }) {
////
////        @Override
////        protected Map<String, String> getParams() throws AuthFailureError {
////            Map<String, String> parameters = new HashMap<String, String>();
////            parameters.put("firstname", firstname.getText().toString());
////            parameters.put("lastname", lastname.getText().toString());
////            parameters.put("age", age.getText().toString());
////
////            return parameters;
////        }
////    };
////                requestQueue.add(request);
////}
////
////        });
////
////
////                }
////                }
////                }
//
//    public void getAllMarkers() {
//        MarkerService markerService = MarkerService.RETROFIT.create(MarkerService.class);
//        final Call<List<MarkerBD>> call = markerService.getAllMarker();
//
//        call.enqueue(new Callback<List<MarkerBD>>() {
//            @Override
//            public void onResponse(Call<List<MarkerBD>> call, Response<List<MarkerBD>> response) {
//                final List<MarkerBD> listamMarkerBDs = response.body();
//                if (listamMarkerBDs != null) {
//                    for (int i = 0; i < listamMarkerBDs.size(); i++) {
////                        Log.i(TAG, "MARKER: " + listamMarkerBDs.get(i).getId());
////                        Log.d("ID", String.valueOf(listamMarkerBDs.get(i).getId()));
////                        Log.d("IDUSER", String.valueOf(listamMarkerBDs.get(i).getIdUser()));
////                        if (listamMarkerBDs.get(i).getIdMarker() == null){
////                            Log.d("IDMARKER", "null");
////                        }else{
////                            Log.d("IDMARKER", listamMarkerBDs.get(i).getIdMarker());
////                        }
////
////                        Log.d("LATITUDE", String.valueOf(listamMarkerBDs.get(i).getLatitude()));
////                        Log.d("LONGITUDE", String.valueOf(listamMarkerBDs.get(i).getLongitude()));
////                        Log.d("TITLE", listamMarkerBDs.get(i).getTitle());
////                        Log.d("LIFETIME", String.valueOf(listamMarkerBDs.get(i).getLifeTime()));
////                        Log.d("IMAGE", String.valueOf(listamMarkerBDs.get(i).getImage()));
////                        //Log.d("CREATIONDATE", listamMarkerBDs.get(i).getCreationDate());
////                        Log.d("DRAGGABLE", String.valueOf(listamMarkerBDs.get(i).isDraggable()));
////                        Log.d("STATUS", String.valueOf(listamMarkerBDs.get(i).isStatus()));
////                        Log.d("Size", String.valueOf(listamMarkerBDs.size()));
//                        Marker marker = mMap.addMarker(new MarkerOptions()
//                                .position(new LatLng(listamMarkerBDs.get(i).getLatitude(), listamMarkerBDs.get(i).getLongitude()))
//                        );
//                        mHashMap.put(marker, listamMarkerBDs.get(i).getId());
//                    }
//                }
//            }
//
//            @Override
//            public void onFailure(Call<List<MarkerBD>> call, Throwable t) {
//                Log.e(TAG, "Erro: " + t.toString());
//            }
//        });
//    }
//
//    public void getMarker(String id) {
//        MarkerService markerService = MarkerService.RETROFIT.create(MarkerService.class);
//        final Call<MarkerBD> call = markerService.getMarker(id);
//
//        call.enqueue(new Callback<MarkerBD>() {
//            @Override
//            public void onResponse(Call<MarkerBD> call, Response<MarkerBD> response) {
//                MarkerBD markerBD = response.body();
//                Log.i(TAG, "MARKER: " + markerBD.getId());
//            }
//
//            @Override
//            public void onFailure(Call<MarkerBD> call, Throwable t) {
//                Log.e(TAG, "Erro: " + t.toString());
//            }
//        });
//
//    }
//
//    public static void getRaio(String lat, String lng, String km) {
//        MarkerService markerService = MarkerService.RETROFIT.create(MarkerService.class);
//        final Call<List<MarkerBD>> call = markerService.getRaio(lat, lng, km);
//
//        call.enqueue(new Callback<List<MarkerBD>>() {
//
//            @Override
//            public void onResponse(Call<List<MarkerBD>> call, Response<List<MarkerBD>> response) {
//                final List<MarkerBD> listamMarkerBDs = response.body();
//                if (listamMarkerBDs != null) {
//                    for (int i = 0; i < listamMarkerBDs.size(); i++) {
//                        Marker marker = googleMapFinal.addMarker(new MarkerOptions()
//                                .position(new LatLng(listamMarkerBDs.get(i).getLatitude(), listamMarkerBDs.get(i).getLongitude()))
//                        );
//                        mHashMap.put(marker, listamMarkerBDs.get(i).getId());
//                    }
//                }
//            }
//
//            @Override
//            public void onFailure(Call<List<MarkerBD>> call, Throwable t) {
//                Log.e(TAG, "Erro: " + t.toString());
//            }
//        });
//    }
//
//
//    public void updateMarker(MarkerBD markerBD) {
//        MarkerService markerService = MarkerService.RETROFIT.create(MarkerService.class);
//        Call<Void> call = markerService.updateMarker(String.valueOf(markerBD.getId()), markerBD);
//
//        call.enqueue(new Callback<Void>() {
//            @Override
//            public void onResponse(Call<Void> call, Response<Void> response) {
//                Toast.makeText(getContext(), "Marker alterado com sucesso", Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void onFailure(Call<Void> call, Throwable t) {
//                Log.e(TAG, "Erro: " + t.toString());
//            }
//        });
//    }
//
//    public void deleteMarker(String id) {
//        MarkerService markerService = MarkerService.RETROFIT.create(MarkerService.class);
//        Call<Void> call = markerService.deleteMarker(id);
//
//        call.enqueue(new Callback<Void>() {
//            @Override
//            public void onResponse(Call<Void> call, Response<Void> response) {
//                Toast.makeText(getContext(), "Marker removido com sucesso", Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void onFailure(Call<Void> call, Throwable t) {
//                Log.e(TAG, "Erro: " + t.toString());
//            }
//        });
//    }
//
//    public void insertMarker(MarkerBD markerBD) {
//        MarkerService markerService = MarkerService.RETROFIT.create(MarkerService.class);
//        final Call<Void> call = markerService.insertMarker(markerBD);
//        call.enqueue(new Callback<Void>() {
//            @Override
//            public void onResponse(Call<Void> call, Response<Void> response) {
//                Toast.makeText(getContext(), "Marker cadastrado com sucesso", Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void onFailure(Call<Void> call, Throwable t) {
//                Log.e(TAG, "Erro: " + t.toString());
//            }
//        });
//    }
//
//    public void insertFirebase(LatLng latLng) {
//        getServerTime();
//        mDatabase = ConfigFireBase.getFirebase();
//        final String itemId = mDatabase.child("Marker").push().getKey();
//        MarkerBD markerBD = new MarkerBD(latLng.latitude, latLng.longitude);
//
//
//        mDatabase.child("Marker").child(itemId).setValue(markerBD);
//        mDatabase.child("Marker").child(itemId).child("fim").setValue(ServerValue.TIMESTAMP);
//        mDatabase.child("Marker").child(itemId).child("idUser").setValue("Qualquer");
//
//        DatabaseReference tempDataBaseReference = mDatabase.child("Marker");
//        tempDataBaseReference.child(itemId).addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot snapshot) {
//                try {
//                    MarkerBD markerBD1 = new MarkerBD(itemId, (Double) snapshot.child("latitude").getValue(), (Double) snapshot.child("longitude").getValue(), (Long) snapshot.child("fim").getValue());
//                    markerInsertLocal(markerBD1);
//                    insertValidar(markerBD1, "Qualquer", 20000);
//                    Marker marker = googleMapFinal.addMarker(new MarkerOptions()
//                            .position(new LatLng(markerBD1.getLatitude(), markerBD1.getLongitude()))
//                    );
//                    markerHashMap.put(markerBD1.getId(), marker);
//                } catch (Throwable e) {
//                    System.err.println("onCreate error: " + e);
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError error) {
//            }
//        });
//
//
//        GeoFire geoFire = new GeoFire(mDatabase.child("marker_location"));
//        geoFire.setLocation(itemId, new GeoLocation(markerBD.getLatitude(), markerBD.getLongitude()), new GeoFire.CompletionListener() {
//            @Override
//            public void onComplete(String key, DatabaseError error) {
//                if (error != null) {
//                    System.err.println("There was an error saving the location to GeoFire: " + error);
//                } else {
//                    System.out.println("Location saved on server successfully!");
//                }
//            }
//        });
//
//        GeoHash geoHash = new GeoHash(new GeoLocation(markerBD.getLatitude(), markerBD.getLongitude()));
//        Map<String, Object> updates = new HashMap<>();
//        updates.put("marker_location/" + itemId + "/g", geoHash.getGeoHashString());
//        updates.put("marker_location/" + itemId + "/l", Arrays.asList(markerBD.getLatitude(), markerBD.getLongitude()));
//        mDatabase.updateChildren(updates);
//        try {
//
////            Toast.makeText(getContext(), "Marker inserido com sucesso", Toast.LENGTH_LONG).show();
//
//
//        } catch (Exception e) {
//
//        }
//    }
//
//    public void getAllFirebase() {
//        mDatabase = ConfigFireBase.getFirebase();
//        mDatabase.child("Marker").addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                markerBDList.clear();
//                if (dataSnapshot != null) {
//                    for (DataSnapshot objSnapShot : dataSnapshot.getChildren()) {
//                        Log.i(TAG, "onDataChange: " + objSnapShot);
//                        MarkerBD markerBD = objSnapShot.getValue(MarkerBD.class);
//                        markerBDList.add(markerBD);
//                    }
//                    Log.i(TAG, "getAllFirebase: Size: " + markerBDList);
//                    if (markerBDList != null) {
//                        for (int i = 0; i < markerBDList.size(); i++) {
//                            Marker marker = googleMapFinal.addMarker(new MarkerOptions()
//                                    .position(new LatLng(markerBDList.get(i).getLatitude(), markerBDList.get(i).getLongitude()))
//                            );
//                            mHashMap.put(marker, markerBDList.get(i).getId());
//                        }
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//    }
//
//
//    public void getTimestampFirebase() {
//        mDatabase = ConfigFireBase.getFirebase();
//        mDatabase.child("current_timestamp").setValue(ServerValue.TIMESTAMP, new DatabaseReference.CompletionListener() {
//            @Override
//            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
//                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        Log.i(TAG, "MarkerDB: snap" + dataSnapshot);
//                        Long server_timestamp = dataSnapshot.getValue(Long.class);
//                        mDatabase.child("Marker").orderByChild("fim").endAt(server_timestamp).addValueEventListener(new ValueEventListener() {
//                            @Override
//                            public void onDataChange(DataSnapshot dataSnapshot) {
//                                mHashMap.clear();
//                                if (dataSnapshot != null) {
//                                    for (DataSnapshot objSnapShot : dataSnapshot.getChildren()) {
//                                        MarkerBD markerBD = objSnapShot.getValue(MarkerBD.class);
//                                        if (markerBD != null) {
//                                            DateHelper dateHelper = new DateHelper();
//                                            Marker marker = googleMapFinal.addMarker(new MarkerOptions()
//                                                    .position(new LatLng(markerBD.getLatitude(), markerBD.getLongitude()))
//                                            );
//                                            mHashMap.put(marker, markerBD.getId());
//                                        }
//                                    }
//                                }
//                            }
//
//                            @Override
//                            public void onCancelled(DatabaseError databaseError) {
//                            }
//                        });
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError databaseError) {
//                    }
//                });
//            }
//        });
//    }
//
//    public void getTimestampFirebase2() {
//        mDatabase = ConfigFireBase.getFirebase();
//        DatabaseReference markerReference = mDatabase.child("Marker");
//        Long temp = getServerTime();
//        Query query = markerReference.orderByChild("fim").startAt(temp);
//        if (query != null) {
//            query.addValueEventListener(new ValueEventListener() {
//                @Override
//                public void onDataChange(DataSnapshot dataSnapshot) {
//                    markerBDList.clear();
//                    if (dataSnapshot != null) {
//                        for (DataSnapshot objSnapShot : dataSnapshot.getChildren()) {
//                            Log.i(TAG, "onDataChange: " + objSnapShot);
//                            MarkerBD markerBD = objSnapShot.getValue(MarkerBD.class);
//                            markerBDList.add(markerBD);
//                        }
//                        Log.i(TAG, "getAllFirebase: Size: " + markerBDList.size());
//                        if (markerBDList != null) {
//                            for (int i = 0; i < markerBDList.size(); i++) {
//                                Marker marker = googleMapFinal.addMarker(new MarkerOptions()
//                                        .position(new LatLng(markerBDList.get(i).getLatitude(), markerBDList.get(i).getLongitude()))
//                                );
//                                mHashMap.put(marker, markerBDList.get(i).getId());
//                            }
//                        }
//                    }
//                }
//
//                @Override
//                public void onCancelled(DatabaseError databaseError) {
//
//                }
//            });
//
//        }
//    }
//
//    public void getRaioFirebase(Double lat, Double lng, Double radius) {
//        getServerTime();
//        mDatabase = ConfigFireBase.getFirebase();
////        GeoFire geoFire = new GeoFire(mDatabase.child("marker_location"));
//        firebaseDatabase = ConfigFireBase.getFirebaseDatabase();
//        GeoFire geoFire = new GeoFire(firebaseDatabase.getReferenceFromUrl("https://websan-46271.firebaseio.com/marker_location/"));
//
//        final GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(lat, lng), radius);
//
//        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
//            @Override
//            public void onKeyEntered(final String key, GeoLocation location) {
//                mDatabase = ConfigFireBase.getFirebase();
//                mDatabase.child("Marker").child(key).addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        if (dataSnapshot != null && dataSnapshot.getValue() != null) {
//                            MarkerBD markerBD = dataSnapshot.getValue(MarkerBD.class);
//                            if (markerBD != null && dataSnapshot.child("fim").getValue() != null) {
//                                if (getServerTime() < (Long) dataSnapshot.child("fim").getValue()) {
//                                    if (markerHashMap.get(key) == null) {
//                                        Log.i(TAG, "Entrou Criar: " + key);
//                                        Marker marker = googleMapFinal.addMarker(new MarkerOptions()
//                                                .position(new LatLng(markerBD.getLatitude(), markerBD.getLongitude()))
//                                        );
//                                        marker.setTag(key);
//                                        markerHashMap.put(key, marker);
//                                    }
//                                } else {
//                                    if (markerHashMap.get(key) != null) {
//                                        Log.i(TAG, "Entrou Remover: " + key);
//                                        markerHashMap.get(key).setVisible(false);
//                                        markerHashMap.get(key).remove();
//                                        markerHashMap.remove(key);
//                                    }
//                                }
//                            }
//                            Log.i(TAG, "hashmap size: "+markerHashMap.size());
//                            for (String key : markerHashMap.keySet()) {
//                                Log.i(TAG, "FORHASHMAP: " + markerHashMap.get(key));
//                            }
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError databaseError) {
//
//                    }
//                });
//            }
//
//            @Override
//            public void onKeyExited(String key) {
//
//            }
//
//            @Override
//            public void onKeyMoved(String key, GeoLocation location) {
//
//            }
//
//            @Override
//            public void onGeoQueryReady() {
//
//            }
//
//            @Override
//            public void onGeoQueryError(DatabaseError error) {
//
//            }
//        });
//    }
//
//
//    public void updateMarkerFirebase(MarkerBD markerBD) {
//        mDatabase = ConfigFireBase.getFirebase();
//        mDatabase.child("Marker").child(markerBD.getId()).setValue(markerBD);
//    }
//
//    public void deleteMarkerFirebase(MarkerBD markerBD) {
//        mDatabase = ConfigFireBase.getFirebase();
//        mDatabase.child("Marker").child(markerBD.getId()).removeValue();
//    }
//
//    public void insertDenunciar(MarkerBD markerBD, String idUser) {
//        mDatabase = ConfigFireBase.getFirebase();
//        mDatabase.child("Denunciar").child(markerBD.getId()).setValue(idUser);
//    }
//
//    public void insertValidar(final MarkerBD markerBD, String idUser, final long time) {
//        mDatabase = ConfigFireBase.getFirebase();
//        Log.i(TAG, "IDMARKER: " + (markerBD.getId()));
//        mDatabase.child("Validar").child(markerBD.getId()).child("idUser").setValue(idUser);
//        mDatabase.child("Validar").child(markerBD.getId()).child("validou").setValue(true);
//        mDatabase.child("Marker").child(markerBD.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot snapshot) {
//                try {
//                    Long timestamp = (Long) snapshot.child("fim").getValue();
//                    DateHelper dateHelper = new DateHelper();
//                    Log.i(TAG, "Timestamp antes: " + dateHelper.getTimestamp(markerBD.getCreationDate()));
//                    timestamp += time;
//                    mDatabase.child("Marker").child(markerBD.getId()).child("fim").setValue(timestamp);
//                    Log.i(TAG, "Timestamp depois: " + dateHelper.getTimestamp(timestamp));
//                } catch (Throwable e) {
//                    System.err.println("onCreate error: " + e);
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError error) {
//            }
//        });
//    }
//
//    public Long getServerTime() {
//        mDatabase = ConfigFireBase.getFirebase();
//        final Long[] timestampServer = new Long[1];
//        mDatabase.child("current_timestamp").setValue(ServerValue.TIMESTAMP);
//        mDatabase.child("current_timestamp").addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                timestampServer[0] = (Long) dataSnapshot.getValue();
//                timestamp = timestampServer[0];
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//        return timestamp;
//    }
}
