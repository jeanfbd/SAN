package desenvolvimentoads.san;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Geocoder;

import android.location.Location;

import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import desenvolvimentoads.san.Marker.MarkerDialog;
import desenvolvimentoads.san.Observer.Action;
import desenvolvimentoads.san.Observer.ActionObserver;
import desenvolvimentoads.san.notification.NotificationApp;


import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.model.MarkerOptions;


public class MapsPrincipal extends SupportMapFragment implements LocationListener, OnMapReadyCallback, GoogleMap.OnMapClickListener, ActionObserver, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    LocationRequest mLocationRequest;
    Marker mCurrLocation;
    LatLng newLatLng;
    GoogleApiClient mGoogleApiClient;

    /*Um int randomico para o callback do metodo de request permission, só vai utilizado caso formos tratar alguma coisa com ele */
    public static final int REQUEST_PERMISSION_LOCATION = 10;

    /*Não sei pq tem que ser essa constante mas é para ser assim para funcionar*/
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;

    Boolean permissionOK = false;

    private GoogleMap mMap;
    Marker marcador = null;
    private boolean buttomAddMarkerVisivel;
    Action action = Action.getInstance();
    /* Esse hashmap é responsavel por controlar o add e remove dos markers pelo Latlng para os identificar*/
    HashMap<LatLng,Marker> marcadores = new HashMap<>();

    /* Classe com os metodos dos markers */
    MarkerDialog markerDialog = new MarkerDialog();

    Geocoder geocoder2;

    /*Iniciando dados estaticos de posição*/
    private double longitude = -45.4130600;
    private double latitude = -23.6202800;


    protected LocationSettingsRequest mLocationSettingsRequest;

    protected Location mCurrentLocation;

    int RQS_GooglePlayServices = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getMapAsync(this);
    }


    public void checkPermission() {

        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this.getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {



            } else {



                ActivityCompat.requestPermissions(this.getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION_LOCATION);


            }


        } else {


        }


    }

    /*Se fosse necessario mudar algo apos as permissões*/
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    permissionOK = true;


                } else {
                    permissionOK = false;

                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    /* Sem isso aqui o frango azéda*/
    public void checkPlayService() {


         /*Se não pegar a instancia do googleservice o fused não funciona -.-*/
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int resultCode = googleAPI.isGooglePlayServicesAvailable(getActivity());
        if (resultCode == ConnectionResult.SUCCESS) {
            mGoogleApiClient.connect();
        } else {
            googleAPI.getErrorDialog(getActivity(), resultCode, RQS_GooglePlayServices);
        }

    }

    /* 1 - Construindo a googleApiClient*/
    protected synchronized void buildGoogleApiClient() {
        Toast.makeText(getContext(), "buildGoogleApiClient", Toast.LENGTH_SHORT).show();
        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    /* 2 - Criando as configurações do locationRequest*/
    protected void createLocationRequest() {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000); //5 seconds
        mLocationRequest.setFastestInterval(3000); //3 seconds
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        //mLocationRequest.setSmallestDisplacement(0.1F); //1/10 meter


    }

    /* 3 - Construindo o location Settings request*/
    protected void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    /* 4 - criando o method que irá checar o status do locationSettings */
    protected void checkLocationSettings() {
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(
                        mGoogleApiClient,
                        mLocationSettingsRequest
                );

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {

            /* 5 Aqui que controla caso o location não estiver pronto*/
            @Override
            public void onResult(LocationSettingsResult locationSettingsResult) {


                final Status status = locationSettingsResult.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:


                        Toast.makeText(getContext(), "Location is already on.", Toast.LENGTH_SHORT).show();
                        startLocationUpdates();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:

                        Toast.makeText(getContext(), "Location dialog will be open", Toast.LENGTH_SHORT).show();

                        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {


                            /*Aqui só é chamado quando o user nega a primeira vez, nessa segunda vez o android deixa lançar uma mensagem para o user.*/
                            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                                    Manifest.permission.ACCESS_FINE_LOCATION)) {


                                //     try {
                                // Show the dialog by calling startResolutionForResult(), and check the result
                                // in onActivityResult().

                                //
                                // User selected the Never Ask Again Option Change settings in app settings manually
                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                                alertDialogBuilder.setTitle("Change Permissions in Settings");
                                alertDialogBuilder
                                        .setMessage("" +
                                                "\nClick SETTINGS to Manually Set\n"+"Permissions to use Location")
                                        .setCancelable(false)
                                        .setPositiveButton("SETTINGS", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                                Uri uri = Uri.fromParts("package",getContext().getPackageName(), null);
                                                intent.setData(uri);
                                                startActivityForResult(intent, REQUEST_CHECK_SETTINGS);     // step 6
                                            }
                                        });

                                AlertDialog alertDialog = alertDialogBuilder.create();
                                alertDialog.show();


                            } else {



                                ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION_LOCATION);


                            }


                        } else {


                        }




                            //move to step 6 in onActivityResult to check what action user has taken on settings dialog
                      //    status.startResolutionForResult(getActivity(), REQUEST_CHECK_SETTINGS);
                     //   } catch (IntentSender.SendIntentException e) {

                    //    }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:

                        break;
                }
            }
        });
    }

    /*Aqui que vai ser o callback do status resolution do step 4/5*/
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.i("Teste", "User agreed to make required location settings changes.");
                        startLocationUpdates();
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i("Teste", "User chose not to make required location settings changes.");
                        checkLocationSettings();
                        break;
                }
                break;
        }
    }


    protected void startLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION_LOCATION);

        } else {


            goAndDetectLocation();
        }

    }


    public void goAndDetectLocation() {

        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION_LOCATION);


        } else {

           /* LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            Toast.makeText(getContext(),""+LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient),
                    Toast.LENGTH_SHORT).show();
*/
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient,
                    mLocationRequest, this
            ).setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(Status status) {

                }
            });


        }

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        action.registraInteressados(this);
        buttomAddMarkerVisivel = action.getButtomAddMakerClickado();
        geocoder2 = new Geocoder(getContext());

        buildGoogleApiClient();
        createLocationRequest();
        buildLocationSettingsRequest();
        checkLocationSettings();
        checkPlayService();




        /*               ....       */
        /*Adiciona o listener no infoWindows(tag) do marker*/
        googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                //  marker.showInfoWindow();
                Toast.makeText(getContext(), "Clickou", Toast.LENGTH_LONG).show();
            }
        });

        /*Criando o listener do click longo*/
        googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {

            @Override
            public void onMapLongClick(LatLng arg0) {
                // TODO Auto-generated method stub

                if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION_LOCATION);
                } else {
                    Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                            mGoogleApiClient);
                    if (mLastLocation != null) {
                        newLatLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                        Toast.makeText(getContext(), "Presta atençao " + newLatLng.toString(), Toast.LENGTH_LONG).show();


                        if (MenuInicial.vDenunciar) {
                            /* Verifico a proximidade do user com o local que ele vai por o marcador*/
                            if (markerDialog.closeToMe(newLatLng, arg0)) {
                                  /* Verifico se existe algum marcador proximo */
                                    if (!markerDialog.hasNearby(marcadores, arg0)) {
                                        markerDialog.dialogAdd2(arg0, getContext(), mMap, geocoder2, marcadores);


                                } else {
                                        Toast.makeText(getContext(), "TEM MARCADOR AQUI PERTO!!!", Toast.LENGTH_LONG).show();


                                }

                            } else {

                                Toast.makeText(getContext(), "Você precisa estar proximo do ponto a ser marcado", Toast.LENGTH_LONG).show();

                            }


                        }
                    } else {

                        Toast.makeText(getContext(), " Não foram encontrados os dados da ultima localização", Toast.LENGTH_LONG).show();


                    }


                }


            }
        });


        /*Criando o listener click on marker*/
        googleMap = markerDialog.setMarkerClick(googleMap, getContext(),marcadores);


        /*Criando o listener do drag*/
        googleMap = markerDialog.setListenerDragDiag(googleMap, marcador, getContext(), getView());

        mMap = googleMap;

        /*Checkando a permissão dos acessos, vulgo frescura do Android..*/
        if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {

            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.setOnMapClickListener(this);
            mMap.getUiSettings().setMapToolbarEnabled(false);
            mMap.getUiSettings().setZoomControlsEnabled(true);


        /*Listener responsavel adicionar o botão da localização*/
            mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                @Override
                public boolean onMyLocationButtonClick() {
                    LatLng hue = new LatLng(latitude, longitude);
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(hue));
                    return false;
                }
            });


        } else {

            ActivityCompat.requestPermissions(this.getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION_LOCATION);
        }


        /*Notificação*/
        NotificationApp notificationApp = new NotificationApp(getView());
        notificationApp.notification();


        /*Teste do serviço de thread*/
       /* Intent intent = new Intent(getContext(),ServiceThread.class);
        getContext().startService(intent);*/
    }


    /*Override do metodo onMapClick..*/
    @Override
    public void onMapClick(LatLng latLng) {
        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this.getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION_LOCATION);
        } else {

            /*AS coisas estavam dando errado aqui, mas agora funciona.. apenas para teste..*/
            Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);

            if (mLastLocation != null) {
                newLatLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                Toast.makeText(getContext(), "Presta atençao " + latLng.toString(), Toast.LENGTH_LONG).show();


                    /*Aqui é normal*/


                if (!buttomAddMarkerVisivel) {


              /* Verifico a proximidade do user com o local que ele vai por o marcador*/
                    if (markerDialog.closeToMe(newLatLng, latLng)) {
                                  /* Verifico se existe algum marcador proximo */
                        if (!markerDialog.hasNearby(marcadores, latLng)) {
                            markerDialog.dialogAdd2(latLng, this.getContext(), mMap, geocoder2, marcadores);
                        } else {
                            Toast.makeText(getContext(), "TEM MARCADOR AQUI PERTO!!!", Toast.LENGTH_LONG).show();

                        }

                    } else {
                        Toast.makeText(getContext(), " É necessario estar proximo ao local a ser marcado", Toast.LENGTH_LONG).show();

                    }

                } else {


                    Toast.makeText(getContext(), "Coordenadas: " + latLng.toString(), Toast.LENGTH_LONG).show();
                }


            } else {
                Toast.makeText(getContext(), " Não foram encontrados os dados da ultima localização", Toast.LENGTH_LONG).show();
            }


        }


    }


    @Override
    public void notificaticarInteressados(Action action) {
        buttomAddMarkerVisivel = action.getButtomAddMakerClickado();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Toast.makeText(getContext(), "onConnected", Toast.LENGTH_SHORT).show();


        if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {

            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);


        } else {

            ActivityCompat.requestPermissions(this.getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION_LOCATION);

        }


    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(getContext(), "onConnectionSuspended", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(getContext(), "onConnectionFailed", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onLocationChanged(Location location) {
        Toast.makeText(getContext(), "Location Changed", Toast.LENGTH_SHORT).show();
        /*Armazenando a ultima posição*/
        mCurrentLocation = location;


    }


}
