package desenvolvimentoads.san;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import desenvolvimentoads.san.Observer.Action;
import desenvolvimentoads.san.Observer.ActionObserver;
import desenvolvimentoads.san.Observer.SharedContext;

public class MenuInicial extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.OnConnectionFailedListener, ActionObserver {


    private static final int MY_PERMISSION_REQUEST_CODE = 2508;
    private GoogleApiClient googleApiClient;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

    NavigationView navigationView;
    private String userName;
    private String userEmail;
    private String userId;
    private Uri photourl;

    //    0 - jeanfelipe.brock@gmail.com
//    1 - jeanbrock.felipe@gmail.com
//    2 - felipemrk2e@gmail.com
    private String[] users = {"Pi02YW9d7NYgPvhjmeaB1m27Aiy2", "Wpk4eUKu53hsUg9tEFAXkbuEgeg2", "pouNOiMFxgeDxOgn0GXquvvkH9G2"};

    private boolean buttomAddMarkerVisivel;
    private static final String TAG = "MenuInicial";

    private FragmentManager fragmentManager;
    public static FloatingActionButton btnAddMarker;
    public static FloatingActionButton btnCancelAddMarker;
    static MenuItem denunciar;
    public static Boolean vDenunciar = true;
    public static Boolean permissionOk = false;
    boolean menuStarted = false;
    SharedContext sharedContext = SharedContext.getInstance();
    Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Action.getInstance().registraInteressados(this);
        buttomAddMarkerVisivel = Action.getInstance().getButtomAddMakerClickado();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_inicial);
        sharedContext.setContext(this.getBaseContext());
        SharedPreferences mySharedPrefers = getSharedPreferences("tutorial", Context.MODE_PRIVATE);
        if (!mySharedPrefers.getBoolean("skip", false)) {
            firstTutorial();

        }


        //     Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        //     startActivity(intent);// step 6


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

        navigationView = (NavigationView) findViewById(R.id.nav_view);


        if (currentUser != null) {
            userName = currentUser.getDisplayName();
            userEmail = currentUser.getEmail();
            userId = currentUser.getUid();

            TextView textView = (TextView) navigationView.getHeaderView(0).findViewById(R.id.textView);
            textView.setText(userEmail);

            TextView userText = (TextView) navigationView.getHeaderView(0).findViewById(R.id.userText);
            userText.setText(userName);

            if (currentUser.getPhotoUrl() != null) {
                ImageView imageView = (ImageView) navigationView.getHeaderView(0).findViewById(R.id.imageView);
                Picasso.with(this).load(currentUser.getPhotoUrl()).placeholder(R.mipmap.ic_logo_san)
                        .error(R.mipmap.ic_logo_san)
                        .into(imageView, new com.squareup.picasso.Callback() {

                                    @Override
                                    public void onSuccess() {

                                    }

                                    @Override
                                    public void onError() {

                                    }
                                }
                        );
            }
        }

        if (Build.VERSION.SDK_INT <= 22) {
            permissionOk = true;
            Log.i("teste", "------------------- BUILD VERSION ---------------------  " + Build.VERSION.SDK_INT);
            menuStart();

        } else {
            checkPermission();

        }


    }

    public void checkPermission() {


        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {


            ActivityCompat.requestPermissions((Activity) context, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_REQUEST_CODE);


        } else {

            menuStart();

        }


    }

    public void menuStart() {
        navigationView.setNavigationItemSelectedListener(this);


        btnAddMarker = (FloatingActionButton) findViewById(R.id.fab);
        btnAddMarker.setVisibility(View.VISIBLE);


        btnCancelAddMarker = (FloatingActionButton) findViewById(R.id.fabCancel);
        btnCancelAddMarker.setVisibility(View.GONE);


        btnAddMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!vDenunciar) {

                    changeDenunciar();


                }


                Snackbar.make(view, "Clique no mapa para criar um marcador ou clique novamente para Cancelar.", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                addMarkerFloatButtom();

            }
        });

        btnCancelAddMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Snackbar.make(view, "Criação do novo marcador Cancelada.", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                addMarkerFloatButtom();


            }
        });

        menuStarted = true;

        startFragment();
    }

    public void startFragment() {
        fragmentManager = getSupportFragmentManager();

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.add(R.id.container, new MapsTerceiro(), "MapsTerceiro");

        fragmentTransaction.commitAllowingStateLoss();

    }

    public void addMarkerFloatButtom() {

        if (buttomAddMarkerVisivel) {
            // if(btnAddMarker.isShown()){
            btnAddMarker.setVisibility(View.GONE);
            btnCancelAddMarker.setVisibility(View.VISIBLE);
            Action.getInstance().setButtomAddMakerClickado(false);
        } else {
            btnAddMarker.setVisibility(View.VISIBLE);
            btnCancelAddMarker.setVisibility(View.GONE);
            Action.getInstance().setButtomAddMakerClickado(true);
        }


    }

    public void changeDenunciar() {
        if (vDenunciar) {
            vDenunciar = false;
            Action.getInstance().setReportNotSelected(false);
            denunciar.setTitle("Cancel");
            Snackbar.make(getCurrentFocus(), "Clique no marcador que deseja denunciar.", Snackbar.LENGTH_LONG).setAction("Action", null).show();

        } else {

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


            if (!buttomAddMarkerVisivel) {
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

        switch (id) {
            case R.id.nav_all_marker:

                showFragment(new MapsTerceiro(), "Todos Marcadores");
                setTitle("Todos Marcadores");
                findViewById(R.id.denunciar).setVisibility(View.VISIBLE);
                btnAddMarker = (FloatingActionButton) findViewById(R.id.fab);
                btnAddMarker.setVisibility(View.VISIBLE);
                break;
            case R.id.nav_user_marker:
                showFragment(new MapsSegundo(), "Marcadores do Usuário");
                setTitle("Marcadores do Usuário");
                findViewById(R.id.denunciar).setVisibility(View.GONE);
                btnAddMarker = (FloatingActionButton) findViewById(R.id.fab);
                btnAddMarker.setVisibility(View.GONE);
                break;
            case R.id.nav_tutorial:
                tutorial();
                break;
            case R.id.nav_sign_out:
                signOut();
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void signOut() {
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.signOut();

        Intent intent = new Intent(MenuInicial.this, TelaInicial.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

        //Apaga cache do app
        if (Build.VERSION_CODES.KITKAT <= Build.VERSION.SDK_INT) {
            ((ActivityManager) this.getSystemService(ACTIVITY_SERVICE))
                    .clearApplicationUserData();
        }

    }

    private void showFragment(Fragment fragment, String name) {
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
        if (!buttomAddMarkerVisivel) {
            // if(btnAddMarker.isShown()){
            btnAddMarker.setVisibility(View.GONE);
            btnCancelAddMarker.setVisibility(View.VISIBLE);

        } else {
            btnAddMarker.setVisibility(View.VISIBLE);
            btnCancelAddMarker.setVisibility(View.GONE);

        }


        if (vDenunciar != action.isReportNotSelected()) {
            Log.i("Teste", "Denunciar mudou..");
            changeDenunciar();

        }


    }

    /*Se fosse necessario mudar algo apos as permissões*/
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        Log.i("teste", "Permission called !!!");
        Log.i("teste", "Permission called  code " + requestCode + " mycode " + MY_PERMISSION_REQUEST_CODE);
        switch (requestCode) {

            case MY_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Log.i("teste", "permission terceiro accepted ");
                    permissionOk = true;
                    if (!menuStarted) {
                        menuStart();

                    }

                } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
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
                        alertDialogBuilder.setTitle("   Olá.    ");
                        alertDialogBuilder
                                .setMessage("" +
                                        "\n O App San utiliza de dados da localização para seu funcionamento, ao negar a permissão o aplicativo deixa de funcionar." + "" +
                                        "\n Por isso pedimos para que aceite que ele utilize da permissão do Location.")
                                .setCancelable(false)
                                .setPositiveButton("Ok.", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        ActivityCompat.requestPermissions((Activity) context, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_REQUEST_CODE);

                                    }
                                });

                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                    } else {
            /*never asked selecionado...*/
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                        alertDialogBuilder.setTitle("Permissão necessária.");
                        alertDialogBuilder
                                .setMessage("" +
                                        "\n O App San utiliza de dados da localização para seu funcionamento." + "\n Ao selecionar para nunca mais ser requisitado permissão e negar o aplicativo deixa de funcionar."
                                        + "\n Para que ele funcione agora será necessário mudar manualmente a permissão do location nas configurações do App")
                                .setCancelable(false)
                                .setNegativeButton("Não.", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        finishAffinity();

                                    }
                                })
                                .setPositiveButton("Configurações.", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        Uri uri = Uri.fromParts("package", getBaseContext().getPackageName(), null);
                                        intent.setData(uri);
                                        startActivityForResult(intent, 1);// step 6

                                    }
                                });

                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                    }
                }
                break;

            default:
                Log.i("teste", "Permission neged!!!");

        }
    }

    public void firstTutorial() {


        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Bem vindo!.");
        alertDialogBuilder
                .setMessage("" +
                        "\nGostaria de visualizar um breve tutorial? \n " + "Ainda será possível velo clicando na guia lateral a esquerda em 'Tutorial'.")
                .setCancelable(false)
                .setPositiveButton("Sim, gostaria.", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        tutorial();
                    }
                }).setNegativeButton("Não.", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                createPrefers("tutorial", "skip", true);
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

    }

    public void tutorial() {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Criação de marcadores.");
        alertDialogBuilder
                .setMessage("" +
                        "\nÉ possível criar um marcador de alerta pressionado o dedo sobre o local por alguns segundos ou apertando no ícone do marcador no lado direito da tela e clicando novamente no mapa. " +
                        "\n " + "Só seré criado um marcador se sua ultima localização estiver próxima do ponto selecionado.")
                .setCancelable(false)
                .setPositiveButton("Proximo.", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        messageOne();
                    }
                }).setNegativeButton("Finalizar.", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

    }

    public void messageOne() {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Validação de marcadores.");
        alertDialogBuilder
                .setMessage("" +
                        "\nÉ possível validar marcadores criados pelos outros usuários  clicando em cima deles assim aumentando o tempo de exposição do alerta ou diminuindo caso seja invalido" +
                        "\n " + "Ao selecionar Valido, você esta dizendo que a informação esta correta." + "" +
                        "\nSelecionando informação invalidar você esta dizendo que a informação não é mais valida.")
                .setCancelable(false)
                .setPositiveButton("Próximo.", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        messageTwo();
                    }
                }).setNegativeButton("Finalizar.", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public void messageTwo() {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Denunciando marcadores.");
        alertDialogBuilder
                .setMessage("" +
                        "\nÉ possível Denunciar os marcadores criados pelos outros usuários, selecione a opção Denunciar no canto superior direito e clicando em cima do marcador a ser denunciado." + "\n Ao denunciar você está dizendo ao sistema que aquela informação é uma tentativa de criar falsos alertas.")
                .setCancelable(false)
                .setPositiveButton("Próximo.", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        messageThree();
                    }
                }).setNegativeButton("Finalizar.", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

    }

    public void messageThree() {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Histórico.");
        alertDialogBuilder
                .setMessage("" +
                        "\n É possível Visualizar o histórico de marcadores adicionados e validados ao passar o dedo no canto esquerdo um menu sera exibido, ao selecionar histórico será exibido um mapa com o seu histórico.")
                .setCancelable(false)
                .setPositiveButton("Finalizar.", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

    }


    public void createPrefers(String packName, String name, boolean bol) {


        SharedPreferences myPreferences = getApplicationContext().getApplicationContext().getSharedPreferences(packName, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = myPreferences.edit();
        edit.putBoolean(name, bol);
        edit.commit();


    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i("teste", "In Stop main");


        if (!ForegroundService.IS_SERVICE_RUNNING) {
            Intent service = new Intent(MenuInicial.this, ForegroundService.class);
            service.setAction(ForegroundService.START);
            startService(service);
        }


    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i("teste", "In Resume main");


        if (isMyServiceRunning(ForegroundService.class)) {


            Log.i("teste", "shutdown");
            Intent service = new Intent(MenuInicial.this, ForegroundService.class);
            service.setAction(ForegroundService.STOP);
            startService(service);


            Log.i("teste", "In Resume is running" + ForegroundService.IS_SERVICE_RUNNING);

        }

    }

    //    0 - jeanfelipe.brock@gmail.com
    //    1 - jeanbrock.felipe@gmail.com
    //    2 - felipemrk2e@gmail.com
//    public String getUsers() {
//        return users[2];
//    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("teste", "In onDestroy Menuinicial");
        if (ForegroundService.IS_SERVICE_RUNNING) {
            Intent service = new Intent(MenuInicial.this, ForegroundService.class);
            service.setAction(ForegroundService.STOP);
            startService(service);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i("teste", "---- onActivityResult main called :') ---- ");
        Intent intent = new Intent(MenuInicial.this, MenuInicial.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();

    }

}




