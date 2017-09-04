package desenvolvimentoads.san.Marker;

import android.content.Context;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.support.design.widget.Snackbar;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.*;

import java.io.IOException;
import java.util.List;

import desenvolvimentoads.san.MenuInicial;
import desenvolvimentoads.san.Observer.Action;
import desenvolvimentoads.san.R;

//import desenvolvimentoads.san.Model.MarkerBD;

/**
 * Created by master on 19/07/2017.
 */

public class MarkerDialog {

    Marker marcador;
    AlertDialog alerta;
    private int image;
    private static Circle circle = null;
    Context context;
    Boolean secondScreen = false;
    LatLng loc;
    int nivel;


    public GoogleMap setMarkerClick(GoogleMap googleMap, Context c){

        this.context = c;

        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if(MenuInicial.vDenunciar){

                    diagValidate(marker,context);

                }
               else{
                    MenuInicial.changeDenunciar();
                    circle.remove();
                    marker.remove();

                }


                return true;


            }
        });

        return googleMap;
    }


    public void diagValidate(final Marker marker,Context c){
        MarkerTag markerTag = (MarkerTag) marker.getTag();
        nivel = markerTag.getNivel();
        circle = markerTag.getCircle();
        LayoutInflater li = LayoutInflater.from(c);

        //inflamos o layout alerta.xml na view
        final View view = li.inflate(R.layout.dialog_validar, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setTitle("Validação");
        final Button btDislike = (Button) view.findViewById(R.id.btDislike);
        final TableRow tableZero = (TableRow) view.findViewById(R.id.tableZero);
        final TextView streetText = (TextView) view.findViewById(R.id.txInfo);
        final TextView txOne = (TextView) view.findViewById(R.id.tvOne);
        final TextView txTwo = (TextView) view.findViewById(R.id.tvTwo);
        final TextView txThree = (TextView) view.findViewById(R.id.tvThree);
        final Button btValidate = (Button) view.findViewById(R.id.btLike);
        Button btCancel =(Button) view.findViewById(R.id.btCancel);

      final  TableRow tableOne = (TableRow) view.findViewById(R.id.tableOne);
      final  TableRow tableTwo = (TableRow) view.findViewById(R.id.tableTwo);
      final  TableRow tableThree = (TableRow) view.findViewById(R.id.TableThree);

        final ImageView amarelo = (ImageView) view.findViewById(R.id.btOne);
        amarelo.setImageResource(R.mipmap.ic_maker_amarelo);


        final ImageView laranja = (ImageView) view.findViewById(R.id.btTwo);
        laranja.setImageResource(R.mipmap.ic_maker_laranja);


        final ImageView vermelho = (ImageView) view.findViewById(R.id.btThree);
        vermelho.setImageResource(R.mipmap.ic_maker_vermelho);

        streetText.setText(markerTag.getStreet());

        btDislike.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
             alerta.dismiss();
             circle.remove();
             marker.remove();


            }
        });


        btValidate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {

                if(secondScreen)
                {

                    alerta.dismiss();
                    secondScreen = false;


                }
                else
                {
                   btValidate.setText("Ok");
                    if(!tableOne.isShown()){
                        tableOne.setVisibility(View.VISIBLE);

                    }
                    if(!tableTwo.isShown()){
                        tableTwo.setVisibility(View.VISIBLE);

                    }
                    if(!tableThree.isShown()){
                        tableThree.setVisibility(View.VISIBLE);

                    }

                   btDislike.setVisibility(View.GONE);
                   tableZero.setVisibility(View.GONE);
                   secondScreen = true;

                }



            }
        });


        btCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                if(secondScreen)
                {
                    btValidate.setText("Validar");
                    secondScreen = false;
                    if(nivel ==1){
                        tableOne.setVisibility(View.VISIBLE);
                        tableTwo.setVisibility(View.GONE);
                        tableThree.setVisibility(View.GONE);
                        amarelo.setImageResource(R.mipmap.ic_maker_amarelo_star);
                        laranja.setImageResource(R.mipmap.ic_maker_laranja);
                        vermelho.setImageResource(R.mipmap.ic_maker_vermelho);






                    }

                    else if(nivel ==2){
                        tableOne.setVisibility(View.GONE);
                        tableTwo.setVisibility(View.VISIBLE);
                        tableThree.setVisibility(View.GONE);
                        amarelo.setImageResource(R.mipmap.ic_maker_amarelo);
                        laranja.setImageResource(R.mipmap.ic_maker_laranja_star);
                        vermelho.setImageResource(R.mipmap.ic_maker_vermelho);
                    }

                    else if(nivel ==3){
                        tableOne.setVisibility(View.GONE);
                        tableTwo.setVisibility(View.GONE);
                        tableThree.setVisibility(View.VISIBLE);

                        amarelo.setImageResource(R.mipmap.ic_maker_amarelo);
                        laranja.setImageResource(R.mipmap.ic_maker_laranja);
                        vermelho.setImageResource(R.mipmap.ic_maker_vermelho_star);

                    }
                    btDislike.setVisibility(View.VISIBLE);
                    tableZero.setVisibility(View.VISIBLE);
                }
                else
                {
                   alerta.dismiss();

                }




            }
        });

        tableOne.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0r) {


                if(secondScreen)
                {
                    amarelo.setImageResource(R.mipmap.ic_maker_amarelo_star);
                    laranja.setImageResource(R.mipmap.ic_maker_laranja);
                    vermelho.setImageResource(R.mipmap.ic_maker_vermelho);


                }
                else
                {
                  //  alerta.dismiss();

                }



            }
        });


        tableTwo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {


                if(secondScreen)
                {
                    amarelo.setImageResource(R.mipmap.ic_maker_amarelo);
                    laranja.setImageResource(R.mipmap.ic_maker_laranja_star);
                    vermelho.setImageResource(R.mipmap.ic_maker_vermelho);


                }
                else
                {
                  //  alerta.dismiss();

                }


            }
        });


        tableThree.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {

                if(secondScreen)
                {
                    amarelo.setImageResource(R.mipmap.ic_maker_amarelo);
                    laranja.setImageResource(R.mipmap.ic_maker_laranja);
                    vermelho.setImageResource(R.mipmap.ic_maker_vermelho_star);


                }
                else
                {
                   // alerta.dismiss();

                }


            }
        });



        if(nivel ==1){
            tableOne.setVisibility(View.VISIBLE);
            tableTwo.setVisibility(View.GONE);
            tableThree.setVisibility(View.GONE);
            amarelo.setImageResource(R.mipmap.ic_maker_amarelo_star);
            laranja.setImageResource(R.mipmap.ic_maker_laranja);
            vermelho.setImageResource(R.mipmap.ic_maker_vermelho);
            txOne.setText("Nivel Baixo (Atual)");


        }

        else if(nivel ==2){
            tableOne.setVisibility(View.GONE);
            tableTwo.setVisibility(View.VISIBLE);
            tableThree.setVisibility(View.GONE);
            amarelo.setImageResource(R.mipmap.ic_maker_amarelo);
            laranja.setImageResource(R.mipmap.ic_maker_laranja_star);
            vermelho.setImageResource(R.mipmap.ic_maker_vermelho);
            txTwo.setText("Nivel Médio (Atual)");

        }

       else if(nivel ==3){
            tableOne.setVisibility(View.GONE);
            tableTwo.setVisibility(View.GONE);
            tableThree.setVisibility(View.VISIBLE);

            amarelo.setImageResource(R.mipmap.ic_maker_amarelo);
            laranja.setImageResource(R.mipmap.ic_maker_laranja);
            vermelho.setImageResource(R.mipmap.ic_maker_vermelho_star);
            txThree.setText("Nivel Alto (Atual)");

        }
/*


*/
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

                dialogDrag(marker, c);

            }
        });


        return googleMap;
    }


    public void dialogAdd(final LatLng latLng, final Context c, final GoogleMap googleMapFinal, final Geocoder g) {


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

                if(!Action.getInstance().getButtomAddMaker()){

                    Action.getInstance().setButtomAddMaker(true);
                }

                MarkerOptions markerOption = new MarkerOptions();
                markerOption.position(latLng).icon(BitmapDescriptorFactory.fromResource(image));
                marcador = googleMapFinal.addMarker(markerOption);
                //marcador.setTitle(getStreet(latLng, c, g));

                CreateCircle(latLng, googleMapFinal);


                MarkerTag tag = new MarkerTag(circle,marcador.getPosition(), nivel);
                tag.setStreet(getStreet(latLng, c, g));
                marcador.setTag(tag);
                marcador.setDraggable(true);

                zoomMarker(latLng, googleMapFinal);


            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {

                alerta.dismiss();
                if(!Action.getInstance().getButtomAddMaker()){

                    Action.getInstance().setButtomAddMaker(true);
                }


            }
        });

        confirm.setVisibility(View.INVISIBLE);

        final TextView tvChoosed = (TextView) view.findViewById(R.id.tvChoosed);
        tvChoosed.setText("Nivel Escolhido = 'Nenhum'");

        final ImageView amarelo = (ImageView) view.findViewById(R.id.yellow_star);
        amarelo.setImageResource(R.mipmap.ic_maker_amarelo);


        final ImageView laranja = (ImageView) view.findViewById(R.id.orange_star);
        laranja.setImageResource(R.mipmap.ic_maker_laranja);


        final ImageView vermelho = (ImageView) view.findViewById(R.id.red_star);
        vermelho.setImageResource(R.mipmap.ic_maker_vermelho);

        amarelo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nivel =1;
                image = R.mipmap.ic_maker_amarelo_star;
                amarelo.setImageResource(R.mipmap.ic_maker_amarelo_star);
                laranja.setImageResource(R.mipmap.ic_maker_laranja);
                vermelho.setImageResource(R.mipmap.ic_maker_vermelho);
                confirm.setVisibility(View.VISIBLE);
                tvChoosed.setText("Nivel Escolhido = 'Baixo'");
            }
        });

        laranja.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nivel =2;
                image = R.mipmap.ic_maker_laranja_star;
                amarelo.setImageResource(R.mipmap.ic_maker_amarelo);
                laranja.setImageResource(R.mipmap.ic_maker_laranja_star);
                vermelho.setImageResource(R.mipmap.ic_maker_vermelho);
                confirm.setVisibility(View.VISIBLE);
                tvChoosed.setText("Nivel Escolhido = 'Médio'");

            }
        });
        vermelho.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                image = R.mipmap.ic_maker_vermelho_star;
                nivel =3;
                amarelo.setImageResource(R.mipmap.ic_maker_amarelo);
                laranja.setImageResource(R.mipmap.ic_maker_laranja);
                vermelho.setImageResource(R.mipmap.ic_maker_vermelho_star);
                confirm.setVisibility(View.VISIBLE);
                tvChoosed.setText("Nivel Escolhido = 'Alto'");


            }
        });

        builder.setView(view);

        alerta = builder.create();

        alerta.show();


    }

    public void zoomMarker(LatLng arg0, GoogleMap googleMap) {
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(arg0)               // Sets the center of the map
                .zoom(17)                   // Sets the zoom
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





    public void dialogDrag(final Marker marker, final Context c) {
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
                MarkerTag markerTag = (MarkerTag)marker.getTag();
                markerTag.setPosition(marker.getPosition());
                marker.setTag(markerTag);

                circle = markerTag.getCircle();
                circle.remove();
               // marker.setDraggable(false);
                alerta.dismiss();

            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                MarkerTag markerTag = (MarkerTag)marker.getTag();
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
                .radius(10)
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



}

