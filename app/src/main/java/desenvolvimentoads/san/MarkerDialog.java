package desenvolvimentoads.san;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.*;

import java.io.IOException;
import java.util.List;

//import desenvolvimentoads.san.Model.MarkerBD;

/**
 * Created by master on 19/07/2017.
 */

public class MarkerDialog {
   Marker marcador;
    AlertDialog alerta;
    private int image = R.mipmap.ic_maker_amarelo_star;
    private static Circle circle;


    public void dialogAdd(final LatLng latLng, final Context c, final GoogleMap googleMapFinal) {


        //LayoutInflater é utilizado para inflar nosso layout em uma view.
        //-pegamos nossa instancia da classe
        LayoutInflater li = LayoutInflater.from(c);

        //inflamos o layout alerta.xml na view
        final View view = li.inflate(R.layout.dialogadd, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setTitle("Criar Marcador");
        final Button confirm = (Button) view.findViewById(R.id.btConfirm);

        Button cancel = (Button) view.findViewById(R.id.btCancel);

        confirm.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                alerta.dismiss();
               MapsPrincipal.marcadorON =false;
                MenuInicial.changeMind();

                MarkerOptions markerOption = new MarkerOptions();
                markerOption.position(latLng).icon(BitmapDescriptorFactory.fromResource(image));
                googleMapFinal.addMarker(markerOption);
                marcador = googleMapFinal.addMarker(markerOption);
                //    googleMapFinal.setDraggable(true);
                marcador.setTitle(getStreet(latLng,c));
                zoomMarker(latLng, googleMapFinal);


            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {

                alerta.dismiss();
                MapsPrincipal.marcadorON =false;
                MenuInicial.changeMind();


            }
        });

        confirm.setVisibility(View.INVISIBLE);


        final ImageView amarelo = (ImageView) view.findViewById(R.id.yellow_star);
        amarelo.setImageResource(R.mipmap.ic_maker_amarelo);




        final ImageView laranja = (ImageView) view.findViewById(R.id.orange_star);
        laranja.setImageResource(R.mipmap.ic_maker_laranja);


        final ImageView vermelho = (ImageView) view.findViewById(R.id.red_star);
        vermelho.setImageResource(R.mipmap.ic_maker_vermelho);

        amarelo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                image = R.mipmap.ic_maker_amarelo_star;
                amarelo.setImageResource(R.mipmap.ic_maker_amarelo_star);
                laranja.setImageResource(R.mipmap.ic_maker_laranja);
                vermelho.setImageResource(R.mipmap.ic_maker_vermelho);
                confirm.setVisibility(View.VISIBLE);
            }
        });

        laranja.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                image = R.mipmap.ic_maker_laranja_star;
                amarelo.setImageResource(R.mipmap.ic_maker_amarelo);
                laranja.setImageResource(R.mipmap.ic_maker_laranja_star);
                vermelho.setImageResource(R.mipmap.ic_maker_vermelho);
                confirm.setVisibility(View.VISIBLE);

            }
        });
        vermelho.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                image = R.mipmap.ic_maker_vermelho_star;
                amarelo.setImageResource(R.mipmap.ic_maker_amarelo);
                laranja.setImageResource(R.mipmap.ic_maker_laranja);
                vermelho.setImageResource(R.mipmap.ic_maker_vermelho_star);
                confirm.setVisibility(View.VISIBLE);



            }
        });

        builder.setView(view);

        alerta = builder.create();

        alerta.show();





    }

    public void zoomMarker(LatLng arg0, GoogleMap googleMap){
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(arg0)               // Sets the center of the map
                .zoom(17)                   // Sets the zoom
                .bearing(90)                // Sets the orientation of the camera to east
                .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    public String getStreet(LatLng location, Context c) {
        String street = "";
        //Classe que fornece a localização da cidade

        Geocoder geocoder = new Geocoder(c);
        List myLocation = null;

        try {
            //Obtendo os dados do endereço
            myLocation = geocoder.getFromLocation(location.latitude, location.longitude, 1);
            System.out.println(myLocation);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Log.d("My Location", myLocation.toString());
        if (myLocation != null && myLocation.size() > 0) {
            Address address = (Address) myLocation.get(0);
            //Pega nome da cidade
            String city = address.getLocality();
            //Pega nome da rua
            street = address.getAddressLine(0);
        }else{
            street = "Endereço não encontrado";
        }

        return street;
    }

    public GoogleMap setListenerDragDiag(GoogleMap googleMap, final Marker marker, final Context c){
        googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
                for (int i =0; i <100; i++){
                    System.out.println("fffffffffffffff");
                }


              //  Snackbar.make(getView(), "Arraste o marcador sobre o mapa para melhor precisão", Snackbar.LENGTH_LONG)
            //            .setAction("Action", null).show();
            }

            @Override
            public void onMarkerDrag(Marker marker) {
         //       Snackbar.make(getView(), "Confirme a posição do marcador", Snackbar.LENGTH_LONG)
        //                .setAction("Action", null).show();
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                dialogDrag(marker, c);
            }
        });


        return googleMap;
    }

    private void dialogDrag(final Marker marker, final Context c) {
        //LayoutInflater é utilizado para inflar nosso layout em uma view.
        //-pegamos nossa instancia da classe
        LayoutInflater li = LayoutInflater.from(c);

        //inflamos o layout alerta.xml na view
        final View view = li.inflate(R.layout.confirm_drag, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setTitle("Alterar Posição");
        final Button confirm = (Button)  view.findViewById(R.id.btConfirm);

        Button cancel = (Button)  view.findViewById(R.id.btCancel);

        confirm.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {

            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                alerta.dismiss();
            }
        });
        builder.setView(view);
        alerta = builder.create();
        alerta.show();
    }



}
