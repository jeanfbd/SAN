package desenvolvimentoads.san;

import android.location.Address;
import android.location.Geocoder;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.api.BooleanResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapsPrincipal extends SupportMapFragment implements OnMapReadyCallback, GoogleMap.OnMapClickListener{

    private GoogleMap mMap;
    private GoogleMap googleMapFinal;
    static Boolean marcadorON = false;
    Marker marcador = null;
    private int image =  R.mipmap.ic_maker_amarelo_star;
    private AlertDialog alerta;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMapFinal = googleMap;
        mMap = googleMap;

        mMap.setOnMapClickListener(this);

        mMap.getUiSettings().setZoomControlsEnabled(true);
        //Metodo carrega após o mapa estiver pronto usar o timeready da tela inicial
        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);

        MarkerOptions marker = new MarkerOptions();
        marker.position(sydney);
        marker.title("Marker Sidney");
        marker.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_maker_amarelo));

        mMap.addMarker(marker);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));





    }

    @Override
    public void onMapClick(LatLng latLng) {

        if(marcadorON){


            dialogAdd(latLng);


        }
        else{
            Toast.makeText(getContext(), "Coordenadas: "+latLng.toString(), Toast.LENGTH_LONG).show();
        }

    }

    public static void flagOne (){
        marcadorON = true;



    }


    public static void flagTwo (){
        marcadorON = false;



    }


    private void dialogAdd(final LatLng latLng) {

        //LayoutInflater é utilizado para inflar nosso layout em uma view.
        //-pegamos nossa instancia da classe
        LayoutInflater li = LayoutInflater.from(getContext());

        //inflamos o layout alerta.xml na view
       final View view = li.inflate(R.layout.dialogadd, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Criar Marcador");
         final Button confirm = (Button)  view.findViewById(R.id.btConfirm);

        Button cancel = (Button)  view.findViewById(R.id.btCancel);

        confirm.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                alerta.dismiss();
                marcadorON =false;
                MenuInicial.changeMind();

                MarkerOptions markerOption = new MarkerOptions();
                markerOption.position(latLng).icon(BitmapDescriptorFactory.fromResource(image));
                marcador = googleMapFinal.addMarker(markerOption);
                marcador.setDraggable(true);
                marcador.setTitle(getStreet(latLng));
                zoomMarker(latLng, googleMapFinal);


            }
        });

       cancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {

                alerta.dismiss();
                marcadorON = false;
                MenuInicial.changeMind();



            }
        });

        confirm.setVisibility(View.INVISIBLE);


        ImageView amarelo = (ImageView) view.findViewById(R.id.yellow_star);

        amarelo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               image = R.mipmap.ic_maker_amarelo_star;
                confirm.setVisibility(View.VISIBLE);
            }
        });


        ImageView laranja = (ImageView) view.findViewById(R.id.orange_star);

        laranja.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                image = R.mipmap.ic_maker_laranja_star;
                confirm.setVisibility(View.VISIBLE);

            }
        });

        ImageView vermelho = (ImageView) view.findViewById(R.id.red_star);

        vermelho.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                image = R.mipmap.ic_maker_vermelho_star;
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

    public String getStreet(LatLng location) {
        String street = "";
        //Classe que fornece a localização da cidade
        Geocoder geocoder = new Geocoder(this.getContext());
        List myLocation = null;

        try {
            //Obtendo os dados do endereço
            myLocation = geocoder.getFromLocation(location.latitude, location.longitude, 1);
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
}
