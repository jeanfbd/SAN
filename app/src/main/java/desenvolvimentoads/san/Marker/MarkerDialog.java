package desenvolvimentoads.san.Marker;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.support.design.widget.Snackbar;
import android.widget.TableRow;
import android.widget.TextView;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.firebase.geofire.core.GeoHash;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import desenvolvimentoads.san.DAO.ConfigFireBase;
import desenvolvimentoads.san.MenuInicial;
import desenvolvimentoads.san.Observer.Action;
import desenvolvimentoads.san.R;

/**
 * Created by master on 19/07/2017.
 */

public class MarkerDialog {
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth = com.google.firebase.auth.FirebaseAuth.getInstance();
    private FirebaseUser currentUser = mAuth.getCurrentUser();
    //String userId = currentUser.getUid();

    MenuInicial menuInicial = new MenuInicial();
    String userId = menuInicial.getUsers();

    HashMap<String, Circle> circles = new HashMap<String, Circle>();
    Long creationDate;


    long timeAdd = 60000;
    long timestamp;

    Marker marcador;
    AlertDialog alerta;
    private int image;
    private static Circle circle = null;
    Context context;
    LatLng loc;
    final double RADIUS = 12.5;
    Action action = Action.getInstance();
    /* Values in meters */
    double closeToMeRadiusInMeters = 1.0;
    double hasNearbyRadiusInMeters = 0.025;


    public static void deleteDataArrayFirebase(final HashMap<String, Marker> m, final String key) {


        MarkerTag markerTag = (MarkerTag) m.get(key).getTag();

            markerTag.getCircle().remove();
            m.get(key).remove();
            m.remove(key);




    }

    public void addDataArrayFirebase(final LatLng latLng, final Context c, final GoogleMap googleMapFinal, final Geocoder g, final HashMap<String, Marker> m, final String key, boolean validou) {


        if (key.equals(userId)) {
            image = R.mipmap.ic_maker_vermelho_star;
        } else {
            image = R.mipmap.ic_maker_vermelho;
        }


        MarkerOptions markerOption = new MarkerOptions();
        markerOption.position(latLng).icon(BitmapDescriptorFactory.fromResource(image));
        marcador = googleMapFinal.addMarker(markerOption);
        //marcador.setTitle(getStreet(latLng, c, g));

        CreateCircle(latLng, googleMapFinal);
        MarkerTag tag = new MarkerTag(marcador.getPosition().latitude, marcador.getPosition().longitude, circle, validou);

        tag.setStreet(getStreet(latLng, c, g));

        //Pega Referencia do Firebase

        tag.setId(key);

        if (validou) {
            tag.setValidate(true);
            tag.getCircle().setStrokeColor(Color.argb(128, 2, 158, 90));
        } else {
            tag.getCircle().setStrokeColor(Color.argb(128, 224, 158, 90));
        }

        marcador.setTag(tag);
        marcador.setDraggable(false);
        //Persiste os dados sobre a chave itemId


        circles.put(key, circle);


        action.setButtomAddMakerClickado(true);
       // zoomMarker(latLng, googleMapFinal);

        m.put(key, marcador);


    }


    /*Verifica se existe algum marcador proximo retornando true se existir.*/
    public Boolean hasNearby(final HashMap<String, Marker> m, final LatLng latLng) {
        Log.i("teste"," hasneraby RAIO ");
        double proximityInMeters = hasNearbyRadiusInMeters;
        Log.i("teste"," hasneraby RAIO "+m.size());
        double newMarkerCosLat = Math.cos(Math.toRadians(latLng.latitude));
        double newMarkerSinLat = Math.sin(Math.toRadians(latLng.latitude));
        double newMakerRadianLng = Math.toRadians(latLng.longitude);

        for (Map.Entry<String, Marker> markerTemp : m.entrySet()) {
            Log.i("teste"," hasneraby aRRAY ");
            double searchNearby = 6371 *
                    Math.acos(
                            Math.cos(Math.toRadians(markerTemp.getValue().getPosition().latitude)) *
                                    newMarkerCosLat *
                                    Math.cos(Math.toRadians(markerTemp.getValue().getPosition().longitude) - newMakerRadianLng) +
                                    Math.sin(Math.toRadians(markerTemp.getValue().getPosition().latitude)) *
                                            newMarkerSinLat
                    );
            if (searchNearby <= proximityInMeters) {

                return true;

            }
            Log.i("teste","RAIO "+searchNearby);
            Log.i("teste","RAIO P "+proximityInMeters);
        }
        Log.i("teste"," hasneraby RAIO  NED");
        return false;


    }


    /*Verifica se a ultima posição é proxima a do local a onde o marcador sera inserido retornando true se for*/
    public Boolean closeToMe(final LatLng myPosition, final LatLng latLng) {

        double proximityInMeters = closeToMeRadiusInMeters;


        double newMarkerCosLat = Math.cos(Math.toRadians(latLng.latitude));
        double newMarkerSinLat = Math.sin(Math.toRadians(latLng.latitude));
        double newMakerRadianLng = Math.toRadians(latLng.longitude);


        double searchNearby = 6371 *
                Math.acos(
                        Math.cos(Math.toRadians(myPosition.latitude)) *
                                newMarkerCosLat *
                                Math.cos(Math.toRadians(myPosition.longitude) - newMakerRadianLng) +
                                Math.sin(Math.toRadians(myPosition.latitude)) *
                                        newMarkerSinLat
                );
        if (searchNearby <= proximityInMeters) {
            Log.i("teste","RAIO "+searchNearby);
            Log.i("teste","RAIO P "+proximityInMeters);
            return true;

        }


        return false;


    }

    public Boolean closeToMeToHash(final LatLng myPosition, final LatLng latLng,HashMap<String, String> alertHashMap, String idmaker, double radiusValue ) {

        double proximityInMeters = radiusValue;


        double newMarkerCosLat = Math.cos(Math.toRadians(latLng.latitude));
        double newMarkerSinLat = Math.sin(Math.toRadians(latLng.latitude));
        double newMakerRadianLng = Math.toRadians(latLng.longitude);


        double searchNearby = 6371 *
                Math.acos(
                        Math.cos(Math.toRadians(myPosition.latitude)) *
                                newMarkerCosLat *
                                Math.cos(Math.toRadians(myPosition.longitude) - newMakerRadianLng) +
                                Math.sin(Math.toRadians(myPosition.latitude)) *
                                        newMarkerSinLat
                );
        if (searchNearby <= proximityInMeters) {

            alertHashMap.put(idmaker,idmaker);
            return true;


        }


        return false;


    }

    /*Adicionando o listener dos marker para poder deletar e validar*/
    public GoogleMap setMarkerClick(GoogleMap googleMap, Context c, final HashMap<String, Marker> m) {

        this.context = c;

        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                MarkerTag markerTagMap = (MarkerTag) marker.getTag();
                if (action.isReportNotSelected()) {


                    if (!markerTagMap.getValidate()) {

                        diagValidate2(marker, context, m);
                    }

                } else {
                        action.setReportNotSelected(true);
                        insertDenunciar( (MarkerTag) m.get(markerTagMap.getId()).getTag(),userId,m);



                }


                return true;


            }
        });

        return googleMap;
    }


    public void diagValidate2(final Marker marker, Context c, final HashMap<String, Marker> m) {
        final MarkerTag markerTag = (MarkerTag) marker.getTag();
        circle = markerTag.getCircle();
        LayoutInflater li = LayoutInflater.from(c);

        //inflamos o layout alerta.xml na view
        final View view = li.inflate(R.layout.dialog_validar2, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setTitle("Validação");
        final Button btDislike = (Button) view.findViewById(R.id.btDislike);
        final Button btValidate = (Button) view.findViewById(R.id.btLike);
        final Button btCancel = (Button) view.findViewById(R.id.btCancel);
        final TextView street = (TextView) view.findViewById(R.id.street);
        final TextView location = (TextView) view.findViewById(R.id.location);
        final TextView datetime = (TextView) view.findViewById(R.id.datetime);
        final ImageView markerimage = (ImageView) view.findViewById(R.id.markerimage);


        markerimage.setImageResource(R.mipmap.ic_maker_vermelho);
        street.setText(markerTag.getStreet());
        location.setText("Latitude: " + markerTag.getPosition().latitude + "\nLongitude: " + markerTag.getPosition().longitude);
        datetime.setText(convertTime(0));

        btDislike.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {






                        insertValidar((MarkerTag)m.get(markerTag.getId()).getTag(), userId, timeAdd, false,m);
                         alerta.dismiss();

            }
        });


        btValidate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {



                        insertValidar((MarkerTag)m.get(markerTag.getId()).getTag(),  userId, timeAdd, true,m);


                alerta.dismiss();
            }
        });


        btCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {

            Log.i("teste","cancel pressed !!!");
                alerta.dismiss();


            }
        });

        builder.setView(view);
        alerta = builder.create();
        alerta.show();


    }


    public GoogleMap setListenerDragDiag(GoogleMap googleMap, final Marker marker, final Context c, final View v) {


        googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {


                Snackbar.make(v, "Arraste o marcador sobre o mapa para melhor precisão", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();


            }

            @Override
            public void onMarkerDrag(Marker marker) {
                //       Snackbar.make(getView(), "Confirme a posição do marcador", Snackbar.LENGTH_LONG)
                //                .setAction("Action", null).show();
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {

                dialogDrag(marker, c, marker.getPosition());

            }
        });


        return googleMap;
    }


    public void dialogAdd2(final LatLng latLng, final Context c, final GoogleMap googleMapFinal, final Geocoder g, final HashMap<String, Marker> m,final HashMap<String,String> keyAppHash) {



        //LayoutInflater é utilizado para inflar nosso layout em uma view.
        //-pegamos nossa instancia da classe
        LayoutInflater li = LayoutInflater.from(c);

        //inflamos o layout alerta.xml na view
        final View view = li.inflate(R.layout.dialogadd2, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setTitle("Criar Marcador");
        final Button confirm = (Button) view.findViewById(R.id.btConfirm);

        Button cancel = (Button) view.findViewById(R.id.btCancel);

        confirm.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {



                image = R.mipmap.ic_maker_vermelho_star;


                MarkerOptions markerOption = new MarkerOptions();
                markerOption.position(latLng).icon(BitmapDescriptorFactory.fromResource(image));
                marcador = googleMapFinal.addMarker(markerOption);
                //marcador.setTitle(getStreet(latLng, c, g));

                CreateCircle(latLng, googleMapFinal);


                MarkerTag tag = new MarkerTag(marcador.getPosition().latitude, marcador.getPosition().longitude, circle, false);
                tag.setStreet(getStreet(latLng, c, g));

                //Pega Referencia do Firebase
                mDatabase = ConfigFireBase.getFirebase();
                //Persiste uma key no banco do firebase
                final String itemId = mDatabase.child("Marker").push().getKey();
                //Faz referencia da key na tag do marcador
                tag.setId(itemId);
                marcador.setTag(tag);
                marcador.setDraggable(false);

                MarkerTag markerTag = (MarkerTag) marcador.getTag();
                //Persiste os dados sobre a chave itemId
                mDatabase.child("Marker").child(itemId).setValue(markerTag);
                //mDatabase.child("Marker").child(itemId).child("fim").setValue(getCreationDate());

                insertFim(markerTag, timeAdd);
                mDatabase.child("Marker").child(itemId).child("idUser").setValue(userId);
                circles.put(itemId, circle);

                DatabaseReference tempDataBaseReference = mDatabase.child("Marker");
                tempDataBaseReference.child(itemId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        try {
                        } catch (Throwable e) {
                            System.err.println("onCreate error: " + e);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                    }
                });


                GeoFire geoFire = new GeoFire(mDatabase.child("marker_location"));
                geoFire.setLocation(itemId, new GeoLocation(marcador.getPosition().latitude, marcador.getPosition().longitude), new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error) {
                        if (error != null) {
                            System.err.println("There was an error saving the location to GeoFire: " + error);
                        } else {
                            System.out.println("Location saved on server successfully!");
                        }
                    }
                });
/*
                GeoHash geoHash = new GeoHash(new GeoLocation(marcador.getPosition().latitude, marcador.getPosition().longitude));
                Map<String, Object> updates = new HashMap<>();
                updates.put("marker_location/" + itemId + "/g", geoHash.getGeoHashString());
                updates.put("marker_location/" + itemId + "/l", Arrays.asList(marcador.getPosition().latitude, marcador.getPosition().longitude));

  */
            action.setButtomAddMakerClickado(true);
             //zoomMarker(latLng, googleMapFinal);
                m.put(itemId, marcador);
                keyAppHash.put(itemId,itemId);
                action.setItemId(itemId);
                alerta.dismiss();

            }

        });

        cancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {

                Log.i("teste","cancel pressed !!!");

                if (!Action.getInstance().getButtomAddMakerClickado()) {

                    Action.getInstance().setButtomAddMakerClickado(true);
                }

                alerta.dismiss();


            }
        });


        final TextView street = (TextView) view.findViewById(R.id.street);
        street.setText(getStreet(latLng, c, g));

        final ImageView vermelho = (ImageView) view.findViewById(R.id.markerimage);
        vermelho.setImageResource(R.mipmap.ic_maker_vermelho_star);

        final TextView location = (TextView) view.findViewById(R.id.location);
        location.setText("Latitude: " + latLng.latitude + "\nLongitude: " + latLng.longitude);


        builder.setView(view);

        alerta = builder.create();

        alerta.show();
    }

    public void zoomMarker(LatLng arg0, GoogleMap googleMap) {
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(arg0)               // Sets the center of the map
                .zoom(13)                   // Sets the zoom
                //      .bearing(90)                // Sets the orientation of the camera to east
                //  .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
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


    public void dialogDrag(final Marker marker, final Context c, final LatLng latLng) {
        //LayoutInflater é utilizado para inflar nosso layout em uma view.
        //-pegamos nossa instancia da classe
        LayoutInflater li = LayoutInflater.from(c);

        //inflamos o layout alerta.xml na view
        final View view = li.inflate(R.layout.confirm_drag, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setTitle("Alterar Posição");

        final Button confirm = (Button) view.findViewById(R.id.btConfirm);

        Button cancel = (Button) view.findViewById(R.id.btCancel);

        confirm.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                MarkerTag markerTag = (MarkerTag) marker.getTag();
                markerTag.setLatitude(marker.getPosition().latitude);
                markerTag.setLongitude(marker.getPosition().longitude);
                marker.setTag(markerTag);

                circle = markerTag.getCircle();
                circle.setCenter(latLng);
                circle.setStrokeColor(Color.argb(128, 2, 158, 90));

                marker.setDraggable(false);
                alerta.dismiss();

            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                MarkerTag markerTag = (MarkerTag) marker.getTag();
                loc = markerTag.getPosition();
                //   marker.setPosition(new LatLng(loc.latitude, loc.longitude));
                marker.setPosition(markerTag.getPosition());
                alerta.dismiss();
            }
        });
        builder.setView(view);
        alerta = builder.create();
        alerta.show();
    }

    public void CreateCircle(LatLng latLng, GoogleMap googleMap) {
        circle = googleMap.addCircle(new CircleOptions()
                .center(latLng)
                .radius(RADIUS)
                .strokeWidth(10)
                .strokeColor(Color.argb(128, 173, 216, 230))
                .fillColor(Color.argb(24, 30, 144, 255))
                .clickable(true));

        googleMap.setOnCircleClickListener(new GoogleMap.OnCircleClickListener() {

            @Override
            public void onCircleClick(Circle circle) {
                // Flip the r, g and b components of the circle's
                // stroke color.
                int strokeColor = circle.getStrokeColor() ^ 0x00ffffff;
                circle.setStrokeColor(strokeColor);
            }
        });


    }

    public void insertFim(final MarkerTag markerTag, final long time) {
        mDatabase = ConfigFireBase.getFirebase();
        mDatabase.child("Marker").child(markerTag.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                try {
                    Long timestamp = (Long) snapshot.child("fim").getValue();
                    timestamp += time;
                    mDatabase.child("Marker").child(markerTag.getId()).child("fim").setValue(timestamp);
                } catch (Throwable e) {
                    System.err.println("onCreate error: " + e);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });
    }


    public void insertDenunciar(MarkerTag markerTag, String idUser, HashMap<String, Marker> markerHashMap) {
        mDatabase = ConfigFireBase.getFirebase();
        mDatabase.child("Marker").child(markerTag.getId()).child("Denunciar").child(idUser).setValue(idUser);
        if (markerTag.getId() != null) {
            markerHashMap.get(markerTag.getId()).remove();
            markerHashMap.remove(markerTag.getId());
            markerTag.getCircle().remove();
        }

    }

    public void insertValidar(final MarkerTag markerTag, String idUser, final long time, final boolean status, final HashMap<String, Marker> markerHashMap) {


        mDatabase = ConfigFireBase.getFirebase();
        mDatabase.child("Marker").child(markerTag.getId()).child("Validar").child(idUser).setValue(idUser);
        mDatabase.child("Marker").child(markerTag.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                try {
                    Long fim = (Long) snapshot.child("fim").getValue();
                    if (status == true) {
                        fim += time;
                        markerTag.getCircle().setStrokeColor(Color.argb(128, 2, 158, 90));
                    } else {
                        fim -= time;
                        if (markerTag.getId() != null) {
                            markerHashMap.get(markerTag.getId()).setVisible(false);
                            markerTag.getCircle().remove();
                        }
                    }
                    MarkerTag tag = (MarkerTag) markerHashMap.get(markerTag.getId()).getTag();
                    tag.setValidate(true);
                    mDatabase.child("Marker").child(markerTag.getId()).child("fim").setValue(fim);
                } catch (Throwable e) {
                    System.err.println("onCreate error: " + e);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
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

    public String convertTime(long time) {
        Date date = new Date(time);
        Format format = new SimpleDateFormat("dd/MM/yyyy-HH:mm:ss");

        return format.format(date);
    }
}