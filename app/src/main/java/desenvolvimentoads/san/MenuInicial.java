package desenvolvimentoads.san;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;

import desenvolvimentoads.san.Observer.Action;
import desenvolvimentoads.san.Observer.ActionObserver;
import desenvolvimentoads.san.Observer.SharedContext;

public class MenuInicial extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.OnConnectionFailedListener,ActionObserver {

    public static final int REQUEST_PERMISSION_LOCATION = 10;
    private static final int MY_PERMISSION_REQUEST_CODE = 2508;
    private GoogleApiClient googleApiClient;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;
    private FirebaseAuth firebaseAuth;
    private String userName;
    private String userEmail;
    private String userId;

    private boolean buttomAddMarkerVisivel;
    private static final String TAG = "MenuInicial";

    private FragmentManager fragmentManager;
    public static FloatingActionButton btnAddMarker;
    public static FloatingActionButton btnCancelAddMarker;
    static MenuItem denunciar;
    public static Boolean vDenunciar = true;
    public static Boolean permissionOk=false;
    SharedContext sharedContext = SharedContext.getInstance();
    Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Action.getInstance().registraInteressados(this);
        buttomAddMarkerVisivel = Action.getInstance().getButtomAddMakerClickado();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_inicial);
        sharedContext.setContext(this.getBaseContext());
        SharedPreferences mySharedPrefers = getSharedPreferences("tutorial",Context.MODE_PRIVATE);

        if(!mySharedPrefers.getBoolean("skip",false)){
            firstTutorial();

        }

/*
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();*/
/*
        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    setUserData(user);
                } else {
                    goLogInScreen();
                }
            }
        };*/



        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        fragmentManager = getSupportFragmentManager();

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.add(R.id.container, new MapsTerceiro(), "MapsTerceiro");

        fragmentTransaction.commitAllowingStateLoss();


        btnAddMarker = (FloatingActionButton) findViewById(R.id.fab);
        btnAddMarker.setVisibility(View.VISIBLE);


        btnCancelAddMarker = (FloatingActionButton) findViewById(R.id.fabCancel);
        btnCancelAddMarker.setVisibility(View.GONE);



        btnAddMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!vDenunciar){

                    changeDenunciar();


                }



                Snackbar.make(view, "Clique no mapa para criar um marcador ou clique novamente para Cancelar", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                addMarkerFloatButtom();

            }
        });

       btnCancelAddMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Snackbar.make(view, "Criação do novo marcador Cancelada", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                addMarkerFloatButtom();


            }
        });

    }


    public  void addMarkerFloatButtom(){

        if(buttomAddMarkerVisivel){
       // if(btnAddMarker.isShown()){
            btnAddMarker.setVisibility(View.GONE);
            btnCancelAddMarker.setVisibility(View.VISIBLE);
            Action.getInstance().setButtomAddMakerClickado(false);
        }

        else {
            btnAddMarker.setVisibility(View.VISIBLE);
            btnCancelAddMarker.setVisibility(View.GONE);
            Action.getInstance().setButtomAddMakerClickado(true);
        }


    }

    public void changeDenunciar(){
        if(vDenunciar)    {
            vDenunciar = false;
            Action.getInstance().setReportNotSelected(false);
            denunciar.setTitle("Cancel");
            Snackbar.make(getCurrentFocus(), "Clique no marcador que deseja denunciar", Snackbar.LENGTH_LONG).setAction("Action", null).show();

        }else{

            denunciar.setTitle("Denunciar");
            vDenunciar = true;
            Action.getInstance().setReportNotSelected(true);

        }

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_inicial, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        //noinspection SimplifiableIfStatement

        if (id == R.id.denunciar) {
            denunciar = item;


            if(!buttomAddMarkerVisivel){
               addMarkerFloatButtom();

            }
            changeDenunciar();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id){
            case R.id.nav_all_marker:

                showFragment(new MapsPrincipal(), "Todos Marcadores");
                setTitle("Todos Marcadores");
                break;
            case R.id.nav_user_marker:

                showFragment(new MapsSegundo(), "Marcadores do Usuário");
                setTitle("Marcadores do Usuário");
                break;
            case R.id.nav_user_disable:
                showFragment(new MapsTerceiro(), "Marcadores Inativos do sistema");
                setTitle("Marcadores Inativos do sistema");
                break;
            case R.id.nav_integracao:
                showFragment(new MapsQuarto(), "Integração back front");
                setTitle("Integração back front");
                break;
            case R.id.nav_tutorial:
                tutorial();
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showFragment(Fragment fragment, String name){
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.container, fragment, name);

        fragmentTransaction.commit();
    }
/*
    private void setUserData(FirebaseUser user) {
        userName = user.getDisplayName();
        userEmail = user.getEmail();
        userId = user.getUid();

        Log.i(TAG, "setUserData: Nome "+userName+", Email: "+userEmail+", ID: "+userId);
//        Glide.with(this).load(user.getPhotoUrl()).into(photoImageView);
    }*/

    @Override
    protected void onStart() {
        super.onStart();

      //  firebaseAuth.addAuthStateListener(firebaseAuthListener);
    }

    private void goLogInScreen() {
        Intent intent = new Intent(this, TelaInicial.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

  /*  public void logOut(View view) {
        firebaseAuth.signOut();

        Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                if (status.isSuccess()) {
                    goLogInScreen();
                } else {
                    Toast.makeText(getApplicationContext(), "Não foi possível encerrar a sessão", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }*/

   /* public void revoke(View view) {
        firebaseAuth.signOut();

        Auth.GoogleSignInApi.revokeAccess(googleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                if (status.isSuccess()) {
                    goLogInScreen();
                } else {
                    Toast.makeText(getApplicationContext(), "Não foi possível revokar a sessão", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }*/
/*

    @Override
    protected void onStop() {
        super.onStop();

        if (firebaseAuthListener != null) {
            firebaseAuth.removeAuthStateListener(firebaseAuthListener);
        }
    }
*/


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void notificaticarInteressados(Action action) {
       buttomAddMarkerVisivel = action.getButtomAddMakerClickado();
        if(!buttomAddMarkerVisivel){
            // if(btnAddMarker.isShown()){
            btnAddMarker.setVisibility(View.GONE);
            btnCancelAddMarker.setVisibility(View.VISIBLE);

        }

        else {
            btnAddMarker.setVisibility(View.VISIBLE);
            btnCancelAddMarker.setVisibility(View.GONE);

        }



        if(vDenunciar != action.isReportNotSelected()){
            Log.i("Teste","Denunciar mudou..");
            changeDenunciar();

        }


    }
    /*Se fosse necessario mudar algo apos as permissões*/
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        Log.i("teste", "Permission called !!!");
        switch (requestCode) {

            case MY_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Log.i("teste", "permission terceiro accepted ");
                    permissionOk =true;
                }else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    // Should we show an explanation?
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)) {

                        Log.i("teste", "checkGpsPermission Rationale");
                        //     try {
                        // Show the dialog by calling startResolutionForResult(), and check the result
                        // in onActivityResult().

                        //
                        // User selected the Never Ask Again Option Change settings in app settings manually
                        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                        alertDialogBuilder.setTitle("   Olá    ");
                        alertDialogBuilder
                                .setMessage("" +
                                        "\n O App San utiliza de dados da localização para seu funcionamento, ao negar a permissão o aplicativo deixa de funcionar."+"" +
                                        "\n Por isso pedimos para que aceite que ele utilize da permissão do Location.")
                                .setCancelable(false)
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        ActivityCompat.requestPermissions((Activity) context, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION_LOCATION);

                                    }
                                });

                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                    } else {
                        /*never asked selecionado...*/
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                        alertDialogBuilder.setTitle("Permissão necessaria");
                        alertDialogBuilder
                                .setMessage("" +
                                        "\n O App San utiliza de dados da localização para seu funcionamento." + "\n Ao selecionar para nunca mais ser requisitado permissão e negar o aplicativo deixa de funcionar."
                                +"\n Para que ele funcione agora será necessario mudar manualmente a permission do location nas configurações do App")
                                .setCancelable(false)
                                .setNegativeButton("Não",new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                       finishAffinity();

                                    }
                                })
                                .setPositiveButton("Configurações", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        Uri uri = Uri.fromParts("package", getBaseContext().getPackageName(), null);
                                        intent.setData(uri);
                                        startActivity(intent);// step 6

                                    }
                                });

                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                    }
                }
                break;

            default:
                Log.i("teste","Permission neged!!!");

        }
        }

    public void firstTutorial(){


            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle("Bem vindo !!");
            alertDialogBuilder
                    .setMessage("" +
                            "\n Gostaria de visualizar um breve tutorial ? \n " + "Ainda será possivel velo clicando na guia lateral a esquerda em 'Tutorial' .")
                    .setCancelable(false)
                    .setPositiveButton("Sim, gostaria", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            tutorial();
                        }
                    }).setNegativeButton("Não", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    createPrefers("tutorial", "skip", true);
                }
            });

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();

    }

    public void tutorial() {

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle("Criação de marcadores");
            alertDialogBuilder
                    .setMessage("" +
                            "\n É possivel criar um marcador de alerta pressionado o dedo sobre o local por alguns segundos ou apertando no icone do marcador no lado direito da tela e clicando novamente no mapa. " +
                            "\n " + "Só sera criado um marcador se sua ultima localização estiver proxima do ponto selecionado .")
                    .setCancelable(false)
                    .setPositiveButton("Proximo", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            messageOne();
                        }
                    }).setNegativeButton("Finalizar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                }
            });

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();

    }

    public void messageOne(){

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle("Validação de marcadores");
            alertDialogBuilder
                    .setMessage("" +
                            "\n É possivel validar marcadores criados pelos outros usuarios  clicando em cima deles assim aumentando o tempo de exposição do alerta ou diminuindo caso seja invalido" +
                            "\n " + "Ao selecionar Valido, você esta dizendo que a informação esta correta."+"" +
                            "\n Selecionando informação invalidar você esta dizendo que a informação não é mais valida.")
                    .setCancelable(false)
                    .setPositiveButton("Proximo", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            messageTwo();
                        }
                    }).setNegativeButton("Finalizar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                }
            });

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }

    public void messageTwo(){

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle("Denunciando marcadores");
            alertDialogBuilder
                    .setMessage("" +
                            "\n É possivel Denunciar os marcadores criados pelos outros usuarios, selecione a opção Denunciar no canto superior direito e clicando em cima do marcador a ser denunciado."+"\n Ao denunciar você esta dizendo ao sistema que aquela informação é uma tentativa de criar falsos alertas")
                    .setCancelable(false)
                    .setPositiveButton("Proximo", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            messageThree();
                        }
                    }).setNegativeButton("Finalizar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                }
            });

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();

    }
    public void messageThree(){

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle("Historico");
            alertDialogBuilder
                    .setMessage("" +
                            "\n É possivel Visualizar o historico de marcadores adicionados e validados ao passar o dedo no canto esquerdo um menu sera exibido, ao selecionar historico será exibido um mapa com o seu historico.")
                    .setCancelable(false)
                    .setPositiveButton("Finalizar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    });

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();

    }




    public void createPrefers(String packName, String name, boolean bol){


            SharedPreferences myPreferences = getApplicationContext().getApplicationContext().getSharedPreferences(packName, Context.MODE_PRIVATE);
            SharedPreferences.Editor edit = myPreferences.edit();
            edit.putBoolean(name,bol);
            edit.commit();


        }
    }




