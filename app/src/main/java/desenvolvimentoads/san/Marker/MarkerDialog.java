package desenvolvimentoads.san.Marker;

import android.content.Context;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import desenvolvimentoads.san.DAO.ConfigFireBase;
import desenvolvimentoads.san.MenuInicial;
import desenvolvimentoads.san.Observer.Action;
import desenvolvimentoads.san.R;
import desenvolvimentoads.san.TelaInicial;

import static android.content.ContentValues.TAG;

//import desenvolvimentoads.san.Model.MarkerBD;

/**
 * Created by master on 19/07/2017.
 */

public class MarkerDialog {
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth = com.google.firebase.auth.FirebaseAuth.getInstance();
    private FirebaseUser currentUser = mAuth.getCurrentUser();

    // String userId = currentUser.getUid();
    String userId = "123";
    HashMap<String, Circle> circles = new HashMap<String, Circle>();
    Long creationDate;


    long timeAdd = 120000;
    long timestamp;

    Marker marcador;
    AlertDialog alerta;
    private int image;
    private static Circle circle = null;
    Context context;
    LatLng loc;
    final int RADIUS = 500;
    Action action = Action.getInstance();


    public static void deleteDataArrayFirebase(final HashMap<String, Marker> m, final String key) {


        MarkerTag markerTag = (MarkerTag) m.get(key).getTag();

        if (markerTag.getId() == key) {


            //   circle.remove();

            m.get(key).remove();
            m.remove(key);
            markerTag.getCircle().remove();
            //  marker.remove();

        }


    }

    public void addDataArrayFirebase(final LatLng latLng, final Context c, final GoogleMap googleMapFinal, final Geocoder g, final HashMap<String, Marker> m, final String key) {


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


        MarkerTag tag = new MarkerTag(marcador.getPosition().latitude, marcador.getPosition().longitude, circle);
        tag.setStreet(getStreet(latLng, c, g));

        //Pega Referencia do Firebase

        tag.setId(key);

        if (true) {
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
        zoomMarker(latLng, googleMapFinal);

        m.put(key, marcador);


    }

    public HashMap<String, Circle> getCircles() {
        return circles;
    }

    public void setCircles(HashMap<String, Circle> circles) {
        this.circles = circles;
    }

    /*Verifica se existe algum marcador proximo retornando true se existir.*/
    public Boolean hasNearby(final HashMap<String, Marker> m, final LatLng latLng) {

        double proximity = RADIUS * 0.002;

        double newMarkerCosLat = Math.cos(Math.toRadians(latLng.latitude));
        double newMarkerSinLat = Math.sin(Math.toRadians(latLng.latitude));
        double newMakerRadianLng = Math.toRadians(latLng.longitude);

        for (Map.Entry<String, Marker> markerTemp : m.entrySet()) {

            double searchNearby = 6371 *
                    Math.acos(
                            Math.cos(Math.toRadians(markerTemp.getValue().getPosition().latitude)) *
                                    newMarkerCosLat *
                                    Math.cos(Math.toRadians(markerTemp.getValue().getPosition().longitude) - newMakerRadianLng) +
                                    Math.sin(Math.toRadians(markerTemp.getValue().getPosition().latitude)) *
                                            newMarkerSinLat
                    );
            if (searchNearby <= proximity) {

                return true;

            }
        }
        return false;


    }


    /*Verifica se a ultima posição é proxima a do local a onde o marcador sera inserido retornando true se for*/
    public Boolean closeToMe(final LatLng myPosition, final LatLng latLng) {

        double proximity = RADIUS * 1.01;


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
        if (searchNearby <= proximity) {

            return true;

        }


        return false;


    }

    /*Adicionando o listenner dos marker para poder deletar e validar*/
    public GoogleMap setMarkerClick(GoogleMap googleMap, Context c, final HashMap<String, Marker> m) {

        this.context = c;

        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (MenuInicial.vDenunciar) {

                    MarkerTag markerTagMap = (MarkerTag) marker.getTag();
                    if (!markerTagMap.getValidate()) {

                        diagValidate2(marker, context, m);
                    }

                } else {
                    MenuInicial.changeDenunciar();


                    for (Map.Entry<String, Marker> markerTemp : m.entrySet()) {

                        MarkerTag markerTag = (MarkerTag) markerTemp.getValue().getTag();

                        MarkerTag markerTagMap = (MarkerTag) marker.getTag();

                        if (markerTag.getId() == markerTagMap.getId()) {

                            //   circle.remove();
                            insertDenunciar(markerTag,userId,m);


                        }
                    }


                }


                return true;


            }
        });

        return googleMap;
    }


    public void diagValidate2(final Marker marker, Context c, final HashMap<String, Marker> m) {
        MarkerTag markerTag = (MarkerTag) marker.getTag();
        circle = markerTag.getCircle();
        LayoutInflater li = LayoutInflater.from(c);

        //inflamos o layout alerta.xml na view
        final View view = li.inflate(R.layout.dialog_validar2, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setTitle("Validação");
        final Button btDislike = (Button) view.findViewById(R.id.btDislike);
        final TextView streetText = (TextView) view.findViewById(R.id.txInfo);
        final Button btValidate = (Button) view.findViewById(R.id.btLike);
        Button btCancel = (Button) view.findViewById(R.id.btCancel);
        final ImageView vermelho = (ImageView) view.findViewById(R.id.btThree);
        vermelho.setImageResource(R.mipmap.ic_maker_vermelho);

        streetText.setText(markerTag.getStreet());

        btDislike.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                alerta.dismiss();

                for (Map.Entry<String, Marker> markerTemp : m.entrySet()) {

                    MarkerTag markerTag = (MarkerTag) markerTemp.getValue().getTag();

                    if (markerTag.getId() == ((MarkerTag) marker.getTag()).getId()) {


                        insertValidar(markerTag, userId, timeAdd, false,m);


                    }
                }


            }
        });


        btValidate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                for (Map.Entry<String, Marker> markerTemp : m.entrySet()) {

                    MarkerTag markerTag = (MarkerTag) markerTemp.getValue().getTag();

                    if (markerTag.getId() == ((MarkerTag) marker.getTag()).getId()) {


                        insertValidar(markerTag, userId, timeAdd, true,m);

                    }
                }

                alerta.dismiss();
            }
        });


        btCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {


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


    public void dialogAdd2(final LatLng latLng, final Context c, final GoogleMap googleMapFinal, final Geocoder g, final HashMap<String, Marker> m) {


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
                alerta.dismiss();


                image = R.mipmap.ic_maker_vermelho_star;


                MarkerOptions markerOption = new MarkerOptions();
                markerOption.position(latLng).icon(BitmapDescriptorFactory.fromResource(image));
                marcador = googleMapFinal.addMarker(markerOption);
                //marcador.setTitle(getStreet(latLng, c, g));

                CreateCircle(latLng, googleMapFinal);


                MarkerTag tag = new MarkerTag(marcador.getPosition().latitude, marcador.getPosition().longitude, circle);
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
                mDatabase.child("Marker").child(itemId).child("fim").setValue(getCreationDate());
                insertFim(markerTag, timeAdd);
                mDatabase.child("Marker").child(itemId).child("idUser").setValue(userId);
                mDatabase.child("Validar").child(itemId).child("idUser").setValue(userId);
                mDatabase.child("Denunciar").child(itemId).child("idUser").setValue(userId);
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

                GeoHash geoHash = new GeoHash(new GeoLocation(marcador.getPosition().latitude, marcador.getPosition().longitude));
                Map<String, Object> updates = new HashMap<>();
                updates.put("marker_location/" + itemId + "/g", geoHash.getGeoHashString());
                updates.put("marker_location/" + itemId + "/l", Arrays.asList(marcador.getPosition().latitude, marcador.getPosition().longitude));
                try {

//            Toast.makeText(getContext(), "Marker inserido com sucesso", Toast.LENGTH_LONG).show();


                } catch (Exception e) {

                }

                action.setButtomAddMakerClickado(true);
                zoomMarker(latLng, googleMapFinal);

                m.put(itemId, marcador);


            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {

                alerta.dismiss();
                if (!Action.getInstance().getButtomAddMakerClickado()) {

                    Action.getInstance().setButtomAddMakerClickado(true);
                }


            }
        });


        final TextView tvChoosed = (TextView) view.findViewById(R.id.tvChoosed);
        tvChoosed.setText(getStreet(latLng, c, g));


        final ImageView vermelho = (ImageView) view.findViewById(R.id.red_star);
        vermelho.setImageResource(R.mipmap.ic_maker_vermelho);


        builder.setView(view);

        alerta = builder.create();

        alerta.show();


    }

    public void zoomMarker(LatLng arg0, GoogleMap googleMap) {
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(arg0)               // Sets the center of the map
                .zoom(14)                   // Sets the zoom
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
        mDatabase.child("Denunciar").child(markerTag.getId()).setValue(idUser);
        if (markerTag.getId() != null) {
            //markerHashMap.get(markerTag.getId()).setVisible(false);
            markerHashMap.get(markerTag.getId()).remove();
            markerHashMap.remove(markerTag.getId());
            markerTag.getCircle().remove();
        }

    }

    public void insertValidar(final MarkerTag markerTag, String idUser, final long time, final boolean status, final HashMap<String, Marker> markerHashMap) {

        checkIfUserExists(userId, "Validar");
        mDatabase = ConfigFireBase.getFirebase();
        mDatabase.child("Validar").child(markerTag.getId()).child("idUser").setValue(idUser);
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

    public boolean userExistsCallback(boolean exists) {
        if (exists) {
            Log.i(TAG, "userExistsCallback: EXISTE");
        } else {
            Log.i(TAG, "userExistsCallback: NÃO EXISTE");
        }
        return exists;
    }

    public void checkIfUserExists(String idUser, String child) {
        mDatabase = ConfigFireBase.getFirebase();
        mDatabase.child("Marker").child(child).child(idUser).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                try {
                    boolean exists = (snapshot != null);
                    userExistsCallback(exists);
                } catch (Throwable e) {
                    System.err.println("onCreate error: " + e);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

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


    public java.util.Map<String, String> getCreationDate() {
        return ServerValue.TIMESTAMP;
    }

    @Exclude
    public Long getCreationDateLong() {
        return creationDate;
    }

    public void setCreationDate(Long creationDate) {
        this.creationDate = creationDate;
    }
}